package com.aliware.tianchi;

import com.aliware.tianchi.util.Access;
import com.aliware.tianchi.util.MsgCounter;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author daofeng.xjf
 *
 * 服务端过滤器
 * 可选接口
 * 用户可以在服务端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {
    private BlockingQueue<Long> queue = new ArrayBlockingQueue<>(1000);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{
            queue.add(System.currentTimeMillis());
            return invoker.invoke(invocation);
        } catch (Exception e){
            throw e;
        }
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        try {
            Access.msgCounter.add(System.currentTimeMillis() - queue.take());
        } catch (InterruptedException e) {
            System.out.println("out");
            Access.listener.receiveServerMsg(System.getProperty("quota") + "out");
            e.printStackTrace();
        }
        return result;
    }

}
