package com.studyroom.studyroomreserve.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.studyroomreserve.VO.RoomListVo;
import com.studyroom.studyroomreserve.VO.RoomVo;
import com.studyroom.studyroomreserve.entity.StudyRoom;

public interface StudyRoomService extends IService<StudyRoom> {
    IPage<StudyRoom> pageList(int pageNum, int pageSize, String name);

    void addRoom(RoomVo vo);
    void updateRoom(RoomVo vo);
    void deleteRoom(Long id);
    Page<RoomListVo> pageWithFree(int pageNum, int pageSize);

}
