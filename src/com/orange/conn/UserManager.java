package com.orange.conn;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by wukai on 15/12/7.
 * 线上用户管理
 */
public class UserManager {

    //管理所有的用户链接
    private LinkedHashMap<String,UserChannel> mAllUser;

    protected UserManager(int max){
        mAllUser = new LinkedHashMap<>(max);
    }

    //移除用户
    public synchronized  void removeOnLineUser(String address){
        mAllUser.remove(address);

    }

    public synchronized List<String> getAllUsers(){
        List<String> users = new ArrayList<>();
        for (Iterator<String> iter = mAllUser.keySet().iterator();iter.hasNext();){
            users.add(iter.next());
        }
        return users;
    }
    //添加用户
    public synchronized void addOnlineUSer(String userid,UserChannel channel){

        if (mAllUser.containsValue(userid)){
            mAllUser.remove(userid);
        }
        mAllUser.put(userid,channel);
    }

    //查找在线用户
    public synchronized UserChannel  findChannelByUserid(String userid){
       return mAllUser.get(userid);
    }


    //获取
    public synchronized List<UserChannel> getAllUserChannel(){
        List<UserChannel> channels = new ArrayList<>();
        Iterator<UserChannel> iterator = mAllUser.values().iterator();
        for (;iterator.hasNext();){
            channels.add(iterator.next());
        }
        return channels;
    }


}
