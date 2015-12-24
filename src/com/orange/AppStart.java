package com.orange;


import com.orange.conn.NioServer;

//程序入口
public class AppStart {

    public static void main(String[] args) {

        NioServer serverLoader = new NioServer();
        serverLoader.startServer();
    }

}
