package com.orange.conn;

import com.orange.buf.ChannelBuffer;
import com.orange.channel.ChannelMessage;
import com.orange.channel.DownStreamRuannable;
import com.orange.protocol.FrameType;
import com.orange.protocol.ProtocolFrame;
import com.orange.protocol.ProtocolHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by wukai on 15/12/7.
 *
 */
public class UserChannel  {
    //读写通道
    private SocketChannel mSocket;
    //用户id
    private String mChannelId;
    //通道buffer
    private ChannelBuffer mBuffer;

    //是否可以下发数据  默认都是第一帧都是可以下发的
    private  boolean sendFlag = true;

    //当前正在发送的消息帧用于重发
    private ProtocolFrame mCurrentFrame;

    //发送消息缓存
    private BlockingQueue<ProtocolFrame> mFrames = null;

    //当前帧序号
    public int sequnce = 0;
    //协议管理器
    private ProtocolHandler mStackHandler;

    public NioServer nioServer;

    public UserChannel(NioServer server,SocketChannel channel){
        nioServer =server;
        mFrames = new LinkedBlockingDeque<>();
        mStackHandler = new ProtocolHandler(this);
        //缓存10k
        mBuffer = new ChannelBuffer(1024*10);
        mSocket = channel;
    }

    /**
     * 往用户缓存写入数据 ,这个方法调用完并
     * @param data
     */
    public void receivedData(byte[] data){
            mBuffer.writeData(data);
    }

    //获取用户通道的缓冲区
    public ChannelBuffer getChanelBuffer(){
        return  mBuffer;
    }

    public void setSendFlag(boolean flag){
        sendFlag = flag;
    }

    public boolean getSendFlag(){
        return sendFlag;
    }

    //发送网络数据
    public void sendNextFrame(){

        try {
                if (mSocket.isConnected() && sendFlag){

                    mCurrentFrame  = mFrames.take();
                    if (mCurrentFrame!=null){
                        mCurrentFrame.sequnce =sequnce;
                        mSocket.write(ByteBuffer.wrap(ProtocolFrame.getFrameBytes(mCurrentFrame)));
                        mSocket.socket().getOutputStream().flush();
                        System.out.println("发送成功...");

                    }
                //发送一帧数据之后等待确认帧,否则不下发下一帧数据,所有 sendFlag标记要置为false
                    sendFlag =false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("发送异常.....");
        }

    }

    /**
     *
     * @param frame
     */
    public void enqueueFrame(ProtocolFrame frame){

            //将数据帧保存在队列中
            mFrames.add(frame);
            nioServer.channelLooper.addDownStreamTask(new DownStreamRuannable(this));
    }

    //设置通道的userid
    public void setChannelId(String channelID){
        mChannelId= channelID;
    }
    //获取channelId
    public String getChannelId(){
        return mChannelId;
    }


    public void dispatchMessage(ChannelMessage message){
        mStackHandler.handleCmd(message.cmd,message.text);
    }

    /**
     * 发送确认帧
     */
    public void sendAckFrame(int seq){
        ProtocolFrame frame = new ProtocolFrame();
        frame.frameType = FrameType.ACK;
        frame.sequnce = seq;
        try {
            mSocket.write(ByteBuffer.wrap(ProtocolFrame.getAckFrameBytes(frame)));
            mSocket.socket().getOutputStream().flush();
            System.out.println("send Ack frame sequnce->"+seq);
        } catch (IOException e) {
            e.printStackTrace();

        }

    }

}
