package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class PingCommand implements Command {
    private PingMsg pingMsg;

    public PingCommand(PingMsg pingMsg) {
        this.pingMsg = pingMsg;
    }

    @Override
    public void handleMessage(Work.WorkMessage msg, Channel channel) {

    }
}
