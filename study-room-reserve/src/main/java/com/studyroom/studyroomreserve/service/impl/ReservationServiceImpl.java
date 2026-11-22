package com.studyroom.studyroomreserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.studyroomreserve.VO.*;
import com.studyroom.studyroomreserve.entity.Reservation;
import com.studyroom.studyroomreserve.entity.Seat;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.mapper.ReservationMapper;
import com.studyroom.studyroomreserve.service.ReservationService;
import com.studyroom.studyroomreserve.service.SeatService;
import com.studyroom.studyroomreserve.util.LocationUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.studyroom.studyroomreserve.mapper.StudyRoomMapper;
import com.studyroom.studyroomreserve.entity.StudyRoom;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Resource
    private SeatService seatService;
    @Resource
    private StudyRoomMapper studyRoomMapper;


    @Override
    public List<ReservationListVo> myList(Long userId) {
        return this.baseMapper.selectMyList(userId);
    }


    @Override
    @Transactional   // 多步写库，保证一致性
    public void createReservation(Long userId, ReservationVo vo) {
        LocalDateTime start = vo.getStartTime();
        LocalDateTime end = vo.getEndTime();

        // 1. 基础时间校验
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BizException(400, "预约时间不合法");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BizException(400, "不能预约过去的时间");
        }

        // 校验本人在该时间段是否已有预约
        checkUserTimeConflict(userId, start, end);


        // 2. 校验座位存在且未禁用
        Seat seat = seatService.getById(vo.getSeatId());
        if (seat == null) {
            throw new BizException(400, "座位不存在");
        }
        if (seat.getStatus() == 2) {
            throw new BizException(400, "该座位已被禁用");
        }

        // 3. 时间冲突校验：同一座位、状态为待签到(0)或已签到(1)，时间段重叠就算冲突
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getSeatId, vo.getSeatId())
                .in(Reservation::getStatus, 0, 1)
                .lt(Reservation::getStartTime, end)   // 已有开始 < 新结束
                .gt(Reservation::getEndTime, start);  // 已有结束 > 新开始
        long count = this.count(wrapper);
        if (count > 0) {
            throw new BizException(400, "该时间段座位已被预约");
        }

        // 4. 组装并保存，默认状态 0 待签到
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeatId(vo.getSeatId());
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setStatus(0);
        this.save(reservation);
        // 同步把座位置为已预约(红色)
        seat.setStatus(1);
        seatService.updateById(seat);

    }
    // 校验同一用户在该时间段是否已有有效预约（待签到0/已签到1）
    private void checkUserTimeConflict(Long userId, LocalDateTime start, LocalDateTime end) {
        long own = this.count(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .in(Reservation::getStatus, 0, 1)
                .lt(Reservation::getStartTime, end)   // 已有开始 < 新结束
                .gt(Reservation::getEndTime, start)); // 已有结束 > 新开始
        if (own > 0) {
            throw new BizException(400, "你在该时间段已有预约，不能同时预约多个座位");
        }
    }


    @Override
    @Transactional
    public void cancelReservation(Long userId, Long id) {
        Reservation r = this.getById(id);
        if (r == null) {
            throw new BizException(400, "预约不存在");
        }
        // 只能取消自己的预约
        if (!r.getUserId().equals(userId)) {
            throw new BizException(403, "不能操作他人的预约");
        }
        // 只有待签到(0)能取消
        if (r.getStatus() != 0) {
            throw new BizException(400, "当前状态不能取消");
        }
        r.setStatus(2);   // 改为已取消
        this.updateById(r);
        r.setStatus(2);   // 改为已取消
        this.updateById(r);

        // 同步把座位恢复为空闲(绿色)
        Seat seat = seatService.getById(r.getSeatId());
        if (seat != null) {
            seat.setStatus(0);
            seatService.updateById(seat);
        }

    }

    @Override
    @Transactional
    public void signReservation(Long userId, Long id, SignVo vo) {
        Reservation r = this.getById(id);
        if (r == null) {
            throw new BizException(400, "预约不存在");
        }
        if (!r.getUserId().equals(userId)) {
            throw new BizException(403, "不能操作他人的预约");
        }
        if (r.getStatus() != 0) {
            throw new BizException(400, "当前状态不能签到");
        }

        // 找到座位所属的自习室，读取签到坐标
        Seat seat = seatService.getById(r.getSeatId());
        StudyRoom room = studyRoomMapper.selectById(seat.getRoomId());

        if (room == null || room.getLatitude() == null || room.getLongitude() == null) {
            throw new BizException(400, "该自习室未设置签到位置，无法签到");
        }
        if (vo == null || vo.getLatitude() == null || vo.getLongitude() == null) {
            throw new BizException(400, "未获取到你的位置，请开启定位后重试");
        }

        // 计算用户与自习室的距离
        double dist = LocationUtil.distance(
                vo.getLatitude(), vo.getLongitude(),
                room.getLatitude(), room.getLongitude());

        if (dist > room.getRadius()) {
            throw new BizException(400,
                    "你不在自习室范围内（当前距离约 " + Math.round(dist) + " 米），无法签到");
        }

        // 距离合格，执行签到
        r.setStatus(1);
        r.setSignInTime(LocalDateTime.now());
        this.updateById(r);

        seat.setStatus(1);
        seatService.updateById(seat);
    }


    @Override
    @Transactional
    public void signOutReservation(Long userId, Long id) {
        Reservation r = this.getById(id);
        if (r == null) {
            throw new BizException(400, "预约不存在");
        }
        if (!r.getUserId().equals(userId)) {
            throw new BizException(403, "不能操作他人的预约");
        }
        if (r.getStatus() != 1) {
            throw new BizException(400, "只有已签到的预约才能签退");
        }

        r.setStatus(4);                          // 已完成/已签退
        r.setSignOutTime(LocalDateTime.now());   // 记录签退时间
        this.updateById(r);

        // 座位恢复空闲，可被下一个人预约
        Seat seat = seatService.getById(r.getSeatId());
        if (seat != null) {
            seat.setStatus(0);
            seatService.updateById(seat);
        }
    }


    @Override
    @Transactional
    public QuickResultVo quickReserve(Long userId, QuickReservationVo vo) {
        LocalDateTime start = vo.getStartTime();
        LocalDateTime end = vo.getEndTime();

        // 1. 时间校验（和普通预约一致）
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BizException(400, "预约时间不合法");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new BizException(400, "不能预约过去的时间");
        }

        // 一键找座同样要先判断本人是否已有时间冲突
        checkUserTimeConflict(userId, start, end);

        // 2. 候选座位：空闲且未禁用，可选限定房间
        LambdaQueryWrapper<Seat> seatWrapper = new LambdaQueryWrapper<>();
        seatWrapper.eq(Seat::getStatus, 0);
        if (vo.getRoomId() != null) {
            seatWrapper.eq(Seat::getRoomId, vo.getRoomId());
        }
        seatWrapper.orderByAsc(Seat::getRoomId)
                .orderByAsc(Seat::getSeatNo);
        List<Seat> candidates = seatService.list(seatWrapper);

        // 3. 逐个找第一个无冲突的座位
        for (Seat seat : candidates) {
            long conflict = this.count(new LambdaQueryWrapper<Reservation>()
                    .eq(Reservation::getSeatId, seat.getId())
                    .in(Reservation::getStatus, 0, 1)
                    .lt(Reservation::getStartTime, end)
                    .gt(Reservation::getEndTime, start));
            if (conflict == 0) {
                // 命中：创建预约
                Reservation r = new Reservation();
                r.setUserId(userId);
                r.setSeatId(seat.getId());
                r.setStartTime(start);
                r.setEndTime(end);
                r.setStatus(0);
                this.save(r);

                // 组装返回，带上房间名
                StudyRoom room = studyRoomMapper.selectById(seat.getRoomId());
                QuickResultVo result = new QuickResultVo();
                result.setReservationId(r.getId());
                result.setSeatId(seat.getId());
                result.setSeatNo(seat.getSeatNo());
                result.setRoomName(room != null ? room.getName() : "");
                return result;
            }
        }

        // 4. 所有候选都有冲突
        throw new BizException(400, "该时间段没有可用座位");
    }

    @Override
    public StatisticsVo getStatistics(Long userId) {
        // 查出该用户所有已完成(签退,status=4)的预约
        List<Reservation> doneList = this.list(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(Reservation::getStatus, 4)
                .orderByDesc(Reservation::getSignInTime));

        StatisticsVo vo = new StatisticsVo();
        vo.setCheckInCount(doneList.size());

        Set<LocalDate> studyDates = new HashSet<>();
        LocalDate today = LocalDate.now();
        long totalMinutes = 0;
        long todayMinutes = 0;

        for (Reservation r : doneList) {
            if (r.getSignInTime() == null || r.getSignOutTime() == null) {
                continue;
            }

            // 单次学习时长（分钟）
            long minutes = Duration.between(r.getSignInTime(), r.getSignOutTime()).toMinutes();
            if (minutes < 0) continue;

            totalMinutes += minutes;

            LocalDate day = r.getSignInTime().toLocalDate();
            studyDates.add(day);

            if (day.equals(today)) {
                todayMinutes += minutes;
            }
        }

        vo.setTotalMinutes(totalMinutes);
        vo.setTodayMinutes(todayMinutes);
        vo.setContinuousDays(calcContinuousDays(studyDates, today));
        return vo;
    }

    // 从最近的学习日往前数连续天数
    private int calcContinuousDays(Set<LocalDate> studyDates, LocalDate today) {
        if (studyDates.isEmpty()) {
            return 0;
        }

        // 今天还没学，就从最近一次学习日开始，避免今天没学就清零
        LocalDate cursor = studyDates.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (studyDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }



}
