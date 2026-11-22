package com.studyroom.studyroomreserve.controller;

import com.studyroom.studyroomreserve.VO.SeatVo;
import com.studyroom.studyroomreserve.service.SeatService;
import com.studyroom.studyroomreserve.util.Result;
import com.studyroom.studyroomreserve.VO.SeatBatchVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/seat")
@Tag(name = "管理员-座位管理")
public class AdminSeatController {

    @Resource
    private SeatService seatService;

    @PostMapping("/batch")
    @Operation(summary = "批量生成座位")
    public Result<?> batch(@RequestBody SeatBatchVo vo) {
        seatService.batchGenerate(vo);
        return Result.success();
    }
    @PutMapping
    @Operation(summary = "修改座位信息")
    public Result<?> update(@RequestBody SeatVo vo) {
        seatService.updateSeat(vo);
        return Result.success();
    }

    @PostMapping("/status")
    @Operation(summary = "启用/禁用座位")
    public Result<?> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        seatService.updateStatus(id, status);
        return Result.success();
    }

}
