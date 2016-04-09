package gash.router.server.CommandHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.workHandlers.WorkCmdMsg;
import io.netty.channel.Channel;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;

/**
 * Created by Student on 4/3/16.
 */
public class CommandsUtils {

    public static Work.WorkMessage getWorkFromCommand(Pipe.CommandMessage msg, ServerState state){

        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(state.getElectionMonitor().getLeaderStatus().getCurLeader());
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        Work.Command.Builder cb = Work.Command.newBuilder();
        if(msg.hasQuery())
            cb.setQuery(msg.getQuery());
        if(msg.hasResponse())
            cb.setResponse(msg.getResponse());

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setCommand(cb);
        wb.setSecret(123);

        return wb.build();
    }

    public static void sendToLeader(Work.WorkMessage workMessage, ServerState state, Channel channel){

        if( (state.getEmon().getOutboundEdges().hasNode(workMessage.getHeader().getDestination())) &&
                ( state.getEmon().getOutboundEdges().getNode(workMessage.getHeader().getDestination()).getChannel().isActive()  &&
                (state.getEmon().getOutboundEdges().getNode(workMessage.getHeader().getDestination()).getChannel() != null))){
            state.getEmon().getOutboundEdges().getNode(workMessage.getHeader().getDestination()).getChannel().writeAndFlush(workMessage);
        }
        else if(workMessage.getHeader().getDestination() == state.getConf().getNodeId()){
            System.out.println("Myself teh leader.. send the command msg");
            new WorkCmdMsg().handleWorkCmdMsg(workMessage,state, channel);
        }

    }

    public static Pipe.CommandMessage buildCommandMsgFromWork(Work.WorkMessage msg, ServerState state){

        Pipe.CommandMessage.Builder cm = Pipe.CommandMessage.newBuilder();

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(999);
        hb.setMaxHops(0);
        hb.setTime(System.currentTimeMillis());

        cm.setHeader(hb);
        cm.setResponse(msg.getCommand().getResponse());

        return cm.build();
    }
}
