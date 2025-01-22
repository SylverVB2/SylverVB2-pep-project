package DAO;

import Model.Account;
import Util.ConnectionUtil;

import java.sql.*;

public class AccountDAO {

    /**
     * Inserts a new account into the database and returns the created account with its generated ID.
     *
     * @param account The account object containing the username and password to insert.
     * @return A new Account object with the generated account ID, or null if the insertion fails.
     */
    public Account insertAccount(Account account){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet pkeyResultSet = null; 
        try {
            // Establishing a connection to the database
            connection = ConnectionUtil.getConnection();
            String sql = "INSERT INTO account (username, password) Values(?, ?)";
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Setting the values for username and password
            preparedStatement.setString(1, account.getUsername());
            preparedStatement.setString(2, account.getPassword());
            
            // Executing the SQL statement
            preparedStatement.executeUpdate();

            // Retrieving the auto-generated account ID
            pkeyResultSet = preparedStatement.getGeneratedKeys();
            if (pkeyResultSet.next()) {
                int generated_account_id = (int) pkeyResultSet.getInt(1);
                // Returning the account with its newly generated ID
                return new Account(generated_account_id, account.getUsername(), account.getPassword());
            }
        } catch (SQLException e) {
            // Logging the exception message for debugging purposes
            System.out.println(e.getMessage());
        } finally {
            // Closing resources in reverse order of their opening
            // Closing the connection is necessary to:
            // 1. Return the connection to the pool for reuse, avoiding connection pool exhaustion.
            // 2. Prevent resource leaks that can lead to memory and database issues.
            try {
                if (pkeyResultSet != null) pkeyResultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close(); // Returning the connection to the pool
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Retrieves an account from the database using the provided username.
     *
     * @param username The username of the account to retrieve.
     * @return An Account object if found, or null if no account matches the username.
     */
    public Account getAccountByUserName(String username) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null; 
        try {
            connection = ConnectionUtil.getConnection();

            // Retrieving account from a database by username
            String sql = "SELECT account_id, password FROM account WHERE username = ?";
            preparedStatement = connection.prepareStatement(sql);

            // Setting the value for username
            preparedStatement.setString(1, username);
            
            // Executing the SQL statement to retrieve the result set
            resultSet = preparedStatement.executeQuery();

            // Checking if the result set contains any row indicating that the account exists
            // If it is found, constructing and returning an Account object
            if (resultSet.next()) {
                // Retrieving account ID and password from the result set
                int accountId = resultSet.getInt("account_id");
                String password = resultSet.getString("password");
                // Constructing and returning an Account object using retrieved data
                return new Account(accountId, username, password);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            // Ensuring that resources (ResultSet, PreparedStatement, Connection) are closed to prevent resource leaks
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Checks whether an account with the given account ID exists in the database.
     *
     * @param accountId The account ID to check for existence.
     * @return True if the account exists, false otherwise.
     */
    public boolean accountExistsById(int accountId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = ConnectionUtil.getConnection();

            // Checking if account_id exists
            // We do not need to retrieve all the column data, just a single constant (1), which is faster.
            String sql = "SELECT 1 FROM account WHERE account_id = ?";
            preparedStatement = connection.prepareStatement(sql);

            // Setting the value for account_id
            preparedStatement.setInt(1, accountId);
            
            // Executing the SQL statement to retrieve the result
            resultSet = preparedStatement.executeQuery();

            // Returning true if a row exists, false otherwise
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            // Ensuring that resources (ResultSet, PreparedStatement, Connection) are closed to prevent resource leaks
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return false;
    }
}