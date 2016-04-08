package gash.router.server.Election;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.HashMap;

/**
 * Created by Raghu on 3/31/16.
 */
public class ElectionStatus {


    private  int Term;
    private int VoteCt;
    private int Quorum;
    private NODE_STATUS status;
    private HashMap<Integer, Boolean> voters;

    public ElectionStatus() {
        Term = 0;
        VoteCt = 0;
        Quorum = 2;
        status = NODE_STATUS.FOLLOWER;
        voters =  new HashMap<Integer, Boolean>();
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

    public HashMap<Integer, Boolean> getVoters() {
        return voters;
    }

    public void setVoters(HashMap<Integer, Boolean> voters) {
        this.voters = voters;
    }

    public void addVoter(int nodeId){
        voters.put(nodeId, true);
    }

    public void removeVoters(int nodeId){
        voters.remove(nodeId);
    }
}
