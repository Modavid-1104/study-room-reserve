package com.studyroom.studyroomreserve.VO;

import lombok.Data;

@Data
public class RoomListVo {
    private Long id;
    private String name;
    private String address;
    private String cover;
    private String description;
    private Integer totalSeat;
    private String openTime;
    private String closeTime;
    private Integer freeSeat;   // 实时空闲座位数
}
