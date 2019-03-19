package server;

import map.Map;
import utils.CommMgr;
import utils.MapDescriptor;
import robot.Robot;
import robot.RobotConstants.DIRECTION;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class RequestHandler extends Thread {

    private static final Logger LOGGER = Logger.getLogger(RequestHandler.class.getName());
    private BufferedWriter out;
    private BufferedReader in;
    private Socket socket;
    private int msgCounter = 0;
    private String prevMsg = null;
    private Map realMap;
    private Map exploredMap;
    private Robot robot;
    
    
    // hard coded for testing
    private Point startPoint = new Point(7, 2);
    private Point wayPoint = new Point(13, 18);

    public RequestHandler(Socket socket) throws IOException {
        this.socket = socket;
        init();
    }

    private void init() throws IOException {
        out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
       
        robot = new Robot(1,1,false);
        exploredMap = new Map(robot);
        realMap = new Map(robot);
       // MapDescriptor.loadMapFromDisk(realMap, System.getProperty(System.getProperty("user.dir")+"/maps/Map2.txt"));
        
        
        //robot.setSensors();
        //robot.sense(exploredMap, realMap);
 
        System.out.println(realMap.calculateAreaExplored());
        
        
        
        
        
    }

    public void sendStartMsg() {
        String msg  ;
        send("EX_START");
        
    }
    
	public void sendReadings() {
		send("SENSOR_DATA|0,4,4,1,1,0");
		send("SENSOR_DATA|4,3,3,0,0,1");
		send("SENSOR_DATA|1,1,1,3,4,1");
		send("SENSOR_DATA|1,1,1,1,1,0");
		send("SENSOR_DATA|0,4,4,1,1,0");
		send("SENSOR_DATA|0,4,4,1,1,0");
		send("SENSOR_DATA|4,3,4,1,1,0");
		send("SENSOR_DATA|3,3,3,1,1,3");
		send("SENSOR_DATA|2,2,2,1,1,2");
		send("SENSOR_DATA|1,1,1,1,1,1");
		send("SENSOR_DATA|1,1,1,0,0,1");
		send("SENSOR_DATA|0,0,4,3,1,1");
		send("SENSOR_DATA|0,0,4,0,2,1");
		send("SENSOR_DATA|2,3,0,2,2,0");
		send("SENSOR_DATA|1,2,0,1,2,0");
		send("SENSOR_DATA|0,0,0,0,1,2");
		send("SENSOR_DATA|4,4,0,0,2,2");
		send("SENSOR_DATA|2,0,0,2,3,4");
		send("SENSOR_DATA|1,0,4,1,3,4");
		send("SENSOR_DATA|4,4,4,0,1,3");
		send("SENSOR_DATA|3,3,3,0,0,3");
		send("SENSOR_DATA|0,4,4,2,4,3");
		send("SENSOR_DATA|0,4,4,1,3,1");
		send("SENSOR_DATA|0,3,4,0,2,3");
		send("SENSOR_DATA|0,3,3,0,1,3");
		send("SENSOR_DATA|0,2,2,0,0,2");
		send("SENSOR_DATA|0,0,0,0,0,0");
		send("SENSOR_DATA|0,0,0,1,0,0");
		send("SENSOR_DATA|0,0,0,2,0,0");
		send("SENSOR_DATA|0,0,0,3,1,0");
		send("SENSOR_DATA|0,0,0,4,2,3");
		send("SENSOR_DATA|2,3,4,0,0,0");
		send("SENSOR_DATA|1,2,3,1,0,0");
		send("SENSOR_DATA|0,0,0,3,1,4");
		send("SENSOR_DATA|0,0,0,4,2,0");
		send("SENSOR_DATA|2,3,4,2,0,0");
		send("SENSOR_DATA|1,2,4,1,0,0");
		send("SENSOR_DATA|0,0,0,3,1,0");
		send("SENSOR_DATA|0,0,0,4,2,0");
		send("SENSOR_DATA|2,0,4,2,0,0");
		send("SENSOR_DATA|1,4,4,1,3,0");
		send("SENSOR_DATA|0,0,0,3,1,0");
		send("SENSOR_DATA|0,0,0,4,4,0");
		send("SENSOR_DATA|4,4,4,2,4,0");
		send("SENSOR_DATA|3,3,3,1,3,3");
		send("SENSOR_DATA|2,2,2,1,2,3");
		send("SENSOR_DATA|3,4,0,2,2,0");
		send("SENSOR_DATA|3,3,4,2,2,0");
		send("SENSOR_DATA|2,2,2,2,3,2");
		send("SENSOR_DATA|1,1,1,2,2,2");
		send("SENSOR_DATA|2,2,2,0,0,1");
		send("SENSOR_DATA|1,1,1,2,2,2");
		send("SENSOR_DATA|3,3,2,1,1,0");
		send("SENSOR_DATA|1,1,1,1,1,0");
		send("SENSOR_DATA|1,1,1,3,3,1");
		send("SENSOR_DATA|1,1,1,1,1,0");
		send("SENSOR_DATA|0,0,0,1,1,3");
		send("SENSOR_DATA|0,0,0,0,1,4");
		send("SENSOR_DATA|0,0,0,0,1,0");
		send("SENSOR_DATA|4,0,0,0,0,0");
		send("SENSOR_DATA|0,0,4,4,4,4");
		send("SENSOR_DATA|0,0,4,1,4,4");
		send("SENSOR_DATA|4,4,4,1,4,0");
		send("SENSOR_DATA|4,4,4,4,1,0");
		send("SENSOR_DATA|3,3,3,4,1,0");
		send("SENSOR_DATA|2,2,2,4,4,0");
		send("SENSOR_DATA|4,4,4,0,0,2");
		send("SENSOR_DATA|3,3,3,1,0,1");
		send("SENSOR_DATA|2,2,2,2,0,2");
		send("SENSOR_DATA|1,1,1,2,1,2");
		send("SENSOR_DATA|2,2,1,1,0,1");
		send("SENSOR_DATA|0,0,1,3,2,2");
		send("SENSOR_DATA|0,0,0,3,1,1");
		send("SENSOR_DATA|0,0,0,4,3,4");
		send("SENSOR_DATA|3,3,4,3,3,0");
		send("SENSOR_DATA|2,3,3,1,4,0");
		send("SENSOR_DATA|3,4,1,3,3,2");
		send("SENSOR_DATA|2,3,3,1,4,0");
		send("SENSOR_DATA|0,0,4,3,2,0");
		send("SENSOR_DATA|0,4,4,3,3,0");
		send("SENSOR_DATA|0,4,4,4,3,0");
		send("SENSOR_DATA|0,3,3,4,4,0");
		send("SENSOR_DATA|1,1,2,4,4,3");
		send("SENSOR_DATA|3,1,0,0,2,0");
		send("SENSOR_DATA|4,4,4,1,3,2");
		send("SENSOR_DATA|1,3,2,1,0,1");
		send("SENSOR_DATA|1,1,1,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,3");
		send("SENSOR_DATA|1,2,2,1,0,3");
		send("SENSOR_DATA|1,1,2,1,0,3");
		send("SENSOR_DATA|1,1,2,1,0,3");
		send("SENSOR_DATA|1,1,2,1,0,4");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,3");
		send("SENSOR_DATA|1,2,2,1,0,3");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,3,3,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,2,2,1,0,2");
		send("SENSOR_DATA|1,3,3,1,0,2");
		send("SENSOR_DATA|1,3,3,1,0,2");
		
		// String message = receive();
		
		// while (!message.contains("EX_DONE;" )){
			
		// 	message = receive();
			
		// }
		
		// try {
		// 	Thread.sleep(8000);
		// } catch (InterruptedException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }
		
		// send("FP_START|;");
		
		
		
		
	}

    public void sendWayPoint() {
    	send("WP|3,3;");
    }

    // @Override
    // public void run() {
    //     try {
    //         System.out.println( "Received a connection" );
    //         String data;

//            TimeUnit.MILLISECONDS.sleep(1000);

            // send "checklist"
            // send(NetworkConstants.START_CHECKLIST);
            // sendStartMsg();

            // while (true) {
            //     // wait for incoming data
            //     do {
            //         data = receive();
            //     } while(data == null);

            //     handle(data);
            // }

            // Close our connection
//            in.close();
//            out.close();
//            socket.close();

//            System.out.println( "Connection closed" );
    //     }
    //     catch( Exception e )
    //     {
    //         e.printStackTrace();
    //     }
    // }

    // public void handle(String msg) throws InterruptedException {
    //     char firstChar;
    //     msg = msg.substring(1);
    //     firstChar = msg.charAt(0);

    //     if (firstChar == '{') {
    //         System.out.println("Unhandled: " + msg);
    //     }
    //     else {
    //         String[] commands = msg.split("\\|");
    //         for (String cmd: commands) {
    //             execute_command(cmd);
    //         }
    //     }
    // }

    // private void execute_command(String cmd) throws InterruptedException {
    //     char firstChar = cmd.charAt(0);
    //     int step = 1;
    //     if (cmd.length() > 1) {
    //         step = Integer.parseInt(cmd.substring(1));
    //     }
    //     switch (firstChar) {
    //         case 'U':
    //             sendSensorRes();
    //             break;
    //         case 'W':
    //             robot.move(Command.FORWARD, step, exploredMap, RobotConstants.STEP_PER_SECOND);
    //             sendSensorRes();
    //             break;
    //         case 'X':
    //             robot.move(Command.BACKWARD, step, exploredMap, RobotConstants.STEP_PER_SECOND);
    //             sendSensorRes();
    //             break;
    //         case 'D':
    //             robot.turn(Command.TURN_RIGHT, RobotConstants.STEP_PER_SECOND);
    //             sendSensorRes();
    //             break;
    //         case 'A':
    //             robot.turn(Command.TURN_LEFT, RobotConstants.STEP_PER_SECOND);
    //             sendSensorRes();
    //             break;
    //         default:
    //             LOGGER.warning("Wrong char, do nothing");
    //             break;
    //     }
    //     robot.sense(exploredMap, realMap);
    // }

    // public void sendSensorRes() {
    //     HashMap<String, Integer> sensorRes = robot.getSensorRes(exploredMap, realMap);
    //     send(formatSensorRes(sensorRes));
    // }

    // public String formatSensorRes(HashMap<String, Integer> sensorRes) {
    //     StringBuilder sb = new StringBuilder();
    //     int obsBlock;
    //     for (String sname: robot.getSensorList()) {
    //         sb.append(sname);
    //         sb.append(":");
    //         obsBlock = sensorRes.get(sname);
    //         if (obsBlock == -1) {
    //             sb.append(obsBlock);
    //         }
    //         else {
    //             sb.append(obsBlock - 1);
    //         }
    //         sb.append("|");
    //     }
    //     return sb.toString();
    // }

    /**
     * Sending a String type msg through socket
     * @param msg
     * @return true if the message is sent out successfully
     */
    public boolean send(String msg) {
        try {
            LOGGER.log(Level.FINE, "Sending Message...");
            out.write(msg);
            out.newLine();
            out.flush();
            msgCounter++;
            LOGGER.info(msgCounter +" Message Sent: " + msg);
            prevMsg = msg;
            return true;
        } catch (IOException e) {
            LOGGER.info("Sending Message Failed (IOException)!");
            return false;
        } catch (Exception e) {
            LOGGER.info("Sending Message Failed!");
            e.printStackTrace();
            return false;
        }
    }

    public String receive() {
        try {
            LOGGER.log(Level.FINE, "Receving Message...");
            String receivedMsg = in.readLine();
            if(receivedMsg != null && receivedMsg.length() > 0) {
                LOGGER.info("Received in receive(): " + receivedMsg);
                return receivedMsg;
            }
        } catch(IOException e) {
            LOGGER.info("Receiving Message Failed (IOException)!");
        } catch(Exception e) {
            LOGGER.info("Receiving Message Failed!");
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        RequestHandler handler = new RequestHandler(null);
    }


}
