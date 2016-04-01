package gash.router.server.Election;

/**
 * Created by Student on 3/31/16.
 */
public class LeaderStatus {

    private int CurLeader;
    private String LeaderHost;
    private LEADER_STATE leader_state;

    public LeaderStatus() {
        leader_state = LEADER_STATE.LEADERUNKNOWN;
        LeaderHost = "";
        CurLeader = -1;
    }

    public enum LEADER_STATE {
        LEADERUNKNOWN,
        LEADERALIVE,
        LEADERDEAD
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

    public LEADER_STATE getLeader_state() {
        return leader_state;
    }

    public void setLeader_state(LEADER_STATE leader_state) {
        this.leader_state = leader_state;
    }
}
