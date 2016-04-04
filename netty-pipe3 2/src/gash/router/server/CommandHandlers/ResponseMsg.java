package gash.router.server.CommandHandlers;

import gash.router.server.Election.CommonUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import routing.Pipe;

/**
 * Created by Student on 4/3/16.
 */
public class ResponseMsg {

    protected static Logger logger = LoggerFactory.getLogger("cmd");

    public void handleResponseMsg(Pipe.CommandMessage msg, Channel channel){
        System.out.println("Response Message -- this is respond to the command channel");
        if(CommonUtils.state.getCmdChannel() != null){
            CommonUtils.state.getCmdChannel().writeAndFlush(msg);
        }
    }

}
