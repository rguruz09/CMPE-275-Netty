package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;
import routing.Pipe;

import static sun.font.FontManager.logger;

/**
 * Created by Student on 4/3/16.
 */
public class PingMsg {

    public void handlePingMsg(Pipe.CommandMessage msg, Channel channel){
        logger.info("ping from " + msg.getHeader().getNodeId());
    }
}
