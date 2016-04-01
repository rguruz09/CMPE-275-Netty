package gash.router.server.workHandlers;

import gash.router.server.Election.ElectionStatus;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pipe.common.Common;
import pipe.election.Election;
import pipe.work.Work;

/**
 * Created by vinay on 4/1/16.
 */
    public class VoteMsg {

    protected static Logger logger = LoggerFactory.getLogger("Vote");
    private ServerState state;

    public VoteMsg(ServerState state) {
        this.state = state;
    }

    public void handleVoteMsg(Work.WorkMessage msg, Channel channel){

        logger.info("Vote msg from " + msg.getHeader().getNodeId());
        if(msg.getVote().getVtype() == Work.VoteMsg.VoteMsgType.VOTEREQ &&
                msg.getVote().getTerm() > state.getElectionMonitor().getElectionStatus().getTerm() ) {

            forwardToAll(msg);
            Work.WorkMessage wm = createVoteRespMsg(msg);
            channel.writeAndFlush(wm);
        }

        else if(msg.getVote().getVtype() == Work.VoteMsg.VoteMsgType.VOTERES){

            // 1. CHeck if the response for me
            if(msg.getHeader().getNodeId() == state.getConf().getNodeId()) {
                // If Im the candidate process the response else not
                if(state.getElectionMonitor().getElectionStatus().getStatus() == ElectionStatus.NODE_STATUS.CANDIDATE){
                    state.getElectionMonitor().getElectionStatus().setVoteCt(state.getElectionMonitor().getElectionStatus().getVoteCt()+1);
                    // Check if i got majority
                    if(state.getElectionMonitor().getElectionStatus().getVoteCt() >= state.getElectionMonitor().getElectionStatus().getQuorum()){
                        System.out.println("Majority votes received: I will be the new leader: Node "+ msg.getHeader().getNodeId());
                        Work.WorkMessage wm = LeaderMsg.createLeaderRespMsg(-1);
                        forwardToAll(wm);
                    }
                }
            }else {
                // 2. If not for me send it to appropriate node.
                if(state.getEmon().getInboundEdges().hasNode(msg.getHeader().getNodeId())){
                    Channel c = state.getEmon().getInboundEdges().getNode(msg.getHeader().getNodeId()).getChannel();
                    c.writeAndFlush(msg);
                }else if(state.getEmon().getOutboundEdges().hasNode(msg.getHeader().getNodeId())){
                    Channel c = state.getEmon().getOutboundEdges().getNode(msg.getHeader().getNodeId()).getChannel();
                    c.writeAndFlush(msg);
                }else {
                    forwardToAll(msg);
                }
            }
        }

    }

    public void forwardToAll(Work.WorkMessage msg){
        for (EdgeInfo ei : state.getEmon().getOutboundEdges().getAllNodes().values()) {
            if (ei.isActive() && ei.getChannel() != null) {
                ei.getChannel().writeAndFlush(msg);
            }
        }
        for (EdgeInfo ei : state.getEmon().getInboundEdges().getAllNodes().values()) {
            if (ei.isActive() && ei.getChannel() != null) {
                ei.getChannel().writeAndFlush(msg);
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
        wb.setSecret(123);

        return wb.build();

    }
}
