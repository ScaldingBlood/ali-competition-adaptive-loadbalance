package com.aliware.tianchi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.aliware.tianchi.remote.Access;
import com.aliware.tianchi.remote.Balancer;

import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {
    private Balancer balancer = new Balancer();
    private ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Override
    public void receiveServerMsg(String msg) {
        threadPool.submit( () -> {
            String[] strs = msg.split(" ");
            if(Access.maxAvailableThreads.containsKey(strs[0])) {
                double duration = Double.valueOf(strs[1]);
//            if("out".equals(strs[1]))
//                Access.providerMap.get(strs[0]).decreaseSize();
//            else
                Access.providerMap.get(strs[0]).release(duration);
                balancer.balance(strs[0], duration);
            }
            else
                Access.maxAvailableThreads.put(strs[0], Integer.valueOf(strs[1]));
        });
    }
}
