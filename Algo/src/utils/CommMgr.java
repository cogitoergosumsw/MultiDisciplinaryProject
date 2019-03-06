package utils;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Communication manager to communicate with the different parts of the system via the RasPi.
 *
 * 
 */

public class CommMgr {

    public static final String EX_START = "EX_START";       // Android --> PC
    public static final String FP_START = "FP_START";       // Android --> PC
    public static final String EX_DONE = "EXPLORE_DONE";			// PC --> Android
    public static final String FP_DONE = "FASTEST_DONE";			// PC --> Android
    public static final String TIME_EX_START = "TIME_EX_START";
    public static final String COVERAGE_EX_START = "COVERAGE_EX_START";
    
    public static final String BOT_POS = "BOT_POS";         // PC --> Android
    public static final String BOT_START = "BOT_START";     // PC --> Arduino
    public static final String INSTRUCTIONS = "INSTR";      // PC --> Arduino
    public static final String MAP_STRING1 ="EXPLORED_DATA"; // PC --> Arduino
    public static final String MAP_STRING2 = "OBSTACLE_DATA"; // PC --> Arduino
    public static final String SENSOR_DATA = "SENSOR_DATA";       // Arduino --> PC
    public static final String WAYPOINT_DATA = "WAYPOINT_DATA"; // // Arduino --> PC

    public static final String MOVE = "MOVE";
    
    
    
    private static CommMgr commMgr = null;
    private static Socket conn = null;

    private BufferedWriter writer;
    private BufferedReader reader;

    private CommMgr() {
    }

    

    public static CommMgr getCommMgr() {
        if (commMgr == null) {
            commMgr = new CommMgr();
        }
        return commMgr;
    }

    public void openConnection() {
        System.out.println("Opening connection...");

        try {
            String HOST = "192.168.18.18";
            int PORT = 12317;
//        	String HOST = "127.0.0.1";
//        	int PORT = 10010;
//        	
            conn = new Socket(HOST, PORT);
            System.out.println("connected...");
            
            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(conn.getOutputStream())));
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            System.out.println("openConnection() --> " + "Connection established successfully!");

            return;
        } catch (UnknownHostException e) {
            System.out.println("openConnection() --> UnknownHostException");
        } catch (IOException e) {
            System.out.println("openConnection() --> IOException" + e.getMessage());
        } catch (Exception e) {
            System.out.println("openConnection() --> Exception");
            System.out.println(e.toString());
        }

        System.out.println("Failed to establish connection!");
    }

    public void closeConnection() {
        System.out.println("Closing connection...");

        try {
            reader.close();

            if (conn != null) {
                conn.close();
                conn = null;
            }
            System.out.println("Connection closed!");
        } catch (IOException e) {
            System.out.println("closeConnection() --> IOException");
        } catch (NullPointerException e) {
            System.out.println("closeConnection() --> NullPointerException");
        } catch (Exception e) {
            System.out.println("closeConnection() --> Exception");
            System.out.println(e.toString());
        }
    }

    public void sendMsg(String msg, String msgType) {
        System.out.println("Sending a message...");
        try {
            String outputMsg;
            if (msg == null) {
                outputMsg = msgType + ";\n";
            } else{
                outputMsg = msgType + "|" + msg + ";\n";
            }      
            
            System.out.println("Sending out message:\n" + outputMsg);
            writer.write(outputMsg);
            writer.flush();
        } catch (IOException e) {
            System.out.println("sendMsg() --> IOException");
        } catch (Exception e) {
            System.out.println("sendMsg() --> Exception");
            System.out.println(e.toString());
        }
    }

    public void sendMsg(String msg){
    	this.sendMsg(null, msg);
    }
    
    
    public String recvMsg() {
        System.out.println("\nReceiving a message...");

        try {
            StringBuilder sb = new StringBuilder();
            String input = reader.readLine();

            if (input != null && input.length() > 0) {
                sb.append(input);
                System.out.println(sb.toString());
                return sb.toString();
            }
        } catch (IOException e) {
            System.out.println("recvMsg() --> IOException");
        } catch (Exception e) {
            System.out.println("recvMsg() --> Exception");
            System.out.println(e.toString());
        }

        return null;
    }

    public boolean isConnected() {
        return conn.isConnected();
    }
}
