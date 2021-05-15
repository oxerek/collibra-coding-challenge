package com.collibra.codechallenge.server;

import com.collibra.codechallenge.ioc.Configuration;
import com.collibra.codechallenge.protocol.Protocol;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = Logger.getLogger(ChannelHandler.class);

    private static final long SESSION_EXPIRATION_TIME = 30000L;

    private final Protocol protocol;

    private Timer timer;
    private TimerTask timerTask;
    private List<String> unfinishedMessages;
    private String sessionId;
    private long sessionStartTime;

    public ChannelHandler(Map<String, Map<String, List<Integer>>> nodes) {
        this.protocol = Configuration.graphMessagesProtocol(nodes);
        this.unfinishedMessages = Collections.synchronizedList(Lists.newLinkedList());
        this.sessionId = UUID.randomUUID().toString();
        MDC.put("Session-Id", sessionId);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Session created");
        log.info("Channel active");

        sessionStartTime = System.currentTimeMillis();
        scheduleSessionExpirationTask(ctx);
        String helloMessage = protocol.helloMessage(sessionId);
        ctx.writeAndFlush(helloMessage);
        ctx.fireChannelActive();

        log.info("Hello message sent: " + helloMessage);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("Message received: " + msg);

        cancelScheduledSessionExpirationTask();
        scheduleSessionExpirationTask(ctx);

        if(!unfinishedMessages.isEmpty()) {
            String part = unfinishedMessages.stream().collect(Collectors.joining());
            msg = part + msg;
            unfinishedMessages.clear();
        }

        if(!msg.contains(System.lineSeparator())) {
            unfinishedMessages.add(msg);
            return;
        }

        msg = protocol.processMessage(msg, String.valueOf(System.currentTimeMillis() - sessionStartTime));
        ctx.writeAndFlush(msg);

        log.info("Message sent: " + msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Channel inactive");

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause);

        String msg = cause.getMessage();
        if(msg != null) {
            ctx.writeAndFlush(msg);

            log.info("Message sent: " + msg);
        }
    }

    private void scheduleSessionExpirationTask(ChannelHandlerContext ctx) {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                String byeMessage = protocol.byeMessage(String.valueOf(System.currentTimeMillis() - sessionStartTime));
                ctx.writeAndFlush(byeMessage);

                log.info("Bye message sent: " + byeMessage);

                ctx.channel().close();
            }
        };
        timer.schedule(timerTask, SESSION_EXPIRATION_TIME);
    }

    private void cancelScheduledSessionExpirationTask() {
        timer.cancel();
        timerTask.cancel();
    }
}
