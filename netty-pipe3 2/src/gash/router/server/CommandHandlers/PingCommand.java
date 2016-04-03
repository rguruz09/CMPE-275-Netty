package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by Student on 4/3/16.
 */
public class PingCommand implements Command{

    private PingMsg pingMsg;

    public PingCommand(PingMsg pingMsg){
        this.pingMsg = pingMsg;
    }

    @Override
    public void handleRequest(Pipe.CommandMessage msg, Channel channel) {
        pingMsg.handlePingMsg(msg, channel);
    }
}
