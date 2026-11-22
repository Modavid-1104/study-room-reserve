package com.studyroom.studyroomreserve.VO;

import lombok.Data;

@Data
public class ReviewVo {
    private Long reservationId;
    private Integer score;
    private String content;
}
