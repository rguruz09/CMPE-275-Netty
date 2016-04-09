package gash.router.server.Election;

import gash.router.client.ClientHealper;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import pipe.common.Common;
import pipe.election.Election;
import pipe.work.Work;
import storage.Storage;

import java.util.HashMap;

import static pipe.work.Work.HbType.LEADERREQ;

/**
 * Created by Student on 4/1/16.
 */
public class CommonUtils {

    public static final int MAX_HOPS = 1;
    public static ServerState state;


    public static void sendMessageToEveryone(ServerState state, Work.WorkMessage wb){

       // Work.WorkMessage wb = CreateGenericHBReqMsg(state,Work.HbType.DISCOVERREQ);

        for (EdgeInfo ei : state.getEmon().getOutboundEdges().getAllNodes().values()){
            if(ei.isActive() && ei.getChannel() != null ){
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


    public static void forwardToAll(Work.WorkMessage msg, ServerState state, boolean all, int inb){

        if(msg.getHeader().getMaxHops() > 0){

            Work.WorkMessage msg1 = updateMaxHops(msg);

            if(msg1.hasCommand()){
                System.out.println("command -------------------->>>>>>>>>>>>>");
            }

            for (EdgeInfo ei : state.getEmon().getOutboundEdges().getAllNodes().values()) {
                if (ei.isActive() && ei.getChannel() != null) {
                        ei.getChannel().writeAndFlush(msg1);
                }
            }

        }else {
            System.out.println("Message expires");
        }
    }

    public static Work.WorkMessage updateMaxHops(Work.WorkMessage msg){


        Work.Heartbeat.Builder bb = null;
        Election.LeaderStatus.Builder eb = null;
        Work.VoteMsg.Builder vb = null;
        Work.Command.Builder cb = null;

        Work.HeartBeatMsgType.Builder heartBeatMsgType = Work.HeartBeatMsgType.newBuilder();
        heartBeatMsgType.setType(msg.getBeat().getMsgType().getType());

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(msg.getHeader().getNodeId());
        hb.setDestination(msg.getHeader().getDestination());
        hb.setTime(msg.getHeader().getTime());
        hb.setMaxHops(msg.getHeader().getMaxHops()-1);

        if(msg.hasBeat()){
            bb = Work.Heartbeat.newBuilder();
            bb.setState(msg.getBeat().getState());
            bb.setMsgType(heartBeatMsgType);
        }

        if(msg.hasLeader()){
            eb = Election.LeaderStatus.newBuilder();
            eb.setAction(msg.getLeader().getAction());
            eb.setState(msg.getLeader().getState());
            eb.setLeaderHost(msg.getLeader().getLeaderHost());
            eb.setLeaderId(msg.getLeader().getLeaderId());
            eb.setTerm(msg.getLeader().getTerm());
        }

        if(msg.hasVote()){
            vb = Work.VoteMsg.newBuilder();
            vb.setState(msg.getVote().getState());
            vb.setTerm(msg.getVote().getTerm());
            vb.setVtype(msg.getVote().getVtype());
        }

        if(msg.hasCommand()){
            cb = Work.Command.newBuilder();
            cb.setQuery(msg.getCommand().getQuery());
            cb.setResponse(msg.getCommand().getResponse());
        }

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);

        if(bb != null){
            wb.setBeat(bb);
        }else if(cb != null){
            wb.setCommand(cb);
        }else if(vb != null){
            wb.setVote(vb);
        } else if(eb != null){
            wb.setLeader(eb);
        }

        wb.setSecret(msg.getSecret());

        return wb.build();
    }

    public static void addFollower(ServerState state, int nodeId){
        FollowerInfo followerInfo = new FollowerInfo();
        followerInfo.setActive(true);
        followerInfo.setLastHBResp(System.currentTimeMillis());
        followerInfo.setNodeId(nodeId);
        followerInfo.setVoted(true);
        state.getElectionMonitor().addNewFollowers(nodeId, followerInfo);
        //state.getElectionMonitor().getFollowers().put(nodeId,followerInfo);
    }

    public static Storage.Response.Builder CreateCmdRes(Work.WorkMessage msg, boolean success, int seq){


        Storage.Response.Builder rsp = Storage.Response.newBuilder();
        rsp.setAction(msg.getCommand().getQuery().getAction());
        rsp.setSuccess(success);
        rsp.setSequenceNo(seq);
//        rsp.setMetaData(meta);

        return rsp;

    }
}




























