package com.buaa01.illumineer_backend.im.handler;

import com.alibaba.fastjson2.JSONObject;
import com.buaa01.illumineer_backend.entity.IMResponse;
import com.buaa01.illumineer_backend.entity.Notice;
import com.buaa01.illumineer_backend.im.IMServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class NoticeHandler extends WebSocketHandler{
    public static void NoticeHandle(ChannelHandlerContext ctx, TextWebSocketFrame tx){
        try{
            Notice notice = JSONObject.parseObject(tx.text(), Notice.class);
            sendNotice(notice);
        }catch (Exception e) {
            log.error("发送消息通知出错：" + e);
            ctx.channel().writeAndFlush(IMResponse.error("发送消息通知出错，请检查NoticeHandle"));
        }
    }

    public static void sendNotice(Notice notice) {
        Map<String,Notice> map = new HashMap<>();
        map.put("通知", notice);
        System.out.println("NoticeHandler: "+IMServer.userChannel);
        // 发给对方的全部channel
        Set<Channel> to = IMServer.userChannel.get(notice.getUid());
        if (to != null) {
            for (Channel channel : to) {
                channel.writeAndFlush(IMResponse.message("notice", map));
            }
        }
    }
}