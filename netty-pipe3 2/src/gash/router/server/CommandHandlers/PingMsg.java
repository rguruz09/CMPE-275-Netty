package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.work.Work;
import routing.Pipe;


/**
 * Created by Student on 4/3/16.
 */
public class PingMsg {

    protected static Logger logger = LoggerFactory.getLogger("cmd");

    public void handlePingMsg(Pipe.CommandMessage msg, Channel channel){
        logger.info("ping from " + msg.getHeader().getNodeId());
    }
}
