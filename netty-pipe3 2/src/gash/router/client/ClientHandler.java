package gash.router.client;

import io.netty.channel.Channel;
import routing.Pipe;

import java.util.HashMap;

/**
 * Created by Student on 4/4/16.
 */
public class ClientHandler {

    public static HashMap<Integer, byte[]> hashMap;

    public static void rebuildData (Channel channel, Pipe.CommandMessage msg){
        if(msg.getResponse().getSequenceNo() == 0){

        }
    }

}
