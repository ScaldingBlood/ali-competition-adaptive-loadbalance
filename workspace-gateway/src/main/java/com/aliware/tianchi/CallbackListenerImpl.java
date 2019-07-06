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
    @Override
    public void receiveServerMsg(String msg) {
        String[] strs = msg.split(" ");
        if(Access.maxAvailableThreads.containsKey(strs[0])) {
            if("out".equals(strs[1]))
                Access.providerMap.get(strs[0]).decreaseSize();
            else
                Access.providerMap.get(strs[0]).release(Double.valueOf(strs[1]));
        }
        else
            Access.maxAvailableThreads.put(strs[0], Integer.valueOf(strs[1]));
    }
}
