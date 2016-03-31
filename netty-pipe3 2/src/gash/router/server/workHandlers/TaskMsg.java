package gash.router.server.workHandlers;

import gash.router.server.ServerState;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class TaskMsg {

    protected static Logger logger = LoggerFactory.getLogger("work");
    private ServerState state;

    public TaskMsg(ServerState state) {
        this.state = state;
    }

    public void handletaskMsg(Work.WorkMessage msg, Channel channel){

        Work.Task t = msg.getTask();

    }
}
