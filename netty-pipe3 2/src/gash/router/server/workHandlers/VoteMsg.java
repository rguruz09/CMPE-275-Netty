package gash.router.server.workHandlers;

import gash.router.server.ServerState;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;

/**
 * Created by vinay on 4/1/16.
 */
    public class VoteMsg {

    protected static Logger logger = LoggerFactory.getLogger("Vote");
    private ServerState state;

    public VoteMsg(ServerState state) {
        this.state = state;
    }

    public void handleVoteMsg(Work.WorkMessage msg, Channel channel){

        logger.info("Vote msg from " + msg.getHeader().getNodeId());


    }
}
