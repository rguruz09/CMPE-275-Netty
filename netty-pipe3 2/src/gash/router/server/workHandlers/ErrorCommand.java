package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class ErrorCommand implements Command {
    private ErrorMsg errorMsg;

    public ErrorCommand(ErrorMsg err){
        errorMsg = err;
    }
    @Override
    public void handleMessage(Work.WorkMessage msg, Channel channel) {
        errorMsg.handleErrMsg(msg, channel);
    }
}
