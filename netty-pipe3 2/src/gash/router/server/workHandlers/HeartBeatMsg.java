package gash.router.server.workHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.Election.FollowerInfo;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeMonitor;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;

import static gash.router.server.Election.CommonUtils.CreateGenericHBResMsg;
import static gash.router.server.Election.CommonUtils.addFollower;
import static gash.router.server.Election.CommonUtils.forwardToAll;

/**
 * Created by vinay on 3/29/16.
 */
public class HeartBeatMsg {

    private ServerState state;
    public HeartBeatMsg(ServerState state) {
        this.state = state;
    }

    protected static Logger logger = LoggerFactory.getLogger("work");

    public void handleHBMsg(Work.WorkMessage msg, Channel channel){


        //System.out.println("HB type is " + msg.getBeat().getMsgType().getType());
        //If it's a NEIGHBOR msg
        if(msg.getBeat().getMsgType().getType() == Work.HbType.NEIGHBORREQ ||
                msg.getBeat().getMsgType().getType() == Work.HbType.NEIGHBORRES){
            //If its a response msg
            if (msg.getHeader().getDestination() == state.getConf().getNodeId()) {
                System.out.println("Its a NEIGHBORRES from NODE "+msg.getHeader().getNodeId());
            } else {
                // Respond back
                System.out.println("Its a NEIGHBORREQ from NODE "+msg.getHeader().getNodeId());
                Work.WorkMessage rB = returnHB(msg.getHeader().getNodeId());
                channel.writeAndFlush(rB);
                state.getEmon().createInboundIfNew(msg.getHeader().getNodeId(), channel.remoteAddress().toString(), 1200);
                if(state.getEmon().getInboundEdges().getNode(msg.getHeader().getNodeId()).getChannel() == null){
                    System.out.println("Saving the channel info");
                    state.getEmon().getInboundEdges().getNode(msg.getHeader().getNodeId()).setChannel(channel);
                }
            }
            //If it's a LEADER msg
        }else if(msg.getBeat().getMsgType().getType() == Work.HbType.LEADERREQ ||
                msg.getBeat().getMsgType().getType() == Work.HbType.LEADERRES){
            // HB from Leader
            if(msg.getBeat().getMsgType().getType() == Work.HbType.LEADERREQ){
                System.out.println("LEADERREQ received from NODE "+msg.getHeader().getNodeId());
                Work.WorkMessage wm = CreateGenericHBResMsg(state,Work.HbType.LEADERRES, msg.getHeader().getNodeId());
                state.getElectionMonitor().setLastHBReceived(System.currentTimeMillis());
                channel.writeAndFlush(wm);
                forwardToAll(msg,state,false);
            }else {
                System.out.println("LEADERRES received from NODE "+msg.getHeader().getNodeId());
                if(msg.getHeader().getDestination() == state.getConf().getNodeId()) {
                    if (!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())) {
                        addFollower(state, msg.getHeader().getNodeId());
                    }
                    state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).setLastHBResp(System.currentTimeMillis());
                    state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).setActive(true);
                }else {
                    forwardToAll(msg, state,false);
                }
            }
            //If it's a DISCOVER msg
        }else if(msg.getBeat().getMsgType().getType() == Work.HbType.DISCOVERREQ ||
                msg.getBeat().getMsgType().getType() == Work.HbType.DISCOVERRES){
            // Discover req from Leader
            if(msg.getBeat().getMsgType().getType() == Work.HbType.DISCOVERREQ ){
                System.out.println("DISCOVERREQ received from NODE "+msg.getHeader().getNodeId());
                Work.WorkMessage wm = CreateGenericHBResMsg(state,Work.HbType.DISCOVERRES, msg.getHeader().getNodeId());
                channel.writeAndFlush(wm);
                forwardToAll(msg,state,false);
            }else {
                System.out.println("DISCOVERRES received from NODE "+msg.getHeader().getNodeId());
                if(msg.getHeader().getNodeId() == state.getConf().getNodeId()){
                    if(!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())){
                        addFollower(state,msg.getHeader().getNodeId());
                    }
                }else {
                    forwardToAll(msg,state,false);
                }
            }
        }



//        if(msg.getHeader().getDestination() != state.getConf().getNodeId()){
//
//            if(state.getEmon().getOutboundEdges().hasNode(msg.getHeader().getDestination())){
//                EdgeInfo ef = state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination());
//                ef.getChannel().writeAndFlush(msg);
//            }
//            else if(state.getEmon().getInboundEdges().hasNode(msg.getHeader().getDestination())){
//                EdgeInfo ef = state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination());
//                ef.getChannel().writeAndFlush(msg);
//            }
//            else {
//                if(msg.getHeader().getDestination() == -1){
//                    System.out.println("Its a broadcast HB.. Handling for myself");
//                    System.out.println("Sender of HB is - "+msg.getHeader().getNodeId());
//                    Work.WorkMessage rB = returnHB(msg.getHeader().getNodeId());
//                    channel.writeAndFlush(rB);
//                }
//                for (EdgeInfo ei : state.getEmon().getOutboundEdges().getAllNodes().values()) {
//                    if (ei.isActive() && ei.getChannel() != null) {
//                        ei.getChannel().writeAndFlush(msg);
//                    }
//                }
//            }
//        }else{
//            if(state.getEmon().getOutboundEdges().hasNode(msg.getHeader().getNodeId())){
//                if(state.getEmon().getInboundEdges().hasNode(msg.getHeader().getNodeId())) {
//                    if(channel == EdgeMonitor.getChannel()){
//                        System.out.println("Loop: Do nothing");
//                    }
//                    else{
//                        state.getEmon().createInboundIfNew(msg.getHeader().getNodeId(),channel.remoteAddress().toString(),1200);
//                        logger.debug("heartbeat from " + msg.getHeader().getNodeId());
//                        System.out.println("Sender of HB is - "+msg.getHeader().getNodeId());
//                        Work.WorkMessage rB = returnHB(msg.getHeader().getNodeId());
//                        channel.writeAndFlush(rB);
//                    }
//                }
//                else {
//                    System.out.println("Its a response hb.. drop the packet..");
//                }
//            }else {
//                state.getEmon().createInboundIfNew(msg.getHeader().getNodeId(),channel.remoteAddress().toString(),1200);
//                logger.debug("heartbeat from " + msg.getHeader().getNodeId());
//                System.out.println("Sender of HB is - "+msg.getHeader().getNodeId());
//                Work.WorkMessage rB = returnHB( msg.getHeader().getNodeId());
//                channel.writeAndFlush(rB);
//            }
//            System.out.println("Hearbeat received");
//        }

    }

    private Work.WorkMessage returnHB(int dest) {
        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Work.HeartBeatMsgType.Builder heartBeatMsgType = Work.HeartBeatMsgType.newBuilder();
        heartBeatMsgType.setType(Work.HbType.NEIGHBORRES);

        Work.Heartbeat.Builder bb = Work.Heartbeat.newBuilder();
        bb.setState(sb);
        bb.setMsgType(heartBeatMsgType);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(dest);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(2);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setBeat(bb);
        wb.setSecret(123);

        return wb.build();
    }



}
