package gash.router.server.Storage;

/**
 * Created by Student on 4/4/16.
 */
public class chunks {

    private String ID;
    private String metaID;
    private int seqNum;
    private byte[] data;
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMetaID() {
        return metaID;
    }

    public void setMetaID(String metaID) {
        this.metaID = metaID;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
