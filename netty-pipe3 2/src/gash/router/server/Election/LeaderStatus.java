package gash.router.server.Election;

import pipe.election.Election;

/**
 * Created by Student on 3/31/16.
 */
public class LeaderStatus {

    private int CurLeader;
    private String LeaderHost;
    private Election.LeaderStatus.LeaderState leader_state;

    public LeaderStatus() {
        leader_state = Election.LeaderStatus.LeaderState.LEADERUNKNOWN;
        LeaderHost = "";
        CurLeader = -1;
    }

    public int getCurLeader() {
        return CurLeader;
    }

    public void setCurLeader(int curLeader) {
        CurLeader = curLeader;
    }

    public String getLeaderHost() {
        return LeaderHost;
    }

    public void setLeaderHost(String leaderHost) {
        LeaderHost = leaderHost;
    }

    public Election.LeaderStatus.LeaderState getLeader_state() {
        return leader_state;
    }

    public void setLeader_state(Election.LeaderStatus.LeaderState leader_state) {
        this.leader_state = leader_state;
    }
}
