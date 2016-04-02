package gash.router.server.Election;

/**
 * Created by Student on 4/1/16.
 */
public class FollowerInfo {

    int nodeId;
    boolean isActive;
    long lastHBResp;
    boolean isVoted;

    public boolean isVoted() {
        return isVoted;
    }

    public void setVoted(boolean voted) {
        isVoted = voted;
    }

    public long getLastHBResp() {
        return lastHBResp;
    }

    public void setLastHBResp(long lastHBResp) {
        this.lastHBResp = lastHBResp;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
}
