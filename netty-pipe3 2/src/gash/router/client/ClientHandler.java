package gash.router.client;

import io.netty.channel.Channel;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import routing.Pipe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.concurrent.Exchanger;

/**
 * Created by Student on 4/4/16.
 */
public class ClientHandler {

    public static HashMap<Integer, byte[]> hashMap;

    int sq_id;
    int sq_siz;
    String filename;

    public ClientHandler(int siz, String fname){
        hashMap = new HashMap<Integer, byte[]>();
        sq_id = 0;
        sq_siz = siz;
        filename = fname;
    }

    public void rebuildData (Channel channel, Pipe.CommandMessage msg){
        if(msg.getResponse().getSequenceNo() == 0){
            System.out.println("meta  chunk. .clear the hash");
            hashMap.clear();
            //  hashMap.put(0,msg.getResponse().getData().toByteArray());
        }else {

            int sq_id = msg.getResponse().getSequenceNo();

            if(sq_id == sq_siz){
                System.out.println("last chunk");
                hashMap.put(sq_id,msg.getResponse().getData().toByteArray());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {

                    for (int i = 1; i <= hashMap.size(); i++) {
                        baos.write(hashMap.get(i));
                    }
                }catch (Exception ex){
                    System.out.println("Exception from IOStream");
                }

                createFile(baos.toByteArray());
            }
            hashMap.put(sq_id,msg.getResponse().getData().toByteArray());
        }
    }


    public void createFile(byte[] data){

        try {

            String path = "str/"+filename;
            File file = new File(path);

            file.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(path);
            outputStream.write(data);
            System.out.println("Saved to the disk..");
        }catch (Exception e){
            System.out.println("File not found exc");
        }
    }

}
