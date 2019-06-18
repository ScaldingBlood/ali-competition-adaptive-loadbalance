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
import org.apache.dubbo.rpc.listener.CallbackListener;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author daofeng.xjf
 *
 * 服务端过滤器
 * 可选接口
 * 用户可以在服务端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {
    private String quota = System.getProperty("quota");

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{
            Result result = invoker.invoke(invocation);
            return result;
        }catch (Exception e){
            throw e;
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
//        long duration = System.currentTimeMillis();
        if(Access.listener != null)
            Access.listener.receiveServerMsg(quota);
//        System.out.println(System.currentTimeMillis() - duration);
        return result;
    }

}
