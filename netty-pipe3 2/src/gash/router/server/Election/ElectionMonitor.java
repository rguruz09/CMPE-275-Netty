package gash.router.server.Election;

import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import pipe.common.Common;
import pipe.election.Election;
import pipe.work.Work;

/**
 * Created by Raghu on 3/31/16.
 */

public class ElectionMonitor implements Runnable{

    private ServerState state;
    private long lastHBReceived;
    private ElectionStatus electionStatus;
    private LeaderStatus leaderStatus;

    public ElectionMonitor(ServerState state) {

        if (state == null){
            throw new RuntimeException("state is null");
        }
        this.state = state;
        lastHBReceived = System.currentTimeMillis();
        electionStatus = new ElectionStatus();
        leaderStatus = new LeaderStatus();
    }

    public LeaderStatus getLeaderStatus() {
        return leaderStatus;
    }

    public void setLeaderStatus(LeaderStatus leaderStatus) {
        this.leaderStatus = leaderStatus;
    }

    public ElectionStatus getElectionStatus() {
        return electionStatus;
    }

    public void setElectionStatus(ElectionStatus electionStatus) {
        this.electionStatus = electionStatus;
    }

    public ServerState getState() {
        return state;
    }

    public void setState(ServerState state) {
        this.state = state;
    }

    public long getLastHBReceived() {
        return lastHBReceived;
    }

    public void setLastHBReceived(long lastHBReceived) {
        this.lastHBReceived = lastHBReceived;
    }


    @Override
    public void run() {

        try {
            System.out.println("Election monitor. going to sleep");
            Thread.sleep(10000);

            while (true){
                System.out.println(" Check the last HB timer.. ");

                if(electionStatus.getStatus() == ElectionStatus.NODE_STATUS.FOLLOWER &&  (System.currentTimeMillis() - lastHBReceived ) > state.getConf().getHeartbeatDt()){
                    System.out.println("No HB from leader.. ill be the candidate");

                    Work.WorkMessage wb;

                    if(leaderStatus.getLeader_state() == Election.LeaderStatus.LeaderState.LEADERUNKNOWN){
                        //     Query for  Leader

                        wb =  createLeaderQueryMsg();

                    } else {
                              // Election process
                        electionStatus.setStatus(ElectionStatus.NODE_STATUS.CANDIDATE);
                        electionStatus.setVoteCt(electionStatus.getVoteCt()+1);
                        electionStatus.setTerm(electionStatus.getTerm()+1);
                        leaderStatus.setLeader_state(Election.LeaderStatus.LeaderState.LEADERDEAD);

                        wb = createVoteReqMsg();

                    }

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
                Thread.sleep(state.getConf().getHeartbeatDt());
            }
        }
        catch (Exception e){
            System.out.println(e.getStackTrace());
        }

    }

    public Work.WorkMessage createLeaderQueryMsg(){

        Election.LeaderStatus.Builder ls = Election.LeaderStatus.newBuilder();
        ls.setAction(Election.LeaderStatus.LeaderQuery.WHOISTHELEADER);

        Common.Header.Builder hb = Common.Header.newBuilder();
		hb.setNodeId(state.getConf().getNodeId());
		hb.setDestination(-1);
		hb.setTime(System.currentTimeMillis());
		hb.setMaxHops(4);

		Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
		wb.setHeader(hb);
        wb.setLeader(ls);

        wb.setSecret(123);

        return wb.build();
    }


    public Work.WorkMessage createVoteReqMsg(){

        Work.WorkState.Builder sb = Work.WorkState.newBuilder();
        sb.setEnqueued(-1);
        sb.setProcessed(-1);

        Work.VoteMsg.Builder vm = Work.VoteMsg.newBuilder();
        vm.setState(sb);
        vm.setVtype(Work.VoteMsg.VoteMsgType.VOTEREQ);

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(-1);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(4);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setSecret(123);

        return wb.build();
    }
}
