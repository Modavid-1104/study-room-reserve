package com.studyroom.studyroomreserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.studyroomreserve.VO.RoomListVo;
import com.studyroom.studyroomreserve.entity.Seat;
import com.studyroom.studyroomreserve.entity.StudyRoom;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.mapper.SeatMapper;
import com.studyroom.studyroomreserve.mapper.StudyRoomMapper;
import com.studyroom.studyroomreserve.service.StudyRoomService;
import com.studyroom.studyroomreserve.VO.RoomVo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyRoomServiceImpl extends ServiceImpl<StudyRoomMapper, StudyRoom> implements StudyRoomService {

    @Resource
    private SeatMapper seatMapper;


    @Override
    public IPage<StudyRoom> pageList(int pageNum, int pageSize, String name) {
        Page<StudyRoom> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<StudyRoom> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like(StudyRoom::getName, name);
        }
        wrapper.orderByDesc(StudyRoom::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void addRoom(RoomVo vo) {
        checkRoom(vo);
        StudyRoom room = new StudyRoom();
        // 把 VO 里同名字段一次性拷贝到实体，省去逐个 set
        BeanUtils.copyProperties(vo, room);
        this.save(room);
    }

    @Override
    public void updateRoom(RoomVo vo) {
        if (vo.getId() == null) {
            throw new BizException(400, "缺少自习室id");
        }
        if (this.getById(vo.getId()) == null) {
            throw new BizException(400, "自习室不存在");
        }
        checkRoom(vo);
        StudyRoom room = new StudyRoom();
        BeanUtils.copyProperties(vo, room);
        this.updateById(room);
    }

    @Override
    public void deleteRoom(Long id) {
        if (this.getById(id) == null) {
            throw new BizException(400, "自习室不存在");
        }
        // @TableLogic 自动做逻辑删除，不是真的物理删除
        this.removeById(id);
    }

    // 新增/修改共用的基础校验
    private void checkRoom(RoomVo vo) {
        if (vo.getName() == null || vo.getName().trim().isEmpty()) {
            throw new BizException(400, "自习室名称不能为空");
        }
        if (vo.getTotalSeat() == null || vo.getTotalSeat() <= 0) {
            throw new BizException(400, "总座位数必须大于0");
        }
    }

    @Override
    public Page<RoomListVo> pageWithFree(int pageNum, int pageSize) {
        Page<StudyRoom> roomPage = new Page<>(pageNum, pageSize);
        this.page(roomPage, new LambdaQueryWrapper<StudyRoom>()
                .orderByDesc(StudyRoom::getCreateTime));

        List<RoomListVo> voList = new ArrayList<>();
        for (StudyRoom room : roomPage.getRecords()) {
            RoomListVo vo = new RoomListVo();
            BeanUtils.copyProperties(room, vo);   // 先拷贝基础字段

            // 总座位数：未禁用(status 0或1)的座位，实时统计，覆盖拷贝来的 totalSeat
            long total = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                    .eq(Seat::getRoomId, room.getId())
                    .in(Seat::getStatus, 0, 1));
            vo.setTotalSeat((int) total);

            // 空闲座位：status=0
            long free = seatMapper.selectCount(new LambdaQueryWrapper<Seat>()
                    .eq(Seat::getRoomId, room.getId())
                    .eq(Seat::getStatus, 0));
            vo.setFreeSeat((int) free);

            voList.add(vo);
        }

        Page<RoomListVo> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotal(roomPage.getTotal());
        return result;
    }


}
