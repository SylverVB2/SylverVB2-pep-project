package DAO;

import Model.Account;
import Util.ConnectionUtil;

import java.sql.*;

public class AccountDAO {
    public Account insertAccount(Account account){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet pkeyResultSet = null; 
        try {
            connection = ConnectionUtil.getConnection();
            String sql = "INSERT INTO account (username, password) Values(?, ?)";
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Setting the values for username and password
            preparedStatement.setString(1, account.getUsername());
            preparedStatement.setString(2, account.getPassword());
            
            // Executing the SQL statement
            preparedStatement.executeUpdate();

            // Retrieving the generated keys
            pkeyResultSet = preparedStatement.getGeneratedKeys();
            if (pkeyResultSet.next()) {
                int generated_account_id = (int) pkeyResultSet.getInt(1);
                return new Account(generated_account_id, account.getUsername(), account.getPassword());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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

    public Account getAccountByUserName(String username) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionUtil.getConnection();

            // Retrieving account from a database by username
            String sql = "SELECT account_id, password FROM account WHERE username = ?";
            preparedStatement = connection.prepareStatement(sql);

            // Setting the value for username
            preparedStatement.setString(1, username);
            
            // Executing the SQL statement to retrieve the result
            ResultSet resultSet = preparedStatement.executeQuery();

            // Checking if the result set contains any row indicating that the account exists
            if (resultSet.next()) {
                // Retrieving account ID and password from the result set
                int accountId = resultSet.getInt("account_id");
                String password = resultSet.getString("password");
                // Constructing and returning account object using retrieved data
                return new Account(accountId, username, password);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            // Ensure that resources (PreparedStatement, Connection) are closed to prevent resource leaks
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close(); // Returning the connection to the pool
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }
}