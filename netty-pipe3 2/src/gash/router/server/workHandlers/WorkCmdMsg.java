package gash.router.server.workHandlers;

import com.google.protobuf.ByteString;
import gash.router.client.MessageClient;
import gash.router.server.Election.CommonUtils;
import gash.router.server.ServerState;
import gash.router.server.Storage.MongoUtils;
import gash.router.server.Storage.chunks;
import gash.router.server.Storage.metadata;
import pipe.work.Work;
import storage.Storage;

import java.util.Arrays;

/**
 * Created by Student on 4/3/16.
 */
public class WorkCmdMsg {

    public void handleWorkCmdMsg(Work.WorkMessage msg, ServerState state){

        MongoUtils mongoUtils = new MongoUtils();
        System.out.println("If you Leader Handle it else forward it to Leader");
        if(msg.getHeader().getDestination() == state.getConf().getNodeId()){
            System.out.println("Process It");

            if(msg.getCommand().getQuery().getAction() == Storage.Action.STORE){
                // check if its a meta chunk or data chunk
                if(msg.getCommand().getQuery().getSequenceNo() == 0){
                    System.out.println("Its a meta chunk");
                    metadata md = new metadata();
                    md.setFileName(msg.getCommand().getQuery().getMetadata().getFname());
                    md.setFileType(msg.getCommand().getQuery().getMetadata().getFiletype());
                    md.setTotalChunks(msg.getCommand().getQuery().getMetadata().getSeqSize());
                    md.setTotalSize(msg.getCommand().getQuery().getMetadata().getSize());
                    md.setUserID(msg.getCommand().getQuery().getMetadata().getUid());
                    md.setTime(msg.getCommand().getQuery().getMetadata().getTime());
                    String id = md.getUserID()+md.getFileName()+md.getFileType();
                    md.setPrimaryID( String.valueOf(id.hashCode()) );
                    mongoUtils.addMetaData(md);

                } else {
                    chunks ch = new chunks();

                    String metaid = msg.getCommand().getQuery().getMetadata().getUid() +
                            msg.getCommand().getQuery().getMetadata().getFname() +
                            msg.getCommand().getQuery().getMetadata().getFiletype();

                    ch.setMetaID(String.valueOf(metaid.hashCode()));

                    String id = msg.getCommand().getQuery().getSequenceNo()+
                            msg.getCommand().getQuery().getMetadata().getUid() +
                            msg.getCommand().getQuery().getMetadata().getFname() +
                            msg.getCommand().getQuery().getMetadata().getFiletype();

                    ch.setID(String.valueOf(id.hashCode()));

                    ch.setTime(msg.getCommand().getQuery().getMetadata().getTime());
                    ch.setSeqNum(msg.getCommand().getQuery().getSequenceNo());
                    ch.setData(msg.getCommand().getQuery().getData().toByteArray());

                    mongoUtils.addChunk(ch);

                }
            }else if(msg.getCommand().getQuery().getAction() == Storage.Action.GET){
                System.out.println("retrieve the data");
                String fname = msg.getCommand().getQuery().getMetadata().getFname();

                if(!mongoUtils.findResource(fname)){
                    System.out.println("Invalid file name");
                } else {
                    System.out.println("File found");

                }
            }





        }else{
            System.out.println("Forwording..");
            if(state.getEmon().getInboundEdges().hasNode(msg.getHeader().getDestination())){
                if(state.getEmon().getInboundEdges().getNode(msg.getHeader().getDestination()).getChannel().isActive()){
                    System.out.println("Forwording..found in Inbound list");
                    state.getEmon().getInboundEdges().getNode(msg.getHeader().getDestination()).getChannel().writeAndFlush(msg);
                }
            } else if(state.getEmon().getOutboundEdges().hasNode(msg.getHeader().getDestination())){
                if(state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination()).getChannel().isActive()){
                    System.out.println("Forwording..found in outbound list");
                    state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination()).getChannel().writeAndFlush(msg);
                }
            } else {
                System.out.println("Forwording to ALL.....");
                CommonUtils.forwardToAll(msg,state,false,msg.getHeader().getNodeId());
            }
        }
    }
}
