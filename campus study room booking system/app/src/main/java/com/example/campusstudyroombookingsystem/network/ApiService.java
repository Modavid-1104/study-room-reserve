package com.example.campusstudyroombookingsystem.network;

import com.example.campusstudyroombookingsystem.model.ApiResponse;
import com.example.campusstudyroombookingsystem.model.ChatRequestBody;
import com.example.campusstudyroombookingsystem.model.ConversationResponse;
import com.example.campusstudyroombookingsystem.model.LoginRequest;
import com.example.campusstudyroombookingsystem.model.MessageResponse;
import com.example.campusstudyroombookingsystem.model.MessageSendRequest;
import com.example.campusstudyroombookingsystem.model.PageResponse;
import com.example.campusstudyroombookingsystem.model.PrivateMessageResponse;
import com.example.campusstudyroombookingsystem.model.QuickReservationRequest;
import com.example.campusstudyroombookingsystem.model.QuickResultResponse;
import com.example.campusstudyroombookingsystem.model.RegisterRequest;
import com.example.campusstudyroombookingsystem.model.ReservationResponse;
import com.example.campusstudyroombookingsystem.model.ReviewRequest;
import com.example.campusstudyroombookingsystem.model.ReviewResponse;
import com.example.campusstudyroombookingsystem.model.RoomRequest;
import com.example.campusstudyroombookingsystem.model.SeatBatchRequest;
import com.example.campusstudyroombookingsystem.model.SeatRequest;
import com.example.campusstudyroombookingsystem.model.SeatResponse;
import com.example.campusstudyroombookingsystem.model.SendPrivateMsgRequest;
import com.example.campusstudyroombookingsystem.model.SignRequest;
import com.example.campusstudyroombookingsystem.model.StatisticsResponse;
import com.example.campusstudyroombookingsystem.model.StudyRoomResponse;
import com.example.campusstudyroombookingsystem.model.TokenResponse;
import com.example.campusstudyroombookingsystem.model.CreateReservationRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit 接口：每个方法对应一个后端接口。
 * 注解里的路径会和 RetrofitClient 的 baseUrl 拼接。
 */
public interface ApiService {

    // POST /user/login
    @POST("user/login")
    Call<ApiResponse<TokenResponse>> login(@Body LoginRequest request);

    // POST /user/register
    @POST("user/register")
    Call<ApiResponse<Void>> register(@Body RegisterRequest request);

    // GET room/page?pageNum=1&pageSize=10
    @GET("room/page")
    Call<ApiResponse<PageResponse<StudyRoomResponse>>> roomPage(
            @Query("pageNum") int pageNum,
            @Query("pageSize") int pageSize);

    // GET room/{id}
    @GET("room/{id}")
    Call<ApiResponse<StudyRoomResponse>> roomDetail(@Path("id") long id);

    // GET seat/list?roomId=1
    @GET("seat/list")
    Call<ApiResponse<List<SeatResponse>>> seatList(@Query("roomId") long roomId);
    // GET reservation/my
    @GET("reservation/my")
    Call<ApiResponse<List<ReservationResponse>>> myReservations();
    @POST("reservation/cancel/{id}")
    Call<ApiResponse<Void>> cancelReservation(@Path("id") long id);
    @POST("reservation/sign/{id}")
    Call<ApiResponse<Void>> signReservation(@Path("id") long id, @Body SignRequest body);
    @POST("reservation/signout/{id}")
    Call<ApiResponse<Void>> signOutReservation(@Path("id") long id);
    @POST("reservation/create")
    Call<ApiResponse<Void>> createReservation(@Body CreateReservationRequest request);
    // POST review/create
    @POST("review/create")
    Call<ApiResponse<Void>> createReview(@Body ReviewRequest request);

    // GET review/list?roomId=
    @GET("review/list")
    Call<ApiResponse<List<ReviewResponse>>> reviewList(@Query("roomId") long roomId);
    // 管理员-自习室新增
    @POST("admin/room")
    Call<ApiResponse<Void>> addRoom(@Body RoomRequest request);

    // 管理员-自习室修改
    @PUT("admin/room")
    Call<ApiResponse<Void>> updateRoom(@Body RoomRequest request);

    // 管理员-自习室删除
    @DELETE("admin/room/{id}")
    Call<ApiResponse<Void>> deleteRoom(@Path("id") long id);

    @POST("admin/seat/batch")
    Call<ApiResponse<Void>> batchSeat(@Body SeatBatchRequest request);

    @PUT("admin/seat/status")
    Call<ApiResponse<Void>> updateSeat(@Body SeatRequest request);

    @POST("admin/seat/status")
    Call<ApiResponse<Void>> updateSeatStatus(@Query("id") long id,
                                             @Query("status") int status);
    // 我的消息列表
    @GET("message/list")
    Call<ApiResponse<List<MessageResponse>>> messageList();

    // 标记已读
    @POST("message/read/{id}")
    Call<ApiResponse<Void>> markRead(@Path("id") long id);
    // 管理员-发消息（单发/群发）
    @POST("admin/message/send")
    Call<ApiResponse<Void>> sendMessage(@Body MessageSendRequest request);

    // POST reservation/quick 一键找座
    @POST("reservation/quick")
    Call<ApiResponse<QuickResultResponse>> quickReserve(@Body QuickReservationRequest request);

    // GET reservation/statistics 学习统计
    @GET("reservation/statistics")
    Call<ApiResponse<StatisticsResponse>> statistics();

    // POST ai/chat AI对话
    @POST("ai/chat")
    Call<ApiResponse<String>> aiChat(@Body ChatRequestBody body);

    @Multipart
    @POST("reservation/sign/photo/{id}")
    Call<ApiResponse<Void>> signWithPhoto(@Path("id") Long id,
                                          @Part("lat") RequestBody lat,
                                          @Part("lon") RequestBody lon,
                                          @Part MultipartBody.Part file);


    @Multipart
    @POST("review/create/media")
    Call<ApiResponse<Void>> createReviewMedia(
            @Part("reservationId") RequestBody reservationId,
            @Part("score") RequestBody score,
            @Part("content") RequestBody content,
            @Part List<MultipartBody.Part> files);

    // ===== 学生端：在线咨询 =====

    // 获取或创建我的咨询会话
    @GET("chat/conversation")
    Call<ApiResponse<ConversationResponse>> myConversation();

    // 我的会话列表
    @GET("chat/list")
    Call<ApiResponse<List<ConversationResponse>>> myConversationList();

    // 历史消息
    @GET("chat/history/{conversationId}")
    Call<ApiResponse<List<PrivateMessageResponse>>> privateHistory(
            @Path("conversationId") long conversationId);

    // 发送
    @POST("chat/send")
    Call<ApiResponse<Void>> sendPrivate(@Body SendPrivateMsgRequest request);

    // 标记已读
    @POST("chat/read/{conversationId}")
    Call<ApiResponse<Void>> markPrivateRead(@Path("conversationId") long conversationId);

    // ===== 管理员端：咨询会话 =====

    @GET("admin/chat/list")
    Call<ApiResponse<List<ConversationResponse>>> adminConversationList();

    @GET("admin/chat/history/{conversationId}")
    Call<ApiResponse<List<PrivateMessageResponse>>> adminPrivateHistory(
            @Path("conversationId") long conversationId);

    @POST("admin/chat/send")
    Call<ApiResponse<Void>> adminSendPrivate(@Body SendPrivateMsgRequest request);

    @POST("admin/chat/read/{conversationId}")
    Call<ApiResponse<Void>> adminMarkPrivateRead(@Path("conversationId") long conversationId);


}
