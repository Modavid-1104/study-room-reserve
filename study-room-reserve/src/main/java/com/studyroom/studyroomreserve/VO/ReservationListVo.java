package com.studyroom.studyroomreserve.VO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationListVo {
    private Long id;
    private Long seatId;
    private String seatNo;      // 来自 seat 表
    private String roomName;    // 来自 study_room 表
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime signInTime;
    private LocalDateTime createTime;
    private Long roomId;
    private Double latitude;
    private Double longitude;
    private Integer radius;

}
