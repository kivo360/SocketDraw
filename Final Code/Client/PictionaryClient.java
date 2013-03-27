import javax.swing.*;
import java.awt.*;
import java.io.*;

public class PictionaryClient extends JFrame implements Serializable {
	
	JMenu file;
	JMenuBar mb;
	String ip = "";
	
	
	/***
	 * @constructor
	 * Brings all client stuff together
	 */
	public PictionaryClient(){
	
		setLayout(new BorderLayout());
		mb = new JMenuBar();
		
		file = new JMenu("File");
		mb.add(file);
		setJMenuBar(mb);
		
		ip = JOptionPane.showInputDialog("Please enter the server IP address"); 
		GamePanel gamePanel = new GamePanel(ip);
		Chat chat = new Chat(ip);
		
		Thread gameThread = new Thread(gamePanel);
		gameThread.start();
		
		Thread chatThread = new Thread(chat);
		chatThread.start();
	
		
		add(gamePanel, BorderLayout.NORTH);
		
		add(chat, BorderLayout.SOUTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setMinimumSize(getSize());
		setSize(550,550);
		
		setTitle("Pictionary v1.0");
		
		setVisible(true);
	}
	
	
	public static void main(String[]args){
		new PictionaryClient();
	}
}