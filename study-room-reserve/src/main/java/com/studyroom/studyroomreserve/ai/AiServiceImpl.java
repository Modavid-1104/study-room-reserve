package com.studyroom.studyroomreserve.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyroom.studyroomreserve.VO.*;
import com.studyroom.studyroomreserve.ai.dto.ChatMessage;
import com.studyroom.studyroomreserve.ai.dto.ChatRequest;
import com.studyroom.studyroomreserve.ai.dto.DeepSeekResponse;
import com.studyroom.studyroomreserve.ai.dto.ToolCall;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.studyroom.studyroomreserve.entity.Seat;
import com.studyroom.studyroomreserve.exception.BizException;
import com.studyroom.studyroomreserve.service.ReservationService;
import com.studyroom.studyroomreserve.service.SeatService;
import com.studyroom.studyroomreserve.service.StudyRoomService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiServiceImpl implements AiService {

    @Resource
    private RestClient deepSeekRestClient;
    @Resource
    private StudyRoomService studyRoomService;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private SeatService seatService;
    @Resource
    private ReservationService reservationService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;





    @Value("${deepseek.model}")
    private String model;

    @Override
    public String chat(Long userId, String conversationId, String userMessage,
                       Double latitude, Double longitude) {

        String redisKey = "chat:history:" + conversationId;

        // 1. 从 Redis 读历史（首次会话为空列表）
        List<ChatMessage> history = readHistory(redisKey);

        // 2. 组装本次发给模型的完整消息
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(buildSystemMessage());   // system 不入库，每次重新生成以注入最新时间
        messages.addAll(history);             // 历史对话
        ChatMessage newUser = new ChatMessage("user", userMessage);
        messages.add(newUser);                // 本次新消息

        // 记录"本次新增"的起始位置（system+历史之后）
        int persistStart = messages.size();

        // 3. 工具调用循环
        String finalAnswer;
        int maxRounds = 5;
        boolean answered = false;

        for (int round = 0; round < maxRounds; round++) {
            ChatRequest request = new ChatRequest();
            request.setModel(model);
            request.setMessages(messages);
            request.setTools(buildTools());

            DeepSeekResponse response = deepSeekRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(DeepSeekResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new BizException(500, "大模型无响应");
            }

            DeepSeekResponse.Choice choice = response.getChoices().get(0);
            ChatMessage assistantMsg = choice.getMessage();

            if ("tool_calls".equals(choice.getFinishReason())
                    && assistantMsg.getToolCalls() != null) {

                messages.add(assistantMsg);

                for (ToolCall call : assistantMsg.getToolCalls()) {
                    String toolName = call.getFunction().getName();
                    String arguments = call.getFunction().getArguments();
                    String result = executeTool(userId, toolName, arguments, latitude, longitude);


                    ChatMessage toolMsg = new ChatMessage();
                    toolMsg.setRole("tool");
                    toolMsg.setToolCallId(call.getId());
                    toolMsg.setContent(result);
                    messages.add(toolMsg);
                }
            } else {
                // 最终回答
                finalAnswer = assistantMsg.getContent();
                messages.add(assistantMsg);   // 让最终回复也进入待持久化部分
                answered = true;
                break;
            }
        }

        if (!answered) {
            throw new BizException(500, "工具调用轮次超限");
        }

        // 4. 把本次新增的消息（新user + 工具过程 + 最终assistant）追加进历史，写回 Redis
        List<ChatMessage> newMessages = new ArrayList<>(
                messages.subList(persistStart, messages.size()));
        history.addAll(newMessages);
        saveHistory(redisKey, history);

        return messages.get(messages.size() - 1).getContent();
    }

    // system 消息单独抽出来
    private ChatMessage buildSystemMessage() {
        String now = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return new ChatMessage("system",
                "当前时间是 " + now + "。" +
                        "你是校园自习室预约助手。你可以通过工具查询自习室、查询座位，并能调用 quick_reserve 工具真正帮用户完成预约。" +
                        "当用户表达预约意图时，你必须调用 quick_reserve 工具，不能只推荐座位或声称无法预约。" +
                        "当用户说今天、明天、后天等相对时间时，依据当前时间推算准确日期。" +
                        "时间参数一律使用 yyyy-MM-dd HH:mm:ss 格式。" +
                        "当用户说现在、立即开始时，开始时间要在当前时间基础上至少加1分钟，留出缓冲，不能使用已经过去的时间。" +
                        "不要向用户透露内部状态码数字、工具名称、接口或任何实现细节，也不要解释你的内部调用过程。" +
                        "描述座位状态时只用自然语言：空闲、已被预约、暂不可用（维护中），不要出现0、1、2等数字。" +
                        "回答简洁友好。" +
                        "回答使用纯文本，不要使用 Markdown 符号（如 **、#、- 列表等），用普通文字和换行排版。");

    }

    // 读历史
    @SuppressWarnings("unchecked")
    private List<ChatMessage> readHistory(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof List) {
            return new ArrayList<>((List<ChatMessage>) value);
        }
        return new ArrayList<>();
    }

    // 写回并设置 24 小时过期
    private void saveHistory(String key, List<ChatMessage> history) {
        redisTemplate.opsForValue().set(key, history, Duration.ofHours(24));
    }


    // 定义可用工具
    private List<Map<String, Object>> buildTools() {
        // 工具1：自习室列表
        Map<String, Object> tool1 = new HashMap<>();
        tool1.put("type", "function");
        Map<String, Object> f1 = new HashMap<>();
        f1.put("name", "get_room_list");
        f1.put("description", "获取所有自习室列表，包含每个自习室的ID、总座位数和空闲座位数");
        Map<String, Object> p1 = new HashMap<>();
        p1.put("type", "object");
        p1.put("properties", new HashMap<>());
        f1.put("parameters", p1);
        tool1.put("function", f1);

        // 工具2：查某房间的座位（带参数）
        Map<String, Object> tool2 = new HashMap<>();
        tool2.put("type", "function");
        Map<String, Object> f2 = new HashMap<>();
        f2.put("name", "get_seats_by_room");
        f2.put("description", "查询指定自习室的所有座位及状态，状态0空闲、1已预约、2禁用");
        Map<String, Object> p2 = new HashMap<>();
        p2.put("type", "object");
        Map<String, Object> props = new HashMap<>();
        Map<String, Object> roomIdProp = new HashMap<>();
        roomIdProp.put("type", "integer");
        roomIdProp.put("description", "自习室ID，可从get_room_list获取");
        props.put("roomId", roomIdProp);
        p2.put("type", "object");
        p2.put("properties", props);
        p2.put("required", List.of("roomId"));
        f2.put("parameters", p2);
        tool2.put("function", f2);

        // 工具3：一键预约
        Map<String, Object> tool3 = new HashMap<>();
        tool3.put("type", "function");
        Map<String, Object> f3 = new HashMap<>();
        f3.put("name", "quick_reserve");
        f3.put("description", "一键预约：在指定时间段自动找一个空闲座位完成预约，可限定房间");
        Map<String, Object> p3 = new HashMap<>();
        p3.put("type", "object");
        Map<String, Object> props3 = new HashMap<>();

        Map<String, Object> st = new HashMap<>();
        st.put("type", "string");
        st.put("description", "开始时间，格式 yyyy-MM-dd HH:mm:ss");
        props3.put("startTime", st);

        Map<String, Object> et = new HashMap<>();
        et.put("type", "string");
        et.put("description", "结束时间，格式 yyyy-MM-dd HH:mm:ss");
        props3.put("endTime", et);

        Map<String, Object> rid = new HashMap<>();
        rid.put("type", "integer");
        rid.put("description", "自习室ID，可选，不填则在所有房间找座");
        props3.put("roomId", rid);

        p3.put("properties", props3);
        p3.put("required", List.of("startTime", "endTime"));
        f3.put("parameters", p3);
        tool3.put("function", f3);

        // 工具4：我的预约列表
        Map<String, Object> tool4 = new HashMap<>();
        tool4.put("type", "function");
        Map<String, Object> f4 = new HashMap<>();
        f4.put("name", "get_my_reservations");
        f4.put("description", "查询当前用户的所有预约，返回预约ID、房间、座位、时间和状态，状态0待签到、1已签到、2已取消、3超时、4已完成");
        Map<String, Object> p4 = new HashMap<>();
        p4.put("type", "object");
        p4.put("properties", new HashMap<>());
        f4.put("parameters", p4);
        tool4.put("function", f4);

        // 工具5：取消预约（带参数）
        Map<String, Object> tool5 = new HashMap<>();
        tool5.put("type", "function");
        Map<String, Object> f5 = new HashMap<>();
        f5.put("name", "cancel_reservation");
        f5.put("description", "取消指定的预约，只能取消状态为待签到(0)的预约");
        Map<String, Object> p5 = new HashMap<>();
        p5.put("type", "object");
        Map<String, Object> props5 = new HashMap<>();
        Map<String, Object> ridProp = new HashMap<>();
        ridProp.put("type", "integer");
        ridProp.put("description", "预约ID，可从get_my_reservations获取");
        props5.put("reservationId", ridProp);
        p5.put("properties", props5);
        p5.put("required", List.of("reservationId"));
        f5.put("parameters", p5);
        tool5.put("function", f5);

        // 工具6：签到
        Map<String, Object> tool6 = new HashMap<>();
        tool6.put("type", "function");
        Map<String, Object> f6 = new HashMap<>();
        f6.put("name", "sign_reservation");
        f6.put("description", "对指定预约进行签到，用户到达自习室时使用，只有待签到状态能签到");
        Map<String, Object> p6 = new HashMap<>();
        p6.put("type", "object");
        Map<String, Object> props6 = new HashMap<>();
        Map<String, Object> signRid = new HashMap<>();
        signRid.put("type", "integer");
        signRid.put("description", "预约ID，可从get_my_reservations获取");
        props6.put("reservationId", signRid);
        p6.put("properties", props6);
        p6.put("required", List.of("reservationId"));
        f6.put("parameters", p6);
        tool6.put("function", f6);

        // 工具7：签退
        Map<String, Object> tool7 = new HashMap<>();
        tool7.put("type", "function");
        Map<String, Object> f7 = new HashMap<>();
        f7.put("name", "signout_reservation");
        f7.put("description", "对指定预约进行签退，用户学习结束离开时使用，只有已签到状态能签退");
        Map<String, Object> p7 = new HashMap<>();
        p7.put("type", "object");
        Map<String, Object> props7 = new HashMap<>();
        Map<String, Object> outRid = new HashMap<>();
        outRid.put("type", "integer");
        outRid.put("description", "预约ID，可从get_my_reservations获取");
        props7.put("reservationId", outRid);
        p7.put("properties", props7);
        p7.put("required", List.of("reservationId"));
        f7.put("parameters", p7);
        tool7.put("function", f7);



        return List.of(tool1, tool2, tool3, tool4, tool5, tool6, tool7);
    }

    private String executeTool(Long userId, String name, String arguments,
                               Double latitude, Double longitude) {
        try {
            if ("get_room_list".equals(name)) {
                Page<RoomListVo> page = studyRoomService.pageWithFree(1, 100);
                List<Map<String, Object>> rooms = new ArrayList<>();
                for (RoomListVo vo : page.getRecords()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", vo.getId());          // ★ 加上ID，供工具2使用
                    m.put("name", vo.getName());
                    m.put("totalSeat", vo.getTotalSeat());
                    m.put("freeSeat", vo.getFreeSeat());
                    rooms.add(m);
                }
                return objectMapper.writeValueAsString(rooms);
            }

            if ("get_seats_by_room".equals(name)) {
                // 解析参数
                JsonNode node = objectMapper.readTree(arguments);
                Long roomId = node.get("roomId").asLong();

                List<Seat> seats = seatService.list(new LambdaQueryWrapper<Seat>()
                        .eq(Seat::getRoomId, roomId)
                        .orderByAsc(Seat::getSeatNo));

                List<Map<String, Object>> seatList = new ArrayList<>();
                int free = 0;
                for (Seat s : seats) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("seatNo", s.getSeatNo());
                    m.put("status", s.getStatus());
                    seatList.add(m);
                    if (s.getStatus() == 0) {
                        free++;
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("totalSeat", seats.size());
                result.put("freeSeat", free);
                result.put("seats", seatList);
                return objectMapper.writeValueAsString(result);
            }
            if ("quick_reserve".equals(name)) {
                JsonNode node = objectMapper.readTree(arguments);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                LocalDateTime start = LocalDateTime.parse(node.get("startTime").asText(), fmt);
                LocalDateTime end = LocalDateTime.parse(node.get("endTime").asText(), fmt);

                QuickReservationVo vo = new QuickReservationVo();
                vo.setStartTime(start);
                vo.setEndTime(end);
                if (node.has("roomId") && !node.get("roomId").isNull()) {
                    vo.setRoomId(node.get("roomId").asLong());
                }

                QuickResultVo result = reservationService.quickReserve(userId, vo);
                return objectMapper.writeValueAsString(result);
            }
            if ("get_my_reservations".equals(name)) {
                List<ReservationListVo> list = reservationService.myList(userId);
                List<Map<String, Object>> result = new ArrayList<>();
                for (ReservationListVo vo : list) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("reservationId", vo.getId());
                    m.put("roomName", vo.getRoomName());
                    m.put("seatNo", vo.getSeatNo());
                    m.put("startTime", vo.getStartTime());
                    m.put("endTime", vo.getEndTime());
                    m.put("status", vo.getStatus());
                    result.add(m);
                }
                return objectMapper.writeValueAsString(result);
            }

            if ("cancel_reservation".equals(name)) {
                JsonNode node = objectMapper.readTree(arguments);
                Long reservationId = node.get("reservationId").asLong();
                reservationService.cancelReservation(userId, reservationId);
                return "{\"success\":true,\"message\":\"取消成功\"}";
            }

            if ("sign_reservation".equals(name)) {
                JsonNode node = objectMapper.readTree(arguments);
                Long reservationId = node.get("reservationId").asLong();

                SignVo signVo = new SignVo();
                signVo.setLatitude(latitude);
                signVo.setLongitude(longitude);

                reservationService.signReservation(userId, reservationId, signVo);
                return "{\"success\":true,\"message\":\"签到成功\"}";
            }


            if ("signout_reservation".equals(name)) {
                JsonNode node = objectMapper.readTree(arguments);
                Long reservationId = node.get("reservationId").asLong();
                reservationService.signOutReservation(userId, reservationId);
                return "{\"success\":true,\"message\":\"签退成功\"}";
            }
        } catch (BizException e) {
            throw e;                                   // 业务异常（距离、状态、未定位等）原样抛，保留状态码和友好提示
        } catch (Exception e) {
            throw new BizException(500, "操作失败，请稍后重试");   // 系统异常不暴露内部细节
        }

        throw new BizException(400, "未知工具：" + name);
    }


}
