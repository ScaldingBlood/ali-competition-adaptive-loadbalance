package com.aliware.tianchi;

import com.aliware.tianchi.util.Access;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.protocol.dubbo.status.ThreadPoolStatusChecker;
import org.apache.dubbo.rpc.service.CallbackService;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author daofeng.xjf
 * <p>
 * 服务端回调服务
 * 可选接口
 * 用户可以基于此服务，实现服务端向客户端动态推送的功能
 */
public class CallbackServiceImpl implements CallbackService {

    public CallbackServiceImpl() {
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (callbackListener != null) {
//                    try {
//                        callbackListener.receiveServerMsg(System.getProperty("quota") + " ");
//                    } catch (Throwable t1) {
//                        t1.printStackTrace();
//                    }
//                }
//            }
//        }, 0, 1000);
    }

//    private Timer timer = new Timer();

    /**
     * key: listener type
     * value: callback listener
     */
//    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<>();

    private ThreadPoolStatusChecker checker = new ThreadPoolStatusChecker();

    @Override
    public void addListener(String key, CallbackListener listener) {
        Access.listener = listener;
        String msg = checker.check().getMessage();
        int batch_size = Integer.valueOf(msg.substring(msg.indexOf("max:")+4, msg.indexOf(", core")));
        Access.msgCounter.init(batch_size);
        System.out.println(msg);
        listener.receiveServerMsg(System.getProperty("quota") + " " + batch_size);
    }
}
