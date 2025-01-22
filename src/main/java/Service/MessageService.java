package Service;

import Model.Message;
import DAO.MessageDAO;
import DAO.AccountDAO;
import java.util.List;

public class MessageService {
    private MessageDAO messageDAO;
    private AccountDAO accountDAO;

    /**
     * Default constructor: initializes DAOs for Message and Account.
     */
    public MessageService(){
        messageDAO = new MessageDAO();
        accountDAO = new AccountDAO();
    }

    /**
     * Constructor to inject MessageDAO and AccountDAO.
     *
     * @param messageDAO The Data Access Object for messages.
     * @param accountDAO The Data Access Object for accounts.
     */
    public MessageService(MessageDAO messageDAO, AccountDAO accountDAO){
        this.messageDAO = messageDAO;
        this.accountDAO = accountDAO;
    }

    /**
     * Posts a new message after validating its content and the user posting it.
     *
     * @param message The Message object containing the message text, posted_by (account), and time posted.
     * @return The persisted Message object after being saved to the database.
     * @throws IllegalArgumentException If the message text is invalid or the user does not exist.
     */
    public Message postMessage(Message message) throws IllegalArgumentException {
        int postedBy = message.getPosted_by();
        String text = message.getMessage_text();
        long time = message.getTime_posted_epoch();

        // Validating message_text to ensure it's not empty or too long
        if (text == null || text.isBlank() || text.length() > 255) {
            throw new IllegalArgumentException(""); // "Message cannot be blank and must be no more than 255 characters long."
        }

        // Validating posted_by to ensure the user exists in the system
        if (!accountDAO.accountExistsById(postedBy)) {
            throw new IllegalArgumentException(""); // "The user posting the message does not exist."
        }

        // Persisting the message in the database
        return messageDAO.insertMessage(new Message(postedBy, text, time));
    }

    /**
     * Retrieves all messages from the database.
     *
     * @return A list of all messages.
     */
    public List<Message> getAllMessages() {
        return messageDAO.getAllMessages();
    }

    /**
     * Retrieves a specific message by its unique ID.
     *
     * @param messageId The unique ID of the message to retrieve.
     * @return The Message object corresponding to the given ID, or null if not found.
     */
    public Message getMessageById(int messageId) {
        return messageDAO.getMessageByMessageID(messageId);
    }

    /**
     * Retrieves all messages associated with a specific user by account ID.
     *
     * @param accountId The unique ID of the account (user) whose messages are to be retrieved.
     * @return A list of messages for the specified account (user).
     */
    public List<Message> getMessagesByAccountId(int accountId) {
        return messageDAO.getAllMessagesByAccountID(accountId);
    }

    /**
     * Updates an existing message with new content.
     *
     * @param messageId The ID of the message to update.
     * @param message The Message object containing the updated message content.
     * @return The updated Message object.
     * @throws IllegalArgumentException If the new message text is invalid or the message does not exist.
     */
    public Message updateMessage(int messageId, Message message) throws IllegalArgumentException {
        // Step 1: Validating the input message
        String text = message.getMessage_text();
        if (text == null || text.isBlank() || text.length() > 255) {
            throw new IllegalArgumentException(""); // "Message cannot be blank and must be no more than 255 characters long."
        }
    
        // Step 2: Checking if the message exists (using DAO method)
        Message existingMessage = messageDAO.getMessageByMessageID(messageId);
        if (existingMessage == null) {
            throw new IllegalArgumentException(""); // "No message found with the given message_id."
        }
    
        // Step 3: Updating the message in the database
        messageDAO.updateMessage(messageId, message);
    
        // Step 4: Returning the updated message by fetching it from the database
        return messageDAO.getMessageByMessageID(messageId);
    }
    
    /**
     * Deletes a message from the 'message' table by its ID.
     *
     * @param messageId The unique ID of the message to delete.
     * @return The deleted Message object, or null if the deletion fails or the message does not exist.
     */
    public Message deleteMessage(int messageId) {
        return messageDAO.deleteMessageById(messageId);
    }
}