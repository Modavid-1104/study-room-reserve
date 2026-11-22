package com.studyroom.studyroomreserve.controller;

import com.studyroom.studyroomreserve.VO.*;
import com.studyroom.studyroomreserve.service.ReservationService;
import com.studyroom.studyroomreserve.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservation")
@Tag(name = "预约接口")
public class ReservationController {

    @Resource
    private ReservationService reservationService;

    @PostMapping("/create")
    @Operation(summary = "创建预约")
    public Result<?> create(@RequestAttribute("userId") Long userId,
                            @RequestBody ReservationVo vo) {
        reservationService.createReservation(userId, vo);
        return Result.success();
    }
    @GetMapping("/my")
    @Operation(summary = "我的预约列表")
    public Result<List<ReservationListVo>> myList(@RequestAttribute("userId") Long userId) {
        return Result.success(reservationService.myList(userId));
    }
    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消预约")
    public Result<?> cancel(@RequestAttribute("userId") Long userId,
                            @PathVariable Long id) {
        reservationService.cancelReservation(userId, id);
        return Result.success();
    }
    @PostMapping("/sign/{id}")
    @Operation(summary = "签到（需定位）")
    public Result<?> sign(@RequestAttribute("userId") Long userId,
                          @PathVariable Long id,
                          @RequestBody SignVo vo) {
        reservationService.signReservation(userId, id, vo);
        return Result.success();
    }



    @PostMapping("/quick")
    @Operation(summary = "一键找座")
    public Result<QuickResultVo> quick(@RequestAttribute("userId") Long userId,
                                       @RequestBody QuickReservationVo vo) {
        return Result.success(reservationService.quickReserve(userId, vo));
    }

    @PostMapping("/signout/{id}")
    @Operation(summary = "签退")
    public Result<?> signOut(@RequestAttribute("userId") Long userId,
                             @PathVariable Long id) {
        reservationService.signOutReservation(userId, id);
        return Result.success();
    }
    @GetMapping("/statistics")
    @Operation(summary = "学习统计")
    public Result<StatisticsVo> statistics(@RequestAttribute("userId") Long userId) {
        return Result.success(reservationService.getStatistics(userId));
    }



}
