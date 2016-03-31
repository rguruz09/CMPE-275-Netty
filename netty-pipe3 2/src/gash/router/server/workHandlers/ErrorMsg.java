package gash.router.server.workHandlers;

import gash.router.server.ServerState;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class ErrorMsg {

    protected static Logger logger = LoggerFactory.getLogger("work");
    private ServerState state;

    public ErrorMsg(ServerState state) {
        this.state = state;
    }

    public void handleErrMsg(Work.WorkMessage msg, Channel channel){
        Common.Failure err = msg.getErr();
        logger.error("failure from " + msg.getHeader().getNodeId());
        // PrintUtil.printFailure(err);
    }
}
