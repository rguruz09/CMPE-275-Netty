/*
 * copyright 2016, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.client;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import routing.Pipe.CommandMessage;
import storage.Storage;

/**
 * A client-side netty pipeline send/receive.
 * 
 * Note a management client is (and should only be) a trusted, private client.
 * This is not intended for public use.
 * 
 * @author gash
 * 
 */
public class CommHandler extends SimpleChannelInboundHandler<CommandMessage> {
	protected static Logger logger = LoggerFactory.getLogger("connect");
	protected ConcurrentMap<String, CommListener> listeners = new ConcurrentHashMap<String, CommListener>();
	//private volatile Channel channel;
	private HashMap<String, ClientHandler> handlers ;
	private String clientID;

	public CommHandler() {
		handlers = new HashMap<String, ClientHandler>();
	}

	/**
	 * Notification registration. Classes/Applications receiving information
	 * will register their interest in receiving content.
	 * 
	 * Note: Notification is serial, FIFO like. If multiple listeners are
	 * present, the data (message) is passed to the listener as a mutable
	 * object.
	 * 
	 * @param listener
	 */
	public void addListener(CommListener listener) {
		if (listener == null)
			return;

		listeners.putIfAbsent(listener.getListenerID(), listener);
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
		System.out.println("--> got incoming message");

		for (String id : listeners.keySet()) {
			CommListener cl = listeners.get(id);

			// TODO this may need to be delegated to a thread pool to allow
			// async processing of replies
			cl.onMessage(msg);
		}

		handleResponse(ctx.channel(),msg);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
		System.out.println("--> user event: " + evt.toString());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from channel.", cause);
		ctx.close();
	}

	public void handleResponse(Channel channel, CommandMessage msg){

		if (msg == null) {
			// TODO add logging
			System.out.println("ERROR: Unexpected content - " + msg);
			return;
		}

		try {

			if(msg.hasResponse()){

				if(msg.getResponse().getAction() == Storage.Action.GET){

					System.out.println("Response from command server..");

					int seqSize = 0;
					String fn  = "";
					if(msg.getResponse().hasMetaData()){
						seqSize = msg.getResponse().getMetaData().getSeqSize();
						clientID = msg.getResponse().getMetaData().getUid();
						fn = msg.getResponse().getMetaData().getFname();
					}
					if(handlers.containsKey(clientID)){
						handlers.get(clientID).rebuildData(channel,msg);
					}else {
						ClientHandler c = new ClientHandler(seqSize, fn);
						handlers.put(clientID,c);
						c.rebuildData(channel,msg);
					}

				}else {
					System.out.println("Saved to server");
				}
			}
		}catch (Exception e) {
			// TODO add logging
			System.out.println(e.getStackTrace());
		}

	}

}
