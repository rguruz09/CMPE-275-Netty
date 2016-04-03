package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by Student on 4/3/16.
 */
public class ResponseCommand implements Command {

    private ResponseMsg responseMsg;

    public ResponseCommand(ResponseMsg responseMsg) {
        this.responseMsg = responseMsg;
    }

    @Override
    public void handleRequest(Pipe.CommandMessage msg, Channel channel) {
        responseMsg.handleResponseMsg(msg,channel);
    }
}
