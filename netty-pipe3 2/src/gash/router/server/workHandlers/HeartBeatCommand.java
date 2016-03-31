package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class HeartBeatCommand implements Command {

    private HeartBeatMsg heartBeatMsg;
    public HeartBeatCommand(HeartBeatMsg hbm){
        heartBeatMsg = hbm;
    }

    @Override
    public void handleMessage(Work.WorkMessage msg, Channel channel) {
        heartBeatMsg.handleHBMsg(msg, channel);
    }
}
