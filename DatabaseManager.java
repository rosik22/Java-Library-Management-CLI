import java.sql.*;
import java.time.LocalDate;

public class DatabaseManager {
    // establish connection with the database
    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:library.db");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    // create new tables if they don't exist
    public void createNewTable() {
        // query for creating the Users table
        String sql = "CREATE TABLE IF NOT EXISTS users (\n" + "	username text NOT NULL,\n"
                + "	password text NOT NULL\n" + ");";

        // query for creating the Books table
        String sql2 = "CREATE TABLE IF NOT EXISTS books (\n" + " title text NOT NULL,\n" + " author text NOT NULL,\n"
                + " issue_date text NOT NULL,\n" + " available text NOT NULL,\n" + " PRIMARY KEY (title,author)\n"
                + ");";

        // query for creating the Loaned books table
        String sql3 = " CREATE TABLE IF NOT EXISTS loaned (\n" + " person text NOT NULL,\n"
                + " return_date text NOT NULL,\n" + " authorfk text NOT NULL,\n" + " titlefk text NOT NULL,\n"
                + " PRIMARY KEY (authorfk,titlefk)" + ");";

        try (Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
            stmt.execute(sql2);
            stmt.execute(sql3);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // insert user details in the User table
    public void insertUser(String username, String password) {
        String sql = "INSERT OR IGNORE INTO users(username,password) VALUES(?,?)";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // insert book details in the Books table
    public void insertBook(String title, String author, String issue_date) {
        String sql = "INSERT OR IGNORE INTO books(title,author,issue_date, available) VALUES(?,?,?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, issue_date);
            pstmt.setString(4, "available");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // loan a book to a person
    public void loanBook(String... strings) {
        // query for inserting the details in the Loaned table
        String sql = "INSERT OR IGNORE INTO loaned(person,return_date,authorfk,titlefk) VALUES(?,?,?,?)";
        // updating the availability statement in the Books table
        String sql2 = "UPDATE books SET available='not available' WHERE author=? AND title=?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql2)) {
            pstmt.setString(1, strings[2]);
            pstmt.setString(2, strings[3]);
            // checks if the book exists in the Books table
            // and changes the availability
            if (pstmt.executeUpdate() == 0) {
                System.out.println("There is no such book in the library.\n");
            // if the book exists, it's added to the Loaned table
            } else {
                LocalDate today = LocalDate.now();
                try (PreparedStatement pstmt1 = conn.prepareStatement(sql)) {
                    // if the date string is empty a default value 
                    // for the returning date is set
                    if (strings[1].isEmpty()) {
                        strings[1] = today.plusDays(30).toString();
                    }
                    // checks the date
                    else
                        strings[1] = checkDate(strings[1], today);

                    for (int i = 0; i < strings.length; i++) {
                        pstmt1.setString(i + 1, strings[i]);
                    }
                    pstmt1.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // checks whether the suggested date is valid
    public String checkDate(String date, LocalDate today) {
        LocalDate givenDate = LocalDate.parse(date);
        // if the suggested date is before the current date, a default value will be set
        if (givenDate.isBefore(today)) {
            System.out.println(
                    "Date is invalid. A default date will be assigned -> " + today.plusDays(30).toString() + "\n");
            return today.plusDays(30).toString();
        }
        return date;
    }

    // take back a book after it was loaned
    public void receiveBook(String author, String title) {
        // query for deleting the book from the Loaned table
        String sql = "DELETE FROM loaned WHERE authorfk = ? AND titlefk = ?";
        // query to update the availability of the book in the Books table
        String sql2 = "UPDATE books SET available='available' WHERE author=? AND title=?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, author);
            pstmt.setString(2, title);
            // checks if the book exists in the Books table
            // and changes the availability
            if (pstmt.executeUpdate() == 0) {
                System.out.println("No such book is loaned.\n");
            // if the book exists, it's deleted from the Loaned table
            } else {
                try (PreparedStatement pstmt1 = conn.prepareStatement(sql2)) {
                    pstmt1.setString(1, author);
                    pstmt1.setString(2, title);
                    pstmt1.executeUpdate();
                    System.out.println("The book " + title + " by " + author + " is returned in the library.\n");
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // delete a book from the Books table
    public void removeBook(String author, String title) {
        String sql = "DELETE FROM books WHERE author = ? AND title = ?";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, author);
            pstmt.setString(2, title);
            // checks wheter the book exists
            // and deletes it from the Books table
            if (pstmt.executeUpdate() == 0) {
                System.out.println("There is no such book or author.\n");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // checks whether the book is available or not
    public void isBookAvailable(String author, String title) {
        String query = "SELECT * FROM books WHERE author=? AND title=?";
        try (Connection conn = this.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, author);
            stmt.setString(2, title);
            ResultSet res = stmt.executeQuery();
            // checks only the first result
            if (res.next()) {
                if (res.getString("available").equals("available")) {
                    System.out.println(title + " by " + author + " is available!\n");
                } else {
                    System.out.println(title + " by " + author + " is not available!\n");
                }
            } else {
                System.out.println("There is no such book present in the library.\n");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // checks who is loaning a certain book
    public void whoIsLoaning(String author, String title) {
        String query = "SELECT * FROM loaned WHERE authorfk=? AND titlefk=?";
        try (Connection conn = this.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, author);
            stmt.setString(2, title);
            ResultSet res = stmt.executeQuery();
            // checks only the first result
            if (res.next()) {
                System.out.println("The book " + title + " by " + author + " is loaned by " + res.getString("person")
                        + ". The returning date is " + res.getString("return_date") + ".\n");
            } else {
                System.out.println("Nobody is loaning that book.\n");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // finds book(s) based on the title
    // or a part from the title
    public void findBook(String title) {
        String query = "SELECT * FROM books WHERE title LIKE?";
        try (Connection conn = this.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + title + "%");
            ResultSet res = stmt.executeQuery();

            // checks whether there are matching
            // books with that title
            if (res.next() == false) {
                System.out.println("This books is not present in the library.");
            } else {
                System.out.println("All books found by that name: ");
                do {
                    System.out.println(res.getString("title") + "\t" + res.getString("author") + "\t"
                            + res.getString("issue_date") + "\t" + res.getString("available"));
                } while (res.next());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // checks if the credentials are valid
    // and logs in the user
    public boolean login(String userName, String passWord) {
        String query = "SELECT * FROM users WHERE username=? AND password=?";
        try (Connection conn = this.connect(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userName);
            stmt.setString(2, passWord);
            ResultSet res = stmt.executeQuery();
            if (res.next()) {
                // login successful;
                System.out.println("Hello " + userName);
                return true;
            } else {
                System.out.println("Username or password are wrong.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    // print all the books in the Books table
    public void printBooks() {
        String sql = "SELECT title, author, issue_date, available FROM books";

        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            // checks if there are any books in the table
            if (rs.next() == false) {
                System.out.println("There are no books in the library.");
            } else {
                System.out.println("All present books in the library: ");
                do {
                    System.out.println(rs.getString("title") + "\t" + rs.getString("author") + "\t"
                            + rs.getString("issue_date") + "\t" + rs.getString("available"));
                } while (rs.next());
            }
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // prints all the loaned books from the Loaned table
    public void printLoanedBooks() {
        String sql = "SELECT person, return_date, authorfk, titlefk FROM loaned";

        try (Connection conn = this.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            // checks if there are any books in the table
            if (rs.next() == false) {
                System.out.println("There are no books loaned.");
            } else {
                System.out.println("All loaned books: ");
                do {
                    System.out.println(rs.getString("person") + "\t" + rs.getString("return_date") + "\t"
                            + rs.getString("authorfk") + "\t" + rs.getString("titlefk"));
                } while (rs.next());
            }
            System.out.println();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
