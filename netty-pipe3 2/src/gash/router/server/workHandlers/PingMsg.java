package gash.router.server.workHandlers;

import gash.router.server.ServerState;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class PingMsg {

    protected static Logger logger = LoggerFactory.getLogger("work");
    private ServerState state;

    public PingMsg(ServerState state) {
        this.state = state;
    }

    public void handlePingMsg(Work.WorkMessage msg, Channel channel){

        logger.info("ping from " + msg.getHeader().getNodeId());
        boolean p = msg.getPing();
        Work.WorkMessage.Builder rb = Work.WorkMessage.newBuilder();
        rb.setPing(true);
        channel.write(rb.build());

    }
}
