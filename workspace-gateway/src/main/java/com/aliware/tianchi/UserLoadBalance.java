package com.aliware.tianchi;

import com.aliware.tianchi.remote.Access;
import com.aliware.tianchi.remote.InvokerQueue;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcStatus;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {
    private InvokerQueue queue = new InvokerQueue();

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if("addListener".equals(invocation.getMethodName()))
            return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        String target = queue.acquire();
        for(Invoker<T> invoker : invokers) {
            if (target.equals(invoker.getUrl().getHost().substring(9))) {
                return invoker;
            }
        }
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
