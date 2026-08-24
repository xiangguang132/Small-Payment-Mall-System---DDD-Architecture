package cn.bugstack.types.design.framework.link.multilink.handler;

/**
 * 逻辑处理器
 * @param <T>
 * @param <D>
 * @param <R>
 */
public interface ILogicHandler<T, D, R> {

    // 默认走向下一个链点
    default R next(T requestParameter, D dynamicContext) {
        return null;
    }

    R apply(T requestParameter, D dynamicContext) throws Exception;
}
