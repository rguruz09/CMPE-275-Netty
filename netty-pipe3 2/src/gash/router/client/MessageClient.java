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

import com.google.protobuf.ByteString;
import pipe.common.Common.Header;
import storage.Storage;
import routing.Pipe.CommandMessage;

/**
 * front-end (proxy) to our service - functional-based
 * 
 * @author gash
 * 
 */
public class MessageClient {
	// track requests
	private long curID = 0;

	public MessageClient(String host, int port) {
		init(host, port);
	}

	private void init(String host, int port) {
		CommConnection.initConnection(host, port);
	}

	public void addListener(CommListener listener) {
		CommConnection.getInstance().addListener(listener);
	}

	public void ping() {
		// construct the message to send

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(999);
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(-1);

		CommandMessage.Builder rb = CommandMessage.newBuilder();
		rb.setHeader(hb);
		rb.setPing(true);

		try {
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(String message) {

		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(12);
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(-1);

		CommandMessage.Builder rb = CommandMessage.newBuilder();
		rb.setHeader(hb);
		rb.setMessage(message);

		try {
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean sendGenFile(byte[][] data, int size, String id, String fname, String ftype, long fsize){

		Header.Builder hb = ClientHealper.getHeader(999,-1,4);
		Storage.Metadata.Builder mb = ClientHealper.getMetadata(data.length,fsize,fname,ftype,id);

		System.out.println("Client USER ID - "+ mb.getUid());

		Storage.Query.Builder qb = ClientHealper.getQuery(0, Storage.Action.STORE,mb);
		CommandMessage.Builder cb = CommandMessage.newBuilder();

		cb.setQuery(qb);
		cb.setHeader(hb);

		// to send the metadata
		ClientHealper.sendServrReq(this, cb);
		int i=0;
		//Sending all the chunks
		for (i = 0; i < data.length; i++) {

			Storage.Query.Builder query = ClientHealper.getQuery(i+1, Storage.Action.STORE,mb);
			query.setMetadata(mb);
			query.setData(ByteString.copyFrom(data[i]));

			CommandMessage.Builder cm = CommandMessage.newBuilder();
			cm.setHeader(hb);
			cm.setQuery(query);
			ClientHealper.sendServrReq(this, cm);

		}
		if(i>=data.length){
			System.out.println("Image Sent Successfully");
			return true;

		}else{
			System.out.println("Failed to send the image");
			return false;
		}
	}

	public void getGenFile(String id,String name){

		Header.Builder hb = ClientHealper.getHeader(999,-1,4);
		Storage.Metadata.Builder mb = Storage.Metadata.newBuilder();
		mb.setFname(name);
		mb.setUid(id);
		mb.setSeqSize(1);

		Storage.Query.Builder qb = ClientHealper.getQuery(0, Storage.Action.GET,mb);
		CommandMessage.Builder cb = CommandMessage.newBuilder();
		cb.setQuery(qb);
		cb.setHeader(hb);

		ClientHealper.sendServrReq(this, cb);

	}

	public void sendImage(byte[] image,int seq, String clientId, String filename) {

		Header.Builder hb = Header.newBuilder();
		System.out.println("Inside send Image method of message client...");
		hb.setNodeId(999);
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(-1);

		Storage.Query.Builder qb = Storage.Query.newBuilder();
		qb.setAction(Storage.Action.STORE);
		qb.setData(ByteString.copyFrom(image));
		qb.setSequenceNo(seq);
		qb.setKey(clientId);
		//qb.setFilename(filename);
		//qb.setData(image);
		CommandMessage.Builder rb = CommandMessage.newBuilder();
		rb.setHeader(hb);
		//rb.setPing(true);
		rb.setQuery(qb);

		try {
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendFile(byte[] file,int seq,String clientId, String filename) {
		// construct the message to send
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(12);
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(-1);

		Storage.Query.Builder qb = Storage.Query.newBuilder();
		qb.setAction(Storage.Action.STORE);
		qb.setData(ByteString.copyFrom(file));
		qb.setSequenceNo(seq);
		qb.setKey(clientId);
		//qb.setFilename(filename);

		CommandMessage.Builder rb = CommandMessage.newBuilder();
		rb.setHeader(hb);
		//rb.setPing(true);
		rb.setQuery(qb);
		//rb.setMessage(image.toString());

		try {

			// using queue
			CommConnection.getInstance().enqueue(rb.build());
			//CommConnection.getInstance().enqueue("hello from client");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getData(String id,String name){
		Header.Builder hb = Header.newBuilder();
		hb.setNodeId(999);
		hb.setTime(System.currentTimeMillis());
		hb.setDestination(-1);

		Storage.Response.Builder res = Storage.Response.newBuilder();
		res.setAction(Storage.Action.GET);
		//res.setFilename(filename);
		//qb.setData(ByteString.copyFrom(image));

		CommandMessage.Builder rb = CommandMessage.newBuilder();
		rb.setHeader(hb);
		rb.setResponse(res);

		try {
			CommConnection.getInstance().enqueue(rb.build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void release() {
		CommConnection.getInstance().release();
	}

	/**
	 * Since the service/server is asychronous we need a unique ID to associate
	 * our requests with the server's reply
	 * 
	 * @return
	 */
	private synchronized long nextId() {
		return ++curID;
	}
}
