package gash.router.server.CommandHandlers;

import io.netty.channel.Channel;
import pipe.storage.Storage;
import routing.Pipe;

import static sun.font.FontManager.logger;

/**
 * Created by Student on 4/3/16.
 */
public class QueryMsg {

    public void handleQuertMsg(Pipe.CommandMessage msg, Channel channel){
        System.out.println("Query Message");
        System.out.println("Server receieved an image from client....");
        Storage.Query.Builder qb = Storage.Query.newBuilder();
        logger.info(String.valueOf(qb.getData()));
    }
}
