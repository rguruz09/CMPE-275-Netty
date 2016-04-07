package gash.router.server.workHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.Election.FollowerInfo;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import gash.router.server.edges.EdgeMonitor;
import io.netty.channel.Channel;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.election.Election;
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

        try {
            //System.out.println("HB type is " + msg.getBeat().getMsgType().getType());
            //If it's a NEIGHBOR msg
            if(msg.getBeat().getMsgType().getType() == Work.HbType.NEIGHBORREQ ||
                    msg.getBeat().getMsgType().getType() == Work.HbType.NEIGHBORRES){
                //If its a response msg
                if (msg.getHeader().getDestination() == state.getConf().getNodeId() || msg.getHeader().getNodeId() == state.getConf().getNodeId()) {
                    // System.out.println("Its a NEIGHBORRES from NODE "+msg.getHeader().getNodeId());
                } else {
                    // Respond back
                    System.out.println("Its a NEIGHBORREQ from NODE "+msg.getHeader().getNodeId());
                    Work.WorkMessage rB = returnHB(msg.getHeader().getNodeId());
                    channel.writeAndFlush(rB);
                    state.getEmon().createInboundIfNew(msg.getHeader().getNodeId(), channel.remoteAddress().toString(), 1200);
                    if(state.getEmon().getInboundEdges().getNode(msg.getHeader().getNodeId()).getChannel() == null){
                        System.out.println("Saving the channel info");
                        state.getEmon().getInboundEdges().getNode(msg.getHeader().getNodeId()).setChannel(channel);
                        state.getEmon().getInboundEdges().getNode((msg.getHeader().getNodeId())).setActive(true);
                    }
                }
                //If it's a LEADER msg
            }else if(msg.getBeat().getMsgType().getType() == Work.HbType.LEADERREQ ||
                    msg.getBeat().getMsgType().getType() == Work.HbType.LEADERRES){
                // HB from Leader
                if(msg.getBeat().getMsgType().getType() == Work.HbType.LEADERREQ &&
                        msg.getHeader().getNodeId() != state.getConf().getNodeId()){
                    System.out.println("LEADERREQ received from NODE "+msg.getHeader().getNodeId());
                    state.getElectionMonitor().setLastHBReceived(System.currentTimeMillis());
                    state.getElectionMonitor().getLeaderStatus().setCurLeader(msg.getHeader().getNodeId());
                    state.getElectionMonitor().getLeaderStatus().setLeader_state(Election.LeaderStatus.LeaderState.LEADERALIVE);
                    //state.getElectionMonitor().getLeaderStatus().setLeaderHost(ms);
                    Work.WorkMessage wm = CreateGenericHBResMsg(state,Work.HbType.LEADERRES, msg.getHeader().getNodeId());
                    channel.writeAndFlush(wm);
                    forwardToAll(msg,state,false,msg.getHeader().getNodeId());
                }else if(msg.getBeat().getMsgType().getType() == Work.HbType.LEADERRES){
                    System.out.println("LEADERRES received from NODE "+msg.getHeader().getNodeId());
                    if(msg.getHeader().getDestination() == state.getConf().getNodeId()) {
                        if (!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())) {
                            addFollower(state, msg.getHeader().getNodeId());
                        }
                        state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).setLastHBResp(System.currentTimeMillis());
                        state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).setActive(true);
                    }else {
                        forwardToAll(msg, state,false,msg.getHeader().getNodeId());
                    }
                }
                //If it's a DISCOVER msg
            }else if(msg.getBeat().getMsgType().getType() == Work.HbType.DISCOVERREQ ||
                    msg.getBeat().getMsgType().getType() == Work.HbType.DISCOVERRES){
                // Discover req from Leader
                if(msg.getBeat().getMsgType().getType() == Work.HbType.DISCOVERREQ &&
                        msg.getHeader().getNodeId() != state.getConf().getNodeId()){
                    System.out.println("DISCOVERREQ received from NODE "+msg.getHeader().getNodeId());
                    Work.WorkMessage wm = CreateGenericHBResMsg(state,Work.HbType.DISCOVERRES, msg.getHeader().getNodeId());
                    channel.writeAndFlush(wm);
                    forwardToAll(msg,state,false,msg.getHeader().getNodeId());
                }else if(msg.getBeat().getMsgType().getType() == Work.HbType.DISCOVERRES) {
                    System.out.println("DISCOVERRES received from NODE "+msg.getHeader().getNodeId());
                    if(msg.getHeader().getDestination() == state.getConf().getNodeId()){
                        if(!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())){
                            addFollower(state,msg.getHeader().getNodeId());
                        }
                    }else {
                        forwardToAll(msg,state,false,msg.getHeader().getNodeId());
                    }
                }
            }
        }catch (Exception e){
            System.out.println("Exception from HB..");
        }
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
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setBeat(bb);
        wb.setSecret(123);

        return wb.build();
    }



}
