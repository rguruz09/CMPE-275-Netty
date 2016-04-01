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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import pipe.common.Common.Failure;
import pipe.work.Work.WorkMessage;

/**
 * The message handler processes json messages that are delimited by a 'newline'
 * 
 * TODO replace println with logging!
 * 
 * @author gash
 * 
 */
public class WorkHandler extends SimpleChannelInboundHandler<WorkMessage> {
	protected static Logger logger = LoggerFactory.getLogger("work");
	protected ServerState state;
	protected boolean debug = false;

	public WorkHandler(ServerState state) {
		if (state != null) {
			this.state = state;
		}
	}

	/**
	 * override this method to provide processing behavior. T
	 * 
	 * @param msg
	 */
	public void handleMessage(WorkMessage msg, Channel channel) {
		if (msg == null) {
			// TODO add logging
			System.out.println("ERROR: Unexpected content - " + msg);
			return;
		}

		if (debug)
			PrintUtil.printWork(msg);

		try {
			System.out.println("Try: Handling the client message");
			if (msg.hasBeat()) {
				HeartBeatMsg heartBeatMsg = new HeartBeatMsg(state);
				HeartBeatCommand heartBeatCommand = new HeartBeatCommand(heartBeatMsg);
				heartBeatCommand.handleMessage(msg,channel);
			} else if (msg.hasPing()) {
				PingMsg pingMsg = new PingMsg(state);
				PingCommand pingCommand = new PingCommand(pingMsg);
				pingCommand.handleMessage(msg,channel);
			} else if (msg.hasErr()) {
				ErrorMsg errorMsg = new ErrorMsg(state);
				ErrorCommand errorCommand = new ErrorCommand(errorMsg);
				errorCommand.handleMessage(msg,channel);
			} else if (msg.hasTask()) {
				TaskMsg taskMsg = new TaskMsg(state);
				TaskCommand taskCommand = new TaskCommand(taskMsg);
				taskCommand.handleMessage(msg,channel);
			} else if (msg.hasState()) {
				StateMsg stateMsg = new StateMsg(state);
				StateCommand stateCommand = new StateCommand(stateMsg);
				stateCommand.handleMessage(msg,channel);
			} else if(msg.hasLeader()){
				System.out.println("Leader Query Request");
				LeaderMsg leaderMsg = new LeaderMsg(state);
				LeaderCommand leaderCommand = new LeaderCommand(leaderMsg);
				leaderCommand.handleMessage(msg, channel);
			} else if (msg.hasVote()){
				System.out.println("Its a Vote message");
				VoteMsg voteMsg = new VoteMsg(state);
				VoteCommand voteCommand = new VoteCommand(voteMsg);
				voteCommand.handleMessage(msg,channel);
			}
			else {
				System.out.println("Else part");
			}
		} catch (Exception e) {
			// TODO add logging
			Failure.Builder eb = Failure.newBuilder();
			eb.setId(state.getConf().getNodeId());
			eb.setRefId(msg.getHeader().getNodeId());
			eb.setMessage(e.getMessage());
			WorkMessage.Builder rb = WorkMessage.newBuilder(msg);
			rb.setErr(eb);
			channel.write(rb.build());
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
	protected void channelRead0(ChannelHandlerContext ctx, WorkMessage msg) throws Exception {
		System.out.println("Vinay: ChannelRead0 Handling the client message");
		handleMessage(msg, ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

}