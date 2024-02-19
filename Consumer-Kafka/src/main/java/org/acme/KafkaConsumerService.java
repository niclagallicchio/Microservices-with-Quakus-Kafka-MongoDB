package org.acme;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import com.opencsv.exceptions.CsvValidationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

// Declaring the class as an application-scoped bean managed by CDI.
@ApplicationScoped
public class KafkaConsumerService {

    // Logger for logging messages.
    private static final Logger LOG = Logger.getLogger(KafkaConsumerService.class.getName());

    // Counter to skip the header row in CSV data.
    private static int rowCounter = 0;

    // Injecting the MongoDataRepository to interact with MongoDB.
    @Inject
    MongoDataRepository myDataObject;

    // Injecting configuration property for Kafka consumer topic.
    @ConfigProperty(name = "mp.messaging.incoming.kafka-consumer.topic")
    String kafkaConsumerTopic;

    // Method invoked when a message is received from the Kafka topic.
    @Incoming("outgoing-kafka-topic")
    public void consumeMessage(String csvData) {
        LOG.info("Received message from Kafka: " + csvData);

        // Deserialize CSV data into MyDataObject objects.
        List<MyDataObject> dataObjects = deserializeCsv(csvData);

        if (dataObjects != null) {
            for (MyDataObject singleDataObject : dataObjects) {
                MyDataObject existingDataObject = myDataObject.findByCode(singleDataObject.getCode());
                if (existingDataObject != null) {
                    // Update existing data object with new data.
                    updateDataObject(existingDataObject, singleDataObject);
                    LOG.info("Updated data in MongoDB: " + singleDataObject);
                } else {
                    // Insert new document if no documents with the same ID found.
                    myDataObject.persist(singleDataObject);
                    LOG.info("Data entered into MongoDB: " + singleDataObject);
                }
            }
        } else {
            LOG.warning("Unable to deserialize CSV data");
        }
    }

    // Deserialize CSV data into MyDataObject objects.
    private List<MyDataObject> deserializeCsv(String csvData) {
        List<MyDataObject> dataObjects = new ArrayList<>();
        try {
            // Build CSV parser and reader.
            // these two lines of code set up a CSV parser with comma as the field separator and then build a CSV reader that uses this parser to parse the provided CSV string
            CSVParser csvParser = new CSVParserBuilder().withSeparator(',').build();
            CSVReader csvReader = new CSVReaderBuilder(new StringReader(csvData)).withCSVParser(csvParser).build();

            // This array will be used to store the values of the individual fields in the row currently read by the CSVReader
            String[] nextRecord;
            // Iterate through CSV rows, skipping the first row (header).
            while ((nextRecord = csvReader.readNext()) != null) {
                if (rowCounter == 0) {
                    rowCounter++;
                    continue;
                }
                // Convert each CSV row into a MyDataObject object.
                MyDataObject dataObject = new MyDataObject();
                dataObject.setCode(Integer.parseInt((nextRecord[0])));
                dataObject.setWineName(nextRecord[1]);
                dataObject.setVintage(Integer.parseInt(nextRecord[2]));
                dataObject.setType(nextRecord[3]);
                dataObject.setCountry(nextRecord[4]);
                dataObject.setPrice(Double.parseDouble(nextRecord[5]));

                dataObjects.add(dataObject);
            }
        } catch (NumberFormatException e) {
            LOG.severe("Error deserializing CSV data: " + e.getMessage());
        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }
        return dataObjects;
    }

    // Update existing data object with new data.
    private void updateDataObject(MyDataObject existingDataObject, MyDataObject newDataObject) {
        LOG.info("Updating existing data object: " + existingDataObject);
        LOG.info("With new data: " + newDataObject);

        // Update fields from existing document with values from new document.
        existingDataObject.setWineName(newDataObject.getWineName());
        existingDataObject.setVintage(newDataObject.getVintage());
        existingDataObject.setType(newDataObject.getType());
        existingDataObject.setCountry(newDataObject.getCountry());
        existingDataObject.setPrice(newDataObject.getPrice());

        // Persist updated data object.
        myDataObject.update(existingDataObject);

        LOG.info("Updated data object: " + existingDataObject);
    }
}