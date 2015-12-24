package com.orange.channel;

import com.orange.conn.UserChannel;
import com.orange.utils.ByteReader;
import com.orange.utils.ByteWriter;
import com.orange.utils.ReadOff;

import java.io.UnsupportedEncodingException;

/**
 * Created by wukai on 15/12/9.
 * stack ---
 *
 * port 1byte--cmd 1byte--time 4byte--conLen 4byte--content comBytes
 * 1+1+4+4+
 *
 */

public  class ChannelMessage {

    public String text;
    public int cmd;

    public UserChannel userChannel;

    public static byte[] Message2Bytes(ChannelMessage message){
        byte[] msg  = null;
        int off = 0;
        try {
            msg = new byte[1+message.text.getBytes("utf-8").length];
            //写入消息头
            off = ByteWriter.writeUint8(msg, off, message.cmd);
            off = ByteWriter.writeBytes(msg, off, message.text.getBytes("utf-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msg;
    }


    public static ChannelMessage Bytes2Message(byte[] data) {
        ReadOff ro = new ReadOff(0);

        ChannelMessage cm = new ChannelMessage();
        //读取命令
        cm.cmd = ByteReader.readUint8(data, ro);
        //读取命令携带的信息
        byte[] wrap = ByteReader.readBytes(data, ro,data.length-1);

        try {
            cm.text = new String(wrap,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return cm;

    }


}
