/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.server;

import gash.router.server.workHandlers.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common.Failure;
import pipe.global.Global;
import routing.Pipe;

/**
 * The message handler processes json messages that are delimited by a 'newline'
 *
 * TODO replace println with logging!
 *
 * @author gash
 *
 */

/*
*  Created by Richa on 4/4/2016
*
 */
public class AdapterHandler extends SimpleChannelInboundHandler<Global.GlobalCommandMessage> {
    protected static Logger logger = LoggerFactory.getLogger("Adapter");
    protected ServerState state;
    protected boolean debug = false;
    public static Channel commandChannel;


    public AdapterHandler(ServerState state) {
        if (state != null) {
            this.state = state;
        }
    }

    public AdapterHandler(){

    }

    /**
     * override this method to provide processing behavior. T
     *
     * @param msg
     */
    public void handleMessage(Global.GlobalCommandMessage msg, Channel channel) {
        if (msg == null) {
            // TODO add logging
            System.out.println("ERROR: Unexpected content - " + msg);
            return;
        }

//        if (debug)
//            PrintUtil.printWork(msg);

        try {
            System.out.println("Try: Handling the Inter-cluster message...");

            if(msg.hasPing()){

                 logger.info("ping received by client");

                Pipe.CommandMessage.Builder wm = Pipe.CommandMessage.newBuilder();
                wm.setHeader(msg.getHeader());
                //wm.setSecret(123);

                wm.setQuery(msg.getQuery());
                wm.setResponse(msg.getResponse());
                wm.setPing(true);
                String host = "localhost";
                int port = 4168;
                Bootstrap b = new Bootstrap();
                b.group(new NioEventLoopGroup());
                b.channel(NioSocketChannel.class);
                b.handler(new CommandInit());
                commandChannel = b.connect(host, port).sync().channel();

                System.out.println("Sending the global ping to command server");
                commandChannel.writeAndFlush(wm.build());
                Global.GlobalCommandMessage.Builder gm = Global.GlobalCommandMessage.newBuilder();
                gm.setHeader(msg.getHeader());
                gm.setMessage("Ping received by command server");
                channel.writeAndFlush(gm.build());

            }else if(msg.hasQuery()){
                    Pipe.CommandMessage.Builder wm = Pipe.CommandMessage.newBuilder();
                    wm.setHeader(msg.getHeader());

                    wm.setQuery(msg.getQuery());
                    wm.setResponse(msg.getResponse());
                    String host = "127.0.0.1";
                    int port = 4003;
                    Bootstrap b = new Bootstrap();
                    b.group(new NioEventLoopGroup());
                    b.channel(NioSocketChannel.class);
                    b.handler(new CommandInit());
                    commandChannel = b.connect(host, port).sync().channel();
                    System.out.println("Sending the global message to command server");
                    commandChannel.writeAndFlush(wm.build());
                    Global.GlobalCommandMessage.Builder gm = Global.GlobalCommandMessage.newBuilder();
                    gm.setHeader(msg.getHeader());
                    gm.setMessage("Query successfully sent to Command server");
                    channel.writeAndFlush(gm.build());
                    //channel.writeAndFlush(wm.build());
            }else if(msg.hasResponse()){
                //Global.GlobalCommandMessage.Builder gm = Global.GlobalCommandMessage.newBuilder();
                //gm.setHeader(msg.getHeader());
                //gm.setResponse(msg.getResponse());
                //Sending the response to the inter-cluster Adapter
                channel.writeAndFlush(msg);
            }
        } catch (Exception e) {
            // TODO add logging
            Failure.Builder eb = Failure.newBuilder();
            eb.setId(state.getConf().getNodeId());
            eb.setRefId(msg.getHeader().getNodeId());
            eb.setMessage(e.getMessage());
            //WorkMessage.Builder rb = WorkMessage.newBuilder(msg);
            //rb.setErr(eb);
            //channel.write(rb.build());
        }

        System.out.flush();

    }


    /**
     * a message was received from the server. Here we dispatch the message to
     the client's thread pool to minimize the time it takes to process other
     * messages.
     *
     * @param ctx
     *            The channel the message was received from
     * @param msg
     *            The message
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Global.GlobalCommandMessage msg) throws Exception {
        System.out.println("Richa: ChannelRead0 Handling the Inter-cluster message");
        handleMessage(msg, ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected exception from downstream.", cause);
        ctx.close();
    }

}