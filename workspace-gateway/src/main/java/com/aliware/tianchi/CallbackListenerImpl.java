package com.aliware.tianchi;

import com.aliware.tianchi.remote.Access;
import com.aliware.tianchi.remote.InvokerQueue;
import com.aliware.tianchi.remote.Status;
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.Queue;
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
    private AtomicInteger cnt = new AtomicInteger(0);
    private volatile boolean flag = false;

    static {
        Access.queue = new InvokerQueue();
    }

    @Override
    public void receiveServerMsg(String msg) {
        String[] strs = msg.split(" ");
        if(!flag) {
            Status status = new Status(Access.queue, strs[0]);
            status.init(Integer.valueOf(strs[1]));
            Access.providerMap.put(strs[0], status);
            if(Access.providerMap.size() == 3) {
                flag = true;
                Access.queue.init();
                Access.isReady = true;
            }
        } else{
            if ("out".equals(strs[1]))
                Access.providerMap.get(strs[0]).decreaseSize();
            else
                Access.providerMap.get(strs[0]).release(Double.valueOf(strs[1]));
        }
    }
}
