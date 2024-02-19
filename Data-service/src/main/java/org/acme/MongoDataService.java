package org.acme;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.StringReader;
import java.util.List;

@Path("/data-service")
@ApplicationScoped
public class MongoDataService {

    // Logger for logging messages
    private static final Logger LOG = Logger.getLogger(MongoDataService.class);

    // Injecting MongoDataRepository instance
    @Inject
    MongoDataRepository myDataObject;

    // GET method to retrieve all data as JSON
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllDataAsJson() {
        // Retrieving all data from MongoDB
        List<MyDataObject> allData = myDataObject.listAll();
        // Logging the retrieved data
        LOG.info("Retrieved all data from MongoDB: " + allData);

        // Using Jackson to convert List into a JSON string
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Attempt to convert the list of data objects into a JSON string
            String jsonData = objectMapper.writeValueAsString(allData);
            // Return the JSON string if successful
            return jsonData;
        } catch (JsonProcessingException e) {
            // Logging error if JSON conversion fails
            LOG.error("Error converting data to JSON: " + e.getMessage());
            return null;
        }
    }

    // GET method to retrieve data by code as JSON
    @GET
    @Path("/{code}")
    // Specifies that this method produces JSON media type
    @Produces(MediaType.APPLICATION_JSON)
    public String getDataByCode(@PathParam("code") int code) {
        // Retrieving data by its code from the MongoDB repository
        MyDataObject data = myDataObject.findByCode(code);
        if (data != null) {
            // Logging the retrieved data
            LOG.info("Retrieved data with code " + code + " from MongoDB: " + data);

            // Using Jackson to convert MyDataObject into a JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // serialization, where the Java object is transformed into a JSON representatio
                String jsonData = objectMapper.writeValueAsString(data);
                return jsonData;
            } catch (JsonProcessingException e) {
                // Logging error if JSON conversion fails
                LOG.error("Error converting data to JSON: " + e.getMessage());
                return null;
            }
        } else {
            // Logging warning if data is not found
            LOG.warn("Data with code " + code + " not found in MongoDB.");
            return null;
        }
    }

    // DELETE method to delete data by code
    @DELETE
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDataById(@PathParam("code") int code) {
        // Retrieving data by ID from MongoDB
        MyDataObject deletedData = myDataObject.findByCode(code);
        if (deletedData != null) {
            // Deleting data from MongoDB
            myDataObject.deleteByCode(code);
            // Logging the deletion
            LOG.info("Data with code " + code + " deleted from MongoDB.");
            String confirmationMessage = "Data with code " + code + " deleted with success.";
            return Response.ok(confirmationMessage).build();
        } else {
            // Logging warning if data is not found
            LOG.warn("Data with code " + code + " not found in MongoDB.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateData(@PathParam("code") int code, String jsonData) {
        try {
            // Retrieve existing data by code from MongoDB
            MyDataObject existingData = myDataObject.findByCode(code);

            if (existingData != null) {
                // Create ObjectMapper to handle JSON conversion
                ObjectMapper objectMapper = new ObjectMapper();

                // Convert JSON data to MyDataObject
                MyDataObject newData = objectMapper.readValue(jsonData, MyDataObject.class);

                // Merge existingData with newData
                objectMapper.readerForUpdating(existingData).readValue(jsonData);

                // Update the data in the database
                myDataObject.update(existingData);
                LOG.info("Data with code " + code + " updated in MongoDB.");

                // Convert updated data to JSON
                String updatedJsonData = objectMapper.writeValueAsString(existingData);

                // Return updated JSON as response
                return Response.ok(updatedJsonData).build();
            } else {
                // If data is not found, return 404 (NOT FOUND) response
                LOG.error("Data with code " + code + " not found in MongoDB.");
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            // If exception occurs during JSON parsing or data update, return 400 (BAD REQUEST) response
            LOG.warn("Exception occurred during JSON parsing or data update: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
