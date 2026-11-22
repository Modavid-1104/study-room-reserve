package com.studyroom.studyroomreserve.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.studyroom.studyroomreserve.entity.Reservation;
import com.studyroom.studyroomreserve.entity.Seat;
import com.studyroom.studyroomreserve.service.ReservationService;
import com.studyroom.studyroomreserve.service.SeatService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationTask {

    @Resource
    private ReservationService reservationService;

    @Resource
    private SeatService seatService;

    // 每 60 秒执行一次
    @Scheduled(fixedRate = 60000)
    public void autoReleaseTimeout() {
        // 1. 先查出待签到(0)且结束时间已过的预约
        List<Reservation> timeoutList = reservationService.list(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getStatus, 0)
                        .lt(Reservation::getEndTime, LocalDateTime.now()));

        if (timeoutList.isEmpty()) {
            return;
        }

        // 2. 批量把这些预约改为超时释放(3)
        List<Long> ids = timeoutList.stream()
                .map(Reservation::getId).collect(Collectors.toList());
        reservationService.update(new LambdaUpdateWrapper<Reservation>()
                .in(Reservation::getId, ids)
                .set(Reservation::getStatus, 3));

        // 3. 把对应座位批量恢复为空闲(0)
        List<Long> seatIds = timeoutList.stream()
                .map(Reservation::getSeatId).distinct()
                .collect(Collectors.toList());
        seatService.update(new LambdaUpdateWrapper<Seat>()
                .in(Seat::getId, seatIds)
                .set(Seat::getStatus, 0));
    }
}
