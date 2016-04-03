package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by Student on 4/3/16.
 */
public class TextCommand implements Command {

    private TextMsg textMsg;

    public TextCommand(TextMsg textMsg) {
        this.textMsg = textMsg;
    }

    @Override
    public void handleRequest(Pipe.CommandMessage msg, Channel channel) {
        textMsg.handleTextMsg(msg,channel);
    }
}
