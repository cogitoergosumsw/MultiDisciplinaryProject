package map;

public class ProbMap {
    public static float[][] countGrid;
    public static float[][] probGrid;
    private static final float THRESHOLD = 0f; //0.35f;
    
    public static void init(int numRow, int numCol) {
    	countGrid = new float[numRow][numCol];
    	probGrid = new float[numRow][numCol];
    	for (int i=0; i < numRow; i++) {
    		for (int j=0; j < numCol; j++) {
    			probGrid[i][j] = 0;
    			countGrid[i][j] = 0;
    		}
    	}
    }
    
    // readVal is adjusted by weight
    public static void updateCellReading(int rowPos, int colPos, float readVal) {
    	float prevProb = probGrid[rowPos][colPos];
    	float prevCount = countGrid[rowPos][colPos];
    	float prevProbTotal = prevProb * prevCount;
    	float newProbTotal = prevProbTotal + readVal;
    	countGrid[rowPos][colPos] = prevCount + 1;
    	probGrid[rowPos][colPos] = newProbTotal / (prevCount + 1);
    }
    
    public static Boolean isObstacle(int rowPos, int colPos) {
    	if(probGrid[rowPos][colPos] >= THRESHOLD) {
    		return true;
    	} return false;
    }
}
