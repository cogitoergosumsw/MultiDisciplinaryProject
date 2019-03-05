package robot;
//
//import utils.CommMgr;
//import utils.MapDescriptor;
import java.util.concurrent.TimeUnit;

import map.Cell;
import map.Map;
import robot.RobotConstants.DIRECTION;
import simulator.Constants;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;
import utils.MapDescriptor;
import robot.Sensor;
/**
 * defines the robot 
 * @author Lyu Xintong Isabelle
 * 
 * 
 * the robot is left wall hugging and has:
 *  3 short range sensor in front
 *  2 short range sensor on the left
 *  1 long range sensor on the right
 * 
 *         ^    ^    ^
 *        SR1  SR2  SR3
 *   < SR1 [X] [X] [X] LR >
 *   < SR2 [X] [X] [X] 
 *         [X] [X] [X]
 * 
 */

public class Robot{
    private int posRow; // center cell
    private int posCol; // center cell
    private DIRECTION robotDir;
    private int speed;
	public final Sensor SSFront1;       // senor (short) in front 
	private final Sensor SSFront2;     // senor (short) in front 
	private final Sensor SSFront3;      // senor (short) in front
	private final Sensor SSLeft1;            // senor (short) on the left 
	private final Sensor SSLeft2;           // senor (short) on the left 
	private final Sensor SLRight;            // senor (long) on the right 
    private boolean touchedGoal;
    private final boolean realBot;
   

    public Robot(int row, int col, boolean realBot) {
        posRow = row;
        posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;
        this.realBot = realBot;

        // initialise sensorS    
        SSFront1 = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, this.robotDir, "SSFront1");
        SSFront2 = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol, this.robotDir, "SRSSFront2");
        SSFront3 = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, this.robotDir, "SSFront3");
        SSLeft1 = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1,findNewDirection(MOVEMENT.LEFT) , "SSLeft1");
        SSLeft2 = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "SSLeft2");
        SLRight = new Sensor(RobotConstants.SENSOR_LONG_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow+1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT) , "SLRight");
        
    }
    

    public void setRobotPos(int row, int col) {
        posRow = row;
        posCol = col;
    }

    public int getRobotPosRow() {
        return posRow;
    }

    public int getRobotPosCol() {
        return posCol;
    }

    public void setRobotDir(DIRECTION dir) {
        robotDir = dir;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public DIRECTION getRobotCurDir() {
        return robotDir;
    }

    public boolean getRealBot() {
        return realBot;
    }

    private void updateTouchedGoal() {
        if (this.getRobotPosRow() == RobotConstants.GOAL_ROW && this.getRobotPosCol() == RobotConstants.GOAL_COL)
            this.touchedGoal = true;
    }

    public boolean getTouchedGoal() {
        return this.touchedGoal;
    }

    /**
     * Takes in a MOVEMENT and moves the robot accordingly by changing its position and direction. Sends the movement
     * if this.realBot is set.
     */
    public void move(MOVEMENT m, boolean sendMoveToAndroid){
        if (!realBot) {
            // Emulate real movement by pausing execution.
            try {
                TimeUnit.MILLISECONDS.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }
        }
           
        switch (m) {
        case FORWARD:
            switch (robotDir) {
                case NORTH:
                    posRow++;
                    break;
                case EAST:
                    posCol++;
                    break;
                case SOUTH:
                    posRow--;
                    break;
                case WEST:
                    posCol--;
                    break;
            }
            break;
        case BACKWARD:
            switch (robotDir) {
                case NORTH:
                    posRow--;
                    break;
                case EAST:
                    posCol--;
                    break;
                case SOUTH:
                    posRow++;
                    break;
                case WEST:
                    posCol++;
                    break;
            }
            break;
        case RIGHT:
        case LEFT:
            robotDir = findNewDirection(m);
            break;
        case CALIBRATE:
            break;
        default:
            System.out.println("Error in Robot.move()!");
            break;
    }

    if (realBot) sendMovement(m, sendMoveToAndroid);
    
    System.out.println("Move: " + MOVEMENT.print(m));
    
    updateTouchedGoal();

    }

    /**
     * Overloaded method that calls this.move(MOVEMENT m, boolean sendMoveToAndroid = true).
     */
    public void move(MOVEMENT m) {
        this.move(m, true);
    }


    public void moveForwardMultiple(int count) {
        if (count == 1) {
            move(MOVEMENT.FORWARD);
        } else {
            CommMgr comm = CommMgr.getCommMgr();
            
        	String msg = "F";
        	for (int i = 0; i< count - 1; i++){
        		msg += "F";
        	}
            comm.sendMsg(msg, CommMgr.MOVE);
            
            
//            switch (robotDir) {
//                case NORTH:
//                    posRow += count;
//                    break;
//                case EAST:
//                    posCol += count;
//                    break;
//                case SOUTH:
//                    posRow += count;
//                    break;
//                case WEST:
//                    posCol += count;
//                    break;
//            }
//            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.BOT_POS);
        }
    }
    
    
    /**
     * Uses the CommMgr to send the next movement to the robot.
     */
    private void sendMovement(MOVEMENT m, boolean sendMoveToAndroid) {
        CommMgr comm = CommMgr.getCommMgr();
        comm.sendMsg(Character.toString(MOVEMENT.print(m)), CommMgr.MOVE);
//        if (m != MOVEMENT.CALIBRATE && sendMoveToAndroid) {
//            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.BOT_POS);
//        }
    }
    
    /**
     * Sets the sensors' position and direction according to the robot's current position and direction.
     */
    
    public void setSensors() {
        switch (robotDir) {
            case NORTH:
                SSFront1.setSensor(this.posRow + 1, this.posCol - 1, DIRECTION.NORTH);
                SSFront2.setSensor(this.posRow + 1, this.posCol, DIRECTION.NORTH);
                SSFront3.setSensor(this.posRow + 1, this.posCol + 1, DIRECTION.NORTH);
                SSLeft1.setSensor(this.posRow + 1, this.posCol - 1, DIRECTION.WEST);
                SSLeft2.setSensor(this.posRow, this.posCol - 1,  DIRECTION.WEST);
                SLRight.setSensor(this.posRow + 1, this.posCol + 1,  DIRECTION.EAST);
                break;
            case EAST:
                SSFront1.setSensor(this.posRow + 1, this.posCol + 1, DIRECTION.EAST);
                SSFront2.setSensor(this.posRow, this.posCol + 1, DIRECTION.EAST);
                SSFront3.setSensor(this.posRow - 1, this.posCol + 1, DIRECTION.EAST);
                SSLeft1.setSensor(this.posRow + 1, this.posCol + 1, DIRECTION.NORTH);
                SSLeft2.setSensor(this.posRow + 1, this.posCol, DIRECTION.NORTH);
                SLRight.setSensor(this.posRow - 1, this.posCol + 1, DIRECTION.SOUTH);
                break;
            case SOUTH:
                SSFront1.setSensor(this.posRow - 1, this.posCol + 1, DIRECTION.SOUTH);
                SSFront2.setSensor(this.posRow - 1, this.posCol, DIRECTION.SOUTH);
                SSFront3.setSensor(this.posRow - 1, this.posCol - 1, DIRECTION.SOUTH);
                SSLeft1.setSensor(this.posRow - 1, this.posCol + 1, DIRECTION.EAST);
                SSLeft2.setSensor(this.posRow, this.posCol + 1, DIRECTION.EAST);
                SLRight.setSensor(this.posRow - 1, this.posCol - 1, DIRECTION.WEST);
                break;
            case WEST:
                SSFront1.setSensor(this.posRow - 1, this.posCol - 1, DIRECTION.WEST);
                SSFront2.setSensor(this.posRow, this.posCol - 1, DIRECTION.WEST);
                SSFront3.setSensor(this.posRow + 1, this.posCol - 1, DIRECTION.WEST);
                SSLeft1.setSensor(this.posRow - 1, this.posCol - 1, DIRECTION.SOUTH);
                SSLeft2.setSensor(this.posRow - 1, this.posCol, DIRECTION.SOUTH);
                SLRight.setSensor(this.posRow + 1, this.posCol - 1, DIRECTION.NORTH);
                break;
        }
    }
   
    

    /**
     * Uses the current direction of the robot and the given movement to find the new direction of the robot.
     */
    private DIRECTION findNewDirection(MOVEMENT m) {
        if (m == MOVEMENT.RIGHT) {
            return DIRECTION.getNext(robotDir);
        } else {
            return DIRECTION.getPrevious(robotDir);
        }
    }
    
    
    /**
     * @return an array of 6 elements for the 6 sensors. 1 = obstacle in front detected, -1 = no obstacle detected by the sensor
     * */
    public int[] sense(Map explorationMap, Map realMap) {
        int[] result = new int[6];

        if (!realBot) {
            result[0] = SSFront1.sense(explorationMap, realMap);
            result[1] = SSFront2.sense(explorationMap, realMap);
            result[2] = SSFront3.sense(explorationMap, realMap);
            result[3] = SSLeft1.sense(explorationMap, realMap);
            result[4] = SSLeft2.sense(explorationMap, realMap);
            result[5] = SLRight.sense(explorationMap, realMap);
        } else {
           
        	//debug
        	//System.out.println("debug: inside sense");
        	
        	CommMgr comm = CommMgr.getCommMgr();
            String msg = comm.recvMsg();
            String[] msgArr = msg.split("\\|");
            String[] readings = msgArr[1].split(",");
            
            //TODO change format of msg received 
            if (msgArr[0].equals(CommMgr.SENSOR_DATA)) {
                result[0] = Integer.parseInt(readings[0].replaceAll("\\p{Punct}", ""));
                result[1] = Integer.parseInt(readings[1].replaceAll("\\p{Punct}", ""));
                result[2] = Integer.parseInt(readings[2].replaceAll("\\p{Punct}", ""));
                result[3] = Integer.parseInt(readings[3].replaceAll("\\p{Punct}", ""));
                result[4] = Integer.parseInt(readings[4].replaceAll("\\p{Punct}", ""));
                result[5] = Integer.parseInt(readings[5].replaceAll("\\p{Punct}", ""));
            }
     
            
            //debug
            for (int i=0;i<6;i++){
            	System.out.println(result[i]);
            }
           
            
            
            SSFront1.senseReal(explorationMap, result[0]);
            SSFront2.senseReal(explorationMap, result[1]);
            SSFront3.senseReal(explorationMap, result[2]);
            SSLeft1.senseReal(explorationMap, result[3]);
            SSLeft2.senseReal(explorationMap, result[4]);
            SLRight.senseReal(explorationMap, result[5]);

            
            
            
            
            String[] mapStrings = MapDescriptor.generateMapDescriptor(explorationMap);
            //debug
            System.out.println(mapStrings[0]);
            System.out.println(mapStrings[1]);
            
            
            
            // TODO ???? send here ????
            comm.sendMsg(mapStrings[0], CommMgr.MAP_STRING1);
            comm.sendMsg(mapStrings[1], CommMgr.MAP_STRING2);
        }

        return result;
    }
    


	public void reset(int row, int col, DIRECTION dir){
    	setRobotPos(row, col);
    	setRobotDir(dir);
    	this.touchedGoal = false;
    }

	public Cell getCell() {
		return new Cell(this.getRobotPosRow(), this.getRobotPosCol());
	}
    
}
