package com.orange.conn;

import com.orange.channel.ChannelMessage;
import com.orange.channel.UpStreamRunnable;
import com.orange.protocol.FrameType;
import com.orange.protocol.ProtocolFrame;
import com.orange.protocol.ProtocolHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wukai on 15/12/9.
 */
public class NioDispatcher extends Thread {

    //最大链接数
    private static final int MAX_CONN = 10000;

    private NioServer nioServer;

    public NioDispatcher(NioServer server){
        nioServer = server;
        setName("server--NioDispatcher");
        //ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss

    }

    @Override
    public void run() {
        while (true){
            dispatchEvent();
        }

    }
    //selector事件分发
    private void dispatchEvent(){
        try {
            int count = nioServer.selector.select(3000);
            if (count <= 0)return;
            SelectionKey key = null;
            ByteBuffer buf = ByteBuffer.allocate(1024);
            Iterator<SelectionKey> iterator = nioServer.selector.selectedKeys().iterator();
            //遍历当前可用的key
            for (;iterator.hasNext();){
                key = iterator.next();
                //是否可以初始化一个链接
                if (key.isAcceptable()){
                    ServerSocketChannel server =(ServerSocketChannel)key.channel() ;
                    //接收一个链接
                    SocketChannel clientChannel = server.accept();

                    if (clientChannel == null)return;

                    //非阻塞模式
                    clientChannel.configureBlocking(false);
                    //用户上线
                    onUserOnline(clientChannel);

                }else if (key.isReadable()){

                    SocketChannel channel = (SocketChannel) key.channel();
                    //System.out.println(channel.socket().isClosed());
                    //接收数据
                    int len = channel.read(buf);
                    //当用户断开链接的时候
                    if (len == -1){
                        //用户下线
                        sendOffLineMessage(channel);
                        channel.shutdownInput();
                    }
                    if (len>0){
                        //将接收到的数据添加到用户通道里面
                        //System.out.println("read len = "+len);
                        byte[] content = new byte[len];
                        //移动读指针到0位置
                        buf.flip();
                        buf.get(content,0,len);
                        writeChannelBuffer(channel,content);
                        buf.clear();
                        channel.register(nioServer.selector,SelectionKey.OP_READ);
                    }
                }
                iterator.remove();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("链接异常");

        }catch (NotYetConnectedException e){
            e.printStackTrace();
        }

    }

    /**
     * 把字节加到用户字节缓冲区中
     * @param channel
     * @param data
     */
    private void writeChannelBuffer(SocketChannel channel,byte[] data){
        if (nioServer.connections.containsKey(channel)){
            //将数据添加到用户通道的缓冲区里面
            UserChannel uch = nioServer.connections.get(channel);
            uch.receivedData(data);
            nioServer.channelLooper.addUpStreamChannel(new UpStreamRunnable(
                    nioServer.channelLooper,
                    uch
            ));
        }
    }

    private void removeConnection(SocketChannel channel){
        if (nioServer.connections.containsKey(channel)){
            String userid = nioServer.connections.get(channel).getChannelId();
            nioServer.userManager.removeOnLineUser(userid);
            nioServer.connections.remove(channel);
        }
    }


    public void sendOffLineMessage(SocketChannel channel){

        UserChannel userChannel = nioServer.connections.get(channel);
        if (userChannel == null)return;

        final String userid = userChannel.getChannelId();

        System.out.println("channelUserid->"+userid);
        if (userid == null)return;
        List<UserChannel> channels =nioServer.userManager.getAllUserChannel();
        for(final UserChannel ch:channels){
            if (ch.getChannelId() != null&&!ch.getChannelId().equals(userid)){
                ProtocolFrame pf = new ProtocolFrame();
                pf.version = 1;
                pf.frameType = FrameType.USER_MSG;
                //丁香花
                ChannelMessage msg = new ChannelMessage();
                msg.cmd = ProtocolHandler.OFFLINE;
                System.out.println("sendtoUserid->"+userid);
                msg.text = userid;
                pf.content = ChannelMessage.Message2Bytes(msg);
                ch.enqueueFrame(pf);
                }
            }
        //从链接管理中删除
        removeConnection(channel);

    }

    public void onUserOnline(SocketChannel clientChannel){

        //注册读取事件
        UserChannel user_channel = null;
        try {
            clientChannel.register(nioServer.selector,SelectionKey.OP_READ);
            user_channel = new UserChannel(nioServer,clientChannel);
            //添加链接管理
            nioServer.connections.put(clientChannel,user_channel);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("一个新链接.建立..."+user_channel);
    }

}
