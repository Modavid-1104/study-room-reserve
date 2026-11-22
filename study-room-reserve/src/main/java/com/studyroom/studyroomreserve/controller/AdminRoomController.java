package com.studyroom.studyroomreserve.controller;

import com.studyroom.studyroomreserve.service.StudyRoomService;
import com.studyroom.studyroomreserve.util.Result;
import com.studyroom.studyroomreserve.VO.RoomVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/room")
@Tag(name = "管理员-自习室管理")
public class AdminRoomController {

    @Resource
    private StudyRoomService studyRoomService;

    @PostMapping
    @Operation(summary = "新增自习室")
    public Result<?> add(@RequestBody RoomVo vo) {
        studyRoomService.addRoom(vo);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改自习室")
    public Result<?> update(@RequestBody RoomVo vo) {
        studyRoomService.updateRoom(vo);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除自习室")
    public Result<?> delete(@PathVariable Long id) {
        studyRoomService.deleteRoom(id);
        return Result.success();
    }
}
