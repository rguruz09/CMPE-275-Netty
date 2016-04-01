package gash.router.server.workHandlers;

import gash.router.server.ServerState;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;

/**
 * Created by vinay on 3/31/16.
 */
public class LeaderMsg {
    protected static Logger logger = LoggerFactory.getLogger("LeaderMsg");
    private ServerState state;

    public LeaderMsg(ServerState state) {
        this.state = state;
    }


    public void handleLeaderMsg(Work.WorkMessage msg, Channel channel){
        logger.info("Leadermsg from " + msg.getHeader().getNodeId());
        System.out.println("LeaderMsg");

    }
}
