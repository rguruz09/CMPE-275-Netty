package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by Student on 4/2/16.
 */
public interface Command {
    public abstract void handleRequest(Pipe.CommandMessage msg, Channel channel);
}
