package gash.router.server.CommandHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.Election.ElectionMonitor;
import gash.router.server.ServerState;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;


/**
 * Created by Student on 4/3/16.
 */
public class TextMsg {

    private ServerState state;
    protected static Logger logger = LoggerFactory.getLogger("cmd");
    private int LeaderId;

    public TextMsg(){
        state = CommonUtils.state;
    }

    public void handleTextMsg(Pipe.CommandMessage msg, Channel channel){
        System.out.println("Text Message");


        if(state != null){
            System.out.println("From client handler -- leader is :"+state);
            //  LeaderId = state.getElectionMonitor().getLeaderStatus().getCurLeader();

            if(state.getElectionMonitor().getLeaderStatus().getCurLeader() == state.getConf().getNodeId()){
                System.out.println("I am the Leader and ill handle client request");

            }else {
                System.out.println("I am not the Leader and forward it to leader");
                Work.WorkMessage workMessage = CommandsUtils.getWorkFromCommand(msg,state);
                CommonUtils.forwardToAll(workMessage,state,false);
            }

        }


//        //System.out.println(msg.getMessage());
//        logger.info("Received Message from client : "+msg.getMessage());
//        //logger.info("image byte is "+String.valueOf(msg.getMessageBytes()));
//        System.out.println("Sending reply ......");
//        Common.Header.Builder hb = Common.Header.newBuilder();
//        hb.setNodeId(12);
//        hb.setTime(System.currentTimeMillis());
//        hb.setDestination(-1);
//
//        Pipe.CommandMessage.Builder rb = Pipe.CommandMessage.newBuilder();
//        rb.setHeader(hb);
//        rb.setMessage("Reply from server");
//        channel.writeAndFlush(rb.build());
    }
}
