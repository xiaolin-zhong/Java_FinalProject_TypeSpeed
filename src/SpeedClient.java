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
	TimerClass tc;
	int counter = 60;
	PreparedStatement insertStatement;
	Connection con;
	ResultSet rs;
	int corrWordCount = 0;
	TextFieldListener tfl;
	int max;
		
	private static final int FRAME_WIDTH = 400;
	private static final int FRAME_HEIGHT = 380;
	
	public SpeedClient() {

		//Panel
		createPanel();
		
	}

	private void createPanel() {
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
	}	
	
	private JMenuItem createLeaderboard(final String string) {
		// TODO Auto-generated method stub
		JMenuItem item = new JMenuItem(string);
		
		
		class MenuItemListener implements ActionListener {
			JTextArea idTA, scTA;
			ResultSet rs;
			Connection con;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				openLeaderboard();
			}

			private void openLeaderboard() {
				// TODO Auto-generated method stub
				JFrame leaderboard = new JFrame();
				
				JPanel idPanel = new JPanel();
				idTA = new JTextArea(20,3);
				idTA.setEditable(false);
				//idTA.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				idPanel.add(idTA);
				idPanel.setBorder(new TitledBorder(new EtchedBorder(), "ID"));
				leaderboard.add(idPanel, BorderLayout.WEST);
				
				
				JPanel scPanel = new JPanel();
				scTA = new JTextArea(20, 3);
				scTA.setEditable(false);
				scPanel.add(scTA);
				scPanel.setBorder(new TitledBorder(new EtchedBorder(), "Score"));
				leaderboard.add(scPanel, BorderLayout.EAST);
				
				
				populateLeaderboard();
				
				leaderboard.setSize(120,300);
				leaderboard.setTitle("Leaderboard");
				leaderboard.setVisible(true);
			}

			private void populateLeaderboard() {
				// TODO Auto-generated method stub
				try {
					con = DriverManager.getConnection("jdbc:sqlite:leaderboard.db");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				String query = "SELECT * FROM Leaderboard ORDER BY 2 DESC";
				
				try {
					PreparedStatement ps = con.prepareStatement(query);
					rs = ps.executeQuery();
					
					while (rs.next()) {
						idTA.append(String.valueOf(rs.getInt("id")) + "\n");
						scTA.append(String.valueOf(rs.getInt("score")) + "\n");
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
		ActionListener listener = new MenuItemListener();
		item.addActionListener(listener);
		return item;
	}
	
	public void saveScore() {
		tf.removeActionListener(tfl);
		
		try {
			con = DriverManager.getConnection("jdbc:sqlite:leaderboard.db");
		} catch (SQLException ex1) {
			ex1.printStackTrace();
		}
		
		String insertQuery = "INSERT INTO Leaderboard (id, score) VALUES (?,?)";
		String idQuery = "SELECT max(id) FROM Leaderboard";
		
		try {
			PreparedStatement ps = con.prepareStatement(idQuery);
			rs = ps.executeQuery();
			if (rs.next()) {
				max = rs.getInt(1);
			}
			max++;
			insertStatement = con.prepareStatement(insertQuery);
			insertStatement.setInt(1, max);
			insertStatement.setInt(2, corrWordCount);
			insertStatement.executeUpdate();
		} catch (SQLException ex2) {
			ex2.printStackTrace();
		}
	}
	
	class QuitListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			tc.counter = 0;
		}
		
	}
	
	class StartListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			inPlay = true;
			tf.addActionListener(tfl = new TextFieldListener());
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
				
				tc = new TimerClass(counter);
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
				saveScore();
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
					ta.append("correct: ");
					corrWordCount++;
				}
				else {
					ta.append("incorrect: ");
				}
				ta.append(input + "\n");
				
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
	*/
}
