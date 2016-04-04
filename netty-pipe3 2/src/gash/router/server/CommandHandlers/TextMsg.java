package gash.router.server.CommandHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.Election.ElectionMonitor;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;


/**
 * Created by Raghavendra on 4/3/16.
 */
public class TextMsg {

    private ServerState state;
    protected static Logger logger = LoggerFactory.getLogger("cmd");

    public TextMsg(){
        state = CommonUtils.state;
    }

    public void handleTextMsg(Pipe.CommandMessage msg, Channel channel){
        System.out.println("Text Message");

        if(state != null){

//            System.out.println("From client handler -- leader is :"+state);
//            Work.WorkMessage workMessage = CommandsUtils.getWorkFromCommand(msg,state);
//            if(state.getElectionMonitor().getLeaderStatus().getCurLeader() == state.getConf().getNodeId()){
//                System.out.println("I am the Leader and ill handle client request");
//            }else {
//                System.out.println("I am not the Leader and forward it to leader");
//                CommandsUtils.sendToLeader(workMessage,state);

            logger.info("Received Message from client : "+msg.getMessage());
            System.out.println("Sending reply ......");
            Common.Header.Builder hb = Common.Header.newBuilder();
            hb.setNodeId(state.getConf().getNodeId());
            hb.setTime(System.currentTimeMillis());
            hb.setDestination(-1);

            Pipe.CommandMessage.Builder rb = Pipe.CommandMessage.newBuilder();
            rb.setHeader(hb);
            rb.setMessage("Message received by server successfully");
            channel.writeAndFlush(rb.build());

        }
    }
}
