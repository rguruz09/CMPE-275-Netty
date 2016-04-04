package gash.router.server.workHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.ServerState;
import pipe.work.Work;

/**
 * Created by Student on 4/3/16.
 */
public class WorkCmdMsg {

    public void handleWorkCmdMsg(Work.WorkMessage msg, ServerState state){
        System.out.println("If you Leader Handle it else forward it to Leader");
        if(msg.getHeader().getDestination() == state.getConf().getNodeId()){
            System.out.println("Process It");
        }else{
            System.out.println("Forwording..");
            if(state.getEmon().getInboundEdges().hasNode(msg.getHeader().getDestination())){
                if(state.getEmon().getInboundEdges().getNode(msg.getHeader().getDestination()).getChannel().isActive()){
                    System.out.println("Forwording..found in Inbound list");
                    state.getEmon().getInboundEdges().getNode(msg.getHeader().getDestination()).getChannel().writeAndFlush(msg);
                }
            } else if(state.getEmon().getOutboundEdges().hasNode(msg.getHeader().getDestination())){
                if(state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination()).getChannel().isActive()){
                    System.out.println("Forwording..found in outbound list");
                    state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination()).getChannel().writeAndFlush(msg);
                }
            } else {
                System.out.println("Forwording to ALL.....");
                CommonUtils.forwardToAll(msg,state,false);
            }
        }
    }
}
