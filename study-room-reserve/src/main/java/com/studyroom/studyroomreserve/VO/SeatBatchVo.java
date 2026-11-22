package com.studyroom.studyroomreserve.VO;

import lombok.Data;

@Data
public class SeatBatchVo {
    private Long roomId;
    private Integer count;    // 生成数量
    private String prefix;    // 座位号前缀，如 A
}
