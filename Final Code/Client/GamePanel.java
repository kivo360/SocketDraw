import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.text.*;


/***
 * 
 * @author Kevin Hill, Sahil Chaudhrey, Daniel Flint
 *
 */
public class GamePanel extends JPanel implements Runnable, Serializable
{
	private ButtonGroup bg;
	private JRadioButton sm,md,lg;
	private Canvas canvas;
	private JColorChooser colorChoose;
	private Color currentColor = Color.BLACK;
	private Color lastColor;
	private int brushSize, lastBrushSize;
	
	private Socket socket = null;
	private ObjectInputStream oji;
	private ObjectOutputStream ojo;
	private Vector<Object> objects = new Vector<Object>();
	private Vector<Object> netObjects = new Vector<Object>();
	private boolean yourTurn, gameStarted;
	private JButton clearAll, guessButton;
	private final JPanel colorChoice;
	private JLabel time, teamNumber, scoreLabel, remainingGuesses;
	private int teamNum, guessInt, countDownPeriod= 120, team1Score = 0, team2Score = 0;
	private JTextField guessField;
	private String serverIP = null;
	String currentWord = "";
	
	
	private int coin = 0;
	
	private String teamName = "Team ";
	
	/***
	 * 	Has GUI for GamePanel
	 * @param String s
	 */
	public GamePanel(String s){
	
		serverIP = s;
		GridBagLayout gridBag = new GridBagLayout();
		setLayout(gridBag);
		objects.add(Color.BLACK);
		GridBagConstraints cons = new GridBagConstraints();
		colorChoose = new JColorChooser();
		canvas = new Canvas();
		
		cons.gridx = 0;
		cons.gridy = 0;
		add(canvas, cons);
		
		sm = new JRadioButton("Small");
		md = new JRadioButton("Medium");
		lg = new JRadioButton("Large");
		gridBag.setConstraints(sm, cons);
		gridBag.setConstraints(md, cons);
		gridBag.setConstraints(lg, cons);
		
		
		
		JPanel brushes = new JPanel();
		clearAll = new JButton("Clear Screen");
		clearAll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if(yourTurn && gameStarted)
				{
					try
					{
						ojo.writeObject("Clear");
						ojo.flush();
					}
					catch(IOException ioe){}
				}
			}
			
		});
			
			
	   colorChoice = new JPanel();
		colorChoice.setPreferredSize(new Dimension(10,10));
		bg = new ButtonGroup();
		bg.add(sm);
		bg.add(md);
		bg.add(lg);
		sm.setSelected(true);
		
		brushes.add(clearAll);
		brushes.add(sm);
		brushes.add(md);
		brushes.add(lg);
		brushes.add(new JLabel("Color: "));
		colorChoice.setBackground(Color.BLACK);
		brushes.add(colorChoice);
		colorChoice.addMouseListener(
		
			new MouseListener()
			{
			public void mouseExited(MouseEvent me){}
			public void mouseEntered(MouseEvent me){}
			public void mouseReleased(MouseEvent me){}
			public void mousePressed(MouseEvent me){}
			public void mouseClicked(MouseEvent me){
			
			if(yourTurn && gameStarted)
			{
					currentColor = colorChoose.showDialog(null, "Pick your color", Color.BLACK);
					if(currentColor == null)
					{
						currentColor = Color.BLACK;
						colorChoice.setBackground(currentColor);
					}
					else
					{
						colorChoice.setBackground(currentColor);
					}
			}
			}
			
			}
		);
		
		
		cons.gridx = 0;
		cons.gridy = 1;
		add(brushes, cons);
		
		
		
		teamNumber = new JLabel("Team #");
		time = new JLabel("Time Remaining: ");
		
		remainingGuesses = new JLabel("Guesses Left: "+guessInt);
		guessButton = new JButton("Guess: ");
		
		
		guessButton.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent e)
			{
				String in = guessField.getText();
				if(in.equals("")||in.equals(null))
				{
				
				}
				else
				{
					try{
						ojo.writeObject("ATTEMPT_GUESS");
						ojo.flush();
						ojo.writeObject(teamName);
						ojo.flush();
						ojo.writeObject(in);
						ojo.flush();
						guessField.setText("");
					}
					catch(IOException ioe){}
				}
			
			}});
		
		guessField = new JTextField(15);

		
		scoreLabel = new JLabel("Score:   Team 1: "+team1Score+"  Team 2: "+ team2Score);


		
		cons.gridx = 0;
		cons.gridy = 2;
		add(teamNumber, cons);
		
		cons.gridx = 0;
		cons.gridy = 3;
		add(time, cons);
		
		
		JPanel guessPanel = new JPanel();
		guessPanel.add(guessButton);
		guessPanel.add(guessField);
		guessPanel.add(remainingGuesses);
		
		cons.gridx = 0;
		cons.gridy = 4;
		add(guessPanel, cons);
		
		cons.gridx = 0;
		cons.gridy = 5;
		add(scoreLabel, cons);
	}
	
	/***
	 * Start GamePanel Receiving 
	 */
	public void run()
	{
		try{
				socket = new Socket(serverIP, 16790);
			try
			{
				socket.setTcpNoDelay(true);
			}
			catch(SocketException se){}
				ojo = new ObjectOutputStream(socket.getOutputStream());
				ojo.flush();
				oji = new ObjectInputStream(socket.getInputStream());
			
			}
			catch(IOException ioe){}
			
		while(true)
		{
			// clear Screen will not work unless repaint is called
			canvas.repaint();
			try
			{
				
				Object currentObj = oji.readObject();
				
				if(currentObj instanceof Vector && !yourTurn)
				{
						objects.addAll((Vector<?>)currentObj);
						canvas.repaint();
				}					
				else if(currentObj instanceof Integer)
				{
					if((int)currentObj == 1)
					{
						yourTurn = true;
						coin = 1;
						sm.setEnabled(true);
						md.setEnabled(true);
						lg.setEnabled(true);
						clearAll.setEnabled(true);
					}
					else
					{
						yourTurn = false;
						coin = 0;
						sm.setEnabled(false);
						md.setEnabled(false);
						lg.setEnabled(false);
						clearAll.setEnabled(false);
					}
				}
				else if(currentObj instanceof String)
				{
					if(currentObj.toString().equals("TIME"))
					{
						int timeLeft = countDownPeriod - (int)oji.readObject();
						int minutes = timeLeft/60;
						int seconds = (timeLeft - (minutes*60));
						DecimalFormat df = new DecimalFormat("#00.###");  
						time.setText("Time Remaining: "+minutes+":"+df.format(seconds));
					}
					else if(currentObj.toString().equals("BEGIN"))
					{
					
					
						gameStarted = true;
						currentWord = oji.readObject().toString();
						
						
						if(!yourTurn)
						{
							sm.setEnabled(false);
							md.setEnabled(false);
							lg.setEnabled(false);
							clearAll.setEnabled(false);
						}
						else{
							sm.setEnabled(true);
							md.setEnabled(true);
							lg.setEnabled(true);
							clearAll.setEnabled(true);
						}
					}
					else if(currentObj.toString().equals("CLEAR_BOARD"))
					{
						objects.clear();
						netObjects.clear();
						objects.add(Color.BLACK);
						colorChoice.setBackground(Color.BLACK);
						sm.setSelected(true);
						objects.add(5);
					}
					else if(currentObj.toString().equals("GUESS_COUNT"))
					{
						guessInt = (int)oji.readObject();
						remainingGuesses.setText("Guesses Left: "+guessInt);
					}
					else if(currentObj.toString().equals("UPDATE_SCORE"))
					{
						team1Score = (int)oji.readObject();
						team2Score = (int)oji.readObject();
						scoreLabel.setText("Score:   Team 1: "+team1Score+"  Team 2: "+ team2Score);
					}
					else if(currentObj.toString().equals("WORD"))
					{
						System.out.println(oji.readObject().toString());
					}
					else if(currentObj.toString().equals("UPDATE"))
					{
 						if(yourTurn)
 						{
 							ojo.writeObject(coin);
 							ojo.flush();
 						}
 						else
 						{
 							ojo.writeObject(coin);
 							ojo.flush();
 						}
					}
					else if(currentObj.toString().equals("TEAM"))
					{
						if((int)oji.readObject() == 1)
						{
							teamName = teamName + 1;
							teamNumber.setText(teamName);
						}
						else
						{
							teamName = teamName + 2;
							teamNumber.setText(teamName);
						}
					}
				}
			}
			catch(ClassNotFoundException cnf){System.out.println("Class Not Found");}
			catch(IOException ioe){System.out.println("IO Exception");}
			}
	}
	
	
	
	class Canvas extends JPanel implements MouseListener, MouseMotionListener, Serializable {
		 Toolkit toolkit = Toolkit.getDefaultToolkit();
		 Image imSmall = toolkit.getImage(getClass().getResource("images/small.png"));
		 Image imMedium = toolkit.getImage(getClass().getResource("images/medium.png"));
		 Image imLarge = toolkit.getImage(getClass().getResource("images/large.png"));
		 Cursor small = toolkit.createCustomCursor(imSmall , new Point(super.getX(),super.getY()), "small");
		 Cursor medium = toolkit.createCustomCursor(imMedium , new Point(super.getX(),super.getY()), "medium");
		 Cursor large = toolkit.createCustomCursor(imLarge , new Point(super.getX(),super.getY()), "large");
		 Cursor currentCursor;
		 Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) ;

		 
		public Canvas()
		{
			setPreferredSize(new Dimension(500,300));
			setBackground(Color.WHITE);
			addMouseListener(this);
			addMouseMotionListener(this);
		}
	
	
		
		public void paintComponent(Graphics g){
				super.paintComponent(g);
				
				if(yourTurn)
				{
					g.setFont(new Font("x", 1, 20));
					g.drawString("Your word is: "+currentWord,20,20);
				}
				
				for(Object oj : (Vector<?>)objects.clone())
				{
					if(oj instanceof Point)
					{
						Point pt = (Point)oj;
						g.fillOval((int)pt.getX(),(int)pt.getY(),brushSize,brushSize);
					}
					else if(oj instanceof Color)
					{
						g.setColor((Color)oj);
					}
					else if(oj instanceof Integer)
					{
						brushSize = (int)oj;
					}
				}		
		}
		
		public void checkSize(){
		
			if(!yourTurn)
			{
				super.setCursor(defaultCursor);
			}
			else if(yourTurn && gameStarted)
			{
				if(sm.isSelected())
				{
					super.setCursor(small);
					brushSize = 5;
					
					if(brushSize != lastBrushSize)
					{
						objects.add(5);
						netObjects.add(5);
						checkTemp();
					}
				}
				else if(md.isSelected())
				{
					super.setCursor(medium);
					brushSize = 13;
					
					if(brushSize != lastBrushSize)
					{
						objects.add(13);
						netObjects.add(13);
						checkTemp();
					}

				}
				else if(lg.isSelected())
				{
					super.setCursor(large);
					brushSize = 30;
					
					if(brushSize != lastBrushSize)
					{
						objects.add(30);
						netObjects.add(30);
						checkTemp();
					}

				}
			}
		}
		
		
		/****
		 * Assigns Mouse info
		 * @param MouseEvent me
		 */
		public void mouseExited(MouseEvent me){
			lastColor = currentColor;
			lastBrushSize = brushSize;
		}
		
		/****
		 * Adds brush info to vectors
		 * @param MouseEvent me
		 */
		public void mouseEntered(MouseEvent me){
			checkSize();
			if(yourTurn && gameStarted)
			{
				checkSize();
				if(currentColor != lastColor)
				{
					objects.add(currentColor);
					netObjects.add(currentColor);
					checkTemp();
				}		
			}
		}
		
		/****
		 * get brush location. Add location to vectors
		 * @param MouseEvent me
		 */
		public void mouseClicked(MouseEvent me){
		
			if(yourTurn && gameStarted)
				{
					objects.add(new Point(me.getX(),me.getY()));
					netObjects.add(new Point(me.getX(),me.getY()));
					push();
					repaint();
				}
		}
		public void mousePressed(MouseEvent me){
		}
		public void mouseReleased(MouseEvent me){
				push();
		}
		public void mouseDragged(MouseEvent me){
			if(yourTurn && gameStarted)
			{
				Point point = new Point(me.getX(),me.getY());
				
				if(point.getX()< 500 && point.getY()< 500 && point.getX() >-50 && point.getY() >-50)
				{
					objects.add(point);
					netObjects.add(point);
					checkTemp();
					repaint();
				}
			}
		}
		
		//Only to override
		public void mouseMoved(MouseEvent me){}
		
		/****
		 * Checks Size of ArrayList to Send Data every 5 points
		 */
		public void checkTemp()
		{
			if(yourTurn && gameStarted)
			{
				if(netObjects.size() >= 5)
				{
					try{
							ojo.writeObject(netObjects);
							ojo.flush();
							netObjects = null;
							netObjects = new Vector<Object>();
						}
					catch(IOException ioe){}
				}
			}
		}
		
		/***
		 * Push final points.
		 * Reset netObjects vector
		 */
		public void push()
		{
			if(yourTurn && gameStarted)
				{
				try{
						ojo.writeObject(netObjects);
						ojo.flush();
						netObjects = null;
						netObjects = new Vector<Object>();
					}
						catch(IOException ioe){}
				}
		}
	}
}