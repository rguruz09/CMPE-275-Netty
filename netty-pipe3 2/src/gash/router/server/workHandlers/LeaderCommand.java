package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 3/31/16.
 */
public class LeaderCommand implements Command{

    private LeaderMsg leaderMsg;

    public LeaderCommand(LeaderMsg msg) {
        leaderMsg = msg;
    }

    @Override
    public void handleMessage(Work.WorkMessage msg, Channel channel) {
        leaderMsg.handleLeaderMsg(msg, channel);
    }

}
