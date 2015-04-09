package net.haviss.havissIoTServer;

import com.mongodb.*;

import java.util.*;

/**
 * Created by H�vard on 4/3/2015.
 * This class connects to a mongodb database and handles all storage.
 */
public class havissIoTStorage implements Runnable {
    //Variables
    private String serverAddress = "";
    private int serverPort = 27017;
    private ArrayList<String[]> toStore = new ArrayList<>();
    private boolean stopThread = false;
    private String threadName = "storageThread";
    private boolean threadPaused = false;


    //Objects
    public Thread t;
    private MongoClient mongoClient;
    public DBCollection dbCollection;

    //Functions:
    @Override
    public void run() {
        try {
            while (!stopThread) {
                //TODO: Handle code to be excecuted in new thread
                while (threadPaused) {
                    wait();
                }
                if(toStore.size() > 0) {
                    try {
                        for (String[] s : toStore) {
                            BasicDBObject doc = new BasicDBObject("Topic", s[0]);
                            dbCollection.insert(doc);
                        }
                        toStore.clear(); //All data has been stored, clear toStore list
                    } catch (MongoException e) {
                        e.printStackTrace();
                    }
                } else {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            //TODO: Handle exception
            e.printStackTrace();
        }
    }
    public void start() {
        if(t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
    public void pauseThread() {
        this.threadPaused = true; //Tell thread to pause
    }
    public void resumeThread() {
        this.threadPaused = false;
    }
    //Constructor - stores new values and connects to server
    public havissIoTStorage(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.connect(this.serverAddress, this.serverPort);
    }
    //Overloaded constructor - with authentication
    public havissIoTStorage(String serverAddress, int serverPort, String username, String password, String db) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.connect(this.serverAddress, this.serverPort, username, password, db);
    }
    // Connects to server
    private void connect(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
        this.mongoClient = new MongoClient(serverAddress, serverPort);
    }
    //Overloaded connect funtion to enable autentication
    private void connect(String address, int port, String username, String password, String db) {
        MongoCredential credential = MongoCredential.createCredential(username, db, password.toCharArray());
        this.mongoClient = new MongoClient(new ServerAddress(serverAddress), Arrays.asList(credential));
    }
    //Get collection from database
    public void getCollection(String collection) {
        this.dbCollection = dbCollection.getCollection(collection);
    }
    public void addValues(String topic, String value) {
        String tempValues[] = {topic, value};
        toStore.add(tempValues);
    }


}
