package com.studyroom.studyroomreserve.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyroom.studyroomreserve.VO.*;
import com.studyroom.studyroomreserve.entity.Reservation;

import java.util.List;

public interface ReservationService extends IService<Reservation> {
    List<ReservationListVo> myList(Long userId);
    void cancelReservation(Long userId, Long id);
    void createReservation(Long userId, ReservationVo vo);
    void signReservation(Long userId, Long id, SignVo vo);
    QuickResultVo quickReserve(Long userId, QuickReservationVo vo);
    void signOutReservation(Long userId, Long id);
    StatisticsVo getStatistics(Long userId);
    void signWithPhoto(Long userId,Long id,Double lat,Double lon,
                       org.springframework.web.multipart.MultipartFile file);
}
