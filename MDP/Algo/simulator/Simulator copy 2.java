package simulator;

import javax.swing.*;

import com.sun.java.accessibility.util.java.awt.ButtonTranslator;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.peer.ButtonPeer;
import java.io.File;
import java.util.concurrent.TimeUnit;

import map.Map;
import map.Cell;
import robot.Robot;
import robot.*;

import utils.MapDescriptor;
import utils.CommMgr;
import static utils.MapDescriptor.generateMapDescriptor;


import algorithms.ExplorationAlgo;
import algorithms.FastestPathAlgo;
import javafx.concurrent.Task;

/**
 * Simulator  
 * @author Lyu Xintong Isabelle
 */


public class Simulator{

    private static JFrame mainFrame;
    private static JPanel mapPanel, buttonPanel, textPanel;
    public static JTextArea textArea;
    private static boolean realRun = false;
    private static Robot bot;
    private static Map exploredMap = null;
    private static Map realMap = null;
    private static final CommMgr comm = CommMgr.getCommMgr();
    private static Cell wayPoint = null;
    
    
    private static int timeLimit = 3600;            
    private static int coverageLimit = 300;  
    
    
    private static Exploration thread_exploration;
    
    public static void main(String[] args){
 
    	bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, realRun);

    	if (!realRun) {
            realMap = new Map(bot);
            realMap.setAllUnexplored();
        }

        exploredMap = new Map(bot);
        exploredMap.setAllUnexplored();
    	
    	createSimulator();
    	
    	//if (realRun)
    	realRunConnection();
    	
    }


    private static void realRunConnection() {
    	String msg; 
    	
    	comm.openConnection();
		comm.sendMsg("Dfasdfadf","dfafdsf" );
    	
    	
//    	while (true){
//    		msg = comm.recvMsg();
//    		if (msg != null){
//    			if (msg.equals(CommMgr.EX_START)){  		
//    				new Exploration().execute();           
//        		}
//    	    
//    	    	if (msg.equals(CommMgr.FP_START)){	
//    	    		new FastestPath().execute();
//    	    	}
//    		}
    		
    		
    		
	    		
    	
 //   	}
    	
    	
    	
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
        addTextArea();
        
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


    
    /**
     * 
     * */

    private static void addButtons(){
        if (!realRun) addButton_loadMap();
    	
        addButton_exploration();
    	addButton_fastestPath();
    	addButton_timeLimited();
    	addButton_coverageLimited();
    	addButton_reset();
    	
    	
            
        
    }
    
    private static void addButton_coverageLimited() {
    	JButton btn_coverageLimited = new JButton("Coverage Limited Exploration");
        	
    	btn_coverageLimited.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		JDialog coverageExploDialog = new JDialog(mainFrame, "Coverage-Limited Exploration", true);
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
                JDialog timeExploDialog = new JDialog(mainFrame, "Time-Limited Exploration", true);
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
                

            }
        });
        
        
    	buttonPanel.add(btn_exploration);
       
    }
    
    
    
    
    
    static class ExplorationTask extends Task<Integer> {
        @Override
        protected Integer call() throws Exception {
    
        	
        	System.out.println("hello \n\n");
        	bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
            exploredMap.repaint();

            ExplorationAlgo exploration;
            exploration = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit);

            if (realRun) {
                CommMgr.getCommMgr().sendMsg(null, CommMgr.BOT_START);
            }

            exploration.runExploration();
            generateMapDescriptor(exploredMap);

            if (realRun) {
            	//TODO 
                // figure this out later 
            	//new FastestPath().execute();
            }
    
        	return 1;
        }
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
    private static void addButton_reset(){
        JButton btn_reset = new JButton("Reset");
        btn_reset.addMouseListener(new MouseAdapter(){
        	public void mousePressed(MouseEvent e){
        	
        		bot.reset(RobotConstants.START_ROW, RobotConstants.START_COL, RobotConstants.START_DIR);
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
 	
        	
        	bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
            exploredMap.repaint();

            if (realRun) {
                while (true) {
                    System.out.println("Waiting for FP_START...");
                    String msg = comm.recvMsg();
                    if (msg.equals(CommMgr.FP_START)) break;
                }
            }
            
            FastestPathAlgo fastestPath = new FastestPathAlgo(exploredMap, bot);
            
            
            if (wayPoint != null){
            	// if wayPoint is set, go to way point first before going to the goal 
            	fastestPath.runFastestPath(wayPoint.getRow(),wayPoint.getCol());
                fastestPath = new FastestPathAlgo(exploredMap, bot);
                fastestPath.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
            } else {
            	fastestPath.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);  
            }
            
           return 222;
        }
    }
    
    /**
     *  Exploration Class for Multi-threading 
     */
    public static class Exploration extends SwingWorker<Integer, String> {
        protected Integer doInBackground() throws Exception {
            
        	CardLayout cl = ((CardLayout) mapPanel.getLayout());
            cl.show(mapPanel, "EXPLORED_MAP");
            
            bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
            exploredMap.repaint();

            ExplorationAlgo exploration;
            exploration = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit);

            if (realRun) {
                CommMgr.getCommMgr().sendMsg(null, CommMgr.BOT_START);
            }

            exploration.runExploration();
            generateMapDescriptor(exploredMap);

            if (realRun) {
            	//TODO 
                // figure this out later 
            	//new FastestPath().execute();
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
        	
        	bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
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
        	
        	bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
            exploredMap.repaint();
            ExplorationAlgo timeExplo = new ExplorationAlgo(exploredMap, realMap, bot, coverageLimit, timeLimit);
            timeExplo.runExploration();
            generateMapDescriptor(exploredMap);
            return 333;
        }
    }

}


