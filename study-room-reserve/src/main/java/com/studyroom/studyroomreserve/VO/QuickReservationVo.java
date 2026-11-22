package com.studyroom.studyroomreserve.VO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QuickReservationVo {
    private Long roomId;          // 可选，为空则全部自习室
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
