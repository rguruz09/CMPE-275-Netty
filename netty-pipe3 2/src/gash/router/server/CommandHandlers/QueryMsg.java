package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import pipe.storage.Storage;
import routing.Pipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Student on 4/3/16.
 */
public class QueryMsg {

    protected static Logger logger = LoggerFactory.getLogger("cmd");

    public void handleQuertMsg(Pipe.CommandMessage msg, Channel channel){
        System.out.println("Query Message");
        System.out.println("Server receieved an image from client....");
        Storage.Query.Builder qb = Storage.Query.newBuilder();
        logger.info(String.valueOf(qb.getData()));
    }
}
