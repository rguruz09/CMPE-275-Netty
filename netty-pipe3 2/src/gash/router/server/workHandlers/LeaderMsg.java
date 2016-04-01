package gash.router.server.workHandlers;

import gash.router.server.Election.LeaderStatus;
import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import io.netty.channel.Channel;
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
        logger.info("Leadermsg from " + msg.getHeader().getNodeId());
        System.out.println("LeaderMsg");

        if(false){
            // if im the sender, dont care
        }else{
            if (msg.getLeader().getAction() == Election.LeaderStatus.LeaderQuery.WHOISTHELEADER) {
                Work.WorkMessage wm = createLeaderRespMsg(msg.getHeader().getNodeId());
                channel.writeAndFlush(wm);

            } else if(msg.getLeader().getAction() == Election.LeaderStatus.LeaderQuery.THELEADERIS){

                if(msg.getLeader().getTerm() >= state.getElectionMonitor().getElectionStatus().getTerm()){
                    if(msg.getLeader().getState() == Election.LeaderStatus.LeaderState.LEADERDEAD ||
                            msg.getLeader().getState() == Election.LeaderStatus.LeaderState.LEADERUNKNOWN){
                        state.getElectionMonitor().getLeaderStatus().setLeader_state(Election.LeaderStatus.LeaderState.LEADERDEAD);
                    }else {
                        state.getElectionMonitor().getLeaderStatus().setLeader_state(Election.LeaderStatus.LeaderState.LEADERALIVE);
                        state.getElectionMonitor().getLeaderStatus().setCurLeader(msg.getLeader().getLeaderId());
                        state.getElectionMonitor().getLeaderStatus().setLeaderHost(msg.getLeader().getLeaderHost());
                        state.getElectionMonitor().getElectionStatus().setTerm(msg.getLeader().getTerm());
                    }
                    // if its a broadcast msg fwd it to all
                    if(msg.getHeader().getDestination() == -1){
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
                } else {
                    System.out.println("Old leader");
                }
            }
        }
    }

    public static Work.WorkMessage createLeaderRespMsg(int dest) {

        Election.LeaderStatus.Builder ls = Election.LeaderStatus.newBuilder();
        ls.setAction(Election.LeaderStatus.LeaderQuery.THELEADERIS);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(dest);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(4);

        ls.setLeaderHost(state.getElectionMonitor().getLeaderStatus().getLeaderHost());
        ls.setLeaderId(state.getElectionMonitor().getLeaderStatus().getCurLeader());
        ls.setState(state.getElectionMonitor().getLeaderStatus().getLeader_state());
        ls.setTerm(state.getElectionMonitor().getElectionStatus().getTerm());

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setLeader(ls);

        wb.setSecret(123);

        return wb.build();
    }


}
