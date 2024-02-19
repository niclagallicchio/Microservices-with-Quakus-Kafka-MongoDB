package org.acme;


import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;


// Annotation to specify this class as a MongoDB entity
@MongoEntity(collection = "wine")
public class MyDataObject extends PanacheMongoEntity {


    // Private fields representing the attributes of the data object
    private int code;
    private String wine_name;
    private int vintage;
    private String type;
    private String country;
    private double price;

    // Default constructor
    public MyDataObject() {
    }

    // Getter and setter methods for each attribute
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getWineName() {
        return wine_name;
    }

    public void setWineName(String wine_name) {
        this.wine_name = wine_name;
    }

    public int getVintage() {
        return vintage;
    }

    public void setVintage(int vintage) {
        this.vintage = vintage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

}