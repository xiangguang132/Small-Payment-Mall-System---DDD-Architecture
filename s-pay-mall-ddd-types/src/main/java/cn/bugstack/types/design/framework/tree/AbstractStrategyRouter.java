package cn.bugstack.types.design.framework.tree;

import lombok.Getter;
import lombok.Setter;

/**
 * 实现执行器与策略器
 */
public abstract class AbstractStrategyRouter<T, D, R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R> {

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

}
