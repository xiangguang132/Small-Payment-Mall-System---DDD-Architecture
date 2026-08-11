package cn.bugstack.types.design.framework.tree;

/**
 * 策略处理器
 */
public interface StrategyHandler<T, D, R> {

    // 默认策略
    StrategyHandler DEFAULT = (T, D) -> null;

    // 执行方法，请求参数 与 动态上下文
    R apply(T requestParameter, D dynamicContext) throws Exception;
}
