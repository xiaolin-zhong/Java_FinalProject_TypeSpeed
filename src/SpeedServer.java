import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import javax.swing.Timer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class SpeedServer extends JFrame implements Runnable{
	
	//Server
	DataOutputStream toServer = null;
	DataInputStream fromServer = null;
	Socket socket = null;
	
	//Panel
	private JTextArea wa;
	private JTextArea ta;
	private JLabel timeLeft;
	private static final int FRAME_WIDTH = 400;
	private static final int FRAME_HEIGHT = 380;
	
	//Speed test
	private int clientNo = 0;
	int correctWordCount = 0;
	int counter = 10;
	ArrayList<String> wordList;
	String currWord;
	Timer timer;
	boolean inPlay;
	
	public SpeedServer() {
		
		//Set up GUI & read in the file containing words
		createPanels();
		try {
			readFile();
		} catch (FileNotFoundException fnfe) {
			System.err.println("File not found " + fnfe.getMessage());
		}

		Thread t = new Thread(this);
		t.start();
	}
	
	private void createPanels() {
		// TODO Auto-generated method stub	
		//Timer Panel
		JPanel timePanel = new JPanel();
		timeLeft = new JLabel();
		timeLeft.setText("Time Left: " + counter);
		timePanel.add(timeLeft);
		timePanel.setBorder(new TitledBorder(new EtchedBorder(), "Timer"));
		this.add(timePanel, BorderLayout.NORTH);
		
		//Word Display Panel
		JPanel wPanel = new JPanel();
		wa = new JTextArea(1,30);
		wa.setEditable(false);
		wPanel.add(wa);
		wPanel.setBorder(new TitledBorder(new EtchedBorder(), "Word"));
		this.add(wPanel, BorderLayout.CENTER);
		
		//Activity Across Network Panel
		JPanel taPanel = new JPanel();
		ta = new JTextArea(10,30);
		ta.setEditable(false);
		taPanel.add(ta);
		taPanel.setBorder(new TitledBorder(new EtchedBorder(), "Words Received From Client"));
		JScrollPane scrollPane = new JScrollPane(ta);
		taPanel.add(scrollPane);
		this.add(taPanel, BorderLayout.SOUTH);
		
		this.setSize(400,400);
		this.setTitle("Server");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
	}
	
	//read txt, populate ArrayList with all the words in txt
	public void readFile() throws FileNotFoundException {
		// TODO Auto-generated method stub
		File file = new File("words.txt");
		Scanner sc = new Scanner(file);
		wordList = new ArrayList<String>();
		while (sc.hasNextLine()) {
			wordList.add(sc.nextLine());
		}
	}
	
	//Get the next word randomly from the ArrayList
	private void nextWord() {
		Random rand = new Random();
		int i = rand.nextInt(wordList.size());
		
		wa.setText(null);
		currWord = wordList.get(i);
		wa.append(currWord);
	}
	
	@Override
	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(8000);
			ta.append("Speed test server opened at \n" + new Date() + ".\n");
			
			while (true) {
				ta.append("Press start to begin.\n");
				socket = serverSocket.accept();
				ta.append("Start!\n");

				try {
					// get the input stream from connected socket & create a DataInputStream to read data
					InputStream inputStream = socket.getInputStream();
					DataInputStream dataInputStream = new DataInputStream(inputStream);
					DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
					
					inPlay = dataInputStream.readBoolean();
					
					while (inPlay) {
						//Start timer
						TimerClass tc = new TimerClass(counter);
						timer = new Timer(1000, tc); //every 1 second, fires
						timer.start();
						nextWord();
						
						while (true) {
							//read string from socket
							String input = dataInputStream.readUTF();
							
							System.out.println("inplay: " + inPlay);
							System.out.println("input: " + input);
							
							//TO-DO compare wa boolean
							boolean correct = false;
							if (currWord.equals(input)) {
								correct = true;
								nextWord();
								correctWordCount++;
							}
							System.out.println("correct: " + correct);
							outputToClient.writeBoolean(correct);
							outputToClient.writeBoolean(inPlay);
						}
					}
					inPlay = dataInputStream.readBoolean();
					if (!inPlay) {
						timer.stop();
						timeLeft.setText("Times Up!");
						wa.setText("Words per Minute: " + correctWordCount);
						correctWordCount = 0;
					}
					
				} catch (IOException ex) {
					ex.printStackTrace();
					ta.append("Connection lost from player " + this.clientNo + ".\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				timeLeft.setText("Time Left: " + counter);
			}
			else {
				timeLeft.setText("Times Up!");
				timer.stop();
				inPlay = false;
				wa.setText("Words per Minute: " + correctWordCount);
				correctWordCount = 0;
			}
		}
		
		
	}
	
	//Server to handle multiple clients; won't be used in this implementation, but works
	/*
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			ServerSocket serverSocket = new ServerSocket(8000);
			ta.append("Speed test server opened at \n" + new Date() + ".\n");
			
			while (true) {
				ta.append("Waiting for player to press start.\n");
				Socket socket = serverSocket.accept();
				
				clientNo++;
				
				ta.append("Player pressed start. \n");
				TimerClass tc = new TimerClass(counter);
				timer = new Timer(1000, tc); //every 1 second, fires
				timer.start();
				start = true;
				
				new Thread(new HandleAClient(socket, clientNo)).start();
			}
		} catch (IOException ex) {
			System.err.println();
		}
	}
	
	class HandleAClient implements Runnable {
		private Socket socket;
		private int clientNo;
		
		public HandleAClient(Socket socket, int clientNo) {
			this.socket = socket;
			this.clientNo = clientNo;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				// get the input stream from connected socket & create a DataInputStream to read data
				InputStream inputStream = socket.getInputStream();
				DataInputStream dataInputStream = new DataInputStream(inputStream);
				
				DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());
				nextWord();
				
				while (true) {
					//read string from socket
					String input = dataInputStream.readUTF();
					
					//TO-DO compare wa boolean
					boolean correct = false;
					if (currWord.equals(input) & start) {
						correct = true;
						nextWord();
						correctWordCount++;
					}
					
					outputToClient.writeBoolean(correct);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
				ta.append("Connection lost from player " + this.clientNo + ".\n");
			}
		}		
	} 
	
	public static void main(String[] args) {
	    SpeedServer mts = new SpeedServer();
	    mts.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    mts.setVisible(true);
	 }
	*/
}
