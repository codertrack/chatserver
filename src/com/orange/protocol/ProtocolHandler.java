package com.orange.protocol;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.orange.channel.ChannelMessage;
import com.orange.conn.UserChannel;

import java.util.Iterator;
import java.util.List;

/**
 * Created by wukai on 15/12/9.
 *
 */
public class ProtocolHandler {

    //通道初始化
    public static final int CMD_INIT = 0x01;
    //消息转发
    public static final int CMD_CHAT = 0x02;
    //同步服务器时间
    public static final int SYNC_TIME = 0x03;

    //离线
    public static final int OFFLINE = 0x04;
    //上线
    public static final int ONLINE = 0x05;


    private UserChannel mChannel;

    public ProtocolHandler(UserChannel channel) {
        mChannel = channel;
    }

    public void handleCmd(int cmd, String text) {
        System.out.println("cmd=>"+cmd+"\n+text->"+text);
        if (cmd == CMD_INIT){ //初始化通道信息
            handleInit(text);

        }else if (cmd == CMD_CHAT){ // 消息转发
            transferMessage(text);
        }
    }

    /**
     * 解析用户id
     * 1byte
     *
     * @param userid
     *
     */
    public void handleInit(String userid){
        mChannel.setChannelId(userid);


        // 得到所有用户
        sendOnlineMessage(userid);

        JSONObject obj = new JSONObject();
        obj.put("msg","ok");
        obj.put("users",getAllUsers());
        ProtocolFrame pf = new ProtocolFrame();
        ChannelMessage cm = new ChannelMessage();
        cm.cmd = CMD_INIT;
        cm.text = obj.toJSONString();
        pf.version =1;
        pf.frameType = FrameType.USER_MSG;
        pf.content =ChannelMessage.Message2Bytes(cm) ;
        mChannel.enqueueFrame(pf);



    }

    /**
     *
     *
     * @param text
     */

    public void transferMessage(String text){

        System.out.println("receive_transfer-->"+text);

        ChatMessage chatMessage = ChatMessage.initWithJsonObject(
                JSONObject.parseObject(text));

        String to_userId = chatMessage.toUserid;

        chatMessage.fromUserid = mChannel.getChannelId();

        ProtocolFrame pf = new ProtocolFrame();
        pf.version = 1;
        //封装Channelmessage
        ChannelMessage cm =  new ChannelMessage();
        cm.cmd = CMD_CHAT;
        cm.text = ChatMessage.parseMessage2JsonObject(chatMessage).toString();
        //封装ProtocolFrame
        pf.content = ChannelMessage.Message2Bytes(cm);
        pf.frameType = FrameType.USER_MSG;
        pf.version = 1;
        UserChannel toUserChannel = mChannel.nioServer.userManager.findChannelByUserid(to_userId);
        if (toUserChannel == null){
            System.out.println("用户->"+to_userId+"不在线");
            return;
        }
        //发送消息
        toUserChannel.enqueueFrame(pf);
    }



     public JSONArray getAllUsers(){
         List<String> users = mChannel.nioServer.userManager.getAllUsers();
         if (users == null || users.size() == 0)return null;
         JSONArray jsonArray  = new JSONArray();
         for (String userid:users){
             JSONObject jsonObject = new JSONObject();
             jsonObject.put("userid",userid);
             jsonArray.add(jsonObject);
         }
         return jsonArray;
     }


    //通知所有用户有新用户上线
    public void sendOnlineMessage(String userid){

        List<UserChannel> channels = mChannel.nioServer.userManager.getAllUserChannel();
        for(final UserChannel ch:channels){
                ProtocolFrame pf = new ProtocolFrame();
                pf.version = 1;
                pf.frameType = FrameType.USER_MSG;
                ChannelMessage msg = new ChannelMessage();
                msg.cmd = ONLINE;
                msg.text = userid;
                pf.content = ChannelMessage.Message2Bytes(msg);
                ch.enqueueFrame(pf);
        }
        mChannel.nioServer.userManager.addOnlineUSer(userid,mChannel);
    }
}
