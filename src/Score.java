import java.io.Serializable;

public class Score implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8246516796017973654L;
	private int id;
	private int score;
	
	public Score (int id, int score) {
		this.id = id;
		this.score = score;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	public String toString () {
		return "ID:" + this.id + "\t Score: " + this.score;
	}
}
