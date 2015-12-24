package com.orange.channel;

import com.orange.conn.UserChannel;

import java.util.concurrent.BlockingQueue;

/**
 * Created by wukai on 15/12/9.
 *
 * 发送数据流处理
 * 作用:当在处理循环的时候发现需要下发数据则把当前userchannel的数据进行下发
 */
public class DownStreamRuannable implements Runnable {

    private UserChannel mUserChannel;

    private boolean flag = true;

    public DownStreamRuannable(UserChannel channel){
        mUserChannel = channel;

    }

    @Override
    public void run() {
        System.out.println("下发消息");
        mUserChannel.sendNextFrame();
    }

}
