package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {
    static final String TAG = SimpleDynamoProvider.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final String REMOTE_PORT0ToBeUsed = "5554";
    static final String REMOTE_PORT1ToBeUsed = "5556";
    static final String REMOTE_PORT2ToBeUsed = "5558";
    static final String REMOTE_PORT3ToBeUsed = "5560";
    static final String REMOTE_PORT4ToBeUsed = "5562";
    static final int SERVER_PORT = 10000;
    private final ContentValues newContentValues=new ContentValues();
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    String portOfMe;
    String[] arrayOfNodes=new String[5];
    //reference for treemap: http://www.tutorialspoint.com/java/java_treemap_class.htm
    TreeMap treeMap = new TreeMap();
    int countToDetermineSuccessorAndPredecessor=0;
    String myFirstPredecessor="";
    String mySecondPredecessor="";
    String myFirstSuccessor="";
    String mySecondSuccessor="";
    Map<String, String> localMap = new HashMap<String, String>();
    Map<String, String> myLocalMap = new HashMap<String, String>();
    Map<String, String> firstPredecessorLocalMap = new HashMap<String, String>();
    Map<String, String> secondPredecessorLocalMap = new HashMap<String, String>();
    boolean requestForMessageSent=false;
    String nameOfFileToBeReceived="";
    String contentOfFileToBeReceived="";
    String nameOfFileToBeSent="";
    String contentOfFileToBeSent="";
    String singleMessageToBeSent="";
    boolean requestForStarMessagesSent=false;
    Map<String, String> mapForStar = new HashMap<String, String>();
    Map<String, String> mapToBeSentSecondPredecessor = new HashMap<String, String>();
    Map<String, String> mapToBeSentFirstPredecessor = new HashMap<String, String>();
    Map<String, String> mapToBeSentFirstSuccessor = new HashMap<String, String>();
    Map<String, String> mapToBeSentSecondSuccessor = new HashMap<String, String>();
    Map<String, String> myMap = new HashMap<String, String>();
    Map<String, String> myMap1 = new HashMap<String, String>();
    boolean FlagToInformSecondSuccessorThatMUp=false;
    boolean FlagToInformSecondPredecessorThatMUp=false;
    boolean FlagToInformFirstPredecessorThatMUp=false;
    String nameOfFileToBeSentToSuccessor="";
    String contentOfFileToBeSentToSuccessor="";
    String singleMessageToBeSentToSuccessor="";
    String nameOfFileToBeReceivedToQuery="";
    String contentOfFileToBeReceivedToQuery="";
    boolean requestForMessageSentToQuery=false;
    Map<String, String> mapForInsertWhileRebalance = new HashMap<String, String>();
    Map<String, String> mapForRequesterPort = new HashMap<String, String>();
    //int countFirstSuccessor=0;
    //int countSecondSuccessor=0;
    int countForStar=0;
    int countForTimeOut=0;
    int countForRecovery=0;
    int countForRecoveryQuery=0;
    int countForRecoveryInsert=0;
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        PackageManager m = getContext().getPackageManager();
        String s = getContext().getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            s = p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("yourtag", "Error Package name not found ", e);
        }
        try {
            String path = s + "/files";
            File f = new File(path);
            File file[] = f.listFiles();
            for (int i = 0; i < file.length; i++) {
                getContext().deleteFile(file[i].getName());
            }
        }catch (Exception e){
            Log.e(TAG,"Exception");
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        countForRecoveryInsert=0;
        do{
            try{
                Thread.sleep(100);countForRecoveryInsert++;
            }catch (InterruptedException e){}
        }while ((FlagToInformSecondSuccessorThatMUp || FlagToInformSecondPredecessorThatMUp) && countForRecoveryInsert!=10);
        try{
            String nameOfFile = values.get("key").toString();
            String contentOfFile = values.get("value").toString();
            if(FlagToInformSecondSuccessorThatMUp==true || FlagToInformSecondPredecessorThatMUp==true){
                mapForInsertWhileRebalance.put(nameOfFile,contentOfFile);
            }
            if (genHash(nameOfFile).compareTo(genHash(arrayOfNodes[0])) < 0) {
                new ClientTask62Primary().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask56FirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask54SecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
            }
            else if(genHash(nameOfFile).compareTo(genHash(arrayOfNodes[0]))>0 && genHash(nameOfFile).compareTo(genHash(arrayOfNodes[1]))<0){
                new ClientTask56Primary().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask54FirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask58SecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
            }
            else if(genHash(nameOfFile).compareTo(genHash(arrayOfNodes[1]))>0 && genHash(nameOfFile).compareTo(genHash(arrayOfNodes[2]))<0){
                new ClientTask54Primary().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask58FirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask60SecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
            }
            else if(genHash(nameOfFile).compareTo(genHash(arrayOfNodes[2]))>0 && genHash(nameOfFile).compareTo(genHash(arrayOfNodes[3]))<0){
                new ClientTask58Primary().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask60FirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask62SecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
            }
            else if(genHash(nameOfFile).compareTo(genHash(arrayOfNodes[3]))>0 && genHash(nameOfFile).compareTo(genHash(arrayOfNodes[4]))<0){
                new ClientTask60Primary().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask62FirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask56SecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
            }
            else if(genHash(nameOfFile).compareTo(genHash(arrayOfNodes[4]))>0){
                new ClientTask62Primary().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask56FirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
                new ClientTask54SecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, nameOfFile, contentOfFile);
            }
            else{
                Log.d("Something went wrong while inserting","Something went wrong while inserting");
            }
        }
        catch (NoSuchAlgorithmException e){Log.e(TAG, "NoSuchAlgorithmException");}
        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        portOfMe=String.valueOf(Integer.parseInt(portStr));
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
            treeMap.put(genHash(REMOTE_PORT0ToBeUsed),REMOTE_PORT0ToBeUsed);
            treeMap.put(genHash(REMOTE_PORT1ToBeUsed),REMOTE_PORT1ToBeUsed);
            treeMap.put(genHash(REMOTE_PORT2ToBeUsed),REMOTE_PORT2ToBeUsed);
            treeMap.put(genHash(REMOTE_PORT3ToBeUsed),REMOTE_PORT3ToBeUsed);
            treeMap.put(genHash(REMOTE_PORT4ToBeUsed),REMOTE_PORT4ToBeUsed);
            //reference: http://www.java2novice.com/java-collections-and-util/treemap/iterate/
            Set<String> keys=treeMap.keySet();
            for(String key:keys){
                arrayOfNodes[countToDetermineSuccessorAndPredecessor]=treeMap.get(key).toString();
                countToDetermineSuccessorAndPredecessor++;
                //System.out.println("Value of "+key+" is: "+treeMap.get(key));
            }
            // arrayOfNodes stores values of ports in sorted order
            countToDetermineSuccessorAndPredecessor=0;
            if(arrayOfNodes[0].equals(portOfMe)){
                myFirstPredecessor=arrayOfNodes[4];
                mySecondPredecessor=arrayOfNodes[3];
                myFirstSuccessor=arrayOfNodes[1];
                mySecondSuccessor=arrayOfNodes[2];
            }
            else if(arrayOfNodes[1].equals(portOfMe)){
                myFirstPredecessor=arrayOfNodes[0];
                mySecondPredecessor=arrayOfNodes[4];
                myFirstSuccessor=arrayOfNodes[2];
                mySecondSuccessor=arrayOfNodes[3];
            }
            else if(arrayOfNodes[2].equals(portOfMe)){
                myFirstPredecessor=arrayOfNodes[1];
                mySecondPredecessor=arrayOfNodes[0];
                myFirstSuccessor=arrayOfNodes[3];
                mySecondSuccessor=arrayOfNodes[4];
            }
            else if(arrayOfNodes[3].equals(portOfMe)){
                myFirstPredecessor=arrayOfNodes[2];
                mySecondPredecessor=arrayOfNodes[1];
                myFirstSuccessor=arrayOfNodes[4];
                mySecondSuccessor=arrayOfNodes[0];
            }
            else if(arrayOfNodes[4].equals(portOfMe)){
                myFirstPredecessor=arrayOfNodes[3];
                mySecondPredecessor=arrayOfNodes[2];
                myFirstSuccessor=arrayOfNodes[0];
                mySecondSuccessor=arrayOfNodes[1];
            }
            new InformSecondSuccessorThatMUp().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "SecondPredecessor", portOfMe);
            FlagToInformSecondSuccessorThatMUp=true;
            //do{try{Thread.sleep(1);}catch (InterruptedException e){}}while (FlagToInformSecondSuccessorThatMUp==true);
            new InformSecondPredecessorThatMUp().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "SecondSuccessor", portOfMe);
            FlagToInformSecondPredecessorThatMUp=true;
            //do{try{Thread.sleep(1);}catch (InterruptedException e){}}while (FlagToInformSecondPredecessorThatMUp==true);
            //new InformFirstPredecessorThatMUp().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "FirstSuccessor", portOfMe);
            //try{Thread.sleep(10000);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
            //new ForwardMessagesToFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "ForwardToFirstSuccessor", portOfMe);
            countForRecovery=0;
            do{
                try{
                    Thread.sleep(100);countForRecovery++;
                }catch (InterruptedException e){}
            }while ((FlagToInformSecondSuccessorThatMUp || FlagToInformSecondPredecessorThatMUp) && countForRecovery!=20);
        }
        catch (IOException e){Log.e(TAG, "IOException1");}
        catch (NoSuchAlgorithmException e){Log.e(TAG, "NoSuchAlgorithmException");}
        return false;
    }

    @Override
    public synchronized Cursor query(Uri uri, String[] projection, String selection,
                                     String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        countForRecoveryQuery=0;
        do{
            try{
                Thread.sleep(100);countForRecoveryQuery++;
            }catch (InterruptedException e){}
        }while ((FlagToInformSecondSuccessorThatMUp || FlagToInformSecondPredecessorThatMUp) && countForRecoveryQuery!=10);
        try{
            String[] columnNames = {"key", "value"};
            MatrixCursor matrixCursor=new MatrixCursor(columnNames);
            if(selection.equals("\"*\"")){
                requestForStarMessagesSent=true;
                new RequestForStar62().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, portOfMe, portOfMe);
                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                if(requestForStarMessagesSent==true){
                    try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                    if(requestForStarMessagesSent==true){
                        try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                        if(requestForStarMessagesSent==true){
                            try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            if(requestForStarMessagesSent==true){
                                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            }
                        }
                    }
                }
                requestForStarMessagesSent=true;
                new RequestForStar56().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, portOfMe, portOfMe);
                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                if(requestForStarMessagesSent==true){
                    try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                    if(requestForStarMessagesSent==true){
                        try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                        if(requestForStarMessagesSent==true){
                            try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            if(requestForStarMessagesSent==true){
                                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            }
                        }
                    }
                }
                requestForStarMessagesSent=true;
                new RequestForStar54().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, portOfMe, portOfMe);
                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                if(requestForStarMessagesSent==true){
                    try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                    if(requestForStarMessagesSent==true){
                        try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                        if(requestForStarMessagesSent==true){
                            try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            if(requestForStarMessagesSent==true){
                                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            }
                        }
                    }
                }
                requestForStarMessagesSent=true;
                new RequestForStar58().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, portOfMe, portOfMe);
                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                if(requestForStarMessagesSent==true){
                    try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                    if(requestForStarMessagesSent==true){
                        try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                        if(requestForStarMessagesSent==true){
                            try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            if(requestForStarMessagesSent==true){
                                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            }
                        }
                    }
                }
                requestForStarMessagesSent=true;
                new RequestForStar60().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, portOfMe, portOfMe);
                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                if(requestForStarMessagesSent==true){
                    try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                    if(requestForStarMessagesSent==true){
                        try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                        if(requestForStarMessagesSent==true){
                            try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            if(requestForStarMessagesSent==true){
                                try{Thread.sleep(200);}catch (InterruptedException e){Log.e(TAG, "InterruptedException");}
                            }
                        }
                    }
                }
                Iterator it = mapForStar.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    matrixCursor.addRow(new String[]{pair.getKey().toString(),pair.getValue().toString()});
                    it.remove(); // avoids a ConcurrentModificationException
                }
                return matrixCursor;
            }
            else if(selection.equals("\"@\"")){
                PackageManager m = getContext().getPackageManager();
                String s = getContext().getPackageName();
                try {
                    PackageInfo p = m.getPackageInfo(s, 0);
                    s = p.applicationInfo.dataDir;
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w("yourtag", "Error Package name not found ", e);
                }
                String path = s+"/files";
                File f = new File(path);
                File file[] = f.listFiles();
                if(file!=null) {
                    for (int i = 0; i < file.length; i++) {
                        int n;
                        File newFile = getContext().getFileStreamPath(file[i].getName());
                        FileInputStream fis = getContext().openFileInput(file[i].getName());
                        StringBuffer fileContent = new StringBuffer("");
                        String stringFileContent;
                        // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                        byte[] buffer = new byte[1024];
                        if (newFile.exists()) {
                            while ((n = fis.read(buffer)) != -1) {
                                fileContent.append(new String(buffer, 0, n));
                            }
                        }
                        stringFileContent = fileContent.toString();
                        matrixCursor.addRow(new String[]{file[i].getName(), stringFileContent});
                    }
                }
                return matrixCursor;
            }
            else{
                int countFirstSuccessor=0;
                int countSecondSuccessor=0;
                if (genHash(selection).compareTo(genHash(arrayOfNodes[0])) < 0) {
                    Log.d("Key belongs to 58. Key: ",selection);
                    if(arrayOfNodes[0].equals(portOfMe)){
                        if(myLocalMap.get(selection)==null || myLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }
                            catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, myLocalMap.get(selection)});
                        }
                    }
                    else if(arrayOfNodes[0].equals(myFirstPredecessor)){
                        if(firstPredecessorLocalMap.get(selection)==null || firstPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "1Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, firstPredecessorLocalMap.get(selection)});
                        }
                    }
                    else if(arrayOfNodes[0].equals(mySecondPredecessor)){
                        if(secondPredecessorLocalMap.get(selection)==null || secondPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestMyPredecessorForMessage().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSentToQuery=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSentToQuery == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceivedToQuery, contentOfFileToBeReceivedToQuery});
                                Log.e(TAG, "2Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, secondPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("Size: ",String.valueOf(secondPredecessorLocalMap.size()));
                        //Log.d("3Returning the data. Key: "+selection," Value: "+secondPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[0].equals(myFirstSuccessor)){
                        new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countFirstSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countFirstSuccessor==1000);
                        if(countFirstSuccessor==1000){
                            //Log.d("1Entered","1Entered");
                            new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countFirstSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                    else if(arrayOfNodes[0].equals(mySecondSuccessor)){
                        new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countSecondSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countSecondSuccessor==1000);
                        if(countSecondSuccessor==1000){
                            //Log.d("2Entered","2Entered");
                            new requestSingleMessageSecondPredecessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countSecondSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                }
                else if (genHash(selection).compareTo(genHash(arrayOfNodes[0])) > 0 && genHash(selection).compareTo(genHash(arrayOfNodes[1])) < 0) {
                    if(arrayOfNodes[1].equals(portOfMe)){
                        if(myLocalMap.get(selection)==null || myLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "3Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, myLocalMap.get(selection)});
                        }
                        //Log.d("4Returning the data. Key: "+selection," Value: "+myLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[1].equals(myFirstPredecessor)){
                        if(firstPredecessorLocalMap.get(selection)==null || firstPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "4Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, firstPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("5Returning the data. Key: "+selection," Value: "+firstPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[1].equals(mySecondPredecessor)){
                        if(secondPredecessorLocalMap.get(selection)==null || secondPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestMyPredecessorForMessage().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSentToQuery=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSentToQuery == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceivedToQuery, contentOfFileToBeReceivedToQuery});
                                Log.e(TAG, "5Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, secondPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("6Returning the data. Key: "+selection," Value: "+secondPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[1].equals(myFirstSuccessor)){
                        new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countFirstSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countFirstSuccessor==1000);
                        if(countFirstSuccessor==1000){
                            //Log.d("3Entered","3Entered");
                            new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countFirstSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                    else if(arrayOfNodes[1].equals(mySecondSuccessor)){
                        new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countSecondSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countSecondSuccessor==1000);
                        if(countSecondSuccessor==1000){
                            //Log.d("4Entered","4Entered");
                            new requestSingleMessageSecondPredecessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countSecondSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                }
                else if (genHash(selection).compareTo(genHash(arrayOfNodes[1])) > 0 && genHash(selection).compareTo(genHash(arrayOfNodes[2])) < 0) {
                    if(arrayOfNodes[2].equals(portOfMe)){
                        if(myLocalMap.get(selection)==null || myLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "6Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, myLocalMap.get(selection)});
                        }
                        //Log.d("7Returning the data. Key: "+selection," Value: "+myLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[2].equals(myFirstPredecessor)){
                        if(firstPredecessorLocalMap.get(selection)==null || firstPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "7Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, firstPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("8Returning the data. Key: "+selection," Value: "+firstPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[2].equals(mySecondPredecessor)){
                        if(secondPredecessorLocalMap.get(selection)==null || secondPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestMyPredecessorForMessage().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSentToQuery=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSentToQuery == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceivedToQuery, contentOfFileToBeReceivedToQuery});
                                Log.e(TAG, "8Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, secondPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("9Returning the data. Key: "+selection," Value: "+secondPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[2].equals(myFirstSuccessor)){
                        new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countFirstSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countFirstSuccessor==1000);
                        if(countFirstSuccessor==1000){
                            //Log.d("5Entered","5Entered");
                            new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countFirstSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                    else if(arrayOfNodes[2].equals(mySecondSuccessor)){
                        new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countSecondSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countSecondSuccessor==1000);
                        if(countSecondSuccessor==1000){
                            //Log.d("6Entered","6Entered");
                            new requestSingleMessageSecondPredecessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countSecondSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                }
                else if (genHash(selection).compareTo(genHash(arrayOfNodes[2])) > 0 && genHash(selection).compareTo(genHash(arrayOfNodes[3])) < 0) {
                    if(arrayOfNodes[3].equals(portOfMe)){
                        if(myLocalMap.get(selection)==null || myLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "9Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, myLocalMap.get(selection)});
                        }
                        //Log.d("10Returning the data. Key: "+selection," Value: "+myLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[3].equals(myFirstPredecessor)){
                        if(firstPredecessorLocalMap.get(selection)==null || firstPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "10Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, firstPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("11Returning the data. Key: "+selection," Value: "+firstPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[3].equals(mySecondPredecessor)){
                        if(secondPredecessorLocalMap.get(selection)==null || secondPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestMyPredecessorForMessage().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSentToQuery=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSentToQuery == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceivedToQuery, contentOfFileToBeReceivedToQuery});
                                Log.e(TAG, "11Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, secondPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("12Returning the data. Key: "+selection," Value: "+secondPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[3].equals(myFirstSuccessor)){
                        new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countFirstSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countFirstSuccessor==1000);
                        if(countFirstSuccessor==1000){
                            //Log.d("7Entered","7Entered");
                            new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countFirstSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                    else if(arrayOfNodes[3].equals(mySecondSuccessor)){
                        new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countSecondSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countSecondSuccessor==1000);
                        if(countSecondSuccessor==1000){
                            //Log.d("8Entered","8Entered");
                            new requestSingleMessageSecondPredecessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countSecondSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                }
                else if (genHash(selection).compareTo(genHash(arrayOfNodes[3])) > 0 && genHash(selection).compareTo(genHash(arrayOfNodes[4])) < 0) {
                    if(arrayOfNodes[4].equals(portOfMe)){
                        if(myLocalMap.get(selection)==null || myLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "12Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, myLocalMap.get(selection)});
                        }
                        //Log.d("13Returning the data. Key: "+selection," Value: "+myLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[4].equals(myFirstPredecessor)){
                        if(firstPredecessorLocalMap.get(selection)==null || firstPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "13Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, firstPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("14Returning the data. Key: "+selection," Value: "+firstPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[4].equals(mySecondPredecessor)){
                        if(secondPredecessorLocalMap.get(selection)==null || secondPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestMyPredecessorForMessage().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSentToQuery=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSentToQuery == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceivedToQuery, contentOfFileToBeReceivedToQuery});
                                Log.e(TAG, "14Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, secondPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("15Returning the data. Key: "+selection," Value: "+secondPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[4].equals(myFirstSuccessor)){
                        new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countFirstSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countFirstSuccessor==1000);
                        if(countFirstSuccessor==1000){
                            //Log.d("9Entered","9Entered");
                            new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countFirstSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                    else if(arrayOfNodes[4].equals(mySecondSuccessor)){
                        new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countSecondSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countSecondSuccessor==1000);
                        if(countSecondSuccessor==1000){
                            //Log.d("10Entered","10Entered");
                            new requestSingleMessageSecondPredecessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countSecondSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                }
                else if (genHash(selection).compareTo(genHash(arrayOfNodes[4])) > 0) {
                    if(arrayOfNodes[0].equals(portOfMe)){
                        if(myLocalMap.get(selection)==null || myLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "15Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, myLocalMap.get(selection)});
                        }
                        //Log.d("16Returning the data. Key: "+selection," Value: "+myLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[0].equals(myFirstPredecessor)){
                        if(firstPredecessorLocalMap.get(selection)==null || firstPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSent=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSent == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                                Log.e(TAG, "16Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, firstPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("17Returning the data. Key: "+selection," Value: "+firstPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[0].equals(mySecondPredecessor)){
                        if(secondPredecessorLocalMap.get(selection)==null || secondPredecessorLocalMap.get(selection).isEmpty()){
                            int n;
                            File file = getContext().getFileStreamPath(selection);
                            try {
                                FileInputStream fis = getContext().openFileInput(selection);
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (file.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                matrixCursor.addRow(new String[]{selection, stringFileContent});
                            }catch (FileNotFoundException x){
                                new requestMyPredecessorForMessage().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                                countForTimeOut=0;
                                requestForMessageSentToQuery=true;
                                do{
                                    try {Thread.sleep(1);countForTimeOut++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                                } while (requestForMessageSentToQuery == true && countForTimeOut!=800);
                                if(countForTimeOut==800){
                                    nameOfFileToBeReceived=selection;
                                    contentOfFileToBeReceived=myLocalMap.get(selection);
                                    Log.d("Got the value. Name: "+nameOfFileToBeReceived," Value: "+myLocalMap.get(selection));
                                }
                                matrixCursor.addRow(new String[]{nameOfFileToBeReceivedToQuery, contentOfFileToBeReceivedToQuery});
                                Log.e(TAG, "17Delegating query request");
                            }
                        }
                        else {
                            matrixCursor.addRow(new String[]{selection, secondPredecessorLocalMap.get(selection)});
                        }
                        //Log.d("18Returning the data. Key: "+selection," Value: "+secondPredecessorLocalMap.get(selection));
                    }
                    else if(arrayOfNodes[0].equals(myFirstSuccessor)){
                        new requestSingleMessageFirstSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countFirstSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countFirstSuccessor==1000);
                        if(countFirstSuccessor==1000){
                            //Log.d("11Entered","11Entered");
                            new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countFirstSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                    else if(arrayOfNodes[0].equals(mySecondSuccessor)){
                        new requestSingleMessageSecondSuccessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                        requestForMessageSent=true;
                        do{
                            try {Thread.sleep(1);countSecondSuccessor++;} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                        } while (requestForMessageSent == true || countSecondSuccessor==1000);
                        if(countSecondSuccessor==1000){
                            //Log.d("12Entered","12Entered");
                            new requestSingleMessageSecondPredecessor().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, selection, portOfMe);
                            do{
                                try {Thread.sleep(1);} catch (InterruptedException e) {Log.e(TAG, "InterruptedException");}
                            } while (requestForMessageSent == true);
                        }
                        countSecondSuccessor=0;
                        matrixCursor.addRow(new String[]{nameOfFileToBeReceived, contentOfFileToBeReceived});
                    }
                }
                return matrixCursor;
            }
        }
        catch (NoSuchAlgorithmException e){Log.e(TAG, "NoSuchAlgorithmException");}
        catch (FileNotFoundException e){
            Log.e(TAG, "FileNotFoundException");
        }
        catch (IOException e){Log.e(TAG, "IOException4");}
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            String[] arrayOfMessages=new String[10];
            int count=0;
            String message="",senderPort="";
            try{
                while (true){
                    Socket socket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    ObjectInputStream ois=new ObjectInputStream(socket.getInputStream());
                    Object receivedData=ois.readObject();
                    if(receivedData instanceof InsertMessage62Primary){
                        InsertMessage62Primary insertMessage62Primary=(InsertMessage62Primary)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage62Primary.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage62Primary.contentOfFile.getBytes());
                        myLocalMap.put(insertMessage62Primary.nameOfFile,insertMessage62Primary.contentOfFile);
                        fos.close();
                        //Log.d("1Inserted in coordinator. Key: " + insertMessage62Primary.nameOfFile, " Value: " + insertMessage62Primary.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage62FirstSuccessor){
                        InsertMessage62FirstSuccessor insertMessage62FirstSuccessor=(InsertMessage62FirstSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage62FirstSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage62FirstSuccessor.contentOfFile.getBytes());
                        firstPredecessorLocalMap.put(insertMessage62FirstSuccessor.nameOfFile,insertMessage62FirstSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("1Inserted in first successor. Key: " + insertMessage62FirstSuccessor.nameOfFile, " Value: " + insertMessage62FirstSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage62SecondSuccessor){
                        InsertMessage62SecondSuccessor insertMessage62SecondSuccessor=(InsertMessage62SecondSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage62SecondSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage62SecondSuccessor.contentOfFile.getBytes());
                        secondPredecessorLocalMap.put(insertMessage62SecondSuccessor.nameOfFile,insertMessage62SecondSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("1Inserted in second successor. Key: " + insertMessage62SecondSuccessor.nameOfFile, " Value: " + insertMessage62SecondSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage56Primary){
                        InsertMessage56Primary insertMessage56Primary=(InsertMessage56Primary)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage56Primary.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage56Primary.contentOfFile.getBytes());
                        myLocalMap.put(insertMessage56Primary.nameOfFile,insertMessage56Primary.contentOfFile);
                        fos.close();
                        //Log.d("2Inserted in coordinator. Key: " + insertMessage56Primary.nameOfFile, " Value: " + insertMessage56Primary.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage56FirstSuccessor){
                        InsertMessage56FirstSuccessor insertMessage56FirstSuccessor=(InsertMessage56FirstSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage56FirstSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage56FirstSuccessor.contentOfFile.getBytes());
                        firstPredecessorLocalMap.put(insertMessage56FirstSuccessor.nameOfFile,insertMessage56FirstSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("2Inserted in first successor. Key: " + insertMessage56FirstSuccessor.nameOfFile, " Value: " + insertMessage56FirstSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage56SecondSuccessor){
                        InsertMessage56SecondSuccessor insertMessage56SecondSuccessor=(InsertMessage56SecondSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage56SecondSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage56SecondSuccessor.contentOfFile.getBytes());
                        secondPredecessorLocalMap.put(insertMessage56SecondSuccessor.nameOfFile,insertMessage56SecondSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("2Inserted in second successor. Key: " + insertMessage56SecondSuccessor.nameOfFile, " Value: " + insertMessage56SecondSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage54Primary){
                        InsertMessage54Primary insertMessage54Primary=(InsertMessage54Primary)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage54Primary.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage54Primary.contentOfFile.getBytes());
                        myLocalMap.put(insertMessage54Primary.nameOfFile,insertMessage54Primary.contentOfFile);
                        fos.close();
                        //Log.d("3Inserted in coordinator. Key: " + insertMessage54Primary.nameOfFile, " Value: " + insertMessage54Primary.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage54FirstSuccessor){
                        InsertMessage54FirstSuccessor insertMessage54FirstSuccessor=(InsertMessage54FirstSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage54FirstSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage54FirstSuccessor.contentOfFile.getBytes());
                        firstPredecessorLocalMap.put(insertMessage54FirstSuccessor.nameOfFile,insertMessage54FirstSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("3Inserted in first successor. Key: " + insertMessage54FirstSuccessor.nameOfFile, " Value: " + insertMessage54FirstSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage54SecondSuccessor){
                        InsertMessage54SecondSuccessor insertMessage54SecondSuccessor=(InsertMessage54SecondSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage54SecondSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage54SecondSuccessor.contentOfFile.getBytes());
                        secondPredecessorLocalMap.put(insertMessage54SecondSuccessor.nameOfFile,insertMessage54SecondSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("3Inserted in second successor. Key: " + insertMessage54SecondSuccessor.nameOfFile, " Value: " + insertMessage54SecondSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage58Primary){
                        InsertMessage58Primary insertMessage58Primary=(InsertMessage58Primary)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage58Primary.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage58Primary.contentOfFile.getBytes());
                        myLocalMap.put(insertMessage58Primary.nameOfFile,insertMessage58Primary.contentOfFile);
                        fos.close();
                        //Log.d("4Inserted in coordinator. Key: " + insertMessage58Primary.nameOfFile, " Value: " + insertMessage58Primary.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage58FirstSuccessor){
                        InsertMessage58FirstSuccessor insertMessage58FirstSuccessor=(InsertMessage58FirstSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage58FirstSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage58FirstSuccessor.contentOfFile.getBytes());
                        firstPredecessorLocalMap.put(insertMessage58FirstSuccessor.nameOfFile,insertMessage58FirstSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("4Inserted in first successor. Key: " + insertMessage58FirstSuccessor.nameOfFile, " Value: " + insertMessage58FirstSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage58SecondSuccessor){
                        InsertMessage58SecondSuccessor insertMessage58SecondSuccessor=(InsertMessage58SecondSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage58SecondSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage58SecondSuccessor.contentOfFile.getBytes());
                        secondPredecessorLocalMap.put(insertMessage58SecondSuccessor.nameOfFile,insertMessage58SecondSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("4Inserted in second successor. Key: " + insertMessage58SecondSuccessor.nameOfFile, " Value: " + insertMessage58SecondSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage60Primary){
                        InsertMessage60Primary insertMessage60Primary=(InsertMessage60Primary)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage60Primary.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage60Primary.contentOfFile.getBytes());
                        myLocalMap.put(insertMessage60Primary.nameOfFile,insertMessage60Primary.contentOfFile);
                        fos.close();
                        //Log.d("5Inserted in coordinator. Key: " + insertMessage60Primary.nameOfFile, " Value: " + insertMessage60Primary.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage60FirstSuccessor){
                        InsertMessage60FirstSuccessor insertMessage60FirstSuccessor=(InsertMessage60FirstSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage60FirstSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage60FirstSuccessor.contentOfFile.getBytes());
                        firstPredecessorLocalMap.put(insertMessage60FirstSuccessor.nameOfFile,insertMessage60FirstSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("5Inserted in first successor. Key: " + insertMessage60FirstSuccessor.nameOfFile, " Value: " + insertMessage60FirstSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof InsertMessage60SecondSuccessor){
                        InsertMessage60SecondSuccessor insertMessage60SecondSuccessor=(InsertMessage60SecondSuccessor)receivedData;
                        FileOutputStream fos = getContext().openFileOutput(insertMessage60SecondSuccessor.nameOfFile, Context.MODE_PRIVATE);
                        fos.write(insertMessage60SecondSuccessor.contentOfFile.getBytes());
                        secondPredecessorLocalMap.put(insertMessage60SecondSuccessor.nameOfFile,insertMessage60SecondSuccessor.contentOfFile);
                        fos.close();
                        //Log.d("5Inserted in second successor. Key: " + insertMessage60SecondSuccessor.nameOfFile, " Value: " + insertMessage60SecondSuccessor.contentOfFile);
                    }
                    if(receivedData instanceof requestSingleMessage){
                        requestSingleMessage requestSingleMessage=(requestSingleMessage)receivedData;
                        String stringFileContent;
                        nameOfFileToBeSent=requestSingleMessage.nameOfFile;
                        Log.d("Request received. Key: ",nameOfFileToBeSent);
                        if(myLocalMap.get(nameOfFileToBeSent)==null || myLocalMap.get(nameOfFileToBeSent).isEmpty()) {
                            int n;
                            File newFile = getContext().getFileStreamPath(nameOfFileToBeSent);
                            FileInputStream fis = getContext().openFileInput(nameOfFileToBeSent);
                            StringBuffer fileContent = new StringBuffer("");
                            String stringFileContent1;
                            // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                            byte[] buffer = new byte[1024];
                            if (newFile.exists()) {
                                while ((n = fis.read(buffer)) != -1) {
                                    fileContent.append(new String(buffer, 0, n));
                                }
                            }
                            stringFileContent1 = fileContent.toString();
                            contentOfFileToBeSent=stringFileContent1;
                        }
                        else {
                            contentOfFileToBeSent = myLocalMap.get(nameOfFileToBeSent);
                        }
                        singleMessageToBeSent=nameOfFileToBeSent+"#"+contentOfFileToBeSent;
                        Log.d("singleMessageToBeSent",singleMessageToBeSent);
                        new sendMessageToRequester().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, singleMessageToBeSent, requestSingleMessage.requesterPort);
                    }
                    if(receivedData instanceof sendRequestedMessage){
                        sendRequestedMessage sendRequestedMessage=(sendRequestedMessage)receivedData;
                        nameOfFileToBeReceived=sendRequestedMessage.nameOfFile;
                        contentOfFileToBeReceived=sendRequestedMessage.contentOfFile;
                        requestForMessageSent=false;
                        Log.d("Message received. Key: "+nameOfFileToBeReceived," Value: "+contentOfFileToBeReceived);
                    }
                    if(receivedData instanceof sendRequestedMessageToSuccessor){
                        sendRequestedMessageToSuccessor sendRequestedMessageToSuccessor=(sendRequestedMessageToSuccessor)receivedData;
                        nameOfFileToBeReceivedToQuery=sendRequestedMessageToSuccessor.nameOfFile;
                        contentOfFileToBeReceivedToQuery=sendRequestedMessageToSuccessor.contentOfFile;
                        requestForMessageSentToQuery=false;
                    }
                    if(receivedData instanceof RequestForStar){
                        RequestForStar requestForStar=(RequestForStar)receivedData;

                        PackageManager m = getContext().getPackageManager();
                        String s = getContext().getPackageName();
                        try {
                            PackageInfo p = m.getPackageInfo(s, 0);
                            s = p.applicationInfo.dataDir;
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.w("yourtag", "Error Package name not found ", e);
                        }
                        String path = s+"/files";
                        File f = new File(path);
                        File file[] = f.listFiles();
                        if(file!=null) {
                            for (int i = 0; i < file.length; i++) {
                                int n;
                                File newFile = getContext().getFileStreamPath(file[i].getName());
                                FileInputStream fis = getContext().openFileInput(file[i].getName());
                                StringBuffer fileContent = new StringBuffer("");
                                String stringFileContent;
                                // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                                byte[] buffer = new byte[1024];
                                if (newFile.exists()) {
                                    while ((n = fis.read(buffer)) != -1) {
                                        fileContent.append(new String(buffer, 0, n));
                                    }
                                }
                                stringFileContent = fileContent.toString();
                                localMap.put(file[i].getName(), stringFileContent);
                                //matrixCursor.addRow(new String[]{file[i].getName(), stringFileContent });
                            }
                        }
                        mapForRequesterPort.clear();
                        mapForRequesterPort.put(requestForStar.requesterPort,requestForStar.requesterPort);
                        /*localMap.putAll(myLocalMap);
                        localMap.putAll(firstPredecessorLocalMap);
                        localMap.putAll(secondPredecessorLocalMap);*/
                        new SendMessagesOfMyAVD().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, localMap, mapForRequesterPort);
                    }
                    if(receivedData instanceof SendMessageOfMyAVD){
                        SendMessageOfMyAVD sendMessageOfMyAVD=(SendMessageOfMyAVD)receivedData;
                        mapForStar.putAll(sendMessageOfMyAVD.localMap);
                        requestForStarMessagesSent=false;
                    }
                    if(receivedData instanceof InformSecondSuccessor){
                        InformSecondSuccessor informSecondSuccessor=(InformSecondSuccessor)receivedData;
                        //Log.d("InformSecondSuccessor. Size of map before: ",String.valueOf(mapToBeSentSecondPredecessor.size()));
                        mapToBeSentSecondPredecessor.clear();
                        Iterator it = secondPredecessorLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            mapToBeSentSecondPredecessor.put(pair.getKey().toString(),pair.getValue().toString());
                            //Log.d("Should be Inserted in coordinator. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                        //Log.d("InformSecondSuccessor. Size of map after: ",String.valueOf(mapToBeSentSecondPredecessor.size()));

                        mapToBeSentFirstPredecessor.clear();
                        //Log.d("AgainInformSecondSuccessor. Size of map before: ",String.valueOf(mapToBeSentSecondPredecessor.size()));
                        it = firstPredecessorLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            mapToBeSentFirstPredecessor.put(pair.getKey().toString(),pair.getValue().toString());
                            //Log.d("AgainShould be Inserted in coordinator. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                        //Log.d("AgainInformSecondSuccessor. Size of map after: ",String.valueOf(mapToBeSentSecondPredecessor.size()));
                        new GiveSecondPredecessorItsMap().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, informSecondSuccessor.requesterPort,informSecondSuccessor.requesterPort);
                    }
                    if(receivedData instanceof GiveSecondPredecessor){
                        GiveSecondPredecessor giveSecondPredecessor=(GiveSecondPredecessor)receivedData;
                        //Log.d("GiveSecondPredecessor before. ",String.valueOf(firstPredecessorLocalMap.size()));
                        firstPredecessorLocalMap.putAll(giveSecondPredecessor.mapToBeSentSecondPredecessor);
                        //Log.d("GiveSecondPredecessor after. ",String.valueOf(firstPredecessorLocalMap.size()));
                        Iterator it = firstPredecessorLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            FileOutputStream fos = getContext().openFileOutput(pair.getKey().toString(), Context.MODE_PRIVATE);
                            fos.write(pair.getValue().toString().getBytes());
                            //Log.d("6AgainInserted in first successor. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            fos.close();
                            it.remove(); // avoids a ConcurrentModificationException
                        }

                        //Log.d("AgainGiveSecondPredecessor before. ",String.valueOf(myLocalMap.size()));
                        myLocalMap.putAll(giveSecondPredecessor.mapToBeSentFirstPredecessor);
                        //Log.d("AgainGiveSecondPredecessor after. ",String.valueOf(myLocalMap.size()));
                        it = myLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            FileOutputStream fos = getContext().openFileOutput(pair.getKey().toString(), Context.MODE_PRIVATE);
                            fos.write(pair.getValue().toString().getBytes());
                            //Log.d("6Inserted in coordinator. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            fos.close();
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                        FlagToInformSecondSuccessorThatMUp=false;
                    }
                    if(receivedData instanceof InformSecondPredecessor){
                        InformSecondPredecessor informSecondPredecessor=(InformSecondPredecessor)receivedData;
                        //Log.d("InformSecondPredecessor. Size of map before: ",String.valueOf(mapToBeSentSecondSuccessor.size()));
                        mapToBeSentSecondSuccessor.clear();
                        Iterator it = myLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            mapToBeSentSecondSuccessor.put(pair.getKey().toString(),pair.getValue().toString());
                            //Log.d("Should be Inserted in second successor. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                        //Log.d("InformSecondPredecessor. Size of map after: ",String.valueOf(mapToBeSentSecondSuccessor.size()));
                        new GiveSecondSuccessorItsMap().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, informSecondPredecessor.requesterPort, informSecondPredecessor.requesterPort);
                    }
                    if(receivedData instanceof GiveSecondSuccessor){
                        GiveSecondSuccessor giveSecondSuccessor=(GiveSecondSuccessor)receivedData;
                        //Log.d("GiveSecondPredecessor before. ",String.valueOf(secondPredecessorLocalMap.size()));
                        secondPredecessorLocalMap.putAll(giveSecondSuccessor.mapToBeSentSecondSuccessor);
                        //Log.d("GiveSecondPredecessor after. ",String.valueOf(secondPredecessorLocalMap.size()));
                        Iterator it = secondPredecessorLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            FileOutputStream fos = getContext().openFileOutput(pair.getKey().toString(), Context.MODE_PRIVATE);
                            fos.write(pair.getValue().toString().getBytes());
                            //Log.d("6Inserted in second successor. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            fos.close();
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                        FlagToInformSecondPredecessorThatMUp=false;
                    }
                    if(receivedData instanceof InformFirstPredecessor){
                        InformFirstPredecessor informFirstPredecessor=(InformFirstPredecessor)receivedData;
                        //Log.d("InformFirstPredecessor. Size of map before: ",String.valueOf(mapToBeSentFirstSuccessor.size()));
                        Iterator it = myLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            mapToBeSentFirstSuccessor.put(pair.getKey().toString(),pair.getValue().toString());
                            //Log.d("Should be Inserted in first successor. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                        //Log.d("InformFirstPredecessor. Size of map after: ",String.valueOf(mapToBeSentFirstSuccessor.size()));
                        new GiveFirstSuccessorItsMap().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, informFirstPredecessor.requesterPort, informFirstPredecessor.requesterPort);
                    }
                    if(receivedData instanceof GiveFirstSuccessor){
                        GiveFirstSuccessor giveFirstSuccessor=new GiveFirstSuccessor();
                        //Log.d("GiveFirstPredecessor before. ",String.valueOf(firstPredecessorLocalMap.size()));
                        //Log.d("Size check: ",String.valueOf(giveFirstSuccessor.mapToBeSentFirstSuccessor.size()));
                        firstPredecessorLocalMap.putAll(giveFirstSuccessor.mapToBeSentFirstSuccessor);
                        //Log.d("GiveFirstPredecessor after. ",String.valueOf(firstPredecessorLocalMap.size()));
                        Iterator it = firstPredecessorLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            FileOutputStream fos = getContext().openFileOutput(pair.getKey().toString(), Context.MODE_PRIVATE);
                            fos.write(pair.getValue().toString().getBytes());
                            //Log.d("6Inserted in first successor. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            fos.close();
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                    }
                    if(receivedData instanceof FrwrdMessagesToFirstSuccessor){
                        FrwrdMessagesToFirstSuccessor frwrdMessagesToFirstSuccessor=(FrwrdMessagesToFirstSuccessor)receivedData;
                        firstPredecessorLocalMap.putAll(frwrdMessagesToFirstSuccessor.mapToForward);
                        Iterator it = firstPredecessorLocalMap.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            FileOutputStream fos = getContext().openFileOutput(pair.getKey().toString(), Context.MODE_PRIVATE);
                            fos.write(pair.getValue().toString().getBytes());
                            //Log.d("Again Inserted in first successor. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                            fos.close();
                            it.remove(); // avoids a ConcurrentModificationException
                        }
                    }
                    if(receivedData instanceof requestMyPredecessor){
                        requestMyPredecessor requestMyPredecessor=(requestMyPredecessor)receivedData;
                        String stringFileContent;
                        nameOfFileToBeSentToSuccessor=requestMyPredecessor.nameOfFile;
                        if(myLocalMap.get(nameOfFileToBeSentToSuccessor)==null || myLocalMap.get(nameOfFileToBeSentToSuccessor).isEmpty()) {
                            int n;
                            File newFile = getContext().getFileStreamPath(nameOfFileToBeSentToSuccessor);
                            FileInputStream fis = getContext().openFileInput(nameOfFileToBeSentToSuccessor);
                            StringBuffer fileContent = new StringBuffer("");
                            String stringFileContent1;
                            // Reference : http://stackoverflow.com/questions/9095610/android-fileinputstream-read-txt-file-to-string
                            byte[] buffer = new byte[1024];
                            if (newFile.exists()) {
                                while ((n = fis.read(buffer)) != -1) {
                                    fileContent.append(new String(buffer, 0, n));
                                }
                            }
                            stringFileContent1 = fileContent.toString();
                            contentOfFileToBeSentToSuccessor=stringFileContent1;
                        }
                        else {
                            contentOfFileToBeSentToSuccessor = myLocalMap.get(nameOfFileToBeSentToSuccessor);
                        }
                        singleMessageToBeSentToSuccessor=nameOfFileToBeSentToSuccessor+"#"+contentOfFileToBeSentToSuccessor;
                        //Log.d("singleMessageToBeSent",singleMessageToBeSent);
                        new sendMessageToRequesterInQuery().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, singleMessageToBeSentToSuccessor, requestMyPredecessor.requesterPort);
                    }
                }
            }
            catch (IOException e){Log.e(TAG, "IOException2");}
            catch (ClassNotFoundException e){Log.e(TAG, "ClassNotFoundException");}
            return null;
        }
    }
    private class ClientTask62Primary extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage62Primary im = new InsertMessage62Primary();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[0]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 62Primary. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException3   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask62FirstSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage62FirstSuccessor im = new InsertMessage62FirstSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[0]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 62FirstSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException18   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask62SecondSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage62SecondSuccessor im = new InsertMessage62SecondSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[0]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 62SecondSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException5   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask56Primary extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage56Primary im = new InsertMessage56Primary();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[1]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 56Primary. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException6   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask56FirstSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage56FirstSuccessor im = new InsertMessage56FirstSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[1]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 56FirstSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException7   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask56SecondSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage56SecondSuccessor im = new InsertMessage56SecondSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[1]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 56SecondSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException8   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask54Primary extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage54Primary im = new InsertMessage54Primary();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[2]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 54Primary. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException9   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask54FirstSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage54FirstSuccessor im = new InsertMessage54FirstSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[2]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 54FirstSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException10   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask54SecondSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage54SecondSuccessor im = new InsertMessage54SecondSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[2]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 54SecondSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException11   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask58Primary extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage58Primary im = new InsertMessage58Primary();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[3]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 68Primary. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException12   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask58FirstSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage58FirstSuccessor im = new InsertMessage58FirstSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[3]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 58FirstSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException13   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask58SecondSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage58SecondSuccessor im = new InsertMessage58SecondSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[3]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 58SecondSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException14   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask60Primary extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage60Primary im = new InsertMessage60Primary();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[4]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 60Primary. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException15   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask60FirstSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage60FirstSuccessor im = new InsertMessage60FirstSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[4]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 60FirstSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException16   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class ClientTask60SecondSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InsertMessage60SecondSuccessor im = new InsertMessage60SecondSuccessor();
                im.nameOfFile = msgs[0];
                im.contentOfFile = msgs[1];
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(arrayOfNodes[4]) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(im);
                    Log.d("Inserting in 60SecondSuccessor. Key: "+im.nameOfFile," Value: "+im.contentOfFile);
                }
                catch (UnknownHostException e) {Log.e(TAG, "UnknownHostException");}
                catch (IOException e) {Log.e(TAG, "IOException17   "+im.nameOfFile);}
            }catch (Exception e){Log.e(TAG, "Some problem while inserting data");}
            return null;
        }
    }
    private class requestSingleMessageFirstSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String nameOfFile = msgs[0];
                //Log.d("requestSingleMessageFirstSuccessor. Key:", nameOfFile);
                String requesterPort = msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                requestSingleMessage rsm = new requestSingleMessage();
                rsm.nameOfFile = nameOfFile;
                rsm.requesterPort = requesterPort;
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(myFirstSuccessor) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(rsm);
                } catch (UnknownHostException e) {
                    Log.e(TAG, "UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "IOFException. Key: "+nameOfFile);
                    //countFirstSuccessor=1000;
                    try {
                        socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(mySecondSuccessor) * 2);
                        outputstream = socket.getOutputStream();
                        objectoutputstream = new ObjectOutputStream(outputstream);
                        objectoutputstream.writeObject(rsm);
                        //Log.d("Request delegated","Request delegated");
                    }catch (IOException f){Log.e(TAG, "IOFExceptionOccuredTwice. Key: "+nameOfFile);}
                }
            }catch (Exception e){Log.e(TAG, "Some problem while query data");}
            return null;
        }
    }
    private class requestSingleMessageSecondSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String nameOfFile = msgs[0];
                //Log.d("requestSingleMessageSecondSuccessor. Key:", nameOfFile);
                String requesterPort = msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                requestSingleMessage rsm = new requestSingleMessage();
                rsm.nameOfFile = nameOfFile;
                rsm.requesterPort = requesterPort;
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(mySecondSuccessor) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(rsm);
                } catch (UnknownHostException e) {
                    Log.e(TAG, "UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "IOSException. Key: "+nameOfFile);
                    //countSecondSuccessor=1000;
                    try {
                        socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(mySecondPredecessor) * 2);
                        outputstream = socket.getOutputStream();
                        objectoutputstream = new ObjectOutputStream(outputstream);
                        objectoutputstream.writeObject(rsm);
                        //Log.d("Request delegated","Request delegated");
                    }catch (IOException f){Log.e(TAG, "IOSExceptionOccuredTwice. Key: "+nameOfFile);}
                }
            }catch (Exception e){Log.e(TAG, "Some problem while query data");}
            return null;
        }
    }
    private class requestSingleMessageSecondPredecessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String nameOfFile = msgs[0];
                //Log.d("requestSingleMessageSecondSuccessor. Key:", nameOfFile);
                String requesterPort = msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                requestSingleMessage rsm = new requestSingleMessage();
                rsm.nameOfFile = nameOfFile;
                rsm.requesterPort = requesterPort;
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(mySecondPredecessor) * 2);
                    outputstream = socket.getOutputStream();
                    objectoutputstream = new ObjectOutputStream(outputstream);
                    objectoutputstream.writeObject(rsm);
                } catch (UnknownHostException e) {
                    Log.e(TAG, "UnknownHostException1");
                } catch (IOException e) {
                    Log.e(TAG, "IOSException1. Key: "+nameOfFile);
                }
            }catch (Exception e){Log.e(TAG, "Some problem while query data1");}
            return null;
        }
    }
    private class sendMessageToRequester extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String nameAndContentToBeSent=msgs[0];
                String requesterPort=msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                sendRequestedMessage srm=new sendRequestedMessage();
                int delimiter=nameAndContentToBeSent.indexOf('#');
                srm.nameOfFile=nameAndContentToBeSent.substring(0,delimiter);
                srm.contentOfFile=nameAndContentToBeSent.substring(delimiter+1,nameAndContentToBeSent.length());
                Log.d("Sending message. Key: "+srm.nameOfFile," Value: "+srm.contentOfFile);
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(requesterPort)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(srm);
                Log.d("Message almost Sent. Key: "+srm.nameOfFile," Value: "+srm.contentOfFile);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "IOMRException");
            }
            return null;
        }
    }
    private class sendMessageToRequesterInQuery extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String nameAndContentToBeSent=msgs[0];
                String requesterPort=msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                sendRequestedMessageToSuccessor srmts=new sendRequestedMessageToSuccessor();
                int delimiter=nameAndContentToBeSent.indexOf('#');
                srmts.nameOfFile=nameAndContentToBeSent.substring(0,delimiter);
                srmts.contentOfFile=nameAndContentToBeSent.substring(delimiter+1,nameAndContentToBeSent.length());
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(requesterPort)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(srmts);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "IOMRException");
            }
            return null;
        }
    }
    private class RequestForStar62 extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String requesterPort=msgs[0];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                RequestForStar rfs=new RequestForStar();
                rfs.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(arrayOfNodes[0])*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(rfs);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "IOException");
            }
            return null;
        }
    }
    private class RequestForStar56 extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String requesterPort=msgs[0];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                RequestForStar rfs=new RequestForStar();
                rfs.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(arrayOfNodes[1])*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(rfs);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "IOException");
            }
            return null;
        }
    }
    private class RequestForStar54 extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String requesterPort=msgs[0];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                RequestForStar rfs=new RequestForStar();
                rfs.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(arrayOfNodes[2])*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(rfs);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "IOException");
            }
            return null;
        }
    }
    private class RequestForStar58 extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String requesterPort=msgs[0];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                RequestForStar rfs=new RequestForStar();
                rfs.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(arrayOfNodes[3])*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(rfs);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "IOException");
            }
            return null;
        }
    }
    private class RequestForStar60 extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String requesterPort=msgs[0];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                RequestForStar rfs=new RequestForStar();
                rfs.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(arrayOfNodes[4])*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(rfs);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "IOException");
            }
            return null;
        }
    }
    private class SendMessagesOfMyAVD extends AsyncTask<Map, Void, Void> {
        @Override
        protected Void doInBackground(Map... msgs) {
            try {
                String requesterPort="";
                Iterator it = msgs[1].entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    requesterPort=pair.getKey().toString();
                    it.remove(); // avoids a ConcurrentModificationException
                }
                //String requesterPort=msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                SendMessageOfMyAVD smoma=new SendMessageOfMyAVD();
                smoma.localMap.putAll(msgs[0]);
                //smoma.localMap=localMap;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(requesterPort)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(smoma);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "1IOMAException");
            }
            return null;
        }
    }
    private class InformSecondSuccessorThatMUp extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String identifier=msgs[0];
                String requesterPort=msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InformSecondSuccessor iss=new InformSecondSuccessor();
                iss.identifier=identifier;
                iss.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(myFirstSuccessor)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                //Log.d("InformSecondSuccessorThatMUp. ",mySecondSuccessor);
                objectoutputstream.writeObject(iss);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "2IOMAException");
            }
            return null;
        }
    }
    private class InformSecondPredecessorThatMUp extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String identifier=msgs[0];
                String requesterPort=msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InformSecondPredecessor isp=new InformSecondPredecessor();
                isp.identifier=identifier;
                isp.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(mySecondPredecessor)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                //Log.d("InformSecondPredecessorThatMUp. ",mySecondPredecessor);
                objectoutputstream.writeObject(isp);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "3IOMAException");
            }
            return null;
        }
    }
    private class InformFirstPredecessorThatMUp extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String identifier=msgs[0];
                String requesterPort=msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                InformFirstPredecessor ifp=new InformFirstPredecessor();
                ifp.identifier=identifier;
                ifp.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(myFirstPredecessor)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                //Log.d("InformFirstPredecessorThatMUp. ",myFirstPredecessor);
                objectoutputstream.writeObject(ifp);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "4IOMAException");
            }
            return null;
        }
    }
    private class GiveSecondPredecessorItsMap extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                //String requesterPort=msgs[0];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                GiveSecondPredecessor gsp=new GiveSecondPredecessor();
                myMap.clear();
                Iterator it = mapToBeSentSecondPredecessor.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    myMap.put(pair.getKey().toString(),pair.getValue().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                mapToBeSentSecondPredecessor.clear();
                //Log.d("Verifying the size of mapToBeSent in GiveSecondPredecessorItsMap before. ", String.valueOf(gsp.mapToBeSentSecondPredecessor.size()));
                gsp.mapToBeSentSecondPredecessor=new HashMap<>();
                it = myMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    gsp.mapToBeSentSecondPredecessor.put(pair.getKey().toString(), pair.getValue().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                myMap.clear();
                //Log.d("Verifying the size of mapToBeSent in GiveSecondPredecessorItsMap after. ",String.valueOf(gsp.mapToBeSentSecondPredecessor.size()));

                myMap1.clear();
                it = mapToBeSentFirstPredecessor.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    myMap1.put(pair.getKey().toString(),pair.getValue().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                mapToBeSentFirstPredecessor.clear();
                //Log.d("AgainVerifying the size of mapToBeSent in GiveSecondPredecessorItsMap before. ", String.valueOf(gsp.mapToBeSentFirstPredecessor.size()));
                gsp.mapToBeSentFirstPredecessor=new HashMap<>();
                it = myMap1.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    gsp.mapToBeSentFirstPredecessor.put(pair.getKey().toString(),pair.getValue().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                myMap1.clear();
                //Log.d("AgainVerifying the size of mapToBeSent in GiveSecondPredecessorItsMap after. ",String.valueOf(gsp.mapToBeSentFirstPredecessor.size()));

                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(myFirstPredecessor)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                //Log.d("Verifying the size of mapToBeSent in GiveSecondPredecessorItsMap. ",String.valueOf(gsp.mapToBeSentSecondPredecessor.size()));
                //Log.d("AgainVerifying the size of mapToBeSent in GiveSecondPredecessorItsMap. ",String.valueOf(gsp.mapToBeSentFirstPredecessor.size()));
                objectoutputstream.writeObject(gsp);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "5IOMAException");
            }
            return null;
        }
    }
    private class GiveSecondSuccessorItsMap extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String requesterPort=msgs[0];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                GiveSecondSuccessor gss=new GiveSecondSuccessor();
                myMap.clear();
                Iterator it = mapToBeSentSecondSuccessor.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    myMap.put(pair.getKey().toString(),pair.getValue().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                mapToBeSentSecondSuccessor.clear();
                //Log.d("Verifying the size of mapToBeSent in GiveSecondPredecessorItsMap before. ", String.valueOf(gss.mapToBeSentSecondSuccessor.size()));
                gss.mapToBeSentSecondSuccessor=new HashMap<>();
                it = myMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    gss.mapToBeSentSecondSuccessor.put(pair.getKey().toString(),pair.getValue().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                myMap.clear();
                //Log.d("Verifying the size of mapToBeSent in GiveSecondPredecessorItsMap after. ",String.valueOf(gss.mapToBeSentSecondSuccessor.size()));
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(mySecondSuccessor)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                //Log.d("Verifying the size of mapToBeSent in GiveSecondSuccessorItsMap. ",String.valueOf(gss.mapToBeSentSecondSuccessor.size()));
                objectoutputstream.writeObject(gss);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "6IOMAException");
            }
            return null;
        }
    }
    private class GiveFirstSuccessorItsMap extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String requesterPort=msgs[0];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                GiveFirstSuccessor gfs=new GiveFirstSuccessor();
                Iterator it = mapToBeSentFirstSuccessor.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    myMap.put(pair.getKey().toString(),pair.getValue().toString());
                    //Log.d("Again: Should be Inserted in first successor. Key: "+pair.getKey().toString()," Value: "+pair.getValue().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                //mapToBeSentFirstSuccessor.clear();
                //Log.d("Verifying the size of mapToBeSent in GiveSecondPredecessorItsMap before. ",String.valueOf(gfs.mapToBeSentFirstSuccessor.size()));
                gfs.mapToBeSentFirstSuccessor.putAll(myMap);
                //Log.d("Verifying the size of mapToBeSent in GiveSecondPredecessorItsMap after. ",String.valueOf(gfs.mapToBeSentFirstSuccessor.size()));
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(myFirstSuccessor)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                //Log.d("Verifying the size of mapToBeSent in GiveFirstSuccessorItsMap. My first predecessor is: "+myFirstSuccessor," Size sent: "+String.valueOf(gfs.mapToBeSentFirstSuccessor.size()));
                objectoutputstream.writeObject(gfs);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "7IOMAException");
            }
            return null;
        }
    }
    private class ForwardMessagesToFirstSuccessor extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                FrwrdMessagesToFirstSuccessor fmtfs=new FrwrdMessagesToFirstSuccessor();
                fmtfs.mapToForward=myLocalMap;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(myFirstSuccessor)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(fmtfs);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "8IOMAException");
            }
            return null;
        }
    }
    private class requestMyPredecessorForMessage extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String nameOfFile=msgs[0];
                String requesterPort=msgs[1];
                Socket socket;
                OutputStream outputstream;
                ObjectOutputStream objectoutputstream;
                requestMyPredecessor rmp=new requestMyPredecessor();
                rmp.nameOfFile=nameOfFile;
                rmp.requesterPort=requesterPort;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(myFirstPredecessor)*2);
                outputstream=socket.getOutputStream();
                objectoutputstream=new ObjectOutputStream(outputstream);
                objectoutputstream.writeObject(rmp);
            }catch (UnknownHostException e){
                Log.e(TAG, "UnknownHostException");
            }catch (IOException e){
                Log.e(TAG, "8IOMAException");
            }
            return null;
        }
    }
}