package com.studyroom.studyroomreserve.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.studyroomreserve.VO.RoomListVo;
import com.studyroom.studyroomreserve.entity.StudyRoom;
import com.studyroom.studyroomreserve.service.StudyRoomService;
import com.studyroom.studyroomreserve.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@Tag(name = "自习室接口")
public class StudyRoomController {

    @Resource
    private StudyRoomService studyRoomService;

    @GetMapping("/page")
    @Operation(summary = "分页查询自习室")
    public Result<Page<RoomListVo>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(studyRoomService.pageWithFree(pageNum, pageSize));
    }


    // 详情：/room/1
    @GetMapping("/{id}")
    @Operation(summary = "自习室详情")
    public Result<StudyRoom> getById(@PathVariable Long id) {
        return Result.success(studyRoomService.getById(id));
    }
}
