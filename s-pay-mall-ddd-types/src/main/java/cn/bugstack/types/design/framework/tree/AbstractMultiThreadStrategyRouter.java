package cn.bugstack.types.design.framework.tree;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @param <T> 每个节点的入参
 * @param <D> 节点直接共享的上下文
 * @param <R> 返回值
 */
public abstract class AbstractMultiThreadStrategyRouter<T, D, R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R> {

    @Getter
    @Setter
    protected StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT;

    public R router(T requestParameter, D dynamicContext) throws Exception {
        // 执行路由信息需要先获取信息 -> 该信息是下一个要执行的策略
        StrategyHandler<T, D, R> strategyHandler = get(requestParameter, dynamicContext);
        // 判断获取到的数据是否为空
        // 如果不为空就将数据
        if (strategyHandler != null ) {
            return strategyHandler.apply(requestParameter, dynamicContext);
        }
        // 如果为空，就使用 默认策略执行器
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }

    /**
     * 原本的通用模板，并不考虑多线程异步获取数据的情况，因此这里需要重写一个方法，使用多线程异步地获取数据
     * @param requestParameter
     * @param dynamicContext
     * @return
     * @throws Exception
     */
    @Override
    public R apply(T requestParameter, D dynamicContext) throws  Exception {
        multiThread(requestParameter, dynamicContext);
        return doApply(requestParameter, dynamicContext);
    }

    protected abstract R doApply(T requestParameter, D dynamicContext) throws Exception ;

    protected abstract void multiThread(T requestParameter, D dynamicContext) throws ExecutionException, InterruptedException, TimeoutException;

}
