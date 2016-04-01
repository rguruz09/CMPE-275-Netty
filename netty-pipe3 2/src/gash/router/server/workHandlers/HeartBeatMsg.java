package gash.router.server.workHandlers;

import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeMonitor;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.work.Work;

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

        //
        System.out.println("HB type is " + msg.getBeat().getMsgType());
         //If its a response msg
        if (msg.getHeader().getDestination() == state.getConf().getNodeId()) {
            System.out.println("Its a response msg.. drop msg");
        } else {
            // Respond back
            Work.WorkMessage rB = returnHB(msg.getHeader().getNodeId());
            channel.writeAndFlush(rB);
            //Vinay: ToDo host address and port no
            state.getEmon().createInboundIfNew(msg.getHeader().getNodeId(), channel.remoteAddress().toString(), 1200);
            if(state.getEmon().getInboundEdges().getNode(msg.getHeader().getNodeId()).getChannel() == null){
                System.out.println("Saving the channel info");
                state.getEmon().getInboundEdges().getNode(msg.getHeader().getNodeId()).setChannel(channel);
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
