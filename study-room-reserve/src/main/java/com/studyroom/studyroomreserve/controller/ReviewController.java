package com.studyroom.studyroomreserve.controller;

import com.studyroom.studyroomreserve.entity.Review;
import com.studyroom.studyroomreserve.service.ReviewService;
import com.studyroom.studyroomreserve.util.Result;
import com.studyroom.studyroomreserve.VO.ReviewVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/review")
@Tag(name = "评价接口")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    @PostMapping("/create")
    @Operation(summary = "提交评价")
    public Result<?> create(@RequestAttribute("userId") Long userId,
                            @RequestBody ReviewVo vo) {
        reviewService.createReview(userId, vo);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "按自习室查评价")
    public Result<List<Review>> list(@RequestParam Long roomId) {
        return Result.success(reviewService.listByRoom(roomId));
    }

    @PostMapping("/create/media")
    @Operation(summary = "提交评价（含图片/视频）")
    public Result<?> createMedia(@RequestAttribute("userId") Long userId,
                                 @RequestParam("reservationId") Long reservationId,
                                 @RequestParam("score") Integer score,
                                 @RequestParam("content") String content,
                                 @RequestParam("files") MultipartFile[] files) {
        reviewService.createReviewWithMedia(userId, reservationId, score, content, files);
        return Result.success();
    }

}
