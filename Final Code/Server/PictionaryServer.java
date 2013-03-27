import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;

 /***
  * 
  * @author Kevin Hill, Sahil Chaudhrey, Daniel Flint
  *
  */
public class PictionaryServer{
	//Declare all Variables
	private ServerSocket chatSS, gameSS;
	private Vector<PrintWriter> chatWriters = new Vector<PrintWriter>();
	private Vector<ObjectOutputStream> ojOuts = new Vector<ObjectOutputStream>();
	private Vector<Object> objectList = new Vector<Object>();
	private javax.swing.Timer globalTimer;
	private boolean acceptMore = true;
	private int guessCount = 3;
	private FileGrabber fg;
	private String currentWord = null;
	
	
	private int [] coins = {1,0,0,0};
	private int [] teamName = {1,2,1,2};
	private int team1Score = 0, team2Score = 0;
	
	private boolean hasPlayers = false, teamFlip = true, restartTime = false;
	
	
	public PictionaryServer(){
				
		try
		{
			System.out.println(InetAddress.getLocalHost());
			gameSS = new ServerSocket(16790);
			chatSS = new ServerSocket(16789);
		}
		catch(UnknownHostException uhe){}
		catch(IOException ioe){}
		acceptClients();
		fg = new FileGrabber();
		
	}
	
	/**
	 * Accept all of the clients (Using Threaded Loop)
	 * Stop at 4 clients
	 * **/
	private void acceptClients()
	{
		//Run Thread to 
		new Thread()
		{
			//
			Socket cs;
			int currentThread = 0;
			public void run()
			{
				while(true) 
				{
					
					try{
						cs = chatSS.accept();
						Thread newChat = new Thread(new ClientThreadChat(cs));
						newChat.start();	
						
						currentThread++;
						if(currentThread == 4)break;
					}
					catch(IOException ioe){}
				}
				
			}
			}.start();//End thread
				
				//Starts the Game Timer After All 4 Clients are in
				new Thread() 
				{
					
					Socket gs;
					int currentThread = 0;
					public void run()
					{
						while(true) 
						{
							try{
								//Start game timer
								gs = gameSS.accept();
								Thread newGameTimer = new Thread(new ClientThreadGame(gs, coins[currentThread], teamName[currentThread]));
								newGameTimer.start();
								currentThread++;
								if(currentThread == 4)break;
							}
							catch(IOException ioe){}	
						}
					}
				}.start();//End Thread
				
				
	}
	
	
	/****
	 * 
	 * @author Kevin Hill, Sahil Chaudhrey, Daniel Flint
	 *	Runs Game Thread
	 *
	 */
	class ClientThreadGame implements Runnable{
		
	   private ObjectInputStream oji;
		private ObjectOutputStream ojo;
		private Socket gameSocket;		
		private int coin;
		private int team;
		
		/****
		 * Constructor: Declare all variables in game thread
		 *  
		 * @param Socket s
		 * @param Int c
		 * @param Int t
		 */
		public ClientThreadGame(Socket s, int c, int t)
		{
			coin = c;
			gameSocket = s;
			team = t;
			try
			{
				gameSocket.setTcpNoDelay(true);
			}
			catch(SocketException se){}
		}
		
		/*****
		 * Starts a new game
		 */
		private void newGame()
		{
				teamFlip = !teamFlip;
				guessCount = 3;
				shiftCoins();
				updateCoins();
				currentWord = fg.getWord();
				try
				{
					for(ObjectOutputStream op : ojOuts)
					{
						op.writeObject("CLEAR_BOARD");
						op.flush();
						op.writeObject("GUESS_COUNT");
						op.flush();
						op.writeObject(guessCount);
						op.flush();
						op.writeObject("UPDATE_SCORE");
						op.flush();
						op.writeObject(team1Score);
						op.flush();
						op.writeObject(team2Score);
						op.flush();
						op.writeObject("BEGIN");
						op.flush();
						op.writeObject(currentWord);
						op.flush();
						
						
					}
						
				}
				catch(IOException ioe){}
		}
		

		/****
		 * Changes the rotation of main player
		 */
		private void shiftCoins()
		{
			int currentPosition = 0;
			for(int i=0;i<coins.length;i++)
			{
				if(coins[i] == 0)
				{
				
				}
				else if(coins[i] == 1)
				{
					currentPosition = i;
				}
			}
			coins[currentPosition] = 0;
			if(currentPosition == 3)
			{
				coins[0] = 1;
			}
			else
			{
				currentPosition += 1;
				coins[currentPosition] = 1;
			}
			for(ObjectOutputStream oj : ojOuts){
			
				try{
					oj.writeObject(coins[ojOuts.indexOf(oj)]);
					oj.flush();
				}
				catch(IOException ioe){}
			}
			
			
		}
		
		
		/***
		 * Tells all clients who has the coin (Main Player)
		 */
		private void updateCoins()
		{
			for( ObjectOutputStream oj : ojOuts)
			{
				try{
					oj.writeObject(coins[ojOuts.indexOf(oj)]);
					oj.flush();			
					oj.writeObject("UPDATE");
					oj.flush();			
				}
				catch(IOException ioe){}
			}
		}
		
		/***
		 * 
		 * @author Kevin Hill, Sahil Chaudhrey, Daniel Flint
		 * Used to run game Timer
		 */
		class TimerClass implements Runnable
		{
		
			private int current = 0;
			public TimerClass(){
			
			}
			
			/****
			 * Sets the time to parameter 
			 * @param Int time
			 */
			public void setCurrentTime(int time)
			{
				current = time;
			}
			
			/***
			 * Runs class Thread
			 * Send time to each client
			 */
			public void run()
			{
				globalTimer = new javax.swing.Timer(1000, new ActionListener(){public void actionPerformed(ActionEvent ae)
				{
					if(current == 120)
					{
						globalTimer.stop();
						for( ObjectOutputStream oj : ojOuts)
						{
							try{
								oj.writeObject("TIME");
								oj.flush();
								oj.writeObject(current);
								oj.flush();
							}
							catch(IOException ioe){}
						}
						newGame();
						current = 0;
						globalTimer.start();
					}
					else
					{
						if(!restartTime)
						{
							for( ObjectOutputStream oj : ojOuts)
							{
								try{
									oj.writeObject("TIME");
									oj.flush();
									oj.writeObject(current);
									oj.flush();
								}
								catch(IOException ioe){}
							}
							current++;
						}
						else
						{
							current = 0;
							restartTime = false;
						}	
					}
						
				}});
				
				globalTimer.start();
			}
		}//end Timer Class
//----------------------Restart GamePanel Class---------------------------------------------------------------------------------		
		
		/***
		 * 	This is within GamePanel.
		 * 	Runs GamePanel Thread. 
		 *  Receive and send vectors.
		 *  Starts Chat Thread(Send and Receive Messages). 
		 *  Update Players of game statuses
		 *   
		 */
		public void run()
		{
			try{
				
				ojo = new ObjectOutputStream(gameSocket.getOutputStream());
				ojo.flush();	
				oji = new ObjectInputStream(gameSocket.getInputStream());
				ojOuts.add(ojo);
				
				try{
					ojo.writeObject(coin);
					ojo.flush();
					ojo.writeObject("TEAM");
					ojo.flush();
					ojo.writeObject(team);
					ojo.flush();
					ojo.writeObject("GUESS_COUNT");
					ojo.flush();
					ojo.writeObject(guessCount);
					ojo.flush();
				}
				
				catch(IOException ioe){System.out.println("cx died here");}
				
				while(!hasPlayers){
					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException ie){}
					
					if(ojOuts.size()==4 && chatWriters.size()==4)
					{	
						if(coin == 1)
						{
							currentWord = fg.getWord();
							TimerClass tc = new TimerClass();
							Thread timerThread = new Thread(tc);
							timerThread.start();
							ojo.writeObject("BEGIN");
							ojo.flush();
							ojo.writeObject(currentWord);
							ojo.flush();
						}
						hasPlayers = true;	
								
						while(true)
						{
							Object obi;
							if(coin == 1)
							{
								try
								{
									obi = oji.readObject();
									if(obi instanceof String)
									{
										
										if(obi.toString().equals("Clear"))
										{
											for(ObjectOutputStream op : ojOuts)
											{
												op.writeObject("CLEAR_BOARD");
												op.flush();
											}
										}
									}
									if(obi instanceof Integer)
									{
										coin = (int)obi;
									}
									else
									{

										for(ObjectOutputStream op : ojOuts)
										{
											op.writeObject(obi);
											op.flush();
										}
									}
								}
								catch(ClassNotFoundException cnf){System.out.println("Class not found");}
							}
							else
							{
								Object report;
								try
								{
									report = oji.readObject();
									if(report instanceof Integer)
									{
										coin = (int)report;
									}
									else if(report instanceof String)
									{
										if(report.toString().equals("ATTEMPT_GUESS"))
										{
											String guessTeam = oji.readObject().toString();
											if(teamFlip && guessTeam.equals("Team 1"))
											{	
												
												guessCount--;
												String wordGuess = oji.readObject().toString();
												if(wordGuess.equals(currentWord))
												{
													for(PrintWriter pw : chatWriters)
													{
														pw.println("Team 1 guessed "+wordGuess);
														pw.flush();
														pw.println("Guess was correct!");
														pw.flush();
													}
													team1Score+=1;
													if(team1Score == 5)
													{
														for(PrintWriter pw : chatWriters)
														{
															pw.println("Team 1 has 5 points and has won the game!");
															pw.flush();
															pw.println("Game will now reset!");
															pw.flush();
															team1Score = 0; 
															team2Score = 0;
															restartTime = true;
															newGame();
														}
													}
													else
													{
														restartTime = true;
														newGame();
													}
												}
												else
												{
													for(PrintWriter pw : chatWriters)
													{
														pw.println("Team 1 guessed "+wordGuess);
														pw.println("Guess was incorrect.");
														pw.flush();
													}
													
													for(ObjectOutputStream oj : ojOuts)
													{
														oj.writeObject("GUESS_COUNT");
														oj.flush();
														oj.writeObject(guessCount);
														oj.flush();
													}
													if(guessCount == 0)
													{
														newGame();
													}
												}
											
											}
											else if(!teamFlip && guessTeam.equals("Team 2"))
											{
												guessCount--;
												String wordGuess = oji.readObject().toString();
												if(wordGuess.equals(currentWord))
												{
													for(PrintWriter pw : chatWriters)
													{
														pw.println("Team 2 guessed "+wordGuess);
														pw.flush();
														pw.println("Guess was correct!");
														
													}
													team2Score+=1;
													if(team1Score == 5)
													{
														for(PrintWriter pw : chatWriters)
														{
															pw.println("Team 2 has 5 points and has won the game!");
															pw.flush();
															pw.println("Game will now reset!");
															pw.flush();
															team1Score = 0; 
															team2Score = 0;
															restartTime = true;
															newGame();
														}
													}
													else
													{
														restartTime = true;
														newGame();
													}
												}
												else
												{
													for(PrintWriter pw : chatWriters)
													{
														pw.println("Team 2 guessed "+wordGuess);
														pw.flush();
														pw.println("Guess was incorrect.");
														pw.flush();
													}
													for(ObjectOutputStream oj : ojOuts)
													{
														oj.writeObject("GUESS_COUNT");
														oj.flush();
														oj.writeObject(guessCount);
														oj.flush();
													}
													if(guessCount == 0)
													{
														newGame();
													}
												}	
											}
										}
									}		
								}
								catch(IOException ioe){}
								catch(ClassNotFoundException cnf){}
							}
						}
						
					}
				}	
				
			}
			
			catch(IOException ioe){
				System.out.println("IO Exception");
			}
			
		}
	}	
	
	
	
	
	/****
	 * 
	 * @author Kevin Hill, Sahil Chaudhrey, Daniel Flint
	 *	Explains itself. Chat Thread
	 */
	class ClientThreadChat implements Runnable{
	
		private Socket chatSocket;
		private BufferedReader chatIn;
		private PrintWriter chatOut;

	
		private String chatRead, nickName;
		/***
		 * Assign a Chat Socket
		 * @param Socket s
		 */
		public ClientThreadChat(Socket s){
			chatSocket = s;
		}
		public void run(){
			try{
				chatIn = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
				chatOut = new PrintWriter(chatSocket.getOutputStream());	
				
				
						
				chatOut.println("Welcome. Please enter a nickname.");
				chatOut.flush();
				nickName = chatIn.readLine();
				chatOut.println("Hello, "+nickName+".");
				chatOut.flush();
				
				
				for(PrintWriter pw : chatWriters){
						pw.println(nickName+" has joined the game.");
						pw.flush();
						
				}
				chatWriters.add(chatOut);
				if(chatWriters.size()==4)
				{
					startGame();
				}
				
				while(true){
					sendMessage(nickName,chatIn.readLine());
				}
				
			}
			catch(IOException ioe){
			
				int index = chatWriters.indexOf(this.chatOut); 
				if(index> -1){
					chatWriters.removeElementAt(index);
					for(PrintWriter pw : chatWriters){
						pw.println(nickName+" has left the game.");
						pw.flush();
					}
				}				
			}
				
			}	
		}
	
	
	/***
	 * Sends the message to all clients
	 * @param String nick
	 * @param String message
	 */
	private void sendMessage(String nick, String message){
		for(PrintWriter pw : chatWriters){
						pw.println(nick+": "+message);
						pw.flush();
					}
		
	}
	
	/**
	 * Start Game
	 */
	public void startGame(){
		for(PrintWriter pw : chatWriters)
		{
			pw.println("All players have joined, the game will now begin.");
			pw.flush();
		}
		
	}
	
	/***
	 * Main Method
	 * @param args
	 */
	public static void main(String []args){
		new PictionaryServer();
	}
}


