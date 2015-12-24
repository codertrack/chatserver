package com.orange.channel;

import com.orange.buf.ChannelBuffer;
import com.orange.conn.UserChannel;
import com.orange.protocol.FrameType;
import com.orange.protocol.ProtocolFrame;
import com.orange.utils.ByteReader;
import com.orange.utils.ReadOff;

/**
 * Created by wukai on 15/12/8.
 *
 * //工作线程
 */
public class UpStreamRunnable implements Runnable {

    private ChannelHandlerLooper mLooper;
    private UserChannel mUserChannel;

    public UpStreamRunnable(ChannelHandlerLooper looper, UserChannel channel){
        mLooper = looper;
        mUserChannel = channel;
    }

    @Override
    public void run() {
        handler();
    }

     public void handler() {

            ReadOff ro = new ReadOff(0);
            ChannelBuffer buffer = null;

                    //获取buffer对象
                    buffer = mUserChannel.getChanelBuffer();
                    //从buffer中读取一个帧头进行嗅探
                    byte[] frameHead = buffer.readData(ProtocolFrame.FRAME_HEAD_LEN);
                    while (frameHead != null) {

                        ProtocolFrame pf = new ProtocolFrame();
                        pf.version = ByteReader.readUint8(frameHead, ro);
                        pf.frameType = ByteReader.readUint8(frameHead, ro);
                        pf.sequnce = ByteReader.readUint32(frameHead, ro);
                        pf.conLen = ByteReader.readUint32(frameHead, ro);
                        int crc = ByteReader.readUint8(frameHead, ro);

                        System.out.println("receiced frame:" +
                                "version->" +pf.version+
                                "/sequnce->"+pf.sequnce+
                                "/frametype->" +pf.frameType+
                                "/conLen->" +pf.conLen+
                                "from crc->"+crc

                        );

                        System.out.println("get crc->"+ProtocolFrame.getCheckCrc(frameHead));
                        //重置偏移量标记
                        ro.setValue(0);
                        if (crc != (ProtocolFrame.getCheckCrc(frameHead)&0xff)) {
                            System.out.println("校验位错误");
                            //移动帧头长度
                            buffer.moveReadPostion(ProtocolFrame.FRAME_HEAD_LEN);

                        } else if (pf.frameType == FrameType.ACK) {
                            //移动帧头长度
                            System.out.println("server-接收到确认帧 sequnce->"+pf.sequnce);
                            buffer.moveReadPostion(ProtocolFrame.FRAME_HEAD_LEN);
                            //帧序递增1
                            mUserChannel.sequnce++;
                            //可以下发数据了
                            mUserChannel.setSendFlag(true);
                            //下发数据
                            mLooper.addDownStreamTask(new DownStreamRuannable(mUserChannel));

                        }else if (pf.frameType == FrameType.USER_MSG){


                            //读取内容长度
                            int available = buffer.getAvailableReadLen();
                            //如果内容字节不够则不处理指针不移动
                            if (available<pf.conLen+ProtocolFrame.FRAME_HEAD_LEN)return;
                            //移动读指针
                            buffer.moveReadPostion(ProtocolFrame.FRAME_HEAD_LEN);

                            byte[] content = buffer.readData(pf.conLen);
                            // 取出内容之后把读指针向后移动内容长度个字节数
                            buffer.moveReadPostion(pf.conLen);

                            //当接收到用户消息的时候发送确认帧
                            mUserChannel.sendAckFrame(pf.sequnce);

                            ChannelMessage cm =ChannelMessage.Bytes2Message(content);
                            cm.userChannel = mUserChannel;
                            //放入消息队列
                            mLooper.addMessageRunnable(new MessageRunnabable(cm));

                        }else {
                            System.out.println("fuck error....");
                        }


                        frameHead = buffer.readData(ProtocolFrame.FRAME_HEAD_LEN);
                    }

            }


    }



