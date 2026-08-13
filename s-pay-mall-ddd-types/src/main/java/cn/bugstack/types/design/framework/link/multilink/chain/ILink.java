package cn.bugstack.types.design.framework.link.multilink.chain;

/**
 * 链接口
 * @param <E>
 */
public interface ILink<E> {

    boolean add(E e);

    boolean addFirst(E e);

    boolean addLast(E e);

    boolean remove(Object o);

    E get(int index);

    void printLinkList();

}