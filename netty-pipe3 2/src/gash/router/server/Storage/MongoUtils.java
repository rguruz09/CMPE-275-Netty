package gash.router.server.Storage;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;

/**
 * Created by Student on 4/4/16.
 */
public class MongoUtils {

    static MongoClient mongoClient = null;
    static DB db = null;

    public MongoUtils() {
        try {
            if(mongoClient == null){
                mongoClient  = new MongoClient();
            }
            if(db == null){
                db = mongoClient.getDB( "fluffy" );
            }
        } catch (Exception ex){
            System.out.println("Unable to connect to Mongo");
        }
    }

    public static DB getDb() {
        return db;
    }

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public boolean addMetaData(metadata meta){

        DBCollection metadata = db.getCollection("metadata");

        BasicDBObject doc = new BasicDBObject("_id", meta.getPrimaryID())
                .append("user_id", meta.getUserID())
                .append("file_name", meta.getFileName())
                .append("file_type",meta.getFileType())
                .append("total_chunks", meta.getTotalChunks())
                .append("file_size",meta.getTotalSize())
                .append("time",meta.getTime());

        return true;
    }

    public boolean addChunk(chunks chk){

        DBCollection chunks = db.getCollection("chunks");

        BasicDBObject doc = new BasicDBObject("_id", chk.getID())
                .append("parent_id", chk.getMetaID())
                .append("seq_num", chk.getSeqNum())
                .append("data", chk.getData())
                .append("time",chk.getTime());

        return true;
    }
}
