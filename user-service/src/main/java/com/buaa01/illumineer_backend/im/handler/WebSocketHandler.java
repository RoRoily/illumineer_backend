package com.buaa01.illumineer_backend.im.handler;

import com.alibaba.fastjson2.JSON;
import com.buaa01.illumineer_backend.entity.Command;
import com.buaa01.illumineer_backend.entity.CommandType;
import com.buaa01.illumineer_backend.entity.IMResponse;
import com.buaa01.illumineer_backend.im.IMServer;
import com.buaa01.illumineer_backend.tool.RedisTool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static RedisTool redisTool;
    private static RedisTemplate<String, Object> redisTemplate;
    @Autowired
    public void setDependencies(RedisTool redisTool, RedisTemplate<String, Object> redisTemplate) {
        WebSocketHandler.redisTool = redisTool;
        WebSocketHandler.redisTemplate = redisTemplate;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame tx) {
        // 将接收到的文本帧内容解析为 Command 对象。
        // 当 Netty 接收到一个 WebSocket 文本帧时会调用这个方法进行处理
        try {
            Command command = JSON.parseObject(tx.text(), Command.class);
            // 根据code分发不同处理程序
            switch (CommandType.match(command.getCode())) {
                case CONNETION: // 如果是连接消息就不需要做任何操作了，因为连接上的话在token鉴权那就做了
                    break;
                case NOTICE:
                    //以后可以用来做系统公告
                    Integer user_id = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
                    NoticeHandler.NoticeHandle(ctx,tx);
                    break;
                default: ctx.channel().writeAndFlush(IMResponse.error("不支持的CODE " + command.getCode()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接断开时执行 将channel从集合中移除 如果集合为空则从Map中移除该用户 即离线状态
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 当连接断开时，从 userChannel 中移除对应的 Channel
        Integer uid = (Integer) ctx.channel().attr(AttributeKey.valueOf("userId")).get();
        Set<Channel> userChannels = IMServer.userChannel.get(uid);
        if (userChannels != null) {
            userChannels.remove(ctx.channel());
            // 用户离线操作
            if (IMServer.userChannel.get(uid).isEmpty()) {
                IMServer.userChannel.remove(uid);
                Set<String> channelKeys = redisTemplate.keys("message:" + uid + ":" + "*");
                // 删除匹配的键
                if (channelKeys != null && !channelKeys.isEmpty()) {
                    redisTemplate.delete(channelKeys);
                }
                redisTool.deleteSetMember("login_member", uid);   // 从在线用户集合中移除
            }
        }
        // 继续处理后续逻辑
        ctx.fireChannelInactive();
    }
}