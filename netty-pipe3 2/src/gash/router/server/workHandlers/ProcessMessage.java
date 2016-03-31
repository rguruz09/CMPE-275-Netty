package gash.router.server.workHandlers;

import io.netty.channel.Channel;
import pipe.work.Work;

/**
 * Created by vinay on 3/29/16.
 */
public class ProcessMessage {

    private Command command;

    public ProcessMessage(Command c){
        command = c;
    }

    public void executeMsg(Work.WorkMessage msg, Channel channel){
        command.handleMessage(msg,channel);
    }

}
