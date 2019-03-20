package map;


import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import robot.RobotConstants.DIRECTION;
import robot.Robot;
import simulator.Constants;

/**
 * Map of the arena 
 * @author Lyu Xintong Isabellle
 */

public class Map extends JPanel{    
    public Cell[][] grid;
    private Robot bot;
    
    public Map(Robot bot){
    	this.bot = bot;
        grid  =  new Cell[Constants.MAP_ROW][Constants.MAP_COL];
        for (int row = 0; row < grid.length; row++){
            for (int col = 0; col < grid[0].length; col++){
                grid[row][col] = new Cell(row,col);
                if (row == 0 || row== grid.length-1 || col == 0 || col == grid[0].length-1)
                    grid[row][col].setIsVirtualWall();
            }
        }
        
        setAllUnexplored();
    }
    
    private boolean inStartZone(int row, int col) {
        return (row >= Constants.START_ROW - 1 && row <= Constants.START_ROW + 1 && col >= Constants.START_COL - 1 && col <= Constants.START_COL + 1);
    }
    private boolean inGoalZone(int row, int col) {
    	return (row >= Constants.GOAL_ROW - 1 && row <= Constants.GOAL_ROW + 1 && col >= Constants.GOAL_COL - 1 && col <= Constants.GOAL_COL + 1);
    }

        /**
     * Returns true if the row and column values are valid.
     */
    public boolean checkValidCoordinates(int row, int col) {
        return row >= 0 && col >= 0 && row < Constants.MAP_ROW && col < Constants.MAP_COL;
    }

    /**
     * Returns a particular cell in the grid.
     */
    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    public void setAllExplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setIsExplored();
            }
        }
    }

    /**
     * Sets all cells in the grid to an unexplored state except for the START zone
     */
    public void setAllUnexplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (inStartZone(row, col)) {
                    grid[row][col].setIsExplored();
                } else {
                    grid[row][col].setIsNotExplored();
                }
            }
        }
    }
    
    /**
     * set a cell as obstacle and explored cells around it as virtual wall 
     */    
    public void setObstacleCell(int row, int col) {
        if (inStartZone(row, col) || inGoalZone(row, col))
            return;

        grid[row][col].setIsObstacle();
        grid[row][col].resetClearByLeftOrRightSensorCount();

        if (row >= 1) {
        	//if (grid[row - 1][col].getIsExplored())	
        		grid[row - 1][col].setIsVirtualWall();            // bottom cell
        	repaint();
            if (col < Constants.MAP_COL - 1) {
                //if(grid[row - 1][col + 1].getIsExplored())
                	grid[row - 1][col + 1].setIsVirtualWall();    // bottom-right cell
            }
            repaint();
            if (col >= 1) {
            	//if(grid[row - 1][col - 1].getIsExplored())
            		grid[row - 1][col - 1].setIsVirtualWall();    // bottom-left cell
            }
        }
        repaint();
        if (row < Constants.MAP_ROW - 1) {
        	//if(grid[row + 1][col].getIsExplored())
        		grid[row + 1][col].setIsVirtualWall();            // top cell
        	repaint();
            if (col < Constants.MAP_COL - 1) {
            	//if(grid[row + 1][col + 1].getIsExplored())
            	grid[row + 1][col + 1].setIsVirtualWall();    // top-right cell
            }
            repaint();
            if (col >= 1) {
            	//if(grid[row + 1][col - 1].getIsExplored())
            		grid[row + 1][col - 1].setIsVirtualWall();    // top-left cell
            }
        }
        repaint();
        if (col >= 1) {
        	//if(grid[row][col - 1].getIsExplored())
        		grid[row][col - 1].setIsVirtualWall();            // left cell
        }
        repaint();
        if (col < Constants.MAP_COL - 1) {
        	//if(grid[row][col + 1].getIsExplored())
        		grid[row][col + 1].setIsVirtualWall();            // right cell
        } 
        repaint();
    } 
    
    public void clearObstacleCell(int row, int col){

        grid[row][col].setIsNotObstacle();

        //clear virtual wall cells around the obstacle, if it is valid and at the edge of the arena
        
        if (!isVirtualWallAroundEdge(row - 1 , col) && checkValidCoordinates(row - 1 , col))
        	grid[row - 1][col].setIsNotVirtualWall(); 
        repaint();
        if (!isVirtualWallAroundEdge(row + 1, col) && checkValidCoordinates(row + 1, col))
        	grid[row + 1][col].setIsNotVirtualWall(); 
        repaint();
        if (!isVirtualWallAroundEdge(row, col - 1) && checkValidCoordinates(row, col - 1))
        	grid[row][col - 1].setIsNotVirtualWall();   
        repaint();
        if (!isVirtualWallAroundEdge(row, col + 1) && checkValidCoordinates(row, col + 1))
        	grid[row][col + 1].setIsNotVirtualWall(); 
        repaint();
        if (!isVirtualWallAroundEdge(row - 1, col - 1) && checkValidCoordinates(row - 1, col - 1))
        	grid[row - 1][col - 1].setIsNotVirtualWall(); 
        repaint();
        if (!isVirtualWallAroundEdge(row - 1, col + 1) && checkValidCoordinates(row - 1, col + 1))
        	grid[row - 1][col + 1].setIsNotVirtualWall(); 
        repaint();
        if (!isVirtualWallAroundEdge(row + 1, col - 1) && checkValidCoordinates(row + 1, col - 1))
        	grid[row + 1][col - 1].setIsNotVirtualWall(); 
        repaint();
        if (!isVirtualWallAroundEdge(row + 1, col + 1) && checkValidCoordinates(row + 1, col + 1))
        	grid[row + 1][col + 1].setIsNotVirtualWall(); 
        repaint();
    
}
    
    /**
     * checks if it is cells around on edges of the arena 
     */
    public boolean isVirtualWallAroundEdge(int row, int col){
    	if (row == 0 || row == Constants.MAP_ROW - 1) return true;
    	
    	if (col == 0 || col == Constants.MAP_COL - 1) return true;
    	
    	return false;
    }

    /**
     * @Override JComponent's paintComponent() method. It paints square cells for the grid with the appropriate colors 
     */
    public void paintComponent(Graphics g){
        paintCells(g);
        paintRobot(g);
    }

    public void paintCells(Graphics g){
        for (int row = 0; row < Constants.MAP_ROW; row++) {
            for (int col = 0; col < Constants.MAP_COL; col++) { 
                Color cellColor;
                
                
                
                if (grid[row][col].getIsWayPoint())
                	cellColor = GraphicsConstants.C_WAYPOINT;
                else if (grid[row][col].getIsTrail())
                	cellColor = GraphicsConstants.C_TRAIL;
                else if (inStartZone(row, col))
                    cellColor = GraphicsConstants.C_START;
                else if (inGoalZone(row, col))
                    cellColor = GraphicsConstants.C_GOAL;
               else {
                    if (!grid[row][col].getIsExplored())
                        cellColor = GraphicsConstants.C_UNEXPLORED;
                    else if (grid[row][col].getIsObstacle())
                        cellColor = GraphicsConstants.C_OBSTACLE;
                    else if (grid[row][col].getIsVirtualWall())
                    	cellColor = GraphicsConstants.C_VIRTUALWALL;
                    else
                        cellColor = GraphicsConstants.C_FREE;
                }
                
//                if (grid[row][col].getIsVisitedByBot())
//                	cellColor = GraphicsConstants.C_VISITED;
                
                

                int cellX = col * GraphicsConstants.CELL_SIZE + GraphicsConstants.CELL_LINE_WEIGHT + GraphicsConstants.MAP_X_OFFSET;
                int cellY = GraphicsConstants.MAP_H - (row * GraphicsConstants.CELL_SIZE - GraphicsConstants.CELL_LINE_WEIGHT);
                int cellSize = GraphicsConstants.CELL_SIZE - (GraphicsConstants.CELL_LINE_WEIGHT * 2);

                g.setColor(cellColor);
                g.fillRect(cellX, cellY, cellSize, cellSize);
            }
        }
    }

    public void paintRobot(Graphics g){
         // Paint the robot on-screen.
         g.setColor(GraphicsConstants.C_ROBOT);
         int r = bot.getRobotPosRow();
         int c = bot.getRobotPosCol();
         g.fillOval((c - 1) * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_X_OFFSET + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - (r * GraphicsConstants.CELL_SIZE + GraphicsConstants.ROBOT_Y_OFFSET), GraphicsConstants.ROBOT_W, GraphicsConstants.ROBOT_H);

         // Paint the robot's direction indicator on-screen.
         g.setColor(GraphicsConstants.C_ROBOT_DIR);
         DIRECTION d = bot.getRobotCurDir();
         switch (d) {
             case NORTH:
                 g.fillOval(c * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE - 15, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                 break;
             case EAST:
                 g.fillOval(c * GraphicsConstants.CELL_SIZE + 35 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                 break;
             case SOUTH:
                 g.fillOval(c * GraphicsConstants.CELL_SIZE + 10 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 35, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                 break;
             case WEST:
                 g.fillOval(c * GraphicsConstants.CELL_SIZE - 15 + GraphicsConstants.MAP_X_OFFSET, GraphicsConstants.MAP_H - r * GraphicsConstants.CELL_SIZE + 10, GraphicsConstants.ROBOT_DIR_W, GraphicsConstants.ROBOT_DIR_H);
                 break;
         }
    }


    /**
     * Returns true if the given cell is out of bounds or an obstacle.
     */
    public boolean getIsObstacleOrWall(int row, int col) {
        return !checkValidCoordinates(row, col) || getCell(row, col).getIsObstacle();
    }

    public void reset(){
    	
    	for (int row = 0; row < grid.length; row++){
            for (int col = 0; col < grid[0].length; col++){
              
            	grid[row][col].setIsNotObstacle();
            	grid[row][col].setIsNotTrail();
            	grid[row][col].setIsNotWayPoint();
            	
            	if (inStartZone(row, col) || inGoalZone(row, col)) {
                    grid[row][col].setIsExplored();
                } else {
                    grid[row][col].setIsNotExplored();
                }
                
                if (row == 0 || row== grid.length-1 || col == 0 || col == grid[0].length-1)
                    grid[row][col].setIsVirtualWall();
            }
        }	
    }
    
    public boolean isObstacleCell(int row, int col) {
        return grid[row][col].getIsObstacle();
    }
    
    /**
     * for debugging
     * */
//    public void printMapObstacle(){
//    	for (int i = 0; i < )
//    }
    
    
    /**
     * Returns the number of cells explored in the grid.
     */
    public int calculateAreaExplored() {
        int result = 0;
        for (int r = 0; r < Constants.MAP_ROW; r++) {
            for (int c = 0; c < Constants.MAP_COL; c++) {
                if (this.getCell(r, c).getIsExplored()) {
                    result++;
                }
            }
        }
        return result;
    }


    /**
     * only clear the obstacle if cleared by left or right sensor twice 
     * */
	public void clearObstacleByLeftOrRightSensor(int row, int col) {
		
		grid[row][col].incrementClearByLeftOrRightSensorCount();
		
		if (grid[row][col].getClearByLeftOrRightSensorCount() == 2){
			grid[row][col].resetClearByLeftOrRightSensorCount();
			clearObstacleCell(row, col);			
		}
	
	}
	
	
	public boolean checkIsVirtualWall(int row, int col){
		if (isVirtualWallAroundEdge(row, col)) 
			return true;
		// check if there is any obstacle around the 4 sides and 4 corners of this cell
		
		  if ( checkValidCoordinates(row - 1 , col) && getCell(row, col).getIsObstacle() )
	        	return true;
	        if (checkValidCoordinates(row + 1, col) && getCell(row, col).getIsObstacle())
	        	return true;
	        if (checkValidCoordinates(row, col - 1) && getCell(row, col).getIsObstacle())
	        	return true;           
	        if (checkValidCoordinates(row, col + 1) && getCell(row, col).getIsObstacle())
	        	return true;
	        if (checkValidCoordinates(row - 1, col - 1) && getCell(row, col).getIsObstacle())
	        	return true; 
	        if (checkValidCoordinates(row - 1, col + 1) && getCell(row, col).getIsObstacle())
	        	return true; 
	        if (checkValidCoordinates(row + 1, col - 1) && getCell(row, col).getIsObstacle())
	        	return true; 
	        if (checkValidCoordinates(row + 1, col + 1) && getCell(row, col).getIsObstacle())
	        	return true;
	        
	        
	       return false;
		
	}

	/**
	 *  set isVisitedByBot = true for the 9 cells where the robot is currently at 
	 * */
	public void setCellsVisitedByBot() {
		setCellsVisitedByBot(bot.getRobotPosRow(), bot.getRobotPosCol());		
	}
    
	public void setCellsVisitedByBot(int row, int col){
		grid[row][col].setIsVisitedByBot();
		grid[row][col + 1].setIsVisitedByBot();
		grid[row][col - 1].setIsVisitedByBot();
		grid[row + 1][col].setIsVisitedByBot();
		grid[row + 1][col + 1].setIsVisitedByBot();
		grid[row + 1][col - 1].setIsVisitedByBot();
		grid[row - 1][col].setIsVisitedByBot();
		grid[row - 1][col + 1].setIsVisitedByBot();
		grid[row - 1][col - 1].setIsVisitedByBot();
	}




    /**
     * return true is there are 3 blocks in front or 3 blocks on the left
     * */
    public boolean canCalibrate(Robot bot){
    	return (canCalibrateInFront(bot) || canCalibrateOnLeft(bot)) ;
    }
    
    
    public boolean canCalibrateInFront(Robot bot){
    	int row = bot.getRobotPosRow();
    	int col = bot.getRobotPosCol();
    	
    	DIRECTION dir= bot.getRobotCurDir();
    	
    	int blockInFront = 0; // need at least 2 blocks in front to calibrate 

		switch(dir){
		case NORTH:
			// front wall 			
			if (checkIsObstacleOrBorderWall(row + 2, col - 1)) blockInFront++;
			if (checkIsObstacleOrBorderWall(row + 2, col)) blockInFront++;
			if (checkIsObstacleOrBorderWall(row + 2, col + 1)) blockInFront++;
			if (blockInFront >=2) 
				return true;
			break;
			
			
		case SOUTH:
			// front wall 			
			if (checkIsObstacleOrBorderWall(row - 2, col - 1)) blockInFront++;
			if (checkIsObstacleOrBorderWall(row - 2, col)) blockInFront++;
			if (checkIsObstacleOrBorderWall(row - 2, col + 1)) blockInFront++;
			if (blockInFront >=2) 
				return true;
			break;
		
		case EAST:

			// front wall 			
			if (checkIsObstacleOrBorderWall(row + 1, col + 2)) blockInFront++;
			if (checkIsObstacleOrBorderWall(row, col + 2)) blockInFront++;
			if (checkIsObstacleOrBorderWall(row - 1, col + 2)) blockInFront++;
			if (blockInFront >=2) 
				return true;		
			break;
		
		case WEST:

			// front wall 			
			if (checkIsObstacleOrBorderWall(row + 1, col - 2)) blockInFront++;
			if (checkIsObstacleOrBorderWall(row, col - 2)) blockInFront++;
			if (checkIsObstacleOrBorderWall(row - 1, col - 2)) blockInFront++;
			if (blockInFront >=2) 
				return true;
			break;
		}
	
		return false;
    	
    }

    
    public boolean canCalibrateOnLeft(Robot bot){
    	int row = bot.getRobotPosRow();
    	int col = bot.getRobotPosCol();
    	
    	DIRECTION dir= bot.getRobotCurDir();
    	
    	int blockInFront = 0; // need at least 2 blocks in front to calibrate 

		switch(dir){
		case NORTH:
			// left wall
			if (checkIsObstacleOrBorderWall(row + 1, col - 2) && checkIsObstacleOrBorderWall(row - 1, col - 2))
				return true;

			break;
			
			
		case SOUTH:
			// left wall
			if (checkIsObstacleOrBorderWall(row - 1, col + 2) && checkIsObstacleOrBorderWall(row + 1, col + 2))
				return true;

			break;
		
		case EAST:
			if (checkIsObstacleOrBorderWall(row + 2, col + 1) && checkIsObstacleOrBorderWall(row + 2, col - 1))
				return true;
				
			break;
		
		case WEST:
			if (checkIsObstacleOrBorderWall(row - 2, col + 1) && checkIsObstacleOrBorderWall(row - 2, col - 1))
				return true;

			break;
		}
	
		return false;
    }
    
    public boolean checkIsObstacleOrBorderWall(int row, int col){
    	if (row == -1 || row == Constants.MAP_ROW  || col == -1 || col == Constants.MAP_COL)
    		return true;
    	
    	if (isObstacleCell(row, col)) 
    		return true;

    	return false;
    }
    




}
