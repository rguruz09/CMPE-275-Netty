package gash.router.server.Election;

import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import pipe.common.Common;
import pipe.work.Work;

import java.util.HashMap;

/**
 * Created by Student on 4/1/16.
 */
public class CommonUtils {

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
        hb.setMaxHops(4);

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
        hb.setMaxHops(4);

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
        for (EdgeInfo ei : state.getEmon().getOutboundEdges().getAllNodes().values()) {
            if (ei.isActive() && ei.getChannel() != null && all ) {
                ei.getChannel().writeAndFlush(msg);
            }
        }
        for (EdgeInfo ei : state.getEmon().getInboundEdges().getAllNodes().values()) {
            if (ei.isActive() && ei.getChannel() != null && all) {
                ei.getChannel().writeAndFlush(msg);
            }
        }
    }

    public static void addFollower(ServerState state, int nodeId){
        FollowerInfo followerInfo = new FollowerInfo();
        followerInfo.setActive(true);
        followerInfo.setLastHBResp(System.currentTimeMillis());
        followerInfo.setNodeId(nodeId);
        followerInfo.setVoted(false);
        state.getElectionMonitor().getFollowers().put(nodeId,followerInfo);
    }
}
