package com.aliware.tianchi;

import com.aliware.tianchi.remote.Access;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {
    AtomicInteger cnt = new AtomicInteger();
    volatile boolean flag = true;
    @Override
    public void receiveServerMsg(String msg) {
        if(flag && msg.contains(" ")) {
            String[] strs = msg.split(" ");
            Access.queue.initSize(strs[0], Integer.valueOf(strs[1]));
            if(cnt.getAndIncrement() == 3)
                flag = false;
        } else {
//            long duration = System.currentTimeMillis();
            Access.queue.put(msg);
//            System.out.println(System.currentTimeMillis() - duration);
        }
    }
}
