package com.studyroom.studyroomreserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.studyroomreserve.VO.SeatVo;
import com.studyroom.studyroomreserve.entity.Seat;
import com.studyroom.studyroomreserve.entity.StudyRoom;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.mapper.SeatMapper;
import com.studyroom.studyroomreserve.service.SeatService;
import com.studyroom.studyroomreserve.service.StudyRoomService;
import com.studyroom.studyroomreserve.VO.SeatBatchVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.studyroom.studyroomreserve.mapper.StudyRoomMapper;
import com.studyroom.studyroomreserve.entity.StudyRoom;
import com.studyroom.studyroomreserve.VO.QuickReservationVo;
import com.studyroom.studyroomreserve.VO.QuickResultVo;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatServiceImpl extends ServiceImpl<SeatMapper, Seat> implements SeatService {

    @Resource
    private StudyRoomService studyRoomService;


    @Override
    @Transactional
    public void batchGenerate(SeatBatchVo vo) {
        // 基础校验
        if (vo.getRoomId() == null) {
            throw new BizException(400, "缺少自习室信息");
        }
        if (vo.getCount() == null || vo.getCount() <= 0) {
            throw new BizException(400, "生成数量不合法");
        }
        String prefix = vo.getPrefix() == null ? "" : vo.getPrefix();

        // 校验房间存在
        StudyRoom room = studyRoomService.getById(vo.getRoomId());
        if (room == null) {
            throw new BizException(400, "自习室不存在");
        }

        // 现有座位数 +1 作为起始编号，避免和已有编号冲突
        long existCount = this.count(new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, vo.getRoomId()));
        int start = (int) existCount + 1;

        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < vo.getCount(); i++) {
            Seat seat = new Seat();
            seat.setRoomId(vo.getRoomId());
            int no = start + i;
            // %02d 补零成两位，如 A01、A02
            seat.setSeatNo(prefix + String.format("%02d", no));
            seat.setStatus(0);   // 默认空闲
            seats.add(seat);
        }

        // 一次性批量插入
        this.saveBatch(seats);
    }
    @Override
    public void updateSeat(SeatVo vo) {
        if (vo.getId() == null) {
            throw new BizException(400, "缺少座位id");
        }
        Seat seat = this.getById(vo.getId());
        if (seat == null) {
            throw new BizException(400, "座位不存在");
        }
        if (vo.getSeatNo() == null || vo.getSeatNo().trim().isEmpty()) {
            throw new BizException(400, "座位号不能为空");
        }
        seat.setSeatNo(vo.getSeatNo());
        seat.setRemark(vo.getRemark());
        this.updateById(seat);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 2)) {
            throw new BizException(400, "状态值不合法，只能设为0或2");
        }
        Seat seat = this.getById(id);
        if (seat == null) {
            throw new BizException(400, "座位不存在");
        }
        seat.setStatus(status);
        this.updateById(seat);
    }

}
