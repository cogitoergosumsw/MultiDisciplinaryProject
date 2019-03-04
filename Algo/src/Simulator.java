
import javax.swing.*;

import com.sun.java.accessibility.util.java.awt.ButtonTranslator;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.peer.ButtonPeer;

import Map.*;
import Robot.*;
import simulator.Constants.DIRECTION;

/**
 * Simulator  
 * @author Lyu Xintong Isabelle
 */


public class Simulator{

    private static JFrame mainFrame;
    private static JPanel mapPanel, buttonPanel;
    private static Map realMap;
    private static boolean realRun = false;
    private static Robot bot;

    public static void main(String[] args){
        createSimulator();

       
    }


    private static void createSimulator(){
        
       //bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, realRun);


        mainFrame = new JFrame("MDP Group 18 Simulator");
        


        mainFrame.setSize(new Dimension(800, 700));
        mainFrame.setResizable(false);
        // center the frame 
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation(dim.width / 2 - mainFrame.getSize().width / 2, dim.height / 2 - mainFrame.getSize().height / 2);
       
        
        //
        mapPanel = new JPanel(new CardLayout()); 
        buttonPanel = new JPanel(new GridLayout(10,1));
        
        //
        Container contentPane = mainFrame.getContentPane();
        contentPane.add(mapPanel, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.EAST);
        
        addMap();
        



        // create a JPanel for the buttons
        
        // buttonPanel.setSize(new Dimension(100,100));
        // buttonPanel.setLayout(new GridLayout(10,1,0,20));
         addButtons();
        




        // display the JFrame
        mainFrame.setVisible(true);
        //mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        
    }

    private static void addMap(){
        realMap = new Map();
        mapPanel.add(realMap, "REAL_MAP");

        CardLayout cl = ((CardLayout) mapPanel.getLayout());
        cl.show(mapPanel, "REAL_MAP");
    

    }


    private static void addButtons(){
        
        JButton btn_loadMap = new JButton("Load Map");
        JButton btn_Explore = new JButton("Explore");
        JButton btn_fastestPath = new JButton("Fastest Path");
        JButton btn_stop = new JButton("Stop");

        buttonPanel.add(btn_loadMap);
        buttonPanel.add(btn_Explore);
        buttonPanel.add(btn_fastestPath);
        buttonPanel.add(btn_stop);

    }
}