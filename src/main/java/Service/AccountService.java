package Service;

import Model.Account;
import DAO.AccountDAO;

public class AccountService {
    private AccountDAO accountDAO;

    // Default constructor
    public AccountService(){
        accountDAO = new AccountDAO();
    }

    // Constructor to inject AccountDAO
    public AccountService(AccountDAO accountDAO){
        this.accountDAO = accountDAO;
    }

    // The method to handle account registration logic
    public Account registerAccount(Account account) {
        // Validating username
        String username = account.getUsername();
        if (username == null || username.trim().isEmpty()) {
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

    // Getting account by username method
    public Account getAccountByUsername(String username) {
        return accountDAO.getAccountByUserName(username);
    }

    // Checking if an account exists by username
    public boolean accountExists(String username) {
        return getAccountByUsername(username) != null;
    }

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