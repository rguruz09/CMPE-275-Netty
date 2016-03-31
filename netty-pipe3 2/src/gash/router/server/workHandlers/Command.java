package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public interface Command {
    public abstract void handleMessage(Work.WorkMessage msg, Channel channel);
}
