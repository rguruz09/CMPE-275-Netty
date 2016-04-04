//import com.mongodb.BasicDBObject;
//import com.mongodb.DB;
//import com.mongodb.gridfs.GridFS;
//import com.mongodb.gridfs.GridFSDBFile;
//import com.mongodb.gridfs.GridFSInputFile;
//
//import java.io.FileOutputStream;
//import java.io.InputStream;
//
///**
// * Created by Richa on 3/29/16.
// */
//public class Main {
//    public static void main(String[] args) throws Exception {
//        try {
//            MongoDBHelper mongoDBHelper = new MongoDBHelper();
//
//            // Load Image from file
//            byte[] imageBytes = mongoDBHelper.getFileBytes("/Users/Rii/Documents/Cmpe275/lab1/fluffy/netty_mongo/test.jpg");
//            byte[] fileBytes = mongoDBHelper.getFileBytes("/Users/Rii/Documents/Cmpe275/lab1/fluffy/netty_mongo/file.pdf");
//            // Store Image to DB
//            System.out.println("test filename "+mongoDBHelper.storeImageBytes("1234", "test", imageBytes));
//
//            // Fetch Image from DB
//            InputStream inputStream = mongoDBHelper.getImage("1234", "testtest");
//            if(inputStream==null){
//                System.out.println("testtest file "+"Image not present in Database");
//            }
//            FileOutputStream outputImage;
//            //Store Image to file
//            if(inputStream != null) {
//                outputImage = new FileOutputStream("/Users/Rii/Documents/Cmpe275/lab1/fluffy/netty_mongo/netty-image.jpg");
//                outputImage.write(mongoDBHelper.readFully(inputStream));
//                outputImage.close();
//            }
//
//            // Store File to DB
//            System.out.println(mongoDBHelper.storeFileBytes("1234", "file1", fileBytes));
//
//            // Fetch File from DB
//            inputStream = mongoDBHelper.getFile("1234", "file1");
//            outputImage = new FileOutputStream("/Users/Rii/Documents/Cmpe275/lab1/fluffy/netty_mongo/test-file.pdf");
//            outputImage.write(mongoDBHelper.readFully(inputStream));
//            outputImage.close();
//            System.out.println("test filename "+mongoDBHelper.deleteImage("1234", "test"));
////            // Load Image from file
////            byte[] imageBytes = mongoDBHelper.getFileBytes("/Users/Rii/Documents/Cmpe275/lab1/fluffy/MongoDBHelper/test.jpg");
////
////            // Store Image to DB
////            DB db = mongoDBHelper.getMongoClient().getDB("assignment_2_db");
////            GridFS fs = new GridFS(db,"Netty-images");
////            GridFSInputFile in = fs.createFile( imageBytes );
////            in.setFilename("test2");
////            in.save();
////
////            // Fetch Image from DB and store to file
////            GridFSDBFile out = fs.findOne( new BasicDBObject( "filename" , "test2" ) );
////            FileOutputStream outputImage = new FileOutputStream("/Users/Rii/Documents/Cmpe275/lab1/fluffy/MongoDBHelper/ankitImage.jpg");
////            out.writeTo(outputImage);
////            outputImage.close();
//        }catch(Exception ex){
//            throw ex;
//        }
//    }
//
////        MongoCollection<BasicDBObject> collection = db.getCollection("image", BasicDBObject.class);
//
////        FindIterable<BasicDBObject> obj = collection.find(Filters.eq("_id", new ObjectId("56fb545ea11540e3f9b9255d")));
////        MongoCollection<BasicDBObject> collection = db.getCollection("image", BasicDBObject.class);
////        BasicDBObject b = new BasicDBObject();
////        b.put("name", "Galaxy.jpg");
////        b.put("client", "Ankit");
////        collection.insertOne(b);
//
////        System.out.println("Records based on ID");
////        for(BasicDBObject bo : obj){
////            System.out.println("BO Obj : "+bo.values());
////        }
////        obj = collection.find(Filters.eq("name", "Netty"));
////        System.out.println("Records based on name");
////        for(BasicDBObject bo : obj){
////            System.out.println("BO Obj : "+bo.values());
////        }
//}
