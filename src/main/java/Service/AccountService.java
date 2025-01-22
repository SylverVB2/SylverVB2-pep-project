package Service;

import Model.Account;
import DAO.AccountDAO;

public class AccountService {
    private AccountDAO accountDAO;

    // Default constructor initializes accountDAO
    public AccountService(){
        accountDAO = new AccountDAO();
    }

    // Constructor to inject AccountDAO
    public AccountService(AccountDAO accountDAO){
        this.accountDAO = accountDAO;
    }

    /**
     * Handles account registration logic.
     * 
     * @param account The Account object containing the user's registration details.
     * @return The newly registered Account object if registration is successful.
     * @throws IllegalArgumentException If any validation fails (e.g., empty username, weak password, or duplicate username).
     */
    public Account registerAccount(Account account) {
        // Validating username
        String username = account.getUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(""); // "Username cannot be blank."
        }

        // Validating password
        String password = account.getPassword();
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException(""); // "Password must be at least 4 characters long."
        }

        // Checking if the username already exists
        if (accountExists(username)) {
            throw new IllegalArgumentException(""); // "Account with this username already exists."
        }

        // If validations pass, persist the account and return the saved object
        return accountDAO.insertAccount(account);
    }

    /**
     * Retrieves an account by its username.
     * 
     * @param username The username to search for in the database.
     * @return The Account object if found, or null if no account exists with the given username.
     */
    public Account getAccountByUsername(String username) {
        return accountDAO.getAccountByUserName(username);
    }

    /**
     * Checks if an account exists by its username.
     * 
     * @param username The username to check for in the database.
     * @return True if an account exists with the given username, false otherwise.
     */
    public boolean accountExists(String username) {
        return getAccountByUsername(username) != null;
    }

    /**
     * Handles account login logic by checking if the username and password match.
     * 
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     * @return The authenticated Account object if login is successful.
     * @throws IllegalArgumentException If the username is invalid or the password does not match.
     */
    public Account login(String username, String password) throws IllegalArgumentException {
        // Step 1: Checking if account exists by username
        Account account = getAccountByUsername(username);

        if (account == null) {
            // Account not found, throwing exception to be handled by the controller
            throw new IllegalArgumentException(""); // "Invalid username or password."
        }

        // Step 2: Checking if the password matches
        if (!account.getPassword().equals(password)) {
            // Invalid password, throwing exception
            throw new IllegalArgumentException(""); // "Invalid username or password."
        }

        // Returning the authenticated account if successful
        return account;
    }
}