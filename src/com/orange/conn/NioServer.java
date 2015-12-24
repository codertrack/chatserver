package com.orange.conn;

import com.orange.channel.ChannelHandlerLooper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

/**
 * Created by wukai on 15/12/7.
 * 服务器启动辅助类
 */

public class NioServer {

    private ServerSocketChannel mServerChannel;
    //服务监听端口号
    private final int SERVER_PORT = 9999;

    //最大在线用户数量
    private final int MAX_USER = 10000;

    //选择器
    public Selector selector;

    //数据处理循环
    public ChannelHandlerLooper channelLooper;

    //数据分发
    public NioDispatcher connDispatcher;

    //链接管理
    public volatile HashMap<SocketChannel,UserChannel> connections;

    //在线用户管理
    public   final UserManager userManager;


    public NioServer(){
        channelLooper = new ChannelHandlerLooper();
        userManager = new UserManager(MAX_USER);
        connections = new HashMap<>();
    }

    public void startServer(){
        try {

            //打开socket监听
            mServerChannel = ServerSocketChannel.open();
            //绑定地址
            mServerChannel.bind(new InetSocketAddress(SERVER_PORT));
            //地址复用
            mServerChannel.socket().setReuseAddress(true);
            //设置非阻塞
            mServerChannel.configureBlocking(false);
            //创建selector对象
            selector = Selector.open();
            //注册用户链接成功事件
            mServerChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务套接字创建成功");

            connDispatcher = new NioDispatcher(this);
            connDispatcher.start();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("server启动异常..."+e.getMessage());
        }
    }

}
