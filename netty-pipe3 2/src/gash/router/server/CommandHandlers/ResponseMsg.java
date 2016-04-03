package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import routing.Pipe;

/**
 * Created by Student on 4/3/16.
 */
public class ResponseMsg {

    public void handleResponseMsg(Pipe.CommandMessage msg, Channel channel){
        System.out.println("Response Message");
    }
}
