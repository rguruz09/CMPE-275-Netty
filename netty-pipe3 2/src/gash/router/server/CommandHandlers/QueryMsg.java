package gash.router.server.CommandHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.ServerState;
import gash.router.server.workHandlers.WorkCmdHandler;
import gash.router.server.workHandlers.WorkCmdMsg;
import io.netty.channel.Channel;
import io.netty.util.internal.SystemPropertyUtil;
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

        if(channel != null){
            state.setCmdChannel(channel);
        }
        System.out.println("Query Message");
        System.out.println("Server receieved a file from client....");
        Storage.Query.Builder qb = Storage.Query.newBuilder();
        logger.info(String.valueOf(qb.getData()));

        if(state != null){
            System.out.println("From client handler -- leader is :"+state);
            Work.WorkMessage workMessage = CommandsUtils.getWorkFromCommand(msg,state);
            System.out.println("Sender of the file is %%%%%% "+msg.getQuery().getMetadata().getUid());
            if(state.getElectionMonitor().getLeaderStatus().getCurLeader() == state.getConf().getNodeId()){
                System.out.println("I am the Leader and ill handle client request");
                WorkCmdMsg workCmdMsg = new WorkCmdMsg();
                WorkCmdHandler workCmdHandler = new WorkCmdHandler(workCmdMsg, state);
                workCmdHandler.handleMessage(workMessage,channel);
            }else {
                System.out.println("I am not the Leader and forward it to leader");
                CommandsUtils.sendToLeader(workMessage,state,channel);
            }
        }
    }


}

