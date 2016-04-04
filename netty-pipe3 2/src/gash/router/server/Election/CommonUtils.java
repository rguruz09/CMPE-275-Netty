package gash.router.server.Election;

import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import pipe.common.Common;
import pipe.work.Work;

import java.util.HashMap;

import static pipe.work.Work.HbType.LEADERREQ;

/**
 * Created by Student on 4/1/16.
 */
public class CommonUtils {

    public static final int MAX_HOPS = 1;
    public static ServerState state;


    public static void getClusterNodes(ServerState state){

        Work.WorkMessage wb = CreateGenericHBReqMsg(state,Work.HbType.DISCOVERREQ);

        for (EdgeInfo ei : state.getEmon().getOutboundEdges().getAllNodes().values()){
            if(ei.isActive() && ei.getChannel() != null){
                ei.getChannel().writeAndFlush(wb);
            }
        }

        for (EdgeInfo ei : state.getEmon().getInboundEdges().getAllNodes().values()){
            if(ei.isActive() && ei.getChannel() != null){
                ei.getChannel().writeAndFlush(wb);
            }
        }
    }

    public static  Work.WorkMessage CreateGenericHBReqMsg(ServerState state, Work.HbType type){
        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Work.HeartBeatMsgType.Builder heartBeatMsgType = Work.HeartBeatMsgType.newBuilder();
        heartBeatMsgType.setType(type);

        Work.Heartbeat.Builder bb = Work.Heartbeat.newBuilder();
        bb.setState(sb);
        bb.setMsgType(heartBeatMsgType);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(-1);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setBeat(bb);
        wb.setSecret(123);

        return wb.build();
    }

    public static  Work.WorkMessage CreateGenericHBResMsg(ServerState state, Work.HbType type, int dest){
        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Work.HeartBeatMsgType.Builder heartBeatMsgType = Work.HeartBeatMsgType.newBuilder();
        heartBeatMsgType.setType(type);

        Work.Heartbeat.Builder bb = Work.Heartbeat.newBuilder();
        bb.setState(sb);
        bb.setMsgType(heartBeatMsgType);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(dest);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setBeat(bb);
        wb.setSecret(123);

        return wb.build();
    }

//    public static Work.WorkMessage createDiscoverReqMsg(ServerState state){
//
//        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
//        sb.setEnqueued(-1);
//        sb.setProcessed(-1);
//
//        Work.HeartBeatMsgType.Builder heartBeatMsgType = Work.HeartBeatMsgType.newBuilder();
//        heartBeatMsgType.setType(Work.HbType.DISCOVERREQ);
//
//        Work.Heartbeat.Builder bb = Work.Heartbeat.newBuilder();
//        bb.setState(sb);
//        bb.setMsgType(heartBeatMsgType);
//
//        Common.Header.Builder hb = Common.Header.newBuilder();
//        hb.setNodeId(state.getConf().getNodeId());
//        hb.setDestination(-1);
//        hb.setTime(System.currentTimeMillis());
//        hb.setMaxHops(4);
//
//        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
//        wb.setHeader(hb);
//        wb.setBeat(bb);
//        wb.setSecret(123);
//
//        return wb.build();
//
//    }

//    public static Work.WorkMessage createDiscoverResMsg(ServerState state, int dest){
//
//        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
//        sb.setEnqueued(-1);
//        sb.setProcessed(-1);
//
//        Work.HeartBeatMsgType.Builder heartBeatMsgType = Work.HeartBeatMsgType.newBuilder();
//        heartBeatMsgType.setType(Work.HbType.DISCOVERRES);
//
//        Work.Heartbeat.Builder bb = Work.Heartbeat.newBuilder();
//        bb.setState(sb);
//        bb.setMsgType(heartBeatMsgType);
//
//        Common.Header.Builder hb = Common.Header.newBuilder();
//        hb.setNodeId(state.getConf().getNodeId());
//        hb.setDestination(dest);
//        hb.setTime(System.currentTimeMillis());
//        hb.setMaxHops(4);
//
//        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
//        wb.setHeader(hb);
//        wb.setBeat(bb);
//        wb.setSecret(123);
//
//        return wb.build();
//
//    }

    public static void forwardToAll(Work.WorkMessage msg, ServerState state, boolean all){

        if(msg.getHeader().getMaxHops() > 0){

            Work.WorkMessage msg1 = updateMaxHops(msg);

            for (EdgeInfo ei : state.getEmon().getOutboundEdges().getAllNodes().values()) {
                if (ei.isActive() && ei.getChannel() != null) {
                        ei.getChannel().writeAndFlush(msg1);
                }
            }
            for (EdgeInfo ei : state.getEmon().getInboundEdges().getAllNodes().values()) {
                if (ei.isActive() && ei.getChannel() != null) {
                    ei.getChannel().writeAndFlush(msg1);
                }
            }
        }else {
            System.out.println("Message expires");
        }
    }

    public static Work.WorkMessage updateMaxHops(Work.WorkMessage msg){

        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Work.HeartBeatMsgType.Builder heartBeatMsgType = Work.HeartBeatMsgType.newBuilder();
        heartBeatMsgType.setType(msg.getBeat().getMsgType().getType());

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(msg.getHeader().getNodeId());
        hb.setDestination(msg.getHeader().getDestination());
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(msg.getHeader().getMaxHops()-1);

        Work.Heartbeat.Builder bb = Work.Heartbeat.newBuilder();
        bb.setState(sb);
        bb.setMsgType(heartBeatMsgType);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setBeat(bb);
        wb.setSecret(123);

        return wb.build();
    }

    public static void addFollower(ServerState state, int nodeId){
        FollowerInfo followerInfo = new FollowerInfo();
        followerInfo.setActive(true);
        followerInfo.setLastHBResp(System.currentTimeMillis());
        followerInfo.setNodeId(nodeId);
        followerInfo.setVoted(false);
        state.getElectionMonitor().addNewFollowers(nodeId, followerInfo);
        //state.getElectionMonitor().getFollowers().put(nodeId,followerInfo);
    }
}
