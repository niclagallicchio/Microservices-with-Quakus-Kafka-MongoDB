package org.acme;


import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
// Declaring the class as an application-scoped bean
@ApplicationScoped
public class MongoDataRepository implements PanacheMongoRepository<MyDataObject> {

    // Method to find data by ID
    public MyDataObject findByCode(int code) {
        // Using Panache's find method to search for data by code (ID) and returning the first result
        return find("code", code).firstResult();
    }

}