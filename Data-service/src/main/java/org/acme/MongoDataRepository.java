package org.acme;


import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MongoDataRepository implements PanacheMongoRepository<MyDataObject> {

    public MyDataObject findByCode(int code) {
        // Using Panache's find method to search for data by ID and returning the first result
        return find("code", code).firstResult();
    }


    public void deleteByCode(int code) {
        delete("code", code);
    }


}
