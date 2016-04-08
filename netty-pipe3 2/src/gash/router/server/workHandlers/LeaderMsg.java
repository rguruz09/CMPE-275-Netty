package gash.router.server.workHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.Election.ElectionStatus;
import gash.router.server.Election.LeaderStatus;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import io.netty.channel.Channel;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.election.Election;
import pipe.work.Work;

/**
 * Created by vinay on 3/31/16.
 */
public class LeaderMsg {
    protected static Logger logger = LoggerFactory.getLogger("LeaderMsg");
    private static ServerState state;

    public LeaderMsg(ServerState state) {
        this.state = state;
    }


    public void handleLeaderMsg(Work.WorkMessage msg, Channel channel) {
       // logger.info("Leadermsg from " + msg.getHeader().getNodeId());
        //System.out.println("In HandleLeaderMsg");

        if(false){
            // if im the sender, dont care
        }else{

            try {
                if (msg.getLeader().getAction() == Election.LeaderStatus.LeaderQuery.WHOISTHELEADER) {
                    System.out.println("WHOISTHELEADER msg received");
                    Work.WorkMessage wm = createLeaderRespMsg(msg.getHeader().getNodeId(), state.getElectionMonitor().getLeaderStatus().getLeader_state());
                    channel.writeAndFlush(wm);

                } else if(msg.getLeader().getAction() == Election.LeaderStatus.LeaderQuery.THELEADERIS){
                    //System.out.println("Received leader response from "+msg.getHeader().getNodeId());

                    if(msg.getLeader().getState() == Election.LeaderStatus.LeaderState.LEADERDEAD ||
                            msg.getLeader().getState() == Election.LeaderStatus.LeaderState.LEADERUNKNOWN){
                        System.out.println("THELEADERIS not known :( :(");
                        state.getElectionMonitor().getLeaderStatus().setLeader_state(Election.LeaderStatus.LeaderState.LEADERDEAD);
                        //  state.getElectionMonitor().getLeaderStatus().setCurLeader(msg.getLeader().getLeaderId());
                        //   state.getElectionMonitor().getLeaderStatus().setLeaderHost(msg.getLeader().getLeaderHost());
                        state.getElectionMonitor().getElectionStatus().setTerm(msg.getLeader().getTerm());
                    }else {
                       // System.out.println("Response: New Leader is found!!");
                        System.out.println("THELEADERIS node "+msg.getLeader().getLeaderId());
                        state.getElectionMonitor().getLeaderStatus().setLeader_state(Election.LeaderStatus.LeaderState.LEADERALIVE);
                        state.getElectionMonitor().getLeaderStatus().setCurLeader(msg.getLeader().getLeaderId());
                        state.getElectionMonitor().getLeaderStatus().setLeaderHost(msg.getLeader().getLeaderHost());
                        state.getElectionMonitor().getElectionStatus().setTerm(msg.getLeader().getTerm());
                        state.getElectionMonitor().getElectionStatus().setStatus(ElectionStatus.NODE_STATUS.FOLLOWER);
                        state.getElectionMonitor().setLastHBReceived(System.currentTimeMillis());
                        Work.WorkMessage vm = createNotifyMsg(msg.getLeader().getTerm(),msg.getLeader().getLeaderId());
                       // CommonUtils.forwardToAll(vm,state,false,msg.getHeader().getNodeId());
                        if(state.getEmon().getOutboundEdges().hasNode(msg.getLeader().getLeaderId()) &&
                                (state.getEmon().getOutboundEdges().getNode(msg.getLeader().getLeaderId()).isActive())){
                            state.getEmon().getOutboundEdges().getNode(msg.getLeader().getLeaderId()).getChannel().writeAndFlush(vm);
                        }
                    }
                    // if its a broadcast msg fwd it to all
//                    if(msg.getHeader().getDestination() == -1){
//                        for (EdgeInfo ei : state.getEmon().getOutboundEdges().getAllNodes().values()) {
//                            if (ei.isActive() && ei.getChannel() != null) {
//                                ei.getChannel().writeAndFlush(msg);
//                            }
//                        }
//                        for (EdgeInfo ei : state.getEmon().getInboundEdges().getAllNodes().values()) {
//                            if (ei.isActive() && ei.getChannel() != null) {
//                                ei.getChannel().writeAndFlush(msg);
//                            }
//                        }
//                    }
                }
            }catch (Exception e){
                System.out.println("Exception from Leader");
            }


        }
    }

    public static Work.WorkMessage createLeaderRespMsg(int dest, Election.LeaderStatus.LeaderState ldst) {

        Election.LeaderStatus.Builder ls = Election.LeaderStatus.newBuilder();
        ls.setAction(Election.LeaderStatus.LeaderQuery.THELEADERIS);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(dest);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        ls.setLeaderHost(state.getElectionMonitor().getLeaderStatus().getLeaderHost());
        ls.setLeaderId(state.getElectionMonitor().getLeaderStatus().getCurLeader());
        ls.setState(ldst);
        ls.setTerm(state.getElectionMonitor().getElectionStatus().getTerm());

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setLeader(ls);

        wb.setSecret(123);

        return wb.build();
    }

    public  Work.WorkMessage createNotifyMsg(int term, int dest) {

        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Work.VoteMsg.Builder vm = Work.VoteMsg.newBuilder();
        vm.setState(sb);
        vm.setVtype(Work.VoteMsg.VoteMsgType.VOTERES);
        vm.setTerm(term);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(dest);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setVote(vm);
        wb.setSecret(123);

        return wb.build();

    }


}
