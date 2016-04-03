package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by Student on 4/3/16.
 */
public class QueryCommand implements Command{

    private QueryMsg queryMsg;

    public QueryCommand(QueryMsg queryMsg) {
        this.queryMsg = queryMsg;
    }

    @Override
    public void handleRequest(Pipe.CommandMessage msg, Channel channel) {
        queryMsg.handleQuertMsg(msg,channel);
    }
}
