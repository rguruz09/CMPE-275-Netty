package gash.router.client;

import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;
import storage.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

/**
 * Created by Raghavendra on 4/4/16.
 */

public class ClientHealper {

    private MessageClient mc;
    public ClientHealper(MessageClient mc){
        this.mc = mc;
    }


    //Chunking the data before it is sent to store.
    public void sendFileToServer(File file, String id){

        try {
            FileInputStream fileInputStream=null;
            int size = (int) file.length();
            byte[] buffer = new byte[size];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(buffer);
            //byte [] imageByte = buffer;
            fileInputStream.close();
            byte [][] chunks = chunkData(buffer,1024000);
            boolean success = mc.sendGenFile(chunks, size, id, file.getName(), file.getName().split("\\.")[1],file.getTotalSpace());
           // System.out.println("Number of chunks is : "+i);
            System.out.flush();
            if(!success){
                System.out.println("Resending the file");
                sendFileToServer(file, id);
            }
            System.out.println("Image sent successfully to server...");
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }
    }

    public byte[][] chunkData(byte[] buffer, int chunksize) {

        byte[][] ret = new byte[(int)Math.ceil(buffer.length / (double)chunksize)][chunksize];
        int start = 0;
        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(buffer,start, start + chunksize);
            start += chunksize ;
        }
        return ret;
    }

    public void retrieveFileFromServer(String id, String fname){
        mc.getGenFile(id,fname);
        System.out.flush();
    }

    public static Storage.Metadata.Builder getMetadata(int sq_size, long totSize, String fname, String ftype, String uid){

        Storage.Metadata.Builder mb = Storage.Metadata.newBuilder();

        mb.setFiletype(ftype);
        mb.setFname(fname);
        mb.setSeqSize(sq_size);
        mb.setSize(totSize);
        mb.setUid(uid);
        mb.setTime(System.currentTimeMillis());

        return mb;
    }

    public static Common.Header.Builder getHeader(int nodeId, int Dest, int maxhops){

        Common.Header.Builder hb = Common.Header.newBuilder();
        hb.setTime(System.currentTimeMillis());
        hb.setDestination(Dest);
        hb.setMaxHops(maxhops);
        hb.setNodeId(nodeId);

        return hb;
    }

    public static Storage.Query.Builder getQuery(int sq_num, Storage.Action action, Storage.Metadata.Builder mb){

        Storage.Query.Builder qb = Storage.Query.newBuilder();
        qb.setAction(action);
        if(mb != null)
            qb.setMetadata(mb);
        qb.setSequenceNo(sq_num);

        return qb;
    }

    public static void sendServrReq(MessageClient mc, Pipe.CommandMessage.Builder cb){
        try {
            CommConnection.getInstance().enqueue(cb.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
