package cn.bugstack.types.design.framework.link.multilink;

import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;

/**
 * 链路装配
 * @param <T>
 * @param <D>
 * @param <R>
 */
public class LinkArmory<T, D, R> {

    private final BusinessLinkedList<T, D, R> logicLink;

    @SafeVarargs
    public LinkArmory(String linkName, ILogicHandler<T, D, R>... logicHandlers) {
        logicLink = new BusinessLinkedList<>(linkName);
        for (ILogicHandler<T, D, R> logicHandler: logicHandlers){
            logicLink.add(logicHandler);
        }
    }

    public BusinessLinkedList<T, D, R> getLogicLink() {
        return logicLink;
    }

}
