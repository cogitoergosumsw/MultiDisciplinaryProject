package map;

/**
 * Map grid is represented as cells 
 * @author Lyu Xintong Isabelle
 */


public class Cell {
    private final int row;
    private final int col;
    private boolean isObstacle = false;
    private boolean isVirtualWall = false;
    private boolean isExplored = false;
    private boolean isTrail = false;
    private boolean isWayPoint = false;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public void setIsObstacle() {
        this.isObstacle = true;
    }
    
    public void setIsNotObstacle(){
    	this.isObstacle = false;
    }

    public boolean getIsObstacle() {
        return this.isObstacle;
    }

    public void setIsVirtualWall() {   
        this.isVirtualWall = true;

    }
    
    public void setIsNotVirtualWall() {   
        this.isVirtualWall = false;

    }

    public boolean getIsVirtualWall() {
        return this.isVirtualWall;
    }

    public void setIsExplored() {
        this.isExplored = true;
    }

    public boolean getIsExplored() {
        return this.isExplored;
    }

	public void setIsNotExplored() {
		this.isExplored = false;
		
	}
	
	@Override
	public String toString(){
		return "Cell (" + row +", " + col + ")";
	}

	public void setIsTrail() {
		this.isTrail = true;
		
	}
	
	public void setIsNotTrail() {
		this.isTrail = false;
		
	}
	
	public boolean getIsTrail(){
		return this.isTrail;
	}

	public void setIsWayPoint() {
		this.isWayPoint = true;
		
	}
	public void setIsNotWayPoint() {
		this.isWayPoint = false;
		
	}
	
	public boolean getIsWayPoint() {
		return this.isWayPoint;
		
	}
	
}
