import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

public class SpeedClient extends JFrame implements ActionListener {
	
	//IO Stream
	DataOutputStream toServer = null;
	DataInputStream fromServer = null;
	Socket socket = null;
	
	//Panel stuff
	JButton connectButton;
	JButton startButton;
	JButton quitButton;
	JTextArea ta = null;
	JTextField tf = null;
	
	boolean inPlay = false;
	boolean correct = false;
	Timer timer;
	int counter = 10;
		
	private static final int FRAME_WIDTH = 400;
	private static final int FRAME_HEIGHT = 380;
	
	public SpeedClient() {

		//Panel
		createControlPanel();
		
	}

	private void createControlPanel() {
		// TODO Auto-generated method stub
		
		// Top Menu Bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu menu = new JMenu("Leaderboard");
		menuBar.add(menu);
		menu.add(createLeaderboard("Leaderboard"));
		
		//Buttons
		connectButton = new JButton("Connect");
		startButton = new JButton("Start");
		quitButton = new JButton ("Quit");
		
		//Button Option Panel, add all the buttons
		JPanel actionPanel = new JPanel();
		actionPanel.add(connectButton);
		actionPanel.add(startButton);
		actionPanel.add(quitButton);
		actionPanel.setBorder(new TitledBorder(new EtchedBorder(), "Options"));
		this.add(actionPanel,BorderLayout.NORTH);
		
		//User Input Panel
		JPanel tfPanel = new JPanel();
		tf = new JTextField(30);
		tfPanel.add(tf);
		tfPanel.setBorder(new TitledBorder(new EtchedBorder(), "Type Here"));
		this.add(tfPanel, BorderLayout.CENTER);
		
		//Activity Received from Server Panel
		JPanel textPanel = new JPanel();
		ta = new JTextArea(10,30);
		textPanel.add(ta);
		textPanel.setBorder(new TitledBorder(new EtchedBorder(), "Words Sent to Server"));
		ta.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(ta);
		textPanel.add(scrollPane);
		this.add(textPanel, BorderLayout.SOUTH);
		
		this.setTitle("Client");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		
		//Add action
		connectButton.addActionListener(new OpenConnectionListener());
		startButton.addActionListener(new StartListener());
		//quitButton.addActionListener(new QuitListener());
		//tf.addActionListener(new TextFieldListener());	
	}	
	
	private JMenuItem createLeaderboard(final String string) {
		// TODO Auto-generated method stub
		JMenuItem item = new JMenuItem(string);
		JLabel lblid, lblsc;
		JTextField tfid, tfsc;
		
		class MenuItemListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				openLeaderboard();
			}

			private void openLeaderboard() {
				// TODO Auto-generated method stub
				JFrame leaderboard = new JFrame();
				leaderboard.setSize(200, 200);
				leaderboard.setTitle("Leaderboard");
				
				JPanel lbpanel = new JPanel();

				
				displayLeaderBoard();
				
				leaderboard.setVisible(true);
			}

			private void displayLeaderBoard() {
				// TODO Auto-generated method stub
				ResultSet rs = null;
				try {
					if (rs == null) {
						Class.forName("org.sqlite.JDBC");
						Connection con = DriverManager.getConnection("jdbc:sqlite:leaderboard.db");
						String sql = "SELECT * FROM Leaderboard";
						PreparedStatement statement = con.prepareStatement(sql);
						rs = statement.executeQuery();
						
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
		return item;
	}

	public void setInPlay(boolean inPlay) {
		this.inPlay = inPlay;
	}
	
	class QuitListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			try {
				inPlay = false;
				toServer.writeBoolean(inPlay);
				toServer.flush();
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}
		
	}
	
	class StartListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			inPlay = true;
			tf.addActionListener(new TextFieldListener());
			quitButton.addActionListener(new QuitListener());
			try {
				fromServer = new DataInputStream(socket.getInputStream());
				toServer = new DataOutputStream(socket.getOutputStream());

			} catch (IOException ex) {
				ta.append(ex.toString()+"\n");
			}
			try {					
				toServer.writeBoolean(inPlay);
				toServer.flush();
				
				TimerClass tc = new TimerClass(counter);
				timer = new Timer(1000, tc);
				timer.start();
			} catch (IOException ex) {
				System.err.println(ex);
			}			
		}	
	}
	
	//Keep a timer, update timer every 1 second
	public class TimerClass implements ActionListener {
		int counter;
		
		public TimerClass(int counter) {
			this.counter = counter;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			counter--;
			
			if (counter > 0) {
				inPlay = true;
			}
			else {
				timer.stop();
				inPlay = false;
				tf.setEditable(false);
			}
		}
	}
	
	//Connect to the server
	class OpenConnectionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			try {
				socket = new Socket("localhost", 8000);
				ta.append("Connecting from: " + socket.getLocalAddress() + "\n");
				ta.append("Connecting to " + socket.getInetAddress().getHostName() + "\n");
				
			} catch (IOException ex) {
				ex.printStackTrace();
				ta.append("Connection Failure\n");
			}
			
		}
	}
	
	//https://gist.github.com/chatton/8955d2f96f58f6082bde14e7c33f69a6	
	//When enter is pressed, the input is sent to the server and info is received from the server.
	class TextFieldListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			try {
				fromServer = new DataInputStream(socket.getInputStream());
				toServer = new DataOutputStream(socket.getOutputStream());

			} catch (IOException ex) {
				ta.append(ex.toString()+"\n");
			}
			try {
				quitButton.addActionListener(new QuitListener());
				String input = tf.getText().trim();
				
				toServer.writeUTF(input);
				toServer.flush();
				
				correct = fromServer.readBoolean();
				
				if(correct) {
					tf.setText(null);
				}
				
				inPlay = fromServer.readBoolean();
				
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	//CaretListener works but does not allow JTextField to be edited, opted to use ActionListener instead.
	class TypingListener implements CaretListener {

		@Override
		public void caretUpdate(CaretEvent e) {
			// TODO Auto-generated method stub
			try {
				fromServer = new DataInputStream(socket.getInputStream());
				toServer = new DataOutputStream(socket.getOutputStream());

			} catch (IOException ex) {
				ta.append(ex.toString()+"\n");
			}
			try { 
				String input = tf.getText().trim();
				
				toServer.writeUTF(input);
				toServer.flush();
				
				correct = fromServer.readBoolean();
				if (correct) {
				}
				
			} catch (IOException ex) {
				System.err.println(ex);
			}
		}
		
	}

	public static void main(String[] args) {
		JFrame frame = new SpeedClient();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	*/
}
