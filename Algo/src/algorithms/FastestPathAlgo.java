package algorithms;

import simulator.Constants;
import utils.CommMgr;
import map.Cell;
import map.Map;
import robot.Robot;
import robot.RobotConstants;
import robot.RobotConstants.DIRECTION;
import robot.RobotConstants.MOVEMENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


/**
 * @author Lyu Xintong Isabelle
 * 
 * */


public class FastestPathAlgo {
	private Map exploredMap;
	private Map realMap = null;
	private Robot bot;
	private Cell curCell;
	private Cell[] neighbors;
	private DIRECTION curDir;
	private ArrayList<Cell> toVisit;
	private ArrayList<Cell> visited;
	private HashMap<Cell, Cell> parents;  // key: child , value: parent
	private int loopCount;
    private boolean explorationMode = false;
    private double[][] gCosts ;  // real cost from start node to the node 
	private boolean fpMode = false;
    
	public FastestPathAlgo(Map exploredMap, Map realMap, Robot bot, Boolean fpMode){
		this(exploredMap, bot, fpMode);
		this.realMap = realMap;
		this.explorationMode = true;
	}
	
	

	public FastestPathAlgo(Map exploredMap, Robot bot, Boolean fpMode){
		this.bot = bot;
		this.exploredMap = exploredMap;
		this.curCell = exploredMap.getCell(bot.getRobotPosRow(), bot.getRobotPosCol());
		this.curDir = bot.getRobotCurDir();
		this.toVisit = new ArrayList<>();
		this.visited = new ArrayList<>();
		this.parents = new HashMap<>();
		this.neighbors =  new Cell[4];
		this.gCosts = new double[Constants.MAP_ROW][Constants.MAP_COL];
		this.loopCount = 0;
		this.fpMode = fpMode;
		
		toVisit.add(curCell);
		updateNeighbors(curCell);
		
		// initialize gCosts to 0 except for cells that cannot be visited
		for (int row = 0; row < Constants.MAP_ROW; row++){
			for (int col = 0; col < Constants.MAP_COL; col++){
				if(!canBeVisited(exploredMap.getCell(row, col))){		
					gCosts[row][col] = RobotConstants.INFINITE_COST;
				}else{
					gCosts[row][col] = 0;
				}
						
			}
		}
	}
	

	public String runFastestPath(int goalRow, int goalCol){
		System.out.println("Calculating fastest path from (" + curCell.getRow() + ", " + curCell.getCol() + ") to goal (" + goalRow + ", " + goalCol + ")...");
		
		//create a stack for the path
		Stack<Cell> path;
		do{
			loopCount++;

			curCell = minimumCostCell(goalRow, goalCol);
			//System.out.println("curCell = "+ curCell.getRow() + " , " + curCell.getCol());
			
			//
			if (parents.containsKey(curCell)){
				curDir = getTargetDir(parents.get(curCell), curCell);
				//System.out.println("parent is ("+parents.get(curCell).getRow() + "," + parents.get(curCell).getCol()+") and curDir = " + curDir );
				
			}
			
			visited.add(curCell);
			toVisit.remove(curCell);
			
			// when goal cell is reached 
            if (visited.contains(exploredMap.getCell(goalRow, goalCol))) {
                System.out.println("Goal visited. Path found!");
                path = getPath(goalRow, goalCol);
                
                printFastestPath(path);
                return executePath(path, goalRow, goalCol);
                
            }
            
            updateNeighbors(curCell);  
		} while (!toVisit.isEmpty());
		
		System.out.println("Path not found!");
		return null;
	}

	 /**
     * Returns true if the cell is explored, not obstacle or virtual wall
     */
    private boolean canBeVisited(Cell c) {
        return c.getIsExplored() && !c.getIsObstacle() && !c.getIsVirtualWall();
    }
	
	private void updateNeighbors(Cell curCell) {
        // neighbor on top 
        if (exploredMap.checkValidCoordinates(curCell.getRow() + 1, curCell.getCol())) {
            neighbors[0] = exploredMap.getCell(curCell.getRow() + 1, curCell.getCol());
            if (!canBeVisited(neighbors[0])) {
                neighbors[0] = null;
            }
        }
        // neighbor at the bottom 
        if (exploredMap.checkValidCoordinates(curCell.getRow() - 1, curCell.getCol())) {
            neighbors[1] = exploredMap.getCell(curCell.getRow() - 1, curCell.getCol());
            if (!canBeVisited(neighbors[1])) {
                neighbors[1] = null;
            }
        }
        // neighbor on the left
        if (exploredMap.checkValidCoordinates(curCell.getRow(), curCell.getCol() - 1)) {
            neighbors[2] = exploredMap.getCell(curCell.getRow(), curCell.getCol() - 1);
            if (!canBeVisited(neighbors[2])) {
                neighbors[2] = null;
            }
        }
        // neighbor on the right
        if (exploredMap.checkValidCoordinates(curCell.getRow(), curCell.getCol() + 1)) {
            neighbors[3] = exploredMap.getCell(curCell.getRow(), curCell.getCol() + 1);
            if (!canBeVisited(neighbors[3])) {
                neighbors[3] = null;
            }
        }
        
        for (int i = 0; i < 4; i++){ 	
        	if (neighbors[i] != null){
        		if (visited.contains(neighbors[i])) continue;
        		
        		int r = neighbors[i].getRow();
            	int c = neighbors[i].getCol();
        		if (!toVisit.contains(neighbors[i])){
        			parents.put(neighbors[i], curCell);
        			gCosts[r][c] = gCosts[curCell.getRow()][curCell.getCol()] + calculateCostG(curCell, neighbors[i], curDir);
        			toVisit.add(neighbors[i]);
        			
        		}else{
        			double currentGCost = gCosts[r][c];
        			double newGCost = gCosts[curCell.getRow()][curCell.getCol()] + calculateCostG(curCell, neighbors[i], curDir);
        			if (newGCost < currentGCost){
        				gCosts[r][c] = newGCost;
        				parents.put(neighbors[i], curCell);
        			}
        		}
        	}  	
        }
        
		
	}
	
	/**
	 * calculate g(n), which is the actual cost one cell to another cell 
	 * */
	private double calculateCostG(Cell a, Cell b, DIRECTION dir) {
		double moveCost;
		double turnCost;
		
		//calculate turn cost, which is based on the degree of turning 
		int numOfTurn = Math.abs(dir.ordinal() - getTargetDir(a,b).ordinal());
		if (numOfTurn > 2) {
            numOfTurn = numOfTurn % 2;
        }
		turnCost = numOfTurn * RobotConstants.TURN_COST;
		moveCost = RobotConstants.MOVE_COST;
		
		return (moveCost + turnCost);
	}

	/**
	 * calculate h(n), which is the heuristic cost one cell to another cell,
	 * which is based on the number of cells need to travel to go from cell to another  
	 * */
	private double calculateCostH(Cell a, int goalRow, int goalCol){
		
		int row = a.getRow();
		int col = a.getCol();
		
		double distance = Math.sqrt(Math.pow((row - goalRow), 2) + Math.pow((col - goalCol),2));
		if (distance == 0) return 0;
		
		double turnCost = 0; 
		double moveCost;
		
		moveCost = distance * RobotConstants.MOVE_COST;
//		if (goalCol - a.getCol() != 0 || goalRow - a.getRow() != 0) {
//            turnCost = RobotConstants.TURN_COST;
//        }
//		
		return (moveCost + turnCost);
		
//		 double moveCost = (Math.abs(goalCol - a.getCol()) + Math.abs(goalRow - a.getRow())) * RobotConstants.MOVE_COST;
//		 if (moveCost == 0) return 0;
//
//		 double turnCost = 0;
//		 if (goalCol - a.getCol() != 0 || goalRow - a.getRow() != 0) 
//			 turnCost = RobotConstants.TURN_COST;
//		
//		return (moveCost + turnCost);
	}

	private Stack<Cell> getPath(int goalRow, int goalCol) {
		Stack<Cell> actualPath = new Stack<>();
		Cell parent = exploredMap.getCell(goalRow, goalCol);
		
		while (true){
			actualPath.push(parent);
			parent = parents.get(parent);
			if (parent == null){
				break;
			}
		}
		return actualPath;
	}

	private String executePath(Stack<Cell> path, int goalRow, int goalCol) {

        StringBuilder outputString = new StringBuilder();
        ArrayList<MOVEMENT> movements = new ArrayList<>();
        DIRECTION targetDir;
        
        Robot tempBot = new Robot(bot.getRobotPosRow(),bot.getRobotPosCol(),false);
        tempBot.setRobotDir(bot.getRobotCurDir());

        tempBot.setSpeed(0);
        MOVEMENT m;
        
        Cell temp = path.pop();

		while((tempBot.getRobotPosRow() != goalRow) || (tempBot.getRobotPosCol() != goalCol)){
			if (tempBot.getRobotPosRow() == temp.getRow() && tempBot.getRobotPosCol() == temp.getCol()) {
                temp = path.pop();
            }
			
			targetDir = getTargetDir(tempBot.getCell(), temp);
//			System.out.println("targetDir " +targetDir);
//			System.out.println("robotDir " +tempBot.getRobotCurDir());
			
			if (tempBot.getRobotCurDir() != targetDir) {
                m = getTargetMove(tempBot.getRobotCurDir(), targetDir);
            } else {
                m = MOVEMENT.FORWARD;
            }
			
			
//			 System.out.println("Movement " + MOVEMENT.print(m) + " from (" + tempBot.getRobotPosRow() + ", " + tempBot.getRobotPosCol() + ") to (" + temp.getRow() + ", " + temp.getCol() + ")");

	         tempBot.move(m);
	         movements.add(m);
	         outputString.append(MOVEMENT.print(m));	
		}
		
		if (!bot.getRealBot() || explorationMode) {
			for (MOVEMENT x: movements){
				if (x == MOVEMENT.FORWARD){
					if (!canMoveForward()) {
                        System.out.println("Early termination of fastest path execution.");
                        return "T";
					}
				}
				
				
				bot.move(x, false);
				this.exploredMap.repaint();
				
				// During exploration, use sensor data to update exploredMap.
				// ???? need this part ????
//                if (explorationMode) {
//                    bot.setSensors();
//                    bot.sense(this.exploredMap, this.realMap);
//                    this.exploredMap.repaint();
//                }
			}
		} else {
			int fCount = 0;
            for (MOVEMENT x : movements) {
                if (x == MOVEMENT.FORWARD) {
                    fCount++;
                    if (fCount == 10) {
                        bot.moveForwardMultiple(fCount);
                        fCount = 0;
                        exploredMap.repaint();
                    }
                } else if (x == MOVEMENT.RIGHT || x == MOVEMENT.LEFT) {
                    if (fCount > 0) {
                        bot.moveForwardMultiple(fCount);
                        fCount = 0;
                        exploredMap.repaint();
                    }
                    bot.move(x, false);
                    exploredMap.repaint();
                }
                
            }  
            if (fCount > 0) {
                bot.moveForwardMultiple(fCount);
                exploredMap.repaint();
            }
		}
//        System.out.println("\nMovements: " + outputString.toString().toLowerCase());
        
        
       System.out.println("");
        
        
        if (bot.getRealBot()){
        	String fpStr = outputString.toString().toLowerCase();
        	String fpStrWithC="";
        	for (int i = 0; i < fpStr.length(); i++){
        		fpStrWithC += fpStr.substring(i, i+1);
        		if (i%3 == 0) 
        			fpStrWithC += "c" ;
        	}

        	CommMgr.getCommMgr().sendMsg(fpStrWithC, CommMgr.MOVE);
        	
        }
        
        return outputString.toString();
	}
	
		
	private boolean canMoveForward() {
		int row = bot.getRobotPosRow();
	    int col = bot.getRobotPosCol();
	
	    switch (bot.getRobotCurDir()) {
	        case NORTH:
	            if (!exploredMap.isObstacleCell(row + 2, col - 1) && !exploredMap.isObstacleCell(row + 2, col) && !exploredMap.isObstacleCell(row + 2, col + 1)) {
	                return true;
	            }
	            break;
	        case EAST:
	            if (!exploredMap.isObstacleCell(row + 1, col + 2) && !exploredMap.isObstacleCell(row, col + 2) && !exploredMap.isObstacleCell(row - 1, col + 2)) {
	                return true;
	            }
	            break;
	        case SOUTH:
	            if (!exploredMap.isObstacleCell(row - 2, col - 1) && !exploredMap.isObstacleCell(row - 2, col) && !exploredMap.isObstacleCell(row - 2, col + 1)) {
	                return true;
	            }
	            break;
	        case WEST:
	            if (!exploredMap.isObstacleCell(row + 1, col - 2) && !exploredMap.isObstacleCell(row, col - 2) && !exploredMap.isObstacleCell(row - 1, col - 2)) {
	                return true;
	            }
	            break;
	    }
	
	    return false;
	}
	

	private MOVEMENT getTargetMove(DIRECTION CurDir, DIRECTION targetDir) {
		switch (CurDir) {
        case NORTH:
            switch (targetDir) {
                case NORTH:
                    return MOVEMENT.ERROR;
                case SOUTH:
                    return MOVEMENT.LEFT;
                case WEST:
                    return MOVEMENT.LEFT;
                case EAST:
                    return MOVEMENT.RIGHT;
            }
            break;
        case SOUTH:
            switch (targetDir) {
                case NORTH:
                    return MOVEMENT.LEFT;
                case SOUTH:
                    return MOVEMENT.ERROR;
                case WEST:
                    return MOVEMENT.RIGHT;
                case EAST:
                    return MOVEMENT.LEFT;
            }
            break;
        case WEST:
            switch (targetDir) {
                case NORTH:
                    return MOVEMENT.RIGHT;
                case SOUTH:
                    return MOVEMENT.LEFT;
                case WEST:
                    return MOVEMENT.ERROR;
                case EAST:
                    return MOVEMENT.LEFT;
            }
            break;
        case EAST:
            switch (targetDir) {
                case NORTH:
                    return MOVEMENT.LEFT;
                case SOUTH:
                    return MOVEMENT.RIGHT;
                case WEST:
                    return MOVEMENT.LEFT;
                case EAST:
                    return MOVEMENT.ERROR;
            }
    }
    return MOVEMENT.ERROR;
	}

	private void printFastestPath(Stack<Cell> path) {
		 System.out.println("\nLooped " + loopCount + " times.");
	        System.out.println("The number of steps is: " + (path.size() - 1) );

	        Stack<Cell> pathForPrint = (Stack<Cell>) path.clone();
	        Cell temp;
	        System.out.println("Path:");
	        while (!pathForPrint.isEmpty()) {
	            temp = pathForPrint.pop();
	            if (!pathForPrint.isEmpty()) System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ") --> ");
	            else System.out.print("(" + temp.getRow() + ", " + temp.getCol() + ")");
	            
	            if (fpMode == true)
	            	exploredMap.grid[temp.getRow()][temp.getCol()].setIsTrail();
	        }
	        System.out.println("\n");
	}
	
	
	/**
     * Returns the Cell in toVisit with the minimum g(n) + h(n).
     */
	private Cell minimumCostCell(int goalRow, int goalCol) {
		Cell minCostCell = null;
		double gCost;
		double hCost;
		double totalCost;
		double minCost = RobotConstants.INFINITE_COST;

		for (int i = 0; i < toVisit.size(); i++){
			gCost = gCosts[toVisit.get(i).getRow()][toVisit.get(i).getCol()];
			hCost =  calculateCostH(toVisit.get(i), goalRow, goalCol) ;
			totalCost = gCost + hCost;

			if (totalCost < minCost){ 
				minCost = totalCost;
				minCostCell = toVisit.get(i);
			}
		}
		return minCostCell;
	}


	
    /**
     * Returns the target direction of the bot from [botR, botC] to target Cell.
     */
    private DIRECTION getTargetDir(Cell curCell, Cell target) {
        if (curCell.getCol() - target.getCol() > 0) {
            return DIRECTION.WEST;
        } else if (target.getCol() - curCell.getCol() > 0) {
            return DIRECTION.EAST;
        } else if (curCell.getRow() - target.getRow() > 0) {
            return DIRECTION.SOUTH;
        } else if (target.getRow() - curCell.getRow() > 0) {
            return DIRECTION.NORTH;
        } else {
        	return null;
        }
    }
    
    
	public void test() {
		System.out.println("test success");
	}
	
	public void execute(){
		
	}
	
	
	
}
