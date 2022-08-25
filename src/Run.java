import javax.swing.JFrame;

public class Run {
	public static void main(String[] args) {
	    SpeedServer ss = new SpeedServer();
	    ss.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    ss.setVisible(true);
	    
	    SpeedClient sc = new SpeedClient();
		sc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sc.setVisible(true);
	 }
}
