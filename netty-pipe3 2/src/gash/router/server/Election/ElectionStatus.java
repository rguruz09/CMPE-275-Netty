package gash.router.server.Election;

/**
 * Created by Student on 3/31/16.
 */
public class ElectionStatus {


    private  int Term;
    private int VoteCt;
    private int Quorum;
    private NODE_STATUS status;

    public ElectionStatus() {
        Term = 0;
        VoteCt = 0;
        Quorum = 3;
        status = NODE_STATUS.FOLLOWER;
    }

    public int getTerm() {
        return Term;
    }

    public void setTerm(int term) {
        Term = term;
    }

    public int getVoteCt() {
        return VoteCt;
    }

    public void setVoteCt(int voteCt) {
        VoteCt = voteCt;
    }

    public int getQuorum() {
        return Quorum;
    }

    public void setQuorum(int quorum) {
        Quorum = quorum;
    }

    public NODE_STATUS getStatus() {
        return status;
    }

    public void setStatus(NODE_STATUS status) {
        this.status = status;
    }

    public enum NODE_STATUS  {
        FOLLOWER, CANDIDATE, LEADER
    }


}
