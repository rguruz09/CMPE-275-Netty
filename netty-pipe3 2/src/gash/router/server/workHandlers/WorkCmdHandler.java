package gash.router.server.workHandlers;

import gash.router.server.ServerState;
import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by Student on 4/3/16.
 */
public class WorkCmdHandler implements Command {
    private WorkCmdMsg workCmdMsg;
    private ServerState state;

    public WorkCmdHandler(WorkCmdMsg workCmdMsg, ServerState state) {
        this.workCmdMsg = workCmdMsg;
    }

    @Override
    public void handleMessage(Work.WorkMessage msg, Channel channel) {
        workCmdMsg.handleWorkCmdMsg(msg, state);
    }
}
