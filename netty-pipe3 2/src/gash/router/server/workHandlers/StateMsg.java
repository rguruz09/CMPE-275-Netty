package gash.router.server.workHandlers;

import gash.router.server.ServerState;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class StateMsg {

    private ServerState state;
    protected static Logger logger = LoggerFactory.getLogger("work");

    public StateMsg(ServerState state) {
        this.state = state;
    }

    public void handleStateMsg(Work.WorkMessage msg, Channel channel){
        Work.WorkState s = msg.getState();
    }

}
