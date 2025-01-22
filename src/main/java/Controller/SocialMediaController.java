package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import java.util.*;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */

/**
 * Controller class responsible for handling the HTTP requests related to social media functionality.
 * It manages the interactions with Account and Message services.
 */
public class SocialMediaController {
    // Declaring accountService and messageService as a class-level field.
    private AccountService accountService;
    private MessageService messageService;

    // Constructing a new SocialMediaController and initializes the AccountService and MessageService.
    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    /**
     * Starts the Javalin application and defines the routes for various endpoints.
     * This method is required to provide a Javalin app object for testing purposes.
     * 
     * @return A Javalin app object that configures the behavior of the controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        
        // Account-related routes
        app.post("/register", this::registerAccountHandler);
        app.post("/login", this::postLoginHandler);

        // Message-related routes
        app.get("/messages", this::getAllMessagesHandler);
        app.get("/messages/{message_id}", this::getMessageByMessageIdHandler);
        app.post("/messages", this::postMessageHandler);
        app.patch("/messages/{message_id}", this::updateMessageHandler);
        app.delete("/messages/{message_id}", this::deleteMessageHandler);

        // Account and message-related routes
        app.get("/accounts/{account_id}/messages", this::getAllMessagesByAccountIdHandler);

        return app;
    }

    /**
     * Registers a new account using data from the request body.
     * Validates the account data, creates the account, and responds with account details.
     * 
     * @param ctx The Javalin Context object, which provides access to the HTTP request and response.
     * @throws JsonProcessingException If there is an issue with JSON parsing or serialization.
     * 
     * When creating an account, the "throws JsonProcessingException" is required because the ObjectMapper's readValue()
     * method can throw this exception if the JSON in the request body is invalid or cannot be deserialized 
     * into the Account class. By declaring it here, we delegate the handling of this exception 
     * to the framework or a global error handler, ensuring clean and centralized error management.
     */
    private void registerAccountHandler(Context ctx) throws JsonProcessingException {
        // Initializing ObjectMapper to handle JSON serialization and deserialization.
        // Deserializing request body into Account object using Jackson ObjectMapper.
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Deserializing the JSON request body into an Account object.
            // The account data sent by the client (username, password) is mapped to the Account class.
            Account account = mapper.readValue(ctx.body(), Account.class);  

            // Delegating account registration to the service layer.
            // Calling the service layer to register the account and persist it in the database.
            Account addedAccount = accountService.registerAccount(account);
        
            // Creating a response object. Here, we should NOT include the password in the response for security reasons.
            // It's good practice to exclude sensitive data like passwords from being returned in API responses.
            Map<String, Object> response = new HashMap<>();
            response.put("account_id", addedAccount.getAccount_id());
            response.put("username", addedAccount.getUsername());
            response.put("password", addedAccount.getPassword()); // Should be removed from the response

            // Serializing the response object into a JSON string and 
            // responding with the newly created account details.
            String jsonResponse = mapper.writeValueAsString(response);

            // Sending the serialized JSON response with status 200 and the account details.
            ctx.status(200).json(jsonResponse);
            } catch (IllegalArgumentException e) {
                // Handling validation failures and responding with 400 Bad Request
                ctx.status(400).result(e.getMessage());
            } catch (JsonProcessingException e) {
                // Handling malformed JSON input
                ctx.status(400).result("Invalid JSON format in request body.");
            }
        }
    
    /**
     * Authenticates a user by verifying their login credentials.
     * If successful, returns the account details in the response.
     * 
     * @param ctx The Javalin Context object manages information about both the HTTP request and response.
     */
    private void postLoginHandler(Context ctx) {
        // Deserializing request body into Account object
        Account credentials = ctx.bodyAsClass(Account.class);

        String username = credentials.getUsername();
        String password = credentials.getPassword();

        try {
            // Delegating the login logic to the service layer
            Account account = accountService.login(username, password);

            // Creating the response with account details (including account_id)
            Map<String, Object> response = new HashMap<>();
            response.put("account_id", account.getAccount_id());
            response.put("username", account.getUsername());
            response.put("password", account.getPassword()); // Needs to be removed as the response should contain only non-sensitive data

            // Sending successful response with account details
            ctx.status(200).json(response);
        } catch (IllegalArgumentException e) {
            // Catching invalid credentials errors and send the error response
            ctx.status(401).json(e.getMessage());
        }
    }

    /**
     * Posts a new message.
     * Validates the message data, creates the message, and responds with the newly created message details.
     * 
     * @param ctx The Javalin Context object.
     * @throws JsonProcessingException If there is an issue with JSON parsing or serialization.
     */
    private void postMessageHandler(Context ctx) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Deserializing the JSON request body into an Message object.
            // The message data sent by the client (posted_by, message_text,time_posted_epoch) is mapped to the Message class.
            Message message = mapper.readValue(ctx.body(), Message.class);

            // Delegating account registration to the service layer.
            // Calling the service layer to verify the message and persist it in the database.
            Message addedMessage = messageService.postMessage(message);

            ctx.status(200).json(addedMessage);
            } catch (IllegalArgumentException e) {
                // Handling validation failures and responding with 400 Bad Request
                ctx.status(400).result(e.getMessage());
            } catch (JsonProcessingException e) {
                // Handling malformed JSON input
                ctx.status(400).result("Invalid JSON format in request body.");
            }
        }
    
    /**
     * Retrieves all messages in the system.
     * 
     * @param ctx The Javalin Context object.
     */
    private void getAllMessagesHandler(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);
    }

    /**
     * Retrieves a specific message by its ID.
     * 
     * @param ctx The Javalin Context object.
     */
    private void getMessageByMessageIdHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id"));
        
        Message message = messageService.getMessageById(messageId);
    
        if (message != null) {
            ctx.json(message);
        } else {
            ctx.status(200).result("");
        }
    }

    /**
     * Retrieves all messages posted by a specific user.
     * 
     * @param ctx The Javalin Context object.
     */
    private void getAllMessagesByAccountIdHandler(Context ctx) {
        int accountId = Integer.parseInt(ctx.pathParam("account_id"));
        
        List<Message> messages = messageService.getMessagesByAccountId(accountId);
    
        if (messages != null) {
            ctx.json(messages);
        } else {
            ctx.status(200).result("");
        }
    }
    
    /**
     * Updates an existing message.
     * 
     * @param ctx The Javalin Context object.
     * @throws JsonProcessingException If there is an issue with JSON parsing or serialization.
     */
    private void updateMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id")); // Getting the message_id from the URL path
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Deserializing the JSON request body into an Message object.
            // The message data sent by the client (posted_by, message_text,time_posted_epoch) is mapped to the Message class.
            Message message = mapper.readValue(ctx.body(), Message.class);

            // Calling the service layer to update the message
            Message updatedMessage = messageService.updateMessage(messageId, message);

            ctx.status(200).json(updatedMessage);
            } catch (IllegalArgumentException e) {
                // Handling validation failures and responding with 400 Bad Request
                ctx.status(400).result(e.getMessage());
            } catch (JsonProcessingException e) {
                // Handling malformed JSON input
                ctx.status(400).result("Invalid JSON format in request body.");
            }
        }
    
    /**
     * Deletes a message by its ID.
     * 
     * @param ctx The Javalin Context object.
     */
    private void deleteMessageHandler(Context ctx) {
        int messageId = Integer.parseInt(ctx.pathParam("message_id")); // Getting the message_id from the URL path
        
        // Calling the service layer to delete the message
        Message deletedMessage = messageService.deleteMessage(messageId);

        if (deletedMessage != null) {
            // If the message existed and was deleted, return it in the response
            ctx.status(200).json(deletedMessage);
        } else {
            // If the message did not exist, return an empty body with a 200 status
            ctx.status(200).result("");
        }
    }
}