package cn.bugstack.types.design.framework.link.multilink.chain;

import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;

/**
 * 业务链
 * @param <T>
 * @param <D>
 * @param <R>
 */
public class BusinessLinkedList<T, D, R> extends LinkedList<ILogicHandler<T, D, R>> implements ILogicHandler<T, D, R> {

    public BusinessLinkedList(String name) {
        super(name);
    }


    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        Node<ILogicHandler<T, D, R>> current = this.first;
        do {
            // 当前节点
            ILogicHandler<T, D, R> item = current.item;
            // 当前节点执行
            R apply = item.apply(requestParameter, dynamicContext);
            if (apply != null) {
                return apply;
            }

            current = current.next;
        } while (current != null);

        return null;
    }
}
