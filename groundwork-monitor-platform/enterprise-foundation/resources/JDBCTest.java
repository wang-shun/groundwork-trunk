import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple class to test the JDBC connection
 * Usage : java -cp postgresql-9.1-901.jdbc3.jar JDBCTest <connectionURL> <user> <password>
 * @author arul
 *
 */
public class JDBCTest {
	
	/**
	 * Constructor
	 */
	public JDBCTest() {
		
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		
		if (args.length !=3) {
			System.err.println("Usage : java -cp postgresql-9.1-901.jdbc3.jar JDBCTest <connectionURL> <user> <password>");
			System.err.println("Ex: java -cp postgresql-9.1-901.jdbc3.jar JDBCTest jdbc:postgresql://127.0.0.1:5432/postgres postgres postgres");
			return;
		}
		String connectionURL = args[0];
		String user = args[1];
		String password = args[2];
		try {
			JDBCTest tester = new JDBCTest();
			tester.try2Connect(connectionURL, user, password);
		}
		catch (ClassNotFoundException cnfe) {
			System.err.println("FAILED! Driver class not found! Is postgresql driver in classpath?");
			System.exit(1);
			
		}
		catch (SQLException se) {
			System.err.println("FAILED! Cannot connect to local/remote postgres @ " + connectionURL + " using " + user + " with password " + password);
			System.exit(2);
		}
	}
	
	/**
	 * Simple method to try the jdbc connection
	 * @param connectionURL
	 * @param user
	 * @param password
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void try2Connect(String connectionURL, String user,
			String password) throws ClassNotFoundException, SQLException{
		Class.forName("org.postgresql.Driver");
		Connection connection = DriverManager.getConnection(
				connectionURL, user,
				password);
		System.out.println("Connection Successful!");
	}
	

}
