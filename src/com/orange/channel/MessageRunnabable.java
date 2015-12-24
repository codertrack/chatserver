package com.orange.channel;

/**
 * Created by wukai on 15/12/16.
 */
public class MessageRunnabable implements Runnable {

    private ChannelMessage mMessage;

    public MessageRunnabable(ChannelMessage channelMessage){
        mMessage = channelMessage;
    }

    @Override
    public void run() {
        mMessage.userChannel.dispatchMessage(mMessage);
    }




}
