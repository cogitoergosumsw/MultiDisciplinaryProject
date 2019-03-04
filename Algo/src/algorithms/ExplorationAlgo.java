package algorithms;

import map.Cell;
import map.Map;
import simulator.Constants;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;
import simulator.Constants;

/**
 * Exploration algorithm for the robot.
 *
 */

public class ExplorationAlgo {
    private final Map exploredMap;
    private final Map realMap;
    private final Robot bot;
    private final int coverageLimit;
    private final int timeLimit;
    private int areaExplored;
    private long startTime;
    private long endTime;
    private int lastCalibrate;
    private boolean calibrationMode;

    public ExplorationAlgo(Map exploredMap, Map realMap, Robot bot, int coverageLimit, int timeLimit) {
        this.exploredMap = exploredMap;
        this.realMap = realMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    /**
     * Main method that is called to start the exploration.
     */
    public void runExploration() {
    	//TODO
//        if (bot.getRealBot()) {
//            System.out.println("Starting calibration...");
//            CommMgr.getCommMgr().recvMsg();
//            if (bot.getRealBot()) {
//                bot.move(MOVEMENT.LEFT, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.LEFT, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.RIGHT, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.CALIBRATE, false);
//                CommMgr.getCommMgr().recvMsg();
//                bot.move(MOVEMENT.RIGHT, false);
//            }
    	
    	//TODO
//
//            while (true) {
//                System.out.println("Waiting for EX_START...");
//                String msg = CommMgr.getCommMgr().recvMsg();
//                //String[] msgArr = msg.split(";");
//                //if (msgArr[0].equals(CommMgr.EX_START)) break;   //????
//                if (msg.equals(CommMgr.EX_START)) break;
//            }
//        }
    	
    	System.out.println("debug: inside runExploration \n\n");
    	
        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg(null, CommMgr.BOT_START);
        }
        
        bot.setSensors();
        bot.sense(exploredMap, realMap);
        exploredMap.repaint();

        areaExplored = exploredMap.calculateAreaExplored();
        System.out.println("Explored Area: " + areaExplored);

        explorationLoop(bot.getRobotPosRow(), bot.getRobotPosCol());
    }

    /**
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    
    /**
     * @TODO move 3 steps if no obstacle in front 
     * */
    private void explorationLoop(int r, int c) {
        do {

        	nextMove(); 
            areaExplored = exploredMap.calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);
            
            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c && areaExplored != Constants.MAP_SIZE) {
                exploreUnexploredArea();
                break;          	      
            }
        } while (areaExplored < coverageLimit && System.currentTimeMillis() < endTime);

       
        goHome();
        
        //delete later 

//        
//        
//        if (bot.getRealBot()){
//        	 CommMgr.getCommMgr().sendMsg(CommMgr.EX_DONE);
//        }
//        
    }
    /**
     * explore unexplored area by going to the nearest unexplored cell
     * move for 5 steps, then go to nearest unexplored cell again
     * 
     * */
    
    private void exploreUnexploredArea(){
    	int areaExplored = exploredMap.calculateAreaExplored();
    	int newAreaExplored = 0;
    	int i = 0;
    	do {   		
    		if (i >= 4){
    			goToNearestUnexplored(bot.getCell());
    			i = 0;
    		}
    		nextMove();
    		i++;
    		newAreaExplored = exploredMap.calculateAreaExplored();
    	} while (newAreaExplored != Constants.MAP_SIZE); 	
    } 

    
    private void goToNearestUnexplored(Cell startCell){
    	
    	Cell nearestUnexplored = nearestUnexplored(startCell);
    	System.out.println(nearestUnexplored.toString());
    	Cell nearestExplored = nearestExplored(nearestUnexplored);
    	
    	System.out.println(nearestExplored.toString());
    	
    	if (!bot.getRealBot()){
    		FastestPathAlgo goToNearestUnexplored = new FastestPathAlgo(exploredMap, realMap, bot);     
    		goToNearestUnexplored.runFastestPath(nearestExplored.getRow(), nearestExplored.getCol());  
    	} else {
    		FastestPathAlgo goToNearestUnexplored = new FastestPathAlgo(exploredMap, bot);      
    		goToNearestUnexplored.runFastestPath(nearestExplored.getRow(), nearestExplored.getCol()); 
    	}
    }
    
    /**
     *  find the the nearest unexplored cell to the startCell
     */
    private Cell nearestUnexplored(Cell startCell){
    	int distance = 1000;
    	int temp;
    	Cell cell, nearest = null;
    	for (int r = 0; r < Constants.MAP_ROW; r++){
    		for (int c = 0; c < Constants.MAP_COL; c++){
    			cell = exploredMap.grid[r][c];
    			temp = Math.abs(r - startCell.getRow()) + Math.abs(c - startCell.getCol()) ;
    			if ( temp < distance && !cell.getIsExplored()){
    				nearest = cell;
    				distance = temp;
    			}
    		}
    	}
    	
    	return nearest;
    }
    
    /**
     *  find the nearest explored cell next to the nearest unexplored cell
     *  because the nearest unexplored might not be accessible 
     */
    
    private Cell nearestExplored(Cell unexploredCell){
    	Cell cell, nearest = null;
    	int temp, distance = 1000;
    	
    	for (int r = 0; r < Constants.MAP_ROW; r++){
    		for (int c = 0; c < Constants.MAP_COL; c++){
    			cell = exploredMap.grid[r][c];
    			temp = Math.abs(r - unexploredCell.getRow()) + Math.abs(c - unexploredCell.getCol()) ;
    			if ( temp < distance && isExploredAndFree(r, c)){
    				nearest = cell;
    				distance = temp;
    			}
    		}
    	}
    	return nearest;
    }
    
    
    /**
     * if there's no obstacle on the left then move left, 
     * if not and if there's no obstacle in front, move forward,
     * if not and if there's no obstacle on the right, move right 
     * else, turn 180 degrees
     */
    private void nextMove() {
        if (lookLeft()) {
            moveBot(MOVEMENT.LEFT);
            if (lookForward()) moveBot(MOVEMENT.FORWARD);
        } else if (lookForward()) {
            moveBot(MOVEMENT.FORWARD);
        } else if (lookRight()) {
            moveBot(MOVEMENT.RIGHT);
            if (lookForward()) moveBot(MOVEMENT.FORWARD);
        } else {
            moveBot(MOVEMENT.LEFT);
            moveBot(MOVEMENT.LEFT);
        }
    }

    /**
     * Returns true if the right side of the robot is free to move into.
     */
    private boolean lookRight() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return eastFree();
            case EAST:
                return southFree();
            case SOUTH:
                return westFree();
            case WEST:
                return northFree();
        }
        return false;
    }

    /**
     * Returns true if the robot is free to move forward.
     */
    private boolean lookForward() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return northFree();
            case EAST:
                return eastFree();
            case SOUTH:
                return southFree();
            case WEST:
                return westFree();
        }
        return false;
    }

    /**
     * * Returns true if the left side of the robot is free to move into.
     */
    private boolean lookLeft() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return westFree();
            case EAST:
                return northFree();
            case SOUTH:
                return eastFree();
            case WEST:
                return southFree();
        }
        return false;
    }

    /**
     * Returns true if the robot can move to the north cell.
     */
    private boolean northFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return isExploredAndFree(botRow + 1, botCol);
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return isExploredAndFree(botRow, botCol + 1) ;
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return isExploredAndFree(botRow - 1, botCol);
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return isExploredAndFree(botRow, botCol - 1);
    }

    /**
     * Returns the robot to START after exploration and points the bot northwards.
     */
    private void goHome() {
        System.out.print(!bot.getTouchedGoal());
        System.out.print(coverageLimit);
        System.out.print(timeLimit);
        
    	
    	if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
    		FastestPathAlgo goToGoal;
			if (bot.getRealBot()) 
				goToGoal = new FastestPathAlgo(exploredMap, bot);     
            else 
            	 goToGoal = new FastestPathAlgo(exploredMap, realMap, bot); 
            
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);  
        } 

        FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot);   // ???? removed realMap from parameter
        returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);

        System.out.println("Exploration complete!");
        areaExplored = exploredMap.calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.println(", " + areaExplored + " Cells");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        //TODO goHome realbot
//        if (bot.getRealBot()) {
//            turnBotDirection(DIRECTION.WEST);
//            moveBot(MOVEMENT.CALIBRATE);
//            turnBotDirection(DIRECTION.SOUTH);
//            moveBot(MOVEMENT.CALIBRATE);
//            turnBotDirection(DIRECTION.WEST);
//            moveBot(MOVEMENT.CALIBRATE);
//        }
        turnBotDirection(DIRECTION.NORTH);
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    private boolean isExploredNotObstacle(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell tmp = exploredMap.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns true for cells that are explored, not virtual walls and not obstacles.
     */
    private boolean isExploredAndFree(int r, int c) {
        if (exploredMap.checkValidCoordinates(r, c)) {
            Cell b = exploredMap.getCell(r, c);
            return (b.getIsExplored() && !b.getIsVirtualWall() && !b.getIsObstacle());
        }
        return false;
    }



    /**
     * Moves the bot, repaints the map and calls senseAndRepaint().
     */
    private void moveBot(MOVEMENT m) {
        bot.move(m);
        exploredMap.repaint();
        if (m != MOVEMENT.CALIBRATE) {
            senseAndRepaint();
        } else {
            CommMgr commMgr = CommMgr.getCommMgr();
            commMgr.recvMsg();
        }

        // ???? calibration ????
        // TODO calibration 
//        if (bot.getRealBot() && !calibrationMode) {
//            calibrationMode = true;
//            if (canCalibrateOnTheSpot(bot.getRobotCurDir())) {
//                lastCalibrate = 0;
//                moveBot(MOVEMENT.CALIBRATE);
//            } else {
//                lastCalibrate++;
//                if (lastCalibrate >= 5) {
//                    DIRECTION targetDir = getCalibrationDirection();
//                    if (targetDir != null) {
//                        lastCalibrate = 0;
//                        calibrateBot(targetDir);
//                    }
//                }
//            }
//
//            calibrationMode = false;
//        }
    }

    /**
     * Sets the bot's sensors, processes the sensor data and repaints the map.
     */
    private void senseAndRepaint() {
        bot.setSensors();
        bot.sense(exploredMap, realMap);
        exploredMap.repaint();
    }

    /**
     * Checks if the robot can calibrate at its current position given a direction.
     */
    private boolean canCalibrateOnTheSpot(DIRECTION botDir) {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();

        switch (botDir) {
            case NORTH:
                return exploredMap.getIsObstacleOrWall(row + 2, col - 1) && exploredMap.getIsObstacleOrWall(row + 2, col) && exploredMap.getIsObstacleOrWall(row + 2, col + 1);
            case EAST:
                return exploredMap.getIsObstacleOrWall(row + 1, col + 2) && exploredMap.getIsObstacleOrWall(row, col + 2) && exploredMap.getIsObstacleOrWall(row - 1, col + 2);
            case SOUTH:
                return exploredMap.getIsObstacleOrWall(row - 2, col - 1) && exploredMap.getIsObstacleOrWall(row - 2, col) && exploredMap.getIsObstacleOrWall(row - 2, col + 1);
            case WEST:
                return exploredMap.getIsObstacleOrWall(row + 1, col - 2) && exploredMap.getIsObstacleOrWall(row, col - 2) && exploredMap.getIsObstacleOrWall(row - 1, col - 2);
        }

        return false;
    }

    /**
     * Returns a possible direction for robot calibration or null, otherwise.
     */
    private DIRECTION getCalibrationDirection() {
        DIRECTION origDir = bot.getRobotCurDir();
        DIRECTION dirToCheck;

        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;

        return null;
    }

    /**
     * Turns the bot in the needed direction and sends the CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
    private void calibrateBot(DIRECTION targetDir) {
        DIRECTION origDir = bot.getRobotCurDir();

        turnBotDirection(targetDir);
        moveBot(MOVEMENT.CALIBRATE);
        turnBotDirection(origDir);
    }

    /**
     * Turns the robot to the required direction.
     */
    private void turnBotDirection(DIRECTION targetDir) {
        int numOfTurn = Math.abs(bot.getRobotCurDir().ordinal() - targetDir.ordinal());
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (DIRECTION.getNext(bot.getRobotCurDir()) == targetDir) {
                moveBot(MOVEMENT.RIGHT);
            } else {
                moveBot(MOVEMENT.LEFT);
            }
        } else if (numOfTurn == 2) {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    }
    
    
    
    
}
