package gash.router.server.workHandlers;

import com.google.protobuf.ByteString;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import gash.router.client.ClientHealper;
import gash.router.client.MessageClient;
import gash.router.server.CommandHandlers.*;
import gash.router.server.Election.CommonUtils;
import gash.router.server.ServerState;
import gash.router.server.Storage.MongoUtils;
import gash.router.server.Storage.chunks;
import gash.router.server.Storage.metadata;
import gash.router.server.edges.EdgeInfo;
import io.netty.channel.Channel;
import io.netty.util.internal.SystemPropertyUtil;
import pipe.common.Common;
import pipe.work.Work;
import routing.Pipe;
import storage.Storage;
import java.util.Arrays;

/**
 * Created by Student on 4/3/16.
 */
public class WorkCmdMsg {

    public void handleWorkCmdMsg(Work.WorkMessage msg, ServerState state, Channel channel){

        MongoUtils mongoUtils = new MongoUtils();
        System.out.println("If you Leader Handle it else forward it to Leader");
        if(msg.getHeader().getDestination() == state.getConf().getNodeId()){

            if(msg.getCommand().hasQuery()){
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
                        if(mongoUtils.addMetaData(md)){
                            System.out.println("Metadata chunk is added to DATABASE..");
                           // SendResponse(state,msg,null,true);
                        }else {
                            System.out.println("Metadata chunk couldnt be added to  DATABASE..");
                            //SendResponse(state,msg,null,false);
                        }
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

                        if(mongoUtils.addChunk(ch,msg.getCommand().getQuery().getMetadata().getFname())){
                            SendResponse(state,msg,null,true, channel);
                        }else {
                            //response err
                            SendResponse(state,msg,null,false, channel);
                        }
                    }
                }else if(msg.getCommand().getQuery().getAction() == Storage.Action.GET){
                    System.out.println("retrieve the data");
                    String fname = msg.getCommand().getQuery().getMetadata().getFname();

                    DBObject result = mongoUtils.findResource(fname);
                    if(result == null){
                        System.out.println("Invalid file name");
                    } else {
                        System.out.println("File found");
                        DBCursor cursor = mongoUtils.getAllChunks(result);
                        buildResDate(state,msg,cursor, channel);
                    }
                }
            }else if(msg.getCommand().hasResponse()) {
                System.out.println("Sending response to Command Server from Leade***********************************************************************************************************************************************************************************************************r"+ state.getCmdChannel());
                Pipe.CommandMessage cm = CommandsUtils.buildCommandMsgFromWork(msg,state);
               // state.getCmdChannel().writeAndFlush(cm);
                channel.writeAndFlush(cm);
            }
        }else{

            if(state.getEmon().getOutboundEdges().hasNode(msg.getHeader().getDestination())) {
                if (state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination()).getChannel().isActive()) {
                    System.out.println("Forwording..found in outbound list");
                    state.getEmon().getOutboundEdges().getNode(msg.getHeader().getDestination()).getChannel().writeAndFlush(msg);
                }
            }
        }

    }

    public void SendResponse(ServerState state, Work.WorkMessage msg, byte [] data, boolean sts, Channel channel){
        Common.Header.Builder Hb = ClientHealper.getHeader(state.getConf().getNodeId(), msg.getHeader().getNodeId(), CommonUtils.MAX_HOPS);

        Work.WorkMessage.Builder wb = Work.WorkMessage.newBuilder();
        wb.setHeader(Hb);
        wb.setSecret(123);

        Storage.Response.Builder rb = CommonUtils.CreateCmdRes(msg, sts, msg.getCommand().getQuery().getSequenceNo());
        if(data != null){
            rb.setData(ByteString.copyFrom(data));
        }
        rb.setMetaData(msg.getCommand().getQuery().getMetadata());
        Work.Command.Builder cmd = Work.Command.newBuilder();
        cmd.setResponse(rb);
        wb.setCommand(cmd);

        CommandsUtils.sendToLeader(wb.build(),state, channel);

    }

    public void buildResDate(ServerState state, Work.WorkMessage msg, DBCursor cursor, Channel channel){

        if(!cursor.hasNext()){
            SendResponse(state,msg,null,false, channel);
        }else {

            Common.Header.Builder hb = ClientHealper.getHeader(state.getConf().getNodeId(),msg.getHeader().getNodeId(),CommonUtils.MAX_HOPS);

            Storage.Metadata.Builder mb = ClientHealper.getMetadata(cursor.size(),1024,msg.getCommand().getQuery().getMetadata().getFname(),
                    msg.getCommand().getQuery().getMetadata().getFiletype(),
                    msg.getCommand().getQuery().getMetadata().getUid());

            Storage.Response.Builder rs = Storage.Response.newBuilder();
            rs.setMetaData(mb);
            rs.setSequenceNo(0);
            rs.setSuccess(true);
            rs.setAction(msg.getCommand().getQuery().getAction());

            Work.Command.Builder cmd = Work.Command.newBuilder();
            cmd.setResponse(rs);


            Work.WorkMessage.Builder wm = Work.WorkMessage.newBuilder();
            wm.setHeader(hb);
            wm.setSecret(123);

            wm.setCommand(cmd);

            CommandsUtils.sendToLeader(wm.build(),state, channel);

            DBObject result = null;
            byte[] data;
            int i = 1;
            while(cursor.hasNext()) {

                System.out.println("Sending the chunk number!!!!!!!!!!!!!!!!!!!!! "+i);

                result = cursor.next();
                data = result.get("data").toString().getBytes();

                Work.WorkMessage.Builder wor = Work.WorkMessage.newBuilder();
                wor.setHeader(hb);

                Storage.Response.Builder res = Storage.Response.newBuilder();
                res.setSequenceNo(i);
                res.setSuccess(true);
                res.setData(ByteString.copyFrom(data));
                res.setAction(msg.getCommand().getQuery().getAction());
                wor.setSecret(123);

                Work.Command.Builder cd = Work.Command.newBuilder();
                cd.setResponse(res);

                wor.setCommand(cd);

                CommandsUtils.sendToLeader(wor.build(),state, channel);
                i++;
            }
        }
    }
}
