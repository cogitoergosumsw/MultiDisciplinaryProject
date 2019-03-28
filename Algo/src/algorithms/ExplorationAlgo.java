package algorithms;

import map.Cell;
import map.Map;
import simulator.Constants;
import simulator.Simulator;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;
import utils.CommMgr;
import utils.MapDescriptor;
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
    private boolean calibrationMode;
    private int numberOfContinuousLeftTurn = 0;
    private int moveCount;
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
    	
        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg(CommMgr.BOT_START);
        }

        senseAndRepaint();

        areaExplored = exploredMap.calculateAreaExplored();  
        System.out.println("Explored Area: " + areaExplored);

        explorationLoop(bot.getRobotPosRow(), bot.getRobotPosCol());
        
        
        if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg(CommMgr.EX_DONE);         
        }        
    }


    
    /**
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    private void explorationLoop(int startR, int startC) {
    	CommMgr commMgr = CommMgr.getCommMgr();
    	
    	//initialCalibration();

    	moveCount = 1;
    	do {

//    		System.out.println("moveCount = " + moveCount);
//        	if (bot.getRealBot() && moveCount % 3 == 0){
//        		if (canCalibrate(bot))
//        			moveBot(MOVEMENT.CALIBRATE);
//        		
//             }
        	System.out.println("last calibrated = " + bot.getLastCalibrated());
        	
        	if (bot.getLastCalibrated() >= 3){
//        		if (exploredMap.canCalibrate(bot)){
        			moveBot(MOVEMENT.CALIBRATE);
//        		}	
        	}
    		
        	exploredMap.setCellsVisitedByBot();
    		
        	
        	nextMove(); 
            areaExplored = exploredMap.calculateAreaExplored();
            //System.out.println("Area explored: " + areaExplored);
            
            
            //TODO remove this , bot stops when it goes back to the starting zone
            if (bot.getRobotPosRow() == startR && bot.getRobotPosCol() == startC && bot.getTouchedGoal() ) {
//                if (areaExplored != Constants.MAP_SIZE){
//                	exploreUnexploredArea();
//                }
            	System.out.print("robot has reached starting zone again, stops exploring");
                break;          	      
            }
            moveCount ++ ; 
        } while (areaExplored < coverageLimit && System.currentTimeMillis() < endTime);
    	
    	System.out.println("areaExplored = " + areaExplored + " , systemtime = " + System.currentTimeMillis() + "endtime = " + endTime);
        goHome();
        
    }
   
    
    /**
     * initial calibration in the starting zone 
     * turn 180 degree to face south, calibrate
     * then turn 90 degree to face west, calibrate 
     * then turn 90 degree to face north again 
     */
    private void initialCalibration(){
    	
    	CommMgr.getCommMgr().recvMsg(); // consume the first sensor reading 
    	moveBot(MOVEMENT.RIGHT);
    	moveBot(MOVEMENT.RIGHT);
    	moveBot(MOVEMENT.CALIBRATE);
    	moveBot(MOVEMENT.RIGHT);
    	moveBot(MOVEMENT.CALIBRATE);
    	moveBot(MOVEMENT.RIGHT);
    
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
            if (lookForward()) {
            	moveBot(MOVEMENT.FORWARD);
            	moveCount ++ ;
            }
            numberOfContinuousLeftTurn++;
            
            if ( numberOfContinuousLeftTurn == 4){
            	numberOfContinuousLeftTurn = 0;
            	goToNearestExploredNearWall();
            	//goToNearestUnexplored(bot.getCell());
            }
        } else if (lookForward()) {
            moveBot(MOVEMENT.FORWARD);
            numberOfContinuousLeftTurn = 0;
            
        } else if (lookRight()) {
        	
        	//?????
        	// calibrate on both left and front before turning right 
        	if (exploredMap.canCalibrate(bot)){
        		
        		
        		if (exploredMap.canCalibrateOnLeft(bot)){
        			moveBot(MOVEMENT.LEFT);
            		moveBot(MOVEMENT.CALIBRATE);
            		moveBot(MOVEMENT.RIGHT);
        		}
        		
        		if(exploredMap.canCalibrateInFront(bot)){
        			moveBot(MOVEMENT.CALIBRATE);
        		}
        		
        		
        		
        	}
        	//
        	
        	
            moveBot(MOVEMENT.RIGHT);
            if (lookForward()){
            	moveBot(MOVEMENT.FORWARD);
            	moveCount++;
            }
            numberOfContinuousLeftTurn = 0;
        } else {
        	
            moveBot(MOVEMENT.LEFT);
            moveBot(MOVEMENT.LEFT);
            
            numberOfContinuousLeftTurn = 0;
            moveCount++;
        }
    }
   
    
    /**
     * explore unexplored area by going to the nearest unexplored cell
     * move for 5 steps, then go to nearest unexplored cell again
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
    	System.out.println("nearest unexplored cell is : "+ nearestUnexplored.toString());
    	Cell nearestExplored = nearestExplored(nearestUnexplored);
    	
    	System.out.println( "goint to cell : "+ nearestExplored.toString());
    	
    	if (!bot.getRealBot()){
    		FastestPathAlgo goToNearestUnexplored = new FastestPathAlgo(exploredMap, realMap, bot, false);     
    		goToNearestUnexplored.runFastestPath(nearestExplored.getRow(), nearestExplored.getCol());  
    	} else {
    		FastestPathAlgo goToNearestUnexplored = new FastestPathAlgo(exploredMap, bot, false);      
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
    
    
    //TODO
    
    
    private void goToNearestExploredNearWall(){
//    	
    	Cell  nearestExploredNearWall = nearestExploredNearWall();
    	
    	// if there is no explored cell near wall, then go to the explored cell next to nearest unexplored cell
    	if (nearestExploredNearWall == null){
    		goToNearestUnexplored(bot.getCell());
    		return;
    	}
    	
    	
    	Cell nearestExplored = nearestExplored(nearestExploredNearWall);
    	System.out.println("nearest explored cell next to wall is : " + nearestExploredNearWall.toString());
    	
    	
    	System.out.println( "goint to cell :" + nearestExplored.toString());
    	

    	if (!bot.getRealBot()){
    		FastestPathAlgo goToNearestExplored = new FastestPathAlgo(exploredMap, realMap, bot, false);     
    		goToNearestExplored.runFastestPath( nearestExplored.getRow(),  nearestExplored.getCol());  
    	} else {
    		FastestPathAlgo goToNearestExplored = new FastestPathAlgo(exploredMap, bot, false);      
    		goToNearestExplored.runFastestPath( nearestExplored.getRow(),  nearestExplored.getCol()); 
    	}
    	
    	
    	// if it's not really next to a wall, then move forward until it cannot 

    	
//    	while(lookForward()){
//    		moveBot(MOVEMENT.FORWARD);
//    		System.out.println("moving to :" + bot.getCell().toString());
//    	}
    	
    	
    	// keep turning left until it cannot anymore 
    	while (lookLeft()){
    		moveBot(MOVEMENT.LEFT);
    		System.out.println("turning left");
    	}
    	
    	
    }
    
    
    
    
   private Cell nearestExploredNearWall(){
	   Cell cell, nearest = null;
	   int temp, distance = 10000;
	   
	   for (int r = 0; r < Constants.MAP_ROW; r++){
		   for (int c = 0; c < Constants.MAP_COL; c++){
			   cell = exploredMap.grid[r][c];
			   if (!(cell.getIsVirtualWall() && isExploredNotObstacle(r,c))) continue;
			   temp = Math.abs(r - bot.getRobotPosRow()) + Math.abs(c - bot.getRobotPosCol()) ;
			   if ( temp < distance){
				   nearest = cell;
				   distance = temp;
			   }
		   }
	   }
	   
	   return nearest;
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
    	// debug
    	System.out.println("going home");
    	
    	System.out.println(!bot.getTouchedGoal());
       // debug
    	System.out.println("coverage limit = " + coverageLimit);
        System.out.println("time limit = "+timeLimit);
        
    	// in case of finished exploring the whole map but the goal has not been touched, go to goal first, then go back to start
    	if (!bot.getTouchedGoal() && coverageLimit == Constants.MAP_SIZE && timeLimit == 3600) {
    		FastestPathAlgo goToGoal;
    		goToGoal = new FastestPathAlgo(exploredMap, realMap, bot, false); 
            goToGoal.runFastestPath(Constants.GOAL_ROW, Constants.GOAL_COL);  
            
        } 

        FastestPathAlgo returnToStart = new FastestPathAlgo(exploredMap, bot, false);   
        returnToStart.runFastestPath(Constants.START_ROW, Constants.START_COL);
        
        System.out.println("Exploration complete!");
        areaExplored = exploredMap.calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / Constants.MAP_SIZE) * 100.0);
        System.out.println(", " + areaExplored + " Cells");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        // turn the robot to face north after arriving at the start zone
        if (bot.getRealBot()) {
        	if (bot.getRobotCurDir() == DIRECTION.WEST){
        		moveBot(MOVEMENT.LEFT); 
        		moveBot(MOVEMENT.CALIBRATE);
        		moveBot(MOVEMENT.RIGHT); 
        		moveBot(MOVEMENT.CALIBRATE);
        		moveBot(MOVEMENT.RIGHT); 
        	} else if (bot.getRobotCurDir() == DIRECTION.SOUTH){    
        		moveBot(MOVEMENT.CALIBRATE);
        		moveBot(MOVEMENT.RIGHT);
        		moveBot(MOVEMENT.CALIBRATE);
        		moveBot(MOVEMENT.RIGHT);
        	} else{
        		moveBot(MOVEMENT.CALIBRATE);
        	}
        		
        }
       
        
        // TODO ??? change to checking if bot is facing north when starting fastest path
       // turnBotDirection(DIRECTION.NORTH);
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
        } 
        // TODO NOWWW
//            else {
//            System.out.println("debug");
//        	CommMgr commMgr = CommMgr.getCommMgr();
//            commMgr.recvMsg();
//        }

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
//    private boolean canCalibrateOnTheSpot(DIRECTION botDir) {
//        int row = bot.getRobotPosRow();
//        int col = bot.getRobotPosCol();
//
//        switch (botDir) {
//            case NORTH:
//                return exploredMap.getIsObstacleOrWall(row + 2, col - 1) && exploredMap.getIsObstacleOrWall(row + 2, col) && exploredMap.getIsObstacleOrWall(row + 2, col + 1);
//            case EAST:
//                return exploredMap.getIsObstacleOrWall(row + 1, col + 2) && exploredMap.getIsObstacleOrWall(row, col + 2) && exploredMap.getIsObstacleOrWall(row - 1, col + 2);
//            case SOUTH:
//                return exploredMap.getIsObstacleOrWall(row - 2, col - 1) && exploredMap.getIsObstacleOrWall(row - 2, col) && exploredMap.getIsObstacleOrWall(row - 2, col + 1);
//            case WEST:
//                return exploredMap.getIsObstacleOrWall(row + 1, col - 2) && exploredMap.getIsObstacleOrWall(row, col - 2) && exploredMap.getIsObstacleOrWall(row - 1, col - 2);
//        }
//
//        return false;
//    }

    /**
     * Returns a possible direction for robot calibration or null, otherwise.
     */
//    private DIRECTION getCalibrationDirection() {
//        DIRECTION origDir = bot.getRobotCurDir();
//        DIRECTION dirToCheck;
//
//        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
//        if (canCalibrateOnTheSpot(dirToCheck)) return dirToCheck;
//
//        return null;
//    }

    /**
     * Turns the bot in the needed direction and sends the CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
//    private void calibrateBot(DIRECTION targetDir) {
//        DIRECTION origDir = bot.getRobotCurDir();
//
//        turnBotDirection(targetDir);
//        moveBot(MOVEMENT.CALIBRATE);
//        turnBotDirection(origDir);
//    }

    /**
     * Turns the robot to the required direction.
     */
//    private void turnBotDirection(DIRECTION targetDir) {
//        int numOfTurn = Math.abs(bot.getRobotCurDir().ordinal() - targetDir.ordinal());
//        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;
//
//        if (numOfTurn == 1) {
//            if (DIRECTION.getNext(bot.getRobotCurDir()) == targetDir) {
//                moveBot(MOVEMENT.RIGHT);
//            } else {
//                moveBot(MOVEMENT.LEFT);
//            }
//        } else if (numOfTurn == 2) {
//            moveBot(MOVEMENT.RIGHT);
//            moveBot(MOVEMENT.RIGHT);
//        }
//    }
   }

    
    
    
    

