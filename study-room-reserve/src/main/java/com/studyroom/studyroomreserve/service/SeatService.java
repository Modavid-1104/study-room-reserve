package com.studyroom.studyroomreserve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.studyroomreserve.VO.SeatBatchVo;
import com.studyroom.studyroomreserve.VO.SeatVo;
import com.studyroom.studyroomreserve.entity.Seat;

public interface SeatService extends IService<Seat> {
    void batchGenerate(SeatBatchVo vo);
    void updateSeat(SeatVo vo);
    void updateStatus(Long id, Integer status);

}
