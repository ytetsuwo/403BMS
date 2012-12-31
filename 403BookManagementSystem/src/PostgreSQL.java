import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQL implements IDatabase {

	Connection	db		= null;
	Statement	st		= null;
	ResultSet	result	= null;

	String		url		= "jdbc:postgresql:postgres";

	public PostgreSQL() {

		try {
			db = DriverManager.getConnection(url, "postgres", "");
			st = db.createStatement();
		} catch (final SQLException e) {

		}
	}

	@Override
	public boolean addBook(String ISBN) {

		final AmazonSearch amzn = new AmazonSearch();
		final Book b = amzn.getBookInfoISBN(ISBN);
		final String title = b.getTitle();
		final String author = b.getAuthor();
		final String ISBN10 = b.getISBN10();
		final String ISBN13 = b.getISBN13();
		final String pub = b.getPublisher();
		final String sql = "INSERT INTO bookshelf values(" + title + ","
				+ author + "," + ISBN10 + "," + ISBN13 + "," + pub + ");";
		try {
			st.execute(sql);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean rmBook(String ISBN) {

		try {
			st.executeQuery("SELECT * FROM bookshelf where ISBN13 =" + ISBN
					+ ";");
			st.execute("DELETE FROM bookshelf where ISBN13=" + ISBN + ";");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean rmBookList(String[] books) {

		return false;
	}

	@Override
	public boolean bBook(String ISBN) {

		return false;
	}

	@Override
	public boolean rBook(String ISBN) {

		return false;
	}

	@Override
	public String[] searchDB(String key) {

		return null;
	}

	@Override
	public String[] listDB() {

		return null;
	}

	@Override
	public String[] listStatus(int mode) {

		return null;
	}

}
