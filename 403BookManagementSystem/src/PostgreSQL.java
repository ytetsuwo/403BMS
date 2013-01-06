import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class PostgreSQL implements IDatabase {

	Scanner			sc		= null;

	Connection		db		= null;
	Statement		st		= null;
	ResultSet		result	= null;

	String			url		= "jdbc:postgresql:postgres";
	String			sql		= null;
	AmazonSearch	amzn	= null;

	/*
	 * CREATE TABLE bookshelf( id SERIAL, title varchar(1000), author
	 * varchar(1000), isbn10 varchar(20), isbn13 varchar(20), picturl
	 * varchar(1000), detailurl varchar(1000), publisher varchar(1000),
	 * publicationdate varchar(50), status boolean, year varchar(10));
	 */

	public PostgreSQL() {

		try {
			db = DriverManager.getConnection(url, "postgres", "");
			st = db.createStatement();
			init();
		} catch (SQLException e) {
			System.out
					.println("データベースへの接続が失敗しました。PostgreSQLが起動しているかどうか確認してください。");
			System.out
					.println("psql -U postgres または、psql -h localhost -U postgresが実行できるか確認してください。");
		}
	}

	private void init() throws SQLException {

		amzn = new AmazonSearch();
		sql = "SELECT * FROM bookshelf;";
		try {
			result = st.executeQuery(sql);
		} catch (SQLException e) {
			sql = "CREATE TABLE bookshelf(id SERIAL,title varchar(1000),author varchar(1000),isbn10 varchar(20),isbn13 varchar(20),picturl varchar(1000),detailurl varchar(1000),publisher varchar(1000),publicationdate varchar(50),status boolean,year varchar(10));";
			st.execute(sql);
			sql = "CREATE TABLE usertable(username varchar(20));";
			st.execute(sql);
			sql = "CREATE TABLE userbooks(username varchar(20),isbn13 varchar(15));";
			st.execute(sql);
		}
	}

	@Override
	public boolean addBook(String ISBN) {

		Book b = amzn.getBookInfoISBN(ISBN);
		String author = b.getAuthor();
		String ISBN10 = b.getISBN10();
		String ISBN13 = b.getISBN13();
		String pictURL = b.getPictURL();
		String detailURL = b.getDetailURL();
		String publisher = b.getPublisher();
		String publicationDate = b.getPublicationDate();
		boolean status = b.getStatus();
		String title = b.getTitle();
		String year = b.getYear();

		if (title == null) {
			return false;
		}

		try {
			sql = "INSERT INTO bookshelf (title , author , isbn10 ,  isbn13 ,  picturl ,  detailurl ,  publisher ,  publicationdate ,  status ,  year ) VALUES('"
					+ title
					+ "','"
					+ author
					+ "','"
					+ ISBN10
					+ "','"
					+ ISBN13
					+ "','"
					+ pictURL
					+ "','"
					+ detailURL
					+ "','"
					+ publisher
					+ "','"
					+ publicationDate
					+ "',"
					+ status
					+ ",'"
					+ year
					+ "');";
			System.out.println(sql);
			st.execute(sql);
			System.out.println(b.getTitle() + "が追加されました");
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean rmBook(String ISBN) {

		try {
			result = st.executeQuery("SELECT * FROM bookshelf WHERE ISBN13 ='"
					+ ISBN + "';");
			result.next();
			String resultstr = result.getString(result.findColumn("isbn13"));
			int index = resultstr.indexOf(ISBN);
			if (index != -1) {
				st.execute("DELETE FROM bookshelf WHERE id =(SELECT min(id) FROM bookshelf WHERE isbn13 ='"
						+ ISBN + "' AND status = true);");
			} else {
				return false;
			}
			return true;
		} catch (SQLException e) {
			// e.printStackTrace();
			System.err
					.println("\nエラーが発生し、削除できませんでした。\n検索をして本がデータベースに登録してあるか確認してください。");
			return false;
		}
	}

	@Override
	public boolean rmBookList(String[] books) {

		return false;
	}

	@Override
	public boolean bBook(String ISBN) {

		// 借りられていない同じ本を探すSQL
		// SELECT * FROM bookshelf WHERE id =(SELECT min(id) FROM bookshelf
		// WHERE status = true AND isbn13 = 'ISBN');

		try {
			sql = "UPDATE bookshelf SET status=false WHERE id =(SELECT min(id) FROM bookshelf WHERE status = true AND isbn13 = '";
			sql += ISBN;
			sql += "');";
			if (st.executeUpdate(sql) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean rBook(String ISBN) {

		try {
			sql = "UPDATE bookshelf SET status=true WHERE id =(SELECT min(id) FROM bookshelf WHERE status = false AND isbn13 = '";
			sql += ISBN;
			sql += "');";
			if (st.executeUpdate(sql) == 1) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String[] searchDB(String key) {

		Book b;
		int bookcount = 0;
		try {
			sql = "SELECT * FROM bookshelf WHERE isbn13 ='";
			sql += key;
			sql += "';";
			result = st.executeQuery(sql);
			while (result.next()) {
				bookcount++;
			}
			if (bookcount != 0) {
				System.out.println("データベース上に " + bookcount + "件 の登録あり");
				bookcount = 0;
				sql = "SELECT * FROM bookshelf WHERE isbn13 ='";
				sql += key;
				sql += "' AND status = true";
				sql += ";";
				result = st.executeQuery(sql);
				while (result.next()) {
					bookcount++;
				}
				if (bookcount != 0) {
					System.out.println(bookcount + "冊 貸し出し可能");
				} else {
					System.out.println("***すべて貸出中です***");
				}
			} else {
				System.out.println("この本はデータベースに登録されていません");
			}

			b = amzn.getBookInfoISBN(key);
			System.out.println();
			System.out.println("タイトル\t: " + b.getTitle());
			System.out.println("著者\t: " + b.getAuthor());
			System.out.println("詳細URL\t: " + b.getDetailURL());
			System.out.println("出版日時\t: " + b.getPublicationDate());
			System.out.println("出版社\t: " + b.getPublisher());
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String listDB() {

		int bookcount = 0;
		String title;
		String jaStatus;
		sql = "SELECT * FROM bookshelf;";
		try {
			result = st.executeQuery(sql);
			while (result.next()) {
				if (result.getString(result.findColumn("status")).startsWith(
						"t")) {
					jaStatus = "貸出可";
				} else {
					jaStatus = "貸出中";
				}
				title = result.getString(result.findColumn("title"));
				System.out.println(title + "\t\t[" + jaStatus + "]");
				bookcount++;
			}
			System.out.println("結果:" + bookcount + "冊");
			return result.toString();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String listStatus(int mode) {

		String jaStatus;
		sql = "SELECT * FROM bookshelf WHERE status = false;";
		try {
			result = st.executeQuery(sql);
			while (result.next()) {
				if (result.getString(result.findColumn("status")).startsWith(
						"t")) {
					jaStatus = "貸出可";
				} else {
					jaStatus = "貸出中";
				}
				System.out.println(result.getString(result.findColumn("title"))
						+ "[" + result.getString(result.findColumn("isbn13"))
						+ "] : " + jaStatus);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addUser(String user) {

		int count = 0;
		sql = "SELECT * FROM usertable where username=";
		sql += "'" + user + "';";
		try {
			result = st.executeQuery(sql);
			while (result.next()) {
				count++;
			}
			if (count == 0) {
				sql = "INSERT INTO usertable VALUES('";
				sql += user;
				sql += "');";
				st.execute(sql);
				System.out.println("ユーザーを追加しました: " + user);
			} else {
				System.out.println("ユーザーがすでに存在します");
			}
		} catch (SQLException e) {

		}
	}

	public void rmUser(String user) {

		int count = 0;
		try {
			sql = "SELECT * FROM usertable where username=";
			sql += "'" + user + "';";
			result = st.executeQuery(sql);
			while (result.next()) {
				count++;
			}
			if(count !=0){
			sql = "DELETE FROM usertable ";
			sql += "WHERE username = ";
			sql += "'" + user + "';";
			st.execute(sql);
			System.out.println("ユーザーを削除しました: " + user);
			}else{
				System.out.println("ユーザーが存在しません");
			}
		} catch (SQLException e) {

		}
	}
}
