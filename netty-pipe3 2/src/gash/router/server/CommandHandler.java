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

import gash.router.server.CommandHandlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gash.router.container.RoutingConf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common;
import pipe.common.Common.Failure;
import storage.Storage;
import routing.Pipe.CommandMessage;

/**
 * The message handler processes json messages that are delimited by a 'newline'
 * 
 * TODO replace println with logging!
 * 
 * @author gash
 * 
 */
public class CommandHandler extends SimpleChannelInboundHandler<CommandMessage> {
	protected static Logger logger = LoggerFactory.getLogger("cmd");
	protected RoutingConf conf;

	public CommandHandler(RoutingConf conf) {
		if (conf != null) {
			this.conf = conf;
		}
	}

	/**
	 * override this method to provide processing behavior. This implementation
	 * mimics the routing we see in annotating classes to support a RESTful-like
	 * behavior (e.g., jax-rs).
	 * 
	 * @param msg
	 */
	public void handleMessage(CommandMessage msg, Channel channel) {
		if (msg == null) {
			// TODO add logging
			System.out.println("ERROR: Unexpected content - " + msg);
			return;
		}

		PrintUtil.printCommand(msg);

		try {
			// TODO How can you implement this without if-else statements?
			if (msg.hasPing()) {
				logger.info("ping from " + msg.getHeader().getNodeId());
			} else if (msg.hasMessage()  ) {
				//System.out.println(msg.getMessage());
				logger.info("Received Message from client : "+msg.getMessage());
				//logger.info("image byte is "+String.valueOf(msg.getMessageBytes()));
				System.out.println("Sending reply ......");
				Common.Header.Builder hb = Common.Header.newBuilder();
				hb.setNodeId(12);
				hb.setTime(System.currentTimeMillis());
				hb.setDestination(-1);

				CommandMessage.Builder rb = CommandMessage.newBuilder();
				rb.setHeader(hb);
				rb.setMessage("Message received by server successfully");
				channel.writeAndFlush(rb.build());
			}
			else if(msg.hasQuery()){
				CommandMessage.Builder rb = CommandMessage.newBuilder();
				Common.Header.Builder hb = Common.Header.newBuilder();
				Storage.Response.Builder res = Storage.Response.newBuilder();
				//Storage.Query.Builder qb = Storage.Query.newBuilder();
				System.out.println("action is :  "+ msg.getQuery().getAction());
				if(msg.getQuery().getAction()==Storage.Action.STORE) {
					System.out.println("Server received an image from client....");

					//Establish connection with MONGODB to store the received data qb.getData()
					logger.info(String.valueOf(msg.getQuery().getData()));
					//qb.setAction(Storage.Action.STORE);

					hb.setNodeId(12);
					hb.setTime(System.currentTimeMillis());
					hb.setDestination(-1);

					//CommandMessage.Builder rb = CommandMessage.newBuilder();
					rb.setHeader(hb);
					//rb.setQuery(qb.getData());
					//qb.setData(qb.getData());
					//rb.setQuery(qb);
					rb.setMessage("Image received by server successfully");

					//channel.writeAndFlush(rb.build());
				}else if(msg.getQuery().getAction()==Storage.Action.GET){
					//Establish connection with MONGODB to get the requested data based on client-ID

					System.out.println("inside get");
					//Common.Header.Builder hb = Common.Header.newBuilder();

					hb.setNodeId(12);
					hb.setTime(System.currentTimeMillis());
					hb.setDestination(-1);
					//res.setData(); Load this method with data retreived from MongoDB
					rb.setHeader(hb);
					//rb.setResponse(res);
					rb.setMessage("get from server");

				}
				//Acknowledgement to the client of receipt of Data
				channel.writeAndFlush(rb.build());
			}

		} catch (Exception e) {
			// TODO add logging
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(conf.getNodeId());
			eb.setRefId(msg.getHeader().getNodeId());
			eb.setMessage(e.getMessage());
			CommandMessage.Builder rb = CommandMessage.newBuilder(msg);
			rb.setErr(eb);
			channel.write(rb.build());
		}

		System.out.flush();
	}

	/**
	 * a message was received from the server. Here we dispatch the message to
	 * the client's thread pool to minimize the time it takes to process other
	 * messages.
	 * 
	 * @param ctx
	 *            The channel the message was received from
	 * @param msg
	 *            The message
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, CommandMessage msg) throws Exception {
		System.out.println("Rii..Inside Command handler channel read");
		handleMessage(msg, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

}