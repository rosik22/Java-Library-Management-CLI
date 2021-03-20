import java.util.Scanner;

public class EventHandle {
    private Scanner scan = new Scanner(System.in);
    private DatabaseManager dbman = new DatabaseManager();
    private boolean running = true;

    public boolean Running() {
        return running;
    }

    // Initializing tables
    public void StartUp() {
        dbman.createNewTable();
    }

    // prompt user to sign-un
    public void promptSignUp() {
        System.out.println("Sign-up");

        System.out.print("Username(at least one character): ");
        String username = scan.nextLine();

        System.out.print("Password(at least five characters): ");
        String password = scan.nextLine();

        // check if the credentials are compatible
        // and insert them in the User table
        if (checkCredentials(username, password)) {
            dbman.insertUser(username, password);
        }
    }

    // prompt user to sign-in
    public boolean promptSignIn() {
        System.out.println("Sign-in");

        System.out.print("Username: ");
        String username = scan.nextLine();

        System.out.print("Password: ");
        String password = scan.nextLine();

        // if the username and password are correct
        // the user is signed in
        if (dbman.login(username, password)) {
            return true;
        }
        return false;
    }

    // checks whether the credentials are correct
    public boolean checkCredentials(String username, String password) {
        if (username.length() < 1 || password.length() < 5) {
            System.out.println("Wrong length of username or password");
            return false;
        }
        return true;
    }

    // prompts the user to either log-in or register
    public String login_register() {
        System.out.println("Choose (s) for sign-in | Choose (r) for registration");
        String choice = scan.nextLine();
        return choice;
    }

    // prompts the user to choose a command
    public boolean menu_prompt() {
        System.out.println("Choose a command:");
        System.out.println(" a(add) - add a book in the library" + "\n l(loan) - loan a book to a person"
                + "\n t(take) - take back a book from a person" + "\n r(remove) - discard a book from the library"
                + "\n av(availability) - check if a book is available"
                + "\n w(who is loaning) - check who is loaning the book"
                + "\n s(search) - search for a book(s) by its name" + "\n u(unavailable) - show all unavailable books"
                + "\n c(catalogue) - show all the books in the library" + "\n e(exit) - log out");

        String choice = scan.nextLine();
        // log out if the exit command is chosen
        if (choice.equals("e")) {
            return false;
        } else {
            // handle every other choice
            handleMenuChoice(choice);
        }
        return true;
    }

    // handles the command which the user
    // chose to execute
    public void handleMenuChoice(String choice) {
        String input;
        String strings[];
        switch (choice) {
        case "a":
            System.out.println("Enter data in the order: title,author,date of issue(YYYY-MM-DD)");
            input = scan.nextLine();
            strings = input.split(",");
            if (strings.length > 1) {
                dbman.insertBook(strings[0], strings[1], strings[2]);
            }
            break;
        case "l":
            System.out.println("Enter data in the order: person,return date(optional),author,title");
            input = scan.nextLine();
            strings = input.split(",");
            if (strings.length > 1) {
                dbman.loanBook(strings);
            }
            break;
        case "t":
            System.out.println("Enter data in the order: author,title");
            input = scan.nextLine();
            strings = input.split(",");
            if (strings.length > 1) {
                dbman.receiveBook(strings[0], strings[1]);
            }
            break;
        case "r":
            System.out.println("Enter data in the order: author,title");
            input = scan.nextLine();
            strings = input.split(",");
            if (strings.length > 1) {
                dbman.removeBook(strings[0], strings[1]);
            }
            break;
        case "av":
            System.out.println("Enter data in the order: author,title");
            input = scan.nextLine();
            strings = input.split(",");
            if (strings.length > 1) {
                dbman.isBookAvailable(strings[0], strings[1]);
            }
            break;
        case "w":
            System.out.println("Enter data in the order: author,title");
            input = scan.nextLine();
            strings = input.split(",");
            if (strings.length > 1) {
                dbman.whoIsLoaning(strings[0], strings[1]);
            }
            break;
        case "s":
            System.out.println("Enter data as follows: title");
            input = scan.nextLine();
            if (input.length() > 0) {
                dbman.findBook(input);
            }
            break;
        case "u":
            dbman.printLoanedBooks();
            break;
        case "c":
            dbman.printBooks();
            break;
        }
    }
}
