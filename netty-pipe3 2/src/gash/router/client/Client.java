/**
 * Copyright 2016 Gash.
 *
 * This file and intellectual content is protected under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package gash.router.client;

import gash.router.client.CommConnection;
import gash.router.client.CommListener;
import gash.router.client.MessageClient;
import routing.Pipe.CommandMessage;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;


/**
 * Created by Richa on 3/31/16.
 */
public class Client implements CommListener {
    private MessageClient mc;

    public Client(MessageClient mc) {
        init(mc);
    }

    private void init(MessageClient mc) {
        this.mc = mc;
        this.mc.addListener(this);
    }


    @Override
    public String getListenerID() {

        return "demo";
    }

    @Override
    public void onMessage(CommandMessage msg) {

        System.out.println("Reply from Server " + msg);
    }

    public static byte[][] chunkData(byte[] buffer, int chunksize) {


        byte[][] ret = new byte[(int)Math.ceil(buffer.length / (double)chunksize)][chunksize];

        int start = 0;

        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(buffer,start, start + chunksize);
            start += chunksize ;
        }

        return ret;
    }
    /**
     * sample application (client) use of our messaging service
     *
     * @param args
     */
    public static void main(String[] args) {
        //System.out.println(args[1]);
       // String host = "127.0.0.1";
       // int port = 4568;
            try {
                //MessageClient mc = new MessageClient("127.0.0.1",4568);
            //    MessageClient mc = new MessageClient(host, port);
                MessageClient mc = new MessageClient(args[0], Integer.parseInt(args[1]));
                Client cl = new Client(mc);
                //System.out.println("Message to send to server is : "+ args[2]);
                // do stuff w/ the connection
                //System.out.println("Sending message to server....." );
                //mc.sendMessage(args[2]);
                Scanner sc = new Scanner(System.in);
                //String ch = "y";
                while(true){
                    System.out.println("Enter your choice : ");
                    System.out.println("1. Send message to Server");
                    System.out.println("2. Send Image to Server");
                    System.out.println("3. Send File/Document to Server");
                    System.out.println("4. Get Data from Server");
                    //System.out.println("5. Get Image from Server");
                    //System.out.println("6. Get File/Document from Server");
                    System.out.println("5. Exit");

                    int choice = sc.nextInt();
                    String id;
                    String filename;
                    if (choice == 1) {
                        System.out.println("Enter Message to send to server....");
                        sc.nextLine();
                        mc.sendMessage(sc.nextLine());
                        System.out.println("Sending message to server.....");
                        System.out.flush();
                        System.out.println("Message sent successfully to server...");
                        //Thread.sleep(10 * 1000);
                    } else if (choice == 2) {
                        System.out.println("Enter client ID..");
                        id= sc.nextLine();
                        sc.nextLine();
                        System.out.println("Enter Filename to store..");
                        filename = sc.nextLine();
                        //sc.nextLine();
                        FileInputStream fileInputStream=null;
                        System.out.println("Sending Image to server.....");
                        File file = new File("/Users/Rii/Documents/Cmpe275/lab1/fluffy/netty_mongo/test.jpg");
                        int size = (int) file.length();
                        byte[] buffer = new byte[size];
                        fileInputStream = new FileInputStream(file);
                        fileInputStream.read(buffer);
                        //byte [] imageByte = buffer;
                        fileInputStream.close();
                        byte [][] chunks = chunkData(buffer,1024000);
                        int i=0;
                        while(i< chunks.length) {
                            mc.sendImage(chunks[i],i+1,id,filename);
                            i++;
                        }
                        System.out.println("Number of chunks is : "+i);
                        System.out.flush();
                        System.out.println("Image sent successfully to server...");
                        //Thread.sleep(10 * 1000);
                    } else if (choice == 3) {
                        //System.out.println("Enter client ID..");
                        //id= sc.nextInt();
                        System.out.println("Enter client ID..");
                        id= sc.nextLine();
                        sc.nextLine();
                        System.out.println("Enter Filename to store..");
                        filename = sc.nextLine();
                        FileInputStream fileInputStream=null;
                        System.out.println("Sending File/Document to server.....");
                        File file = new File("/Users/Rii/Documents/Cmpe275/lab1/fluffy/netty_mongo/test.jpg");
                        int size = (int) file.length();
                        byte[] buffer = new byte[size];
                        fileInputStream = new FileInputStream(file);
                        fileInputStream.read(buffer);
                        byte [][] chunks = chunkData(buffer,1024000);
                        int i=0;
                        while(i< chunks.length) {
                            mc.sendFile(chunks[i],i+1,id,filename);
                            i++;
                        }
                        //mc.sendFile(buffer);
                        System.out.flush();
                        System.out.println("File sent successfully to server...");
                    } else if (choice == 4) {
                        System.out.println("Enter Client ID");
                        id = sc.nextLine();
                        sc.nextLine();
                        System.out.println("Enter Filename");
                        String name = sc.nextLine();
                        mc.getData(id,name);
                        System.out.flush();
                    }else if(choice == 5){
                        System.out.println("Terminating Connection...");
                        CommConnection.getInstance().release();
                        break;
                    }
                    System.out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                //CommConnection.getInstance().release();
                System.out.println("Client connection terminated from the server...");
            }

    }
}
