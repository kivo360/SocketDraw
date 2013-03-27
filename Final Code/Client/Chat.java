import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.awt.*;

public class Chat extends JPanel implements Runnable, ActionListener{

	private JButton send;
	private JTextArea chatArea;
	private JScrollPane sp;
	private JTextField sendField;
	private Socket socket;
	private PrintWriter pw;
	private BufferedReader br;
	private boolean hasNickName = false;
	private String nickName = null;
	private String outText;
	private String serverIP = null;
	
	private JPanel top, bottom;
	
	/***
	 * Starts ClientChat
	 * @param String s
	 */
	public Chat(String s){
		serverIP = s;
		top = new JPanel();
		bottom = new JPanel();
		
		send = new JButton("Send:");
		chatArea = new JTextArea(7,45);
		chatArea.setLineWrap(true);
		chatArea.setEditable(false);
		chatArea.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){sendField.requestFocus();}});
		
		
		sp = new JScrollPane(chatArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sendField = new JTextField(25);
		
		
		top.add(sp);
		bottom.add(send);
		bottom.add(sendField);
		send.addActionListener(this);
		sendField.addActionListener(this);
		
		setLayout(new BorderLayout());
		add(top,BorderLayout.NORTH);
		add(bottom,BorderLayout.SOUTH);
	}
	
	/****
	 * gets action input
	 * @param ActionEvent ae
	 */
	public void actionPerformed(ActionEvent ae){
		
		sendField.requestFocus();
		outText = sendField.getText();
		if(!hasNickName && !outText.equals(null) && !outText.equals("")){
			hasNickName = true;
			nickName = outText;
			pw.println(outText);
			pw.flush();
			
			sendField.setText("");
			chatArea.setText("");
			try{
				chatArea.append(br.readLine());
				scrollToBottom();	
			}
			catch(IOException ioe){}
			Thread chatInc = new Thread(new ChatIncoming());
			chatInc.start();
		}
		else if(!outText.equals(null) && !outText.equals("")){
			pw.println(outText);
			pw.flush();
			sendField.setText("");
		}
	}
		
	/***
	 * Receives and adds message chat windows 
	 */
	public void run(){
		try{
		
			socket = new Socket(serverIP, 16789);
			try{
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				pw = new PrintWriter(socket.getOutputStream());
			
			try{
				chatArea.append(br.readLine());
			}
			catch(IOException ioe){}
		}
		catch(IOException ioe){System.out.print("IOE on creating reader/writer");}
			
		}
		catch(UnknownHostException uhe){System.out.print("UHE on chat socket");}
		catch(IOException ioe){chatArea.append("Please close this window and start the server.");
		}
	}
	
		private void scrollToBottom() {
		    javax.swing.SwingUtilities.invokeLater(new Runnable() {
		       public void run() {
		           try {
		               int endPosition = chatArea.getDocument().getLength();
		               Rectangle bottom = chatArea.modelToView(endPosition);
		               chatArea.scrollRectToVisible(bottom);
		           }
		           catch (Exception e) {
		               System.err.println("Could not scroll to " + e);
		           }
		       }
		    });
		}

		
	class ChatIncoming implements Runnable{
		String message = "";
		public void run(){
			try{
				while(message != null){
					message = br.readLine();
					chatArea.append("\n"+message);
					scrollToBottom();	
				}
			}
			catch(IOException ioe){chatArea.append("\n"+"The server has stopped unexpectedly.\nPlease restart server and client.");}
			scrollToBottom();	
				
			}
		}
	
	}

