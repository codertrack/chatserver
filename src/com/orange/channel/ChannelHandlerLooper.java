package com.orange.channel;

import com.orange.conn.UserChannel;

import java.util.concurrent.*;

/**
 * Created by wukai on 15/12/8.
 *
 */
public class ChannelHandlerLooper {

    private ExecutorService mDownService;

    private ExecutorService mUpStreamService;

    private ExecutorService mMessageService;

    public ChannelHandlerLooper(){
        mDownService = Executors.newCachedThreadPool();
        mUpStreamService = Executors.newSingleThreadExecutor();
        mMessageService = Executors.newCachedThreadPool();
    }

    public void addUpStreamChannel(Runnable channel){
            mUpStreamService.execute(channel);
    }

    public void addDownStreamTask(Runnable runnable){
        mDownService.execute(runnable);
    }

    public void addMessageRunnable(Runnable runnable){
        mMessageService.execute(runnable);
    }


}
