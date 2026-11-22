package com.studyroom.studyroomreserve.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.studyroomreserve.entity.Seat;
import com.studyroom.studyroomreserve.service.SeatService;
import com.studyroom.studyroomreserve.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seat")
@Tag(name = "座位接口")
public class SeatController {

    @Resource
    private SeatService seatService;

    // 按自习室查座位：/seat/list?roomId=1，按座位号排序
    @GetMapping("/list")
    @Operation(summary = "按自习室查询座位")
    public Result<List<Seat>> list(@RequestParam Long roomId) {
        LambdaQueryWrapper<Seat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Seat::getRoomId, roomId)
                .orderByAsc(Seat::getSeatNo);
        return Result.success(seatService.list(wrapper));
    }
}
