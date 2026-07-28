package cn.bugstack.types.sdk.weixin;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

/**
 * 映射xml与java间的关系
 * 微信返回的是xml，需要转换为Java对象
 * Java对象需要映射为xml给微信
 */
@Data
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
}
