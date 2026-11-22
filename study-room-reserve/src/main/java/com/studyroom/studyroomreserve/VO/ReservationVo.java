package com.studyroom.studyroomreserve.VO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationVo {
    private Long seatId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
