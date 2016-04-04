package gash.router.server.CommandHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.ServerState;
import io.netty.channel.Channel;
import pipe.common.Common;
import pipe.work.Work;
import storage.Storage;
import routing.Pipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Raghavendra on 4/3/16.
 */
public class QueryMsg {

    private ServerState state;
    protected static Logger logger = LoggerFactory.getLogger("cmd");

    public QueryMsg() {
        this.state = CommonUtils.state;
    }

    public void handleQuertMsg(Pipe.CommandMessage msg, Channel channel){
        System.out.println("Query Message");
        System.out.println("Server receieved an image from client....");
        Storage.Query.Builder qb = Storage.Query.newBuilder();
        logger.info(String.valueOf(qb.getData()));


        if(state != null){
            System.out.println("From client handler -- leader is :"+state);
            Work.WorkMessage workMessage = CommandsUtils.getWorkFromCommand(msg,state);
            if(state.getElectionMonitor().getLeaderStatus().getCurLeader() == state.getConf().getNodeId()){
                System.out.println("I am the Leader and ill handle client request");
            }else {
                System.out.println("I am not the Leader and forward it to leader");
                CommandsUtils.sendToLeader(workMessage,state);
            }
        }
    }


}










//    Pipe.CommandMessage.Builder rb = Pipe.CommandMessage.newBuilder();
//    Common.Header.Builder hb = Common.Header.newBuilder();
//    Storage.Response.Builder res = Storage.Response.newBuilder();
////Storage.Query.Builder qb = Storage.Query.newBuilder();
//System.out.println("action is :  "+ msg.getQuery().getAction());
//        if(msg.getQuery().getAction()==Storage.Action.STORE) {
//        System.out.println("Server received an image from client....");
//
//        //Establish connection with MONGODB to store the received data qb.getData()
//        logger.info(String.valueOf(msg.getQuery().getData()));
//        //qb.setAction(Storage.Action.STORE);
//
//        hb.setNodeId(12);
//        hb.setTime(System.currentTimeMillis());
//        hb.setDestination(-1);
//
//        //CommandMessage.Builder rb = CommandMessage.newBuilder();
//        rb.setHeader(hb);
//        //rb.setQuery(qb.getData());
//        //qb.setData(qb.getData());
//        //rb.setQuery(qb);
//        rb.setMessage("Image received by server successfully");
//
//        //channel.writeAndFlush(rb.build());
//        }else if(msg.getQuery().getAction()==Storage.Action.GET){
//        //Establish connection with MONGODB to get the requested data based on client-ID
//
//        System.out.println("inside get");
//        //Common.Header.Builder hb = Common.Header.newBuilder();
//
//        hb.setNodeId(12);
//        hb.setTime(System.currentTimeMillis());
//        hb.setDestination(-1);
//        //res.setData(); Load this method with data retreived from MongoDB
//        rb.setHeader(hb);
//        //rb.setResponse(res);
//        rb.setMessage("get from server");
//
//        }
//        //Acknowledgement to the client of receipt of Data
//        channel.writeAndFlush(rb.build());
