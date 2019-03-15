package simulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.peer.ButtonPeer;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import map.Map;
import map.Cell;
import robot.Robot;
import robot.RobotConstants.MOVEMENT;
import robot.*;

import utils.MapDescriptor;
import utils.CommMgr;
import static utils.MapDescriptor.generateMapDescriptor;


import algorithms.ExplorationAlgo;
import algorithms.FastestPathAlgo;


/**
 * Simulator  
 * @author Lyu Xintong Isabelle
 */




public class Simulator{

    private static JFrame mainFrame;
    private static JPanel mapPanel, buttonPanel, textPanel;
    public static JTextArea textArea;
    public static boolean realRun = true;
//    public static boolean realRun = false;
    private static Robot bot;
    private static Map exploredMap = null;
    private static Map realMap = null;
    private static final CommMgr comm = CommMgr.getCommMgr();
    private static Cell wayPoint = null;
    public static boolean explorationDone = false;
    public static boolean fastestPathDone = false;
    private static int timeLimit = 3600;            
    private static int coverageLimit = Constants.MAP_SIZE;  
    
//    private static SwingWorker thread_exploration;
    
    
    public static void main(String[] args){
    	// establish connection if realRun
    	if (realRun)
    		while (!comm.openConnection());
    		
    	
    	bot = new Robot(Constants.START_ROW, Constants.START_COL, realRun);

    	if (!realRun) {
            realMap = new Map(bot);
            realMap.setAllUnexplored();
        }

        exploredMap = new Map(bot);
        exploredMap.setAllUnexplored();
    	
    	createSimulator();
    	
    	
    	 String[] mapStrings = MapDescriptor.generateMapDescriptor(exploredMap);
         //debug               
         System.out.println("EXPLORE|"+mapStrings[0]);
         System.out.println("OBSTACLE|"+mapStrings[1]);
         
    	
    	
    	if (realRun)
    		realRunConnection();
    	
    	
    	
    	
    }   	

    //TODO
    private static void realRunConnection() {
    	String msg; 
    				
		msg = comm.recvMsg();
		

		if (!explorationDone){
			while (!msg.contains(CommMgr.EX_START)){	
				if (msg.contains(CommMgr.EX_START)){  		
					break;         
				}
				
				if (msg.contains(CommMgr.WAYPOINT_DATA)){
					setWayPoint(msg);
				}	
				msg = comm.recvMsg();		
			}
			
			if (msg.contains(CommMgr.EX_START)){  		
					
				//comm.sendMsg(MOVEMENT.CALIBRATE,CommMgr.MOVE);
				new Exploration().execute();   
					
				 //debug
//				coverageLimit = 81;
//				new CoverageExploration().execute();   
//				
	    	}	
		}
		
   
//	    if (msg.contains(CommMgr.FP_START)){	
//	    		new FastestPath().execute();
//	    }
//	    	
//	    if (msg.contains(CommMgr.TIME_EX_START)){	
//	    		new TimeExploration().execute();
//	    }
//	    	
//	    if (msg.contains(CommMgr.COVERAGE_EX_START)){	
//	    		new CoverageExploration().execute();
//	    }
	    	

}




	private static void createSimulator(){

        mainFrame = new JFrame("MDP Group 18 Simulator");
        mainFrame.setSize(new Dimension(800, 700));
        mainFrame.setResizable(false);
        
        // center the frame 
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation(dim.width / 2 - mainFrame.getSize().width / 2, dim.height / 2 - mainFrame.getSize().height / 2);

        //
        mapPanel = new JPanel(new CardLayout()); 
        buttonPanel = new JPanel(new GridLayout(8,1));
        textPanel = new JPanel();
        //
        Container contentPane = mainFrame.getContentPane();
        contentPane.add(mapPanel, "Center");
        contentPane.add(buttonPanel, "East");
        
        addMap();
        addButtons();
        //addTextArea();
        
        // display the JFrame
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
    }

    
    public static void addTextArea(){
    	textArea = new JTextArea("");	
    	textArea.setText("hello");	
    	buttonPanel.add(textArea, "South"); 	
    }
    
    /**
     * 
     * */
    private static void addMap(){
        if(!realRun){
        	mapPanel.add(realMap, "REAL_MAP");
        }
        mapPanel.add(exploredMap, "EXPLORED_MAP");

        CardLayout cl = ((CardLayout) mapPanel.getLayout()); 
        if(!realRun){
        	cl.show(mapPanel, "REAL_MAP");
        }else{
        	cl.show(mapPanel, "EXPLORED_MAP");
        }        
    }

	private static void setWayPoint(String msg) {
		
		// clear previously set way point if any 
		for (int i = 0; i < Constants.MAP_ROW; i++){
			for (int j = 0; j < Constants.MAP_COL; j ++){
				exploredMap.grid[i][j].setIsNotWayPoint();
			}
		}

		
		String[] msgArr = msg.split("\\|");
        String[] data = msgArr[1].split(",");
        
        int row = Integer.parseInt(data[0].replaceAll("\\p{Punct}", ""));
        int col = Integer.parseInt(data[1].replaceAll("\\p{Punct}", ""));
        exploredMap.grid[row][col].setIsWayPoint();
               
        wayPoint = new Cell(row, col);
       
        CardLayout cl = ((CardLayout) mapPanel.getLayout());
     	cl.show(mapPanel, "EXPLORED_MAP");
     	exploredMap.repaint(); 
	}
    
	private static void setWayPoint(int row, int col) {
	
		setWayPoint(CommMgr.WAYPOINT_DATA + "|" + row + "," + col );
	}
	
    /**
     * 
     * */

    private static void addButtons(){
        if (!realRun) addButton_loadMap();
    	
        addButton_exploration();
    	addButton_fastestPath();
    	addButton_timeLimited();
    	addButton_coverageLimited();
    	addButton_wayPoint();
    	addButton_reset();
    	
    	
            
        
    }

    private static void addButton_wayPoint() {
    	JButton btn_wayPoint= new JButton("Set Way Point");	
		
    	btn_wayPoint.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                final JDialog wayPointDialog = new JDialog(mainFrame, "Set Way Point", true);
                wayPointDialog.setSize(400, 60);
                wayPointDialog.setLayout(new FlowLayout());
                final JTextField wayPointTF = new JTextField(5);
                JButton saveButton = new JButton("OK");

                saveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        wayPointDialog.setVisible(false);
                        String wp = wayPointTF.getText();
                        String[] wpArry = wp.split(",");
                        int row = Integer.parseInt(wpArry[0]);
                        int col = Integer.parseInt(wpArry[1]);
                        setWayPoint(row, col);
                    }
                });

                wayPointDialog.add(new JLabel("way point ( row,col E.g. 1,3 ) : "));
                wayPointDialog.add(wayPointTF);
                wayPointDialog.add(saveButton);
                wayPointDialog.setVisible(true);
            }
        });
        buttonPanel.add(btn_wayPoint);	
	}
	
    private static void addButton_coverageLimited() {
    	JButton btn_coverageLimited = new JButton("Coverage Limited Exploration");
        	
    	btn_coverageLimited.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		final JDialog coverageExploDialog = new JDialog(mainFrame, "Coverage-Limited Exploration", true);
                coverageExploDialog.setSize(400, 60);
                coverageExploDialog.setLayout(new FlowLayout());
                final JTextField coverageTF = new JTextField(5);
                JButton coverageSaveButton = new JButton("Run");

                
                coverageSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        coverageExploDialog.setVisible(false);
                        coverageLimit = (int) ((Integer.parseInt(coverageTF.getText())) * Constants.MAP_SIZE / 100.0);
                        new CoverageExploration().execute();
//                        CardLayout cl = ((CardLayout) mapPanel.getLayout());
//                        cl.show(mapPanel, "EXPLORED_MAP");         
                    }
                });

                coverageExploDialog.add(new JLabel("Coverage Limit (% of maze): "));
                coverageExploDialog.add(coverageTF);
                coverageExploDialog.add(coverageSaveButton);
                coverageExploDialog.setVisible(true);        		
        	}
        });
        buttonPanel.add(btn_coverageLimited);	
	}


	
	private static void addButton_timeLimited() {
		JButton btn_timeLimited = new JButton("Time Limited Exploration");	
		
		btn_timeLimited.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                final JDialog timeExploDialog = new JDialog(mainFrame, "Time-Limited Exploration", true);
                timeExploDialog.setSize(400, 60);
                timeExploDialog.setLayout(new FlowLayout());
                final JTextField timeTF = new JTextField(5);
                JButton timeSaveButton = new JButton("Run");

                timeSaveButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        timeExploDialog.setVisible(false);
                        String time = timeTF.getText();
//                        String[] timeArr = time.split(":");
//                        timeLimit = (Integer.parseInt(timeArr[0]) * 60) + Integer.parseInt(timeArr[1]);
                        timeLimit = Integer.parseInt(time);
//                        
//                        CardLayout cl = ((CardLayout) mapPanel.getLayout());
//                        cl.show(mapPanel, "EXPLORED_MAP");
                        new TimeExploration().execute();
                    }
                });

                timeExploDialog.add(new JLabel("Time Limit in seconds: "));
                timeExploDialog.add(timeTF);
                timeExploDialog.add(timeSaveButton);
                timeExploDialog.setVisible(true);
            }
        });
        buttonPanel.add(btn_timeLimited);	
	}

	
	private static void addButton_loadMap(){
    	JButton btn_loadMap = new JButton("Load Map");
        // button for add map 
        btn_loadMap.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
            	JFileChooser fc = new JFileChooser(System.getProperty("user.dir")+"/maps");
            	int returnVal = fc.showOpenDialog(mainFrame);
            	File mapFile = null;
            	if (returnVal == JFileChooser.APPROVE_OPTION) {
    	            mapFile = fc.getSelectedFile();
    	        } 
            	
                MapDescriptor.loadMapFromDisk(realMap, mapFile.getAbsolutePath());
	                CardLayout cl = ((CardLayout) mapPanel.getLayout());
	                cl.show(mapPanel, "REAL_MAP");
	                realMap.repaint();            
	        	}
        });
        buttonPanel.add(btn_loadMap);
    }
    
    private static void addButton_exploration(){
    	JButton btn_exploration = new JButton("Exploration");
    	
        

        // Exploration Button
        
        
        btn_exploration.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {         	
//            	CardLayout cl = ((CardLayout) mapPanel.getLayout());
//              cl.show(mapPanel, "EXPLORED_MAP"); 
            	
            	new Exploration().execute();
//                thread_exploration = new Exploration();
//                thread_exploration.execute();
     

            }
        });
        
        
    	buttonPanel.add(btn_exploration);
       
    }
   
    private static void addButton_fastestPath(){
    	JButton btn_fastestPath = new JButton("Fastest Path");

    	btn_fastestPath.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
//                CardLayout cl = ((CardLayout) mapPanel.getLayout());               
//                cl.show(mapPanel, "EXPLORED_MAP");
                new FastestPath().execute();
            }
        });
    	buttonPanel.add(btn_fastestPath);
    }
     
    /**
     * ???
     * */
    //TODO
    private static void addButton_reset(){
        JButton btn_reset = new JButton("Reset");
        btn_reset.addMouseListener(new MouseAdapter(){
        	public void mousePressed(MouseEvent e){
        	
        		bot.reset(Constants.START_ROW, Constants.START_COL, RobotConstants.START_DIR);
        		exploredMap.reset();   
        		realMap.reset();      		
        		exploredMap.repaint();
        		realMap.repaint(); 			
        	}
        });
        buttonPanel.add(btn_reset);
    }
   
    
    /**
     *  Fastest path Class for Multi-threading
     */
    private static class FastestPath extends SwingWorker<Integer, String> {
        protected Integer doInBackground() throws Exception {
        	
        	CardLayout cl = ((CardLayout) mapPanel.getLayout());
            cl.show(mapPanel, "EXPLORED_MAP");
 		
        	bot.setRobotPos(Constants.START_ROW, Constants.START_COL);
            exploredMap.repaint();

            if (realRun) {
            	System.out.println("debug line 437");
                while (true) {
                    System.out.println("Waiting for FP_START...");
                    String msg = comm.recvMsg();
                    if (msg.contains(CommMgr.FP_START)) break;
                }
            }
           
            
            
            FastestPathAlgo fastestPath = new FastestPathAlgo(exploredMap, bot, true);

            if (wayPoint != null){
            	// if wayPoint is set, go to way point first before going to the goal 
            	String path1 = fastestPath.runFastestPath(wayPoint.getRow(),wayPoint.getCol());
                fastestPath = new FastestPathAlgo(exploredMap, bot, true);
                String path2 = fastestPath.runFastestPath(Constants.GOAL_ROW, Constants.GOAL_COL);
                
                if (realRun) {
                	//TODO send fastest path twice !!!
                	comm.sendMsg(path1, CommMgr.MOVE);
                	comm.sendMsg(path2, CommMgr.MOVE);
                	comm.sendMsg("",CommMgr.FP_DONE);
                }
                
                
                
            } else {
            	String path = fastestPath.runFastestPath(Constants.GOAL_ROW, Constants.GOAL_COL);  
            	            	
            	if (realRun) {
            		comm.sendMsg("",CommMgr.FP_DONE);
            	}
            
            }
 
           return 222;
        }
    }
    
    /**
     *  Exploration Class for Multi-threading 
     */
    public static class Exploration extends SwingWorker<Integer, String> {
        @Override
    	protected Integer doInBackground() throws Exception {
        	
        	CardLayout cl = ((CardLayout) mapPanel.getLayout());
            cl.show(mapPanel, "EXPLORED_MAP");
            
            bot.setRobotPos(Constants.START_ROW, Constants.START_COL);
            exploredMap.repaint();

            ExplorationAlgo exploration;
            exploration = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit);

//            if (realRun) {
//                CommMgr.getCommMgr().sendMsg(CommMgr.BOT_START);
//            }
            
            //debug
            //System.out.println("debug: inside Exploration.....");
            
            exploration.runExploration();
            generateMapDescriptor(exploredMap);
     
            
            // starting the fastest path thread after exploration is done
            if (realRun) {
            	new FastestPath().execute();     
            }
        	
            return 111;
            
        }
    }

    /**
     *  Coverage limited exploration Class for Multi-threading
     */
    private static class CoverageExploration extends SwingWorker<Integer, String> {
        protected Integer doInBackground() throws Exception {
       
        	CardLayout cl = ((CardLayout) mapPanel.getLayout());
            cl.show(mapPanel, "EXPLORED_MAP");
        	
        	bot.setRobotPos(Constants.START_ROW, Constants.START_COL);
            exploredMap.repaint();

            ExplorationAlgo coverageExplo = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit);
            coverageExplo.runExploration();

            generateMapDescriptor(exploredMap);

            return 444;
        }
    }

    /**
     *  Time limited exploration Class for Multi-threading
     */
	private static class TimeExploration extends SwingWorker<Integer, String> {
        protected Integer doInBackground() throws Exception {
            
        	CardLayout cl = ((CardLayout) mapPanel.getLayout());
            cl.show(mapPanel, "EXPLORED_MAP");
        	
        	bot.setRobotPos(Constants.START_ROW, Constants.START_COL);
            exploredMap.repaint();
            ExplorationAlgo timeExplo = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit);
            timeExplo.runExploration();
            generateMapDescriptor(exploredMap);
            return 333;
        }
    }

}

