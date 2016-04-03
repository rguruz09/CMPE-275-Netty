package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import routing.Pipe;


/**
 * Created by Student on 4/3/16.
 */
public class TextMsg {

    protected static Logger logger = LoggerFactory.getLogger("cmd");

    public void handleTextMsg(Pipe.CommandMessage msg, Channel channel){
        System.out.println("Text Message");

        //System.out.println(msg.getMessage());
        logger.info("Received Message from client : "+msg.getMessage());
        //logger.info("image byte is "+String.valueOf(msg.getMessageBytes()));
        System.out.println("Sending reply ......");
        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(12);
        hb.setTime(System.currentTimeMillis());
        hb.setDestination(-1);

        Pipe.CommandMessage.Builder rb = Pipe.CommandMessage.newBuilder();
        rb.setHeader(hb);
        rb.setMessage("Reply from server");
        channel.writeAndFlush(rb.build());
    }
}
