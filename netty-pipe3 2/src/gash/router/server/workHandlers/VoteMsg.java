package gash.router.server.workHandlers;

import gash.router.server.Election.CommonUtils;
import gash.router.server.Election.ElectionStatus;
import gash.router.server.Election.FollowerInfo;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import io.netty.channel.Channel;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.election.Election;
import pipe.work.Work;

import static gash.router.server.Election.CommonUtils.addFollower;
import static gash.router.server.Election.CommonUtils.forwardToAll;
import static gash.router.server.Election.CommonUtils.sendMessageToEveryone;

/**
 * Created by vinay on 4/1/16.
 */
    public class VoteMsg {

    protected static Logger logger = LoggerFactory.getLogger("Vote");
    private ServerState state;

    public VoteMsg(ServerState state) {
        this.state = state;
    }

    public void handleVoteMsg(Work.WorkMessage msg, Channel channel) {


        try {
            //logger.info("Vote msg from " + msg.getHeader().getNodeId());
            if (msg.getVote().getVtype() == Work.VoteMsg.VoteMsgType.VOTEREQ){
                System.out.println("VOTEREQ received from "+msg.getHeader().getNodeId());
                if(msg.getVote().getTerm() > state.getElectionMonitor().getElectionStatus().getTerm()) {

                    // forwardToAll(msg,state,false,msg.getHeader().getNodeId());
                    Work.WorkMessage wm = createVoteRespMsg(msg);
                    channel.writeAndFlush(wm);
                }else{
                    System.out.println("Term is greater, not voting for this candidate NODE "+msg.getHeader().getNodeId());
                }
            } else if (msg.getVote().getVtype() == Work.VoteMsg.VoteMsgType.VOTERES) {

                // 1. CHeck if the response for me
                if (msg.getHeader().getDestination() == state.getConf().getNodeId()) {
                    System.out.println("VOTERES received from NODE "+msg.getHeader().getNodeId()+"and it is for me");

                    // If Im the candidate process the response else not

                    //Check for redundant votes
                    if ((!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId()) ||
                            ! state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).isVoted())) {
             //       if (! state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).isVoted()) {
                        if (state.getElectionMonitor().getElectionStatus().getStatus() == ElectionStatus.NODE_STATUS.CANDIDATE) {
                            state.getElectionMonitor().getElectionStatus().setVoteCt(state.getElectionMonitor().getElectionStatus().getVoteCt() + 1);
                            state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).setVoted(true);
                            // Check if i got majority
                            if (state.getElectionMonitor().getElectionStatus().getVoteCt() >= state.getElectionMonitor().getElectionStatus().getQuorum()) {
                                System.out.println("Majority votes received: I will be the new leader: Node " +  state.getConf().getNodeId());

                                state.getElectionMonitor().getElectionStatus().setStatus(ElectionStatus.NODE_STATUS.LEADER);
                                state.getElectionMonitor().getLeaderStatus().setLeaderHost(state.getConf().getSelfHost());
                                state.getElectionMonitor().getLeaderStatus().setCurLeader(state.getConf().getNodeId());
                                state.getElectionMonitor().getLeaderStatus().setLeader_state(Election.LeaderStatus.LeaderState.LEADERALIVE);

                                if(!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())){
                                    addFollower(state,msg.getHeader().getNodeId());
                                }
                                Work.WorkMessage wm = LeaderMsg.createLeaderRespMsg(-1,Election.LeaderStatus.LeaderState.LEADERALIVE);
                                sendMessageToEveryone(state,wm);
                                //forwardToAll(wm,state,false,msg.getHeader().getNodeId());
                            }else {
                                if(!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())){
                                    addFollower(state,msg.getHeader().getNodeId());
                                }
                            }
                        }else if(state.getElectionMonitor().getElectionStatus().getStatus() == ElectionStatus.NODE_STATUS.LEADER){
                            if(!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())){
                                addFollower(state,msg.getHeader().getNodeId());
                            }
                        }
                    } else {
                        System.out.println("Duplicate vote..");
                    }
                } else {
                    // 2. If not for me send it to appropriate node.
//                    System.out.println("Forwarding vote msg from "+msg.getHeader().getNodeId()+" to "+msg.getHeader().getDestination());
                    System.out.println("Dropping the VOTERES. its not for me");

//                    if (state.getEmon().getInboundEdges().hasNode(msg.getHeader().getDestination())) {
//                        Channel c = state.getEmon().getInboundEdges().getNode(msg.getHeader().getDestination()).getChannel();
//                        c.writeAndFlush(msg);
//                    } else if (state.getEmon().getOutboundEdges().hasNode(msg.getHeader().getDestination())) {
//                        Channel c = state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination()).getChannel();
//                        c.writeAndFlush(msg);
//                    } else {
//                        forwardToAll(msg, state,false,msg.getHeader().getNodeId());
//                    }
                }
            }
        }catch (Exception e){
            System.out.println("Exception from Vote MSg");
        }
    }

    public Work.WorkMessage createVoteRespMsg(Work.WorkMessage msg) {

        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Work.VoteMsg.Builder vm = Work.VoteMsg.newBuilder();
        vm.setState(sb);
        vm.setVtype(Work.VoteMsg.VoteMsgType.VOTERES);
        vm.setTerm(msg.getVote().getTerm());

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(msg.getHeader().getNodeId());
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setVote(vm);
        wb.setSecret(123);

        return wb.build();

    }
}
