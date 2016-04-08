package gash.router.server.Election;

import gash.router.server.ServerState;
import gash.router.server.edges.EdgeInfo;
import pipe.common.Common;
import pipe.election.Election;
import pipe.work.Work;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static gash.router.server.Election.CommonUtils.*;

/**
 * Created by Raghu on 3/31/16.
 */

public class ElectionMonitor implements Runnable{

    private ServerState state;
    private long lastHBReceived;
    private ElectionStatus electionStatus;
    private LeaderStatus leaderStatus;
    private HashMap<Integer, FollowerInfo> followers;

    public ElectionMonitor(ServerState state) {

        if (state == null){
            throw new RuntimeException("state is null");
        }
        this.state = state;
        lastHBReceived = System.currentTimeMillis();
        electionStatus = new ElectionStatus();
        leaderStatus = new LeaderStatus();
        followers = new HashMap<Integer, FollowerInfo>();
    }

    public HashMap<Integer, FollowerInfo> getFollowers() {
        return followers;
    }

    public void setFollowers(HashMap<Integer, FollowerInfo> followers) {
        this.followers = followers;
    }

    public void addNewFollowers(int key, FollowerInfo followerInfo){
        System.out.println("Adding new follower ---- "+ key);
        followers.put(key, followerInfo);
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
            //Thread.sleep(state.getConf().getHeartbeatDt());

            while (true){

                //if leader, send the HB to its nodes
                System.out.println("The current leader is NODE "+getLeaderStatus().getCurLeader());
                if(electionStatus.getStatus() == ElectionStatus.NODE_STATUS.LEADER){

                    System.out.println("I'm the leader, Sending HB msg to my followers..");
                    Work.WorkMessage wm = CreateGenericHBReqMsg(state, Work.HbType.LEADERREQ);
                    forwardToAll(wm,state,true,-1);
                    //Thread.sleep(2000);

                    if(!updatefollowers()){
                        electionStatus.setStatus(ElectionStatus.NODE_STATUS.FOLLOWER);
                        leaderStatus.setCurLeader(-1);
                        leaderStatus.setLeader_state(Election.LeaderStatus.LeaderState.LEADERDEAD);
                    }
                    followers.clear();

                }else {
                    System.out.println(" Check the last HB timer.. ");

                    if((System.currentTimeMillis() - lastHBReceived ) > state.getConf().getHeartbeatDt()){
                        //Reset the Followers Hmap
                        Work.WorkMessage wb;

                        if(leaderStatus.getLeader_state() == Election.LeaderStatus.LeaderState.LEADERUNKNOWN){
                            //     Query for  Leader
                            wb =  createLeaderQueryMsg();
                        } else  {
                            // Election process
                            System.out.println("No HB from leader.. ill be the candidate");
                            if(electionStatus.getStatus() != ElectionStatus.NODE_STATUS.CANDIDATE){
                                electionStatus.setVoteCt(0);
                                electionStatus.setStatus(ElectionStatus.NODE_STATUS.CANDIDATE);
                                electionStatus.setVoteCt(electionStatus.getVoteCt()+1);
                                electionStatus.setTerm(electionStatus.getTerm()+1);
                                leaderStatus.setLeader_state(Election.LeaderStatus.LeaderState.LEADERDEAD);
                            }
                            //System.out.println("Leader state is "+leaderStatus.getLeader_state());
                            wb = createVoteReqMsg();
                        }

                        sendMessageToEveryone(state,wb);

                    }
                    Thread.sleep(2000);
                }

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
		hb.setMaxHops(CommonUtils.MAX_HOPS);

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
        vm.setTerm(electionStatus.getTerm());

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setNodeId(state.getConf().getNodeId());
        hb.setDestination(-1);
        hb.setTime(System.currentTimeMillis());
        hb.setMaxHops(CommonUtils.MAX_HOPS);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(hb);
        wb.setSecret(123);
        wb.setVote(vm);
        return wb.build();
    }

    public boolean updatefollowers(){

        int numActive = 0;
        Iterator it = followers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            FollowerInfo fi = (FollowerInfo) pair.getValue();
            System.out.println("Follower nodes are "+fi.getNodeId());
            if(System.currentTimeMillis() - fi.getLastHBResp() > 3){
                System.out.println("Node is inactive NODE "+fi.getNodeId());
                fi.setActive(false);
                fi.setVoted(false);
            }else {
                System.out.println("Node is active NODE "+fi.getNodeId());
                fi.setActive(true);
                //fi.setVoted();
                numActive++;
            }
            it.remove(); // avoids a ConcurrentModificationException
        }

        if (numActive < electionStatus.getQuorum())
            return false;
        return true;

    }
}
