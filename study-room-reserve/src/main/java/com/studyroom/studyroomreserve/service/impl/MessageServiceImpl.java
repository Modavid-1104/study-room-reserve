package com.studyroom.studyroomreserve.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyroom.studyroomreserve.VO.MessageSendVo;
import com.studyroom.studyroomreserve.entity.Message;
import com.studyroom.studyroomreserve.entity.User;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.mapper.MessageMapper;
import com.studyroom.studyroomreserve.service.MessageService;
import com.studyroom.studyroomreserve.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.studyroom.studyroomreserve.ws.WebSocketServer;
import cn.hutool.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Resource
    private UserService userService;

    @Override
    public List<Message> myList(Long userId) {
        return this.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime));
    }

    @Override
    public void markRead(Long userId, Long id) {
        Message msg = this.getById(id);
        if(msg == null || !msg.getUserId().equals(userId)){
            throw new BizException(400,"消息不存在");
        }
        msg.setIsRead(1);
        this.updateById(msg);
    }
    @Override
    @Transactional
    public void sendMessage(MessageSendVo vo){
        if(vo.getTitle() == null || vo.getTitle().isEmpty()){
            throw new BizException(400,"消息标题不存在");
        }
        if (vo.getUserId() != null) {
            // 单发
            User user = userService.getById(vo.getUserId());
            if (user == null) {
                throw new BizException(400, "目标用户不存在");
            }
            Message msg = buildMessage(vo, vo.getUserId());
            this.save(msg);
            push(msg);
        } else {
            // 群发：所有学生
            List<User> users = userService.list(
                    new LambdaQueryWrapper<User>().eq(User::getRole, 0));
            List<Message> messages = new ArrayList<>();
            for (User u : users) {
                messages.add(buildMessage(vo, u.getId()));
            }
            this.saveBatch(messages);
            for (Message m : messages) {
                push(m);
            }
        }
    }

    // 把消息组成 JSON 推给在线用户
    private void push(Message msg) {
        JSONObject json = new JSONObject();
        json.set("id", msg.getId());
        json.set("title", msg.getTitle());
        json.set("content", msg.getContent());
        WebSocketServer.sendToUser(msg.getUserId(), json.toString());
    }


    private Message buildMessage(MessageSendVo vo, Long userId) {
        Message msg = new Message();
        msg.setUserId(userId);
        msg.setTitle(vo.getTitle());
        msg.setContent(vo.getContent());
        msg.setIsRead(0);
        return msg;
    }
}
