package com.studyroom.studyroomreserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyroom.studyroomreserve.VO.ConversationVo;
import com.studyroom.studyroomreserve.VO.SendPrivateMsgVo;
import com.studyroom.studyroomreserve.entity.Conversation;
import com.studyroom.studyroomreserve.entity.PrivateMessage;
import com.studyroom.studyroomreserve.entity.User;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.mapper.ConversationMapper;
import com.studyroom.studyroomreserve.mapper.PrivateMessageMapper;
import com.studyroom.studyroomreserve.service.ChatService;
import com.studyroom.studyroomreserve.service.UserService;
import com.studyroom.studyroomreserve.ws.WebSocketServer;

import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ConversationMapper conversationMapper;

    @Resource
    private PrivateMessageMapper privateMessageMapper;

    @Resource
    private UserService userService;

    @Override
    public ConversationVo getOrCreateConversation(Long userId) {
        Conversation c = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
                .eq(Conversation::getUserId, userId)
                .last("limit 1"));

        if (c == null) {
            // 找一个管理员，没有就报错
            User admin = userService.getOne(new LambdaQueryWrapper<User>()
                    .eq(User::getRole, 1)
                    .last("limit 1"));
            if (admin == null) {
                throw new BizException(400, "暂无可用管理员");
            }
            c = new Conversation();
            c.setUserId(userId);
            c.setAdminId(admin.getId());
            c.setUserUnread(0);
            c.setAdminUnread(0);
            c.setCreateTime(LocalDateTime.now());
            conversationMapper.insert(c);
        }
        return toVo(c, userId);
    }

    @Override
    public List<ConversationVo> myConversations(Long userId) {
        List<Conversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getUserId, userId)
                        .orderByDesc(Conversation::getLastTime));
        return list.stream().map(c -> toVo(c, userId)).collect(Collectors.toList());
    }

    @Override
    public List<ConversationVo> adminConversations(Long adminId) {
        List<Conversation> list = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getAdminId, adminId)
                        .orderByDesc(Conversation::getLastTime));
        return list.stream().map(c -> toVo(c, adminId)).collect(Collectors.toList());
    }

    @Override
    public List<PrivateMessage> history(Long userId, Long conversationId) {
        requireConversation(userId, conversationId);
        return privateMessageMapper.selectList(new LambdaQueryWrapper<PrivateMessage>()
                .eq(PrivateMessage::getConversationId, conversationId)
                .orderByAsc(PrivateMessage::getCreateTime));
    }

    @Override
    public void sendMessage(Long senderId, SendPrivateMsgVo vo) {
        if (vo.getContent() == null || vo.getContent().trim().isEmpty()) {
            throw new BizException(400, "消息内容不能为空");
        }
        Conversation c = conversationMapper.selectById(vo.getConversationId());
        if (c == null) {
            throw new BizException(400, "会话不存在");
        }

        boolean senderIsStudent = c.getUserId().equals(senderId);
        if (!senderIsStudent && !c.getAdminId().equals(senderId)) {
            throw new BizException(403, "无权在此会话发言");
        }
        Long receiverId = senderIsStudent ? c.getAdminId() : c.getUserId();

        PrivateMessage pm = new PrivateMessage();
        pm.setConversationId(c.getId());
        pm.setSenderId(senderId);
        pm.setReceiverId(receiverId);
        pm.setContent(vo.getContent());
        pm.setIsRead(0);
        pm.setCreateTime(LocalDateTime.now());
        privateMessageMapper.insert(pm);

        // 更新会话：最后消息、时间、接收方未读 +1
        c.setLastMessage(pm.getContent());
        c.setLastTime(pm.getCreateTime());
        if (senderIsStudent) {
            c.setAdminUnread(nullToZero(c.getAdminUnread()) + 1);
        } else {
            c.setUserUnread(nullToZero(c.getUserUnread()) + 1);
        }
        conversationMapper.updateById(c);

        // 实时推送给接收方，带 type 区分这是私信
        JSONObject json = new JSONObject();
        json.set("type", "private");
        json.set("conversationId", c.getId());
        json.set("senderId", senderId);
        json.set("content", pm.getContent());
        json.set("createTime", pm.getCreateTime());
        WebSocketServer.sendToUser(receiverId, json.toString());
    }

    @Override
    public void markRead(Long userId, Long conversationId) {
        Conversation c = requireConversation(userId, conversationId);

        List<PrivateMessage> unread = privateMessageMapper.selectList(
                new LambdaQueryWrapper<PrivateMessage>()
                        .eq(PrivateMessage::getConversationId, conversationId)
                        .eq(PrivateMessage::getReceiverId, userId)
                        .eq(PrivateMessage::getIsRead, 0));
        for (PrivateMessage pm : unread) {
            pm.setIsRead(1);
            privateMessageMapper.updateById(pm);
        }

        boolean iAmStudent = c.getUserId().equals(userId);
        if (iAmStudent) {
            c.setUserUnread(0);
        } else {
            c.setAdminUnread(0);
        }
        conversationMapper.updateById(c);
    }

    // 校验会话存在且当前用户是参与者
    private Conversation requireConversation(Long userId, Long conversationId) {
        Conversation c = conversationMapper.selectById(conversationId);
        if (c == null
                || (!c.getUserId().equals(userId) && !c.getAdminId().equals(userId))) {
            throw new BizException(400, "会话不存在");
        }
        return c;
    }

    // 组装会话展示对象，根据当前用户身份决定"对方"和未读数
    private ConversationVo toVo(Conversation c, Long currentUserId) {
        ConversationVo vo = new ConversationVo();
        vo.setId(c.getId());
        vo.setLastMessage(c.getLastMessage());
        vo.setLastTime(c.getLastTime());

        boolean iAmStudent = c.getUserId().equals(currentUserId);
        Long peerId = iAmStudent ? c.getAdminId() : c.getUserId();
        User peer = userService.getById(peerId);

        vo.setPeerId(peerId);
        if (peer != null) {
            vo.setPeerNickname(peer.getNickname());
            vo.setPeerAvatar(peer.getAvatar());
        }
        vo.setUnread(iAmStudent
                ? nullToZero(c.getUserUnread())
                : nullToZero(c.getAdminUnread()));
        return vo;
    }

    private int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }
}
