package com.studyroom.studyroomreserve.VO;

import lombok.Data;

@Data
public class MessageSendVo {
    private Long userId;
    private String title;
    private String content;
}
