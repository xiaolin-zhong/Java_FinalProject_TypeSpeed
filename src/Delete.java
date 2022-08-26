import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Delete {
	Connection con;
	ResultSet rs;
	PreparedStatement deleteStatement;

	public Delete(int id) {
		// TODO Auto-generated constructor stub
		try {
			con = DriverManager.getConnection("jdbc:sqlite:leaderboard.db");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String deleteQuery = "DELETE FROM Leaderboard WHERE id = ?";
		
		try {

			deleteStatement = con.prepareStatement(deleteQuery);
			deleteStatement.setInt(1, id);
			deleteStatement.executeUpdate();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
    public static void main(String[] args) {
        Delete d = new Delete(6);
        // delete the row with id 3
    }
}
