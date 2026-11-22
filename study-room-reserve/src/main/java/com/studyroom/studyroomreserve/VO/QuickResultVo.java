package com.studyroom.studyroomreserve.VO;

import lombok.Data;

@Data
public class QuickResultVo {
    private Long reservationId;
    private Long seatId;
    private String seatNo;
    private String roomName;
}
