package DAO;

import Util.ConnectionUtil;
import Model.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    /**
     * Retrieves all messages from the 'message' table.
     *
     * @return A list of all messages in the database, or an empty list if no messages exist.
     */
    public List<Message> getAllMessages(){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Message> messages = new ArrayList<>();
        try {
            connection = ConnectionUtil.getConnection();
            String sql = "SELECT * FROM message"; // SQL query to fetch all rows from the 'message' table
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery(); // Executing the query and retrieving the result set
            while(resultSet.next()){
                // Creating a Message object for each row and adding it to the list
                Message message = new Message(resultSet.getInt("message_id"),
                        resultSet.getInt("posted_by"),
                        resultSet.getString("message_text"),
                        resultSet.getInt("time_posted_epoch"));
                messages.add(message);
            }
        } catch(SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            // Closing resources in reverse order of their opening
            // Closing the connection is necessary to:
            // 1. Return the connection to the pool for reuse, avoiding connection pool exhaustion.
            // 2. Prevent resource leaks that can lead to memory and database issues.
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close(); // Returning the connection to the pool
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return messages; // Returning the list of messages
    }

    /**
     * Retrieves a specific message from the 'message' table by its ID.
     *
     * @param message_id The unique ID of the message to retrieve.
     * @return A Message object representing the retrieved message, or null if the message is not found.
     */
    public Message getMessageByMessageID(int message_id){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null; 
        try {
            connection = ConnectionUtil.getConnection();
            String sql = "SELECT * FROM message WHERE message_id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, message_id); // Setting the message_id parameter
            resultSet = preparedStatement.executeQuery(); // Executing the query
            while(resultSet.next()){
                // Constructing a Message object from the result set
                Message message = new Message(resultSet.getInt("message_id"),
                        resultSet.getInt("posted_by"),
                        resultSet.getString("message_text"),
                        resultSet.getInt("time_posted_epoch"));
                return message;
            }
        } catch(SQLException e) {
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
        return null; // Returning null if no message was found
    }

    /**
     * Retrieves all messages posted by a specific user.
     *
     * @param account_id The ID of the user whose messages are to be retrieved.
     * @return A list of messages posted by the specified user, or an empty list if no messages exist for that user.
     */
    public List<Message> getAllMessagesByAccountID(int account_id){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null; 
        List<Message> messages = new ArrayList<>();
        try {
            connection = ConnectionUtil.getConnection();
            String sql = "SELECT * FROM message WHERE posted_by = ?"; // Query to fetch messages by account_id
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, account_id); // Setting the account_id parameter
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Message message = new Message(resultSet.getInt("message_id"),
                        resultSet.getInt("posted_by"),
                        resultSet.getString("message_text"),
                        resultSet.getInt("time_posted_epoch"));
                messages.add(message);
            }
        } catch(SQLException e) {
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
        return messages; // Returning the list of messages
    }

    /**
     * Inserts a new message into the 'message' table.
     *
     * @param message The Message object containing the data to be inserted.
     * @return A new Message object representing the inserted message with its generated ID, or null if the insertion fails.
     */
    public Message insertMessage(Message message){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet pkeyResultSet = null; 
        try {
            connection = ConnectionUtil.getConnection();
            String sql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) Values(?, ?, ?)";
            preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Setting the values for posted_by, message_text, time_posted_epoch
            preparedStatement.setInt(1, message.getPosted_by());
            preparedStatement.setString(2, message.getMessage_text());
            preparedStatement.setLong(3, message.getTime_posted_epoch());
            
            // Executing the SQL statement
            preparedStatement.executeUpdate();

            // Retrieving the generated keys to set the message ID
            pkeyResultSet = preparedStatement.getGeneratedKeys();
            if (pkeyResultSet.next()) {
                int generated_message_id = (int) pkeyResultSet.getInt(1);
                // Returning a new Message object with the generated ID
                return new Message(generated_message_id, message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            // Ensuring that resources (pkeyResultSet, PreparedStatement, Connection) are closed to prevent resource leaks
            try {
                if (pkeyResultSet != null) pkeyResultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return null; // Returning null if the insert failed
    }

    /**
     * Updates the text of an existing message in the 'message' table by its ID.
     *
     * @param message_id The ID of the message to update.
     * @param message The Message object containing the updated text.
     * @return The updated Message object, or null if the update fails or the message is not found.
     */
    public Message updateMessage(int message_id, Message message){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectionUtil.getConnection();
            String sql = "UPDATE message SET message_text = ? WHERE message_id = ?";
            preparedStatement = connection.prepareStatement(sql);

            // Setting parameters for the update query
            preparedStatement.setString(1, message.getMessage_text());
            preparedStatement.setInt(2, message_id);
            
            // Executing the update
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            // Ensuring that resources (PreparedStatement, Connection) are closed to prevent resource leaks
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Deletes a message from the 'message' table by its ID.
     *
     * @param message_id The unique ID of the message to delete.
     * @return The deleted Message object, or null if the deletion fails or the message does not exist.
     */
    public Message deleteMessageById(int message_id){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            // Fetching the message using the existing method
            Message message = getMessageByMessageID(message_id);
            if (message == null) {
                return null; // Message doesn't exist
            }

            // Deleting the message
            connection = ConnectionUtil.getConnection();
            String sql = "DELETE FROM message WHERE message_id = ?";
            preparedStatement = connection.prepareStatement(sql);

            // Setting the values for message_id
            preparedStatement.setInt(1, message_id);
            
            // Executing the delete query
            preparedStatement.executeUpdate();

            return message; // Returning the deleted message
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            // Ensuring that resources (PreparedStatement, Connection) are closed to prevent resource leaks
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        return null;
    }
}