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

    /**
     * sample application (client) use of our messaging service
     *
     * @param args
     */

    public static void main(String[] args) {

            try {
                if(args.length < 2){
                    System.out.println("Invalid number of Arguments");
                    System.out.println("Usage ->  client <Host> <Port>");
                    System.exit(1);
                }
                MessageClient mc = new MessageClient(args[0], Integer.parseInt(args[1]));
                Client cl = new Client(mc);
                Scanner sc = new Scanner(System.in);
                ClientHealper clientHealper = new ClientHealper(mc);

                while(true){
                    System.out.println("Enter your choice : ");
                    System.out.println("1. Ping Server");
                    System.out.println("2. Send message to Server");
                    System.out.println("3. Save Image/File/Document/Video to Server");
                    System.out.println("4. Retrieve Image/File/Document/Video from Server");
                    System.out.println("5. Exit");

                    int choice = sc.nextInt();
                    String id;
                    String filename;

                    switch (choice){

                        case 1:
                            mc.ping();
                            break;
                        case 2:
                            System.out.println("Enter Message to send to server....");
                            sc.nextLine();
                            mc.sendMessage(sc.nextLine());
                            System.out.println("Sending message to server.....");
                            System.out.flush();
                            System.out.println("Message sent successfully to server...");
                            break;
                        case 3:

                           sc.nextLine();
                            System.out.println("Enter client ID..");
                            id= sc.nextLine();
                            System.out.println("Enter File Path which you want to save..");
                            filename = sc.nextLine();
                            File f = new File(filename);
                            if(f.exists() && !f.isDirectory()) {
                                clientHealper.sendFileToServer(f,id);
                            }else {
                                System.out.println("File Not exist.. try again");
                            }
                            break;
                        case 4:
                            sc.nextLine();
                            System.out.println("Enter client ID..");
                            id= sc.nextLine();
                            System.out.println("Enter File name.");
                            filename = sc.nextLine();
                            clientHealper.retrieveFileFromServer(id,filename);
                            break;
                        case 5:
                            System.out.println("Terminating Connection...");
                            CommConnection.getInstance().release();
                            System.exit(2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                System.out.println("Client connection terminated from the server...");
            }
    }
}
