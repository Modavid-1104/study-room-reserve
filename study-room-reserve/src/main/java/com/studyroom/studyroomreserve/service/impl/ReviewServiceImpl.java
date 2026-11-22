package com.studyroom.studyroomreserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.studyroomreserve.entity.Reservation;
import com.studyroom.studyroomreserve.entity.Review;
import com.studyroom.studyroomreserve.entity.Seat;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.mapper.ReviewMapper;
import com.studyroom.studyroomreserve.service.ReservationService;
import com.studyroom.studyroomreserve.service.ReviewService;
import com.studyroom.studyroomreserve.service.SeatService;
import com.studyroom.studyroomreserve.VO.ReviewVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Resource
    private ReservationService reservationService;
    @Resource
    private SeatService seatService;

    @Override
    public void createReview(Long userId, ReviewVo vo) {
        // 基础校验
        if (vo.getReservationId() == null) {
            throw new BizException(400, "缺少预约信息");
        }
        if (vo.getScore() == null || vo.getScore() < 1 || vo.getScore() > 5) {
            throw new BizException(400, "评分必须在 1-5 之间");
        }
        if (vo.getContent() == null || vo.getContent().trim().isEmpty()) {
            throw new BizException(400, "评价内容不能为空");
        }

        // 校验预约：存在、属于本人、已签到
        Reservation reservation = reservationService.getById(vo.getReservationId());
        if (reservation == null) {
            throw new BizException(400, "预约不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BizException(403, "不能评价他人的预约");
        }
        if (reservation.getStatus() != 4) {
            throw new BizException(400, "只有已签退的预约才能评价");
        }

        // 校验该预约是否已评价
        long exists = this.count(new LambdaQueryWrapper<Review>()
                .eq(Review::getReservationId, vo.getReservationId()));
        if (exists > 0) {
            throw new BizException(400, "该预约已评价过");
        }

        // 预约表只有 seatId，需要查 seat 拿 roomId
        Seat seat = seatService.getById(reservation.getSeatId());
        if (seat == null) {
            throw new BizException(400, "座位信息异常");
        }

        Review review = new Review();
        review.setReservationId(vo.getReservationId());
        review.setRoomId(seat.getRoomId());
        review.setUserId(userId);
        review.setScore(vo.getScore());
        review.setContent(vo.getContent());
        this.save(review);
    }

    @Override
    public List<Review> listByRoom(Long roomId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRoomId, roomId)
                .orderByDesc(Review::getCreateTime);
        return this.list(wrapper);
    }
}
