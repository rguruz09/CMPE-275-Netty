package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 4/1/16.
 */
public class VoteCommand implements Command {
    public VoteMsg voteMsg;

    public VoteCommand(VoteMsg msg) {
        voteMsg = msg;
    }
 @Override
 public void handleMessage(Work.WorkMessage msg, Channel channel) {
     voteMsg.handleVoteMsg(msg, channel);
 }

}
