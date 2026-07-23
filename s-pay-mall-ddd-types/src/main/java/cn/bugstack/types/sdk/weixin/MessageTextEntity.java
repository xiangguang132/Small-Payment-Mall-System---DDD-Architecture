package cn.bugstack.types.sdk.weixin;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {数据载体}
 * 类似与VO DTO，专门用于接收和解释服务器与微信来回传递的数据的消息载体
 */

/**
 * XStream 框架
 * @XStreamAlias 的作用是建立 Java 和 XML 之间的映射
 * 在默认情况下，直接使用XStreamAlias注解，不添加值的话，默认使用Java的名称直接映射
 * 因此可以将该注解理解为 java->xml 映射时为了符合 xml 的语法要求进行重命名的别名
 */
@XStreamAlias("xml")
public class MessageTextEntity {

    @XStreamAlias("ToUserName")
    private String toUserName;

    @XStreamAlias("FromUserName")
    private String fromUserName;

    @XStreamAlias("CreateTime")
    private String createTime;

    @XStreamAlias("MsgType")
    private String msgType;

    @XStreamAlias("Event")
    private String event;

    @XStreamAlias("EventKey")
    private String eventKey;

    @XStreamAlias("MsgId")
    private String msgId;

    @XStreamAlias("MsgID")
    private String msgID;

    @XStreamAlias("Status")
    private String status;

    @XStreamAlias("Ticket")
    private String ticket;

    @XStreamAlias("Content")
    private String content;

    // Getters and Setters
    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
