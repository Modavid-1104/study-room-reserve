package com.studyroom.studyroomreserve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.studyroomreserve.entity.Review;
import com.studyroom.studyroomreserve.VO.ReviewVo;

import java.util.List;

public interface ReviewService extends IService<Review> {
    void createReview(Long userId, ReviewVo vo);
    List<Review> listByRoom(Long roomId);
}
