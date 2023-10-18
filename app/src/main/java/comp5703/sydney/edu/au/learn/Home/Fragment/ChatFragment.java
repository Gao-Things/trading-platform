package comp5703.sydney.edu.au.learn.Home.Fragment;

import static android.content.ContentValues.TAG;
import static comp5703.sydney.edu.au.learn.DTO.Message.MessageType.RECEIVED;
import static comp5703.sydney.edu.au.learn.DTO.Message.MessageType.SENT;
import static comp5703.sydney.edu.au.learn.util.NetworkUtils.imageURL;
import static comp5703.sydney.edu.au.learn.util.NetworkUtils.websocketUrl;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import comp5703.sydney.edu.au.learn.DTO.Message;
import comp5703.sydney.edu.au.learn.DTO.MessageHistory;
import comp5703.sydney.edu.au.learn.DTO.User;
import comp5703.sydney.edu.au.learn.Home.Adapter.ChatAdapter;
import comp5703.sydney.edu.au.learn.R;
import comp5703.sydney.edu.au.learn.VO.ReceivedMessage;
import comp5703.sydney.edu.au.learn.VO.SendMessage;
import comp5703.sydney.edu.au.learn.VO.userAndRemoteUserIdVO;
import comp5703.sydney.edu.au.learn.VO.userIdVO;
import comp5703.sydney.edu.au.learn.util.NetworkUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;

    private ImageButton sentBtn;
    private EditText sendContent;

    private final OkHttpClient client = new OkHttpClient();
    private WebSocket webSocket;

    private Integer userId;

    private Integer receiverId;

    private String token;

    private ImageView remoteUserAvatar;
    private TextView remoteUserName;

    private String userAvatarUrl;
    private String remoteUserAvatarUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_view, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 隐藏header
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).hide();

        // 在 ItemDetailFragment 中获取传递的整数值
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getInt("userId");
            receiverId = args.getInt("receiverId");
            token = args.getString("token");
            initWebSocket(userId);
        }


        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        sentBtn = view.findViewById(R.id.sentBtn);
        sendContent = view.findViewById(R.id.myEditText);

        sendContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    sendContent.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (chatAdapter.getItemCount() >1){
                                chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                            }
                        }
                    }, 100);
                }
            }
        });





        remoteUserAvatar = view.findViewById(R.id.userAvatar);
        remoteUserName = view.findViewById(R.id.userName);

        // 创建并设置RecyclerView的LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(layoutManager);


        // 创建并设置RecyclerView的Adapter
        chatAdapter = new ChatAdapter(getContext(),new ArrayList<Message>());
        chatRecyclerView.setAdapter(chatAdapter);


        sentBtn.setOnClickListener(this::sendMessage);

        List<Message> chatData = new ArrayList<>();

        chatAdapter.setMessages(chatData);

        getChatHistoryData();

        getUserInfoById(userId, true);
        getUserInfoById(receiverId, false);

        chatAdapter.notifyDataSetChanged();


    }

    // 发送请求获取用户头像和姓名
    private void getUserInfoById(Integer userId, boolean isLocalUser){
        userIdVO userIdVO = new userIdVO();
        userIdVO.setUserId(userId);
        NetworkUtils.getWithParamsRequest( userIdVO, "/normal/message/getUserInfoById",token, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse2(response, isLocalUser);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                handleFailure(e);
            }
        });
    }

    private void handleResponse2(Response response, boolean isLocalUser) throws IOException {

        String responseBody = response.body().string();
        JSONObject jsonObject = JSONObject.parseObject(responseBody);
        int code = jsonObject.getIntValue("code");

        // 提取 "records" 数组并转换为List
        JSONObject userJson = jsonObject.getJSONObject("data");

        User user = userJson.toJavaObject(User.class);


        if (code == 200) {

            // 通知adapter数据更新
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (isLocalUser){
                        // 更新本地user的头像和姓名
                        userAvatarUrl = user.getAvatarUrl();
                    }else {
                        // 设置远程用户的头像,姓名
                        remoteUserAvatarUrl = user.getAvatarUrl();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                remoteUserName.setText(user.getName());
                                Picasso.get()
                                        .load(imageURL + remoteUserAvatarUrl)
                                        .error(R.drawable.img_5)  // error_image为加载失败时显示的图片
                                        .into(remoteUserAvatar);
                            }
                        });
                    }



                }
            });


        } else {
            Log.d(TAG, "errowwwwwww");
        }
    }


    // 发送请求获取聊天记录
    private void getChatHistoryData() {
        userAndRemoteUserIdVO userAndRemoteUserIdVO = new userAndRemoteUserIdVO();
        userAndRemoteUserIdVO.setUserId(userId);
        userAndRemoteUserIdVO.setRemoteUserId(receiverId);
        NetworkUtils.getWithParamsRequest( userAndRemoteUserIdVO, "/normal/message/getMessageListByUserIdAndRemoteUserId",token, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(response);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                handleFailure(e);
            }
        });
    }

    private void handleFailure(IOException e) {
        System.out.println(e);
    }


    private void handleResponse(Response response) throws IOException {

        String responseBody = response.body().string();
        JSONObject jsonObject = JSONObject.parseObject(responseBody);
        int code = jsonObject.getIntValue("code");

        if (code == 200) {
            // 提取 "records" 数组并转换为List
            JSONArray messageHistoryList = jsonObject.getJSONArray("data");

            List<MessageHistory> recordsListUse = messageHistoryList.toJavaList(MessageHistory.class);

            List<Message> messageList = new ArrayList<>();


            for (MessageHistory messageHistory : recordsListUse){
                // 如果当前数据本地用户是发信人的话，这条消息就渲染为sent,否则就渲染为RECEIVED
                Message message;
                if (messageHistory.getFromUserId() == userId){


                    message = new Message(messageHistory.getPostMessageContent(), messageHistory.getFromUserAvatar(), SENT);
                }else {

                    message = new Message(messageHistory.getPostMessageContent(), messageHistory.getFromUserAvatar(), RECEIVED);
                }
                messageList.add(message);

            }

            // 通知adapter数据更新
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // 更新Adapter的数据
                    chatAdapter.setMessages(messageList);
                    // 在UI线程上更新Adapter的数据
                    chatAdapter.notifyDataSetChanged();

                    if (messageList.size() >= 5){
                        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    }

                }
            });


        } else {
            Log.d(TAG, "errowwwwwww");
        }
    }

    private void sendMessage(View view) {


        String sentString = sendContent.getText().toString();
        SendMessage sendMessage = new SendMessage();

        // 设置发送目标的ID
        sendMessage.setTo(receiverId);
        sendMessage.setText(sentString);

        String jsonString = JSON.toJSONString(sendMessage);

        // 发送消息到服务器
        webSocket.send(jsonString);

        sendContent.setText("");
        // set send message
        Message newSentMessage = new Message(sentString,userAvatarUrl , SENT);
        chatAdapter.addMessage(newSentMessage);
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1); // 滚动到最新的消息


    }


    private void initWebSocket(Integer userId) {
        Request request = new Request.Builder()
                .url(websocketUrl + "/imserver/" + userId)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                // WebSocket connection opened
                Log.d("Websocket message", "websocket连接已打开");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                // Message received from server
                Log.d("Websocket message", "服务器发送的消息：" + text);

                ReceivedMessage receivedMessage = JSON.parseObject(text, ReceivedMessage.class);
                // set received message
                Message newSentMessage = new Message(receivedMessage.getText(),remoteUserAvatarUrl, Message.MessageType.RECEIVED);


                // 通知adapter数据更新
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 更新Adapter的数据
                        chatAdapter.addMessage(newSentMessage);
                        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1); // 滚动到最新的消息

                        try {
                            // 获取系统默认的通知音频的 URI
                            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), notificationSound);
                            ringtone.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                });


            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                // Handle connection failures
                Log.e("Websocket onFailure", "Connection failed: " + t.getMessage());
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                // The remote peer initiated a graceful shutdown
                Log.d("Websocket onClosing", "Remote peer initiated close with code: " + code);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                // WebSocket has been fully closed
                Log.d("Websocket onClosed", "WebSocket closed with code: " + code);
            }
        });
    }

}
