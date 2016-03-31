package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class TaskCommand implements Command{

    private TaskMsg taskMsg;

    public TaskCommand(TaskMsg taskMsg) {
        this.taskMsg = taskMsg;
    }

    @Override
    public void handleMessage(Work.WorkMessage msg, Channel channel) {

    }
}
