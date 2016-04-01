package gash.router.server.workHandlers;

import gash.router.server.Election.LeaderStatus;
import gash.router.server.ServerState;
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
    private ServerState state;

    public LeaderMsg(ServerState state) {
        this.state = state;
    }


    public void handleLeaderMsg(Work.WorkMessage msg, Channel channel) {
        logger.info("Leadermsg from " + msg.getHeader().getNodeId());
        System.out.println("LeaderMsg");

        if (msg.getLeader().getAction() == Election.LeaderStatus.LeaderQuery.WHOISTHELEADER) {
            Work.WorkMessage wm = createLeaderRespMsg(msg.getHeader().getNodeId());
            channel.writeAndFlush(wm);

        }

    }

    public Work.WorkMessage createLeaderRespMsg(int dest) {

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


        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setLeader(ls);

        wb.setSecret(123);

        return wb.build();
    }
}
