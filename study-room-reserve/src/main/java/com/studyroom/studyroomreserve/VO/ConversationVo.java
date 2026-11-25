package com.studyroom.studyroomreserve.VO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationVo {
    private Long id;              // 会话id
    private Long peerId;          // 对方用户id
    private String peerNickname;
    private String peerAvatar;
    private String lastMessage;
    private LocalDateTime lastTime;
    private Integer unread;       // 当前用户视角的未读数
}
