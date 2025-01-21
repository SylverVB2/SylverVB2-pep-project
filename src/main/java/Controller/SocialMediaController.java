package Controller;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import Model.Account;
import Service.AccountService;
import java.util.*;

/**
 * TODO: You will need to write your own endpoints and handlers for your controller. The endpoints you will need can be
 * found in readme.md as well as the test cases. You should
 * refer to prior mini-project labs and lecture materials for guidance on how a controller may be built.
 */
public class SocialMediaController {
    // Declaring accountService as a class-level field
    private AccountService accountService;

    // Constructor to initialize AccountService
    public SocialMediaController() {
        this.accountService = new AccountService();
    }

    /**
     * In order for the test cases to work, you will need to write the endpoints in the startAPI() method, as the test
     * suite must receive a Javalin object from this method.
     * @return a Javalin app object which defines the behavior of the Javalin controller.
     */
    public Javalin startAPI() {
        Javalin app = Javalin.create();
        
        // Account-related routes
        app.post("/register", this::registerAccountHandler);

        // Login route
        app.post("/login", this::postLoginHandler);
  
        // app.start(8080); // redundant here as we're calling it within the setUp() method

        return app;
    }

    /**
     * This is an example handler for an example endpoint.
     * @param context The Javalin Context object manages information about both the HTTP request and response.
     */
    
    // When creating an account, the "throws JsonProcessingException" is required because the ObjectMapper's readValue()
    // method can throw this exception if the JSON in the request body is invalid or cannot be deserialized 
    // into the Account class. By declaring it here, we delegate the handling of this exception 
    // to the framework or a global error handler, ensuring clean and centralized error management.
    private void registerAccountHandler(Context ctx) throws JsonProcessingException {
        // Initializing ObjectMapper to handle JSON serialization and deserialization
        // Deserializing request body into Account object using Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Deserializing the JSON request body into an Account object
            // The account data sent by the client (username, password) is mapped to the Account class
            Account account = mapper.readValue(ctx.body(), Account.class);  

            // Delegating account registration to the service layer
            // Calling the service layer to register the account and persist it in the database
            Account addedAccount = accountService.registerAccount(account);
        
            // Creating a response object. Here, we should NOT include the password in the response for security reasons.
            // It's good practice to exclude sensitive data like passwords from being returned in API responses.
            Map<String, Object> response = new HashMap<>();
            response.put("account_id", addedAccount.getAccount_id());
            response.put("username", addedAccount.getUsername());
            response.put("password", addedAccount.getPassword()); // Should be removed from the response

            // Serializing the response object into a JSON string and 
            // responding with the newly created account details
            String jsonResponse = mapper.writeValueAsString(response);

            // Sending the serialized JSON response with status 200 and the account details
            ctx.status(200).json(jsonResponse);
            } catch (IllegalArgumentException e) {
                // Handling validation failures and responding with 400 Bad Request
                ctx.status(400).result(e.getMessage());
            } catch (JsonProcessingException e) {
                // Handling malformed JSON input
                ctx.status(400).result("Invalid JSON format in request body.");
            }
        }
    
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
}