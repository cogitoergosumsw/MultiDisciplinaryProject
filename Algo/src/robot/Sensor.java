package robot;
import map.Map;
import robot.RobotConstants.DIRECTION;

/**
 * defines sensors on the robot
 *  @author Lyu Xintong Isabelle
 */

 
public class Sensor {
    private final int lowerRange;
    private final int upperRange;
    private int sensorPosRow;
    private int sensorPosCol;
    private DIRECTION sensorDir;
    private final String id;    
    
    /**
     * front sensor (north facing) id: SSFront1, SSFront2, SSFront3
     * left sensor (west facing) id: SSLeft1, SSLeft2
     * right sensor (east facing) id: SLRight 
     */

    public Sensor(int lowerRange, int upperRange, int row, int col, DIRECTION dir, String id) {
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.sensorPosRow = row;
        this.sensorPosCol = col;
        this.sensorDir = dir;
        this.id = id;
    }

    public void setSensor(int row, int col, DIRECTION dir) {
        this.sensorPosRow = row;
        this.sensorPosCol = col;
        this.sensorDir = dir;
    }

    /**
     * Returns the number of cells to the nearest detected obstacle or -1 if no obstacle is detected.
     */
    public int sense(Map exploredMap, Map realMap) {
        switch (sensorDir) {
            case NORTH:
                return getSensorVal(exploredMap, realMap, 1, 0);
            case EAST:
                return getSensorVal(exploredMap, realMap, 0, 1);
            case SOUTH:
                return getSensorVal(exploredMap, realMap, -1, 0);
            case WEST:
                return getSensorVal(exploredMap, realMap, 0, -1);
        }
        return -1;
    }

    /**
     * Sets the appropriate obstacle cell in the map and returns the row or column value of the obstacle cell. Returns
     * -1 if no obstacle is detected.
     */
    private int getSensorVal(Map exploredMap, Map realMap, int rowInc, int colInc) {
        // Check if starting point is valid for sensors with lowerRange > 1.
        if (lowerRange > 1) {
            for (int i = 1; i < this.lowerRange; i++) {
                int row = this.sensorPosRow + (rowInc * i);
                int col = this.sensorPosCol + (colInc * i);
                
                if (!exploredMap.checkValidCoordinates(row, col)) return i;
                
                exploredMap.getCell(row, col).setIsExplored();
                
                if (realMap.getCell(row, col).getIsObstacle()){
                	exploredMap.setObstacleCell(row, col);
                	return i;
                }
            }
        }

        // Check if anything is detected by the sensor and return that value.
        for (int i = this.lowerRange; i <= this.upperRange; i++) {
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);

            if (!exploredMap.checkValidCoordinates(row, col)) return i;

            exploredMap.getCell(row, col).setIsExplored();

            if (realMap.getCell(row, col).getIsObstacle()) {
                exploredMap.setObstacleCell(row, col);
                return i;
            }
        }

        // Else, return -1.
        return -1;
    }

    /**
     * Uses the sensor direction and given value from the actual sensor to update the map.
     */
    public void senseReal(Map exploredMap, int sensorVal) {
        switch (sensorDir) {
            case NORTH:
                processSensorVal(exploredMap, sensorVal, 1, 0);
                break;
            case EAST:
                processSensorVal(exploredMap, sensorVal, 0, 1);
                break;
            case SOUTH:
                processSensorVal(exploredMap, sensorVal, -1, 0);
                break;
            case WEST:
                processSensorVal(exploredMap, sensorVal, 0, -1);
                break;
        }
    }


    /**
     * Sets the correct cells to explored and/or obstacle according to the actual sensor value.
     */
    private void processSensorVal(Map exploredMap, int sensorVal, int rowInc, int colInc) {

    	
//    	// sensor reading gives 0 if there's no obstacle 
//    	if (sensorVal == 0) 
//    		 

//       // If above fails, check if starting point is valid for sensors with lowerRange > 1.
//        for (int i = 1; i < this.lowerRange; i++) {
//            int row = this.sensorPosRow + (rowInc * i);
//            int col = this.sensorPosCol + (colInc * i);
//
//            if (!exploredMap.checkValidCoordinates(row, col)) return;
//            if (exploredMap.getCell(row, col).getIsObstacle()) return;
//        }

        // Update map according to sensor's value.
        for (int i = this.lowerRange; i <= this.upperRange; i++) {
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);

            if (!exploredMap.checkValidCoordinates(row, col)) 
            	continue;

            exploredMap.getCell(row, col).setIsExplored();
      
            if (sensorVal == i) {
                exploredMap.setObstacleCell(row, col);
                exploredMap.repaint(); //debug
                break;
            } else if (exploredMap.getCell(row, col).getIsObstacle()) {
            	// clear previously set obstacle cells if sensors detect no obstacle
            	//if (id.equals("SSFront1") || id.equals("SSFront2") || id.equals("SSFront3")) 
            		
            	exploredMap.clearObstacleCell(row, col);
            	exploredMap.repaint(); //debug
            }
           
        }
    }
    
    public void printSensor(){
    	System.out.println(lowerRange);
    	System.out.println(upperRange);
    	System.out.println(sensorPosRow);
    	System.out.println(sensorPosCol);
    	System.out.println(sensorDir);
    	System.out.println(id);
    	
    }
}
