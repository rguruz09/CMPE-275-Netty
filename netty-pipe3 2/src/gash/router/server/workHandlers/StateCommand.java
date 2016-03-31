package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 3/30/16.
 */
public class StateCommand implements Command {

    private StateMsg stateMsg;

    public StateCommand(StateMsg stateMsg) {
        this.stateMsg = stateMsg;
    }

    @Override
    public void handleMessage(Work.WorkMessage msg, Channel channel) {
        stateMsg.handleStateMsg(msg,channel);
    }

}
