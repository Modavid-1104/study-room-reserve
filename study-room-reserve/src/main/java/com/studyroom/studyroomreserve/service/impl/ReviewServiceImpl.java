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
import com.studyroom.studyroomreserve.entity.ReviewMedia;
import com.studyroom.studyroomreserve.mapper.ReviewMediaMapper;
import com.studyroom.studyroomreserve.config.UploadProperties;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;


import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Resource
    private ReservationService reservationService;
    @Resource
    private SeatService seatService;
    @Resource
    private ReviewMediaMapper reviewMediaMapper;
    @Resource
    private UploadProperties uploadProperties;


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
        List<Review> reviews = this.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getRoomId, roomId)
                .orderByDesc(Review::getCreateTime));
        if (reviews.isEmpty()) return reviews;

        List<Long> ids = reviews.stream().map(Review::getId).collect(Collectors.toList());
        List<ReviewMedia> all = reviewMediaMapper.selectList(
                new LambdaQueryWrapper<ReviewMedia>().in(ReviewMedia::getReviewId, ids));
        Map<Long, List<ReviewMedia>> map = all.stream()
                .collect(Collectors.groupingBy(ReviewMedia::getReviewId));

        for (Review r : reviews) {
            r.setMediaList(map.getOrDefault(r.getId(), new ArrayList<>()));
        }
        return reviews;
    }

    @Override
    @Transactional
    public void createReviewWithMedia(Long userId, Long reservationId, Integer score,
                                      String content, MultipartFile[] files) {
        if (reservationId == null) throw new BizException(400, "缺少预约信息");
        if (score == null || score < 1 || score > 5) throw new BizException(400, "评分必须在1-5之间");
        if (content == null || content.trim().isEmpty()) throw new BizException(400, "评价内容不能为空");
        if (files == null || files.length == 0) throw new BizException(400, "请至少上传一张图片或视频");

        Reservation reservation = reservationService.getById(reservationId);
        if (reservation == null) throw new BizException(400, "预约不存在");
        if (!reservation.getUserId().equals(userId)) throw new BizException(403, "不能评价他人的预约");
        if (reservation.getStatus() != 4) throw new BizException(400, "只有已签退的预约才能评价");

        long dup = this.count(new LambdaQueryWrapper<Review>()
                .eq(Review::getReservationId, reservationId));
        if (dup > 0) throw new BizException(400, "该预约已评价过");

        Seat seat = seatService.getById(reservation.getSeatId());
        if (seat == null) throw new BizException(400, "座位信息异常");

        Review review = new Review();
        review.setReservationId(reservationId);
        review.setRoomId(seat.getRoomId());
        review.setUserId(userId);
        review.setScore(score);
        review.setContent(content);
        this.save(review);

        // 存文件 + 插媒体记录
        File dir = new File(uploadProperties.getUploadPath(), "review");
        if (!dir.exists()) dir.mkdirs();

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String ct = file.getContentType();
            int type = (ct != null && ct.startsWith("video")) ? 2 : 1;

            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf("."))
                    : (type == 2 ? ".mp4" : ".jpg");
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            try {
                file.transferTo(new File(dir, fileName));
            } catch (IOException e) {
                throw new BizException(400, "文件保存失败");
            }

            ReviewMedia media = new ReviewMedia();
            media.setReviewId(review.getId());
            media.setType(type);
            media.setUrl("review/" + fileName);
            media.setCreateTime(LocalDateTime.now());
            reviewMediaMapper.insert(media);
        }
    }

}
