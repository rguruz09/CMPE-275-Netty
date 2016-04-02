package gash.router.server.workHandlers;

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

        logger.info("Vote msg from " + msg.getHeader().getNodeId());
        if (msg.getVote().getVtype() == Work.VoteMsg.VoteMsgType.VOTEREQ &&
                msg.getVote().getTerm() >= state.getElectionMonitor().getElectionStatus().getTerm()) {

            forwardToAll(msg,state,false);
            Work.WorkMessage wm = createVoteRespMsg(msg);
            channel.writeAndFlush(wm);
        } else if (msg.getVote().getVtype() == Work.VoteMsg.VoteMsgType.VOTERES) {

            // 1. CHeck if the response for me
            if (msg.getHeader().getDestination() == state.getConf().getNodeId()) {

                // If Im the candidate process the response else not

                //Check for redundent votes
                if (!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId()) ||
                       ! state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).isVoted()) {
                    if (state.getElectionMonitor().getElectionStatus().getStatus() == ElectionStatus.NODE_STATUS.CANDIDATE) {
                        state.getElectionMonitor().getElectionStatus().setVoteCt(state.getElectionMonitor().getElectionStatus().getVoteCt() + 1);
                        // Check if i got majority
                        if (state.getElectionMonitor().getElectionStatus().getVoteCt() >= state.getElectionMonitor().getElectionStatus().getQuorum()) {
                            System.out.println("Majority votes received: I will be the new leader: Node " + msg.getHeader().getNodeId());

                            state.getElectionMonitor().getElectionStatus().setStatus(ElectionStatus.NODE_STATUS.LEADER);
                            state.getElectionMonitor().getLeaderStatus().setLeaderHost(state.getConf().getSelfHost());
                            state.getElectionMonitor().getLeaderStatus().setCurLeader(state.getConf().getNodeId());
                            state.getElectionMonitor().getLeaderStatus().setLeader_state(Election.LeaderStatus.LeaderState.LEADERALIVE);

                            if(!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())){
                                addFollower(state,msg.getHeader().getNodeId());
                            }
                            state.getElectionMonitor().getFollowers().get(msg.getHeader().getNodeId()).setVoted(true);
                            Work.WorkMessage wm = LeaderMsg.createLeaderRespMsg(-1,Election.LeaderStatus.LeaderState.LEADERALIVE);
                            forwardToAll(wm,state,true);
                        }
                    }else if(state.getElectionMonitor().getElectionStatus().getStatus() == ElectionStatus.NODE_STATUS.LEADER){
                        if(!state.getElectionMonitor().getFollowers().containsKey(msg.getHeader().getNodeId())){
                            addFollower(state,msg.getHeader().getNodeId());
                        }
                    }
                } else {
                    System.out.println("Duplicate votes");
                }
            } else {
                // 2. If not for me send it to appropriate node.
                System.out.println("Forwarding vote msg from "+msg.getHeader().getNodeId()+" to "+msg.getHeader().getDestination());
                if (state.getEmon().getInboundEdges().hasNode(msg.getHeader().getDestination())) {
                    Channel c = state.getEmon().getInboundEdges().getNode(msg.getHeader().getDestination()).getChannel();
                    c.writeAndFlush(msg);
                } else if (state.getEmon().getOutboundEdges().hasNode(msg.getHeader().getDestination())) {
                    Channel c = state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination()).getChannel();
                    c.writeAndFlush(msg);
                } else {
                    forwardToAll(msg, state,false);
                }
            }
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
        hb.setMaxHops(4);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setVote(vm);
        wb.setSecret(123);

        return wb.build();

    }
}
