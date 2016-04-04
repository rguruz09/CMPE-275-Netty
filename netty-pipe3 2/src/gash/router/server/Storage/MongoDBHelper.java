////import java.io.File;
////import java.io.IOException;
////import java.net.UnknownHostException;
//import com.mongodb.*;
//
//import java.io.*;
//
//import com.mongodb.gridfs.GridFS;
//import com.mongodb.gridfs.GridFSDBFile;
//import com.mongodb.gridfs.GridFSInputFile;
//
///**
// * Java MongoDB : Save image
// *
// */
//
//public class MongoDBHelper {
//
//    private MongoClient mongoClient;
//
//    public MongoDBHelper() {
//        MongoClientURI uri = new MongoClientURI("mongodb://Richa:Indore#1@ds041934.mlab.com:41934/assignment_2_db");
//        mongoClient = new MongoClient(uri);
//    }
//
//    public MongoClient getMongoClient() {
//        return mongoClient;
//    }
//
//    //This method will store the image from client to MongoDB
//    public String storeImageBytes(String clientId, String filename, byte[] imageBytes) throws Exception {
//        DB db = mongoClient.getDB("assignment_2_db");
//        GridFS fs = new GridFS(db, "Netty-images");
//        GridFSInputFile in = fs.createFile(imageBytes);
//        filename +=clientId;
//        in.setFilename(filename);
//        in.save();
//        return "Image Successfully stored to MongoDB";
//    }
//
//    //This method will get the image from  MongoDB to client
//    public InputStream getImage(String clientId, String filename) {
//        //MongoClientURI uri = new MongoClientURI("mongodb://Richa:Indore#1@ds041934.mlab.com:41934/assignment_2_db");
//        //MongoClient client = new MongoClient(uri);
//
//        DB db = mongoClient.getDB("assignment_2_db");
//        GridFS fs = new GridFS(db, "Netty-images");
//        filename+=clientId;
//        GridFSDBFile out = fs.findOne(new BasicDBObject("filename", filename));
//        if(out==null){
//            //System.out.println("Image not present : inside mongodbhandler");
//            return null;
//        }
//        return out.getInputStream();
//    }
//
//    public String storeFileBytes(String clientId, String filename, byte[] fileBytes) throws Exception {
//        DB db = mongoClient.getDB("assignment_2_db");
//        GridFS fs = new GridFS(db, "netty-files");
//        GridFSInputFile in = fs.createFile(fileBytes);
//        filename +=clientId;
//        in.setFilename(filename);
//        in.save();
//        return "File Successfully stored to MongoDB";
//    }
//
//    public String deleteImage(String clientId, String filename) {
//        DB db = mongoClient.getDB("assignment_2_db");
//        filename +=clientId;
//        GridFS fs = new GridFS(db, "Netty-images");
//        fs.remove(fs.findOne(filename));
//        return "Image successfully deleted";
//    }
//
//    public InputStream getFile(String clientId, String filename) {
//        //MongoClientURI uri = new MongoClientURI("mongodb://Richa:Indore#1@ds041934.mlab.com:41934/assignment_2_db");
//        //MongoClient client = new MongoClient(uri);
//
//        DB db = mongoClient.getDB("assignment_2_db");
//        GridFS fs = new GridFS(db, "netty-files");
//        filename+=clientId;
//        GridFSDBFile out = fs.findOne(new BasicDBObject("filename", filename));
//        return out.getInputStream();
//    }
//
//    public byte[] getFileBytes(String filePath) throws Exception {
//        File file = new File(filePath);
//        int size = (int) file.length();
//        byte[] buffer = new byte[size];
//        FileInputStream in = new FileInputStream(file);
//        in.read(buffer);
//        in.close();
//        return buffer;
//    }
//
//    public byte[] readFully(InputStream input) throws IOException
//    {
//        byte[] buffer = new byte[8192];
//        int bytesRead;
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        while ((bytesRead = input.read(buffer)) != -1)
//        {
//            output.write(buffer, 0, bytesRead);
//        }
//        return output.toByteArray();
//    }
//}
//
//
