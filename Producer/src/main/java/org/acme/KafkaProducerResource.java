package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;

//@Path("/kafka-producer") and @ApplicationScoped annotations are used to declare that this class will handle HTTP requests starting with /kafka-producer and that it will be handled as a CDI bean with a lifecycle associated with the application
@Path("/kafka-producer")
@ApplicationScoped
public class KafkaProducerResource {

    private static final Logger LOG = Logger.getLogger(KafkaProducerResource.class);

    //@Inject annotation is used to inject an instance of Emitter<String>, which is a component of SmallRye Reactive Messaging, into the kafkaTopicEmitter field. This emitter will be used to send messages to the Kafka topic.
    @Inject
    @Channel("outgoing-kafka-topic")
    Emitter<String> kafkaTopicEmitter;

    @ConfigProperty(name = "mp.messaging.outgoing.words-out.topic")
    String kafkaTopic;


    //@GET, which means it will be invoked when an HTTP GET request arrives to /kafka-producer.
    @GET
    public String produceToKafka() {
        //path of the CSV file to read
        String filePath = "src/main/resources/file.csv";

        //created a BufferedReader object to read the file line by line
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            //while: for each line read kafkaTopicEmitter.send(line) is invoked to send the contents of the line to the Kafka topic.
            //A log message is recorded using the Logger object to indicate that the message was successfully sent to the Kafka topic
            while ((line = br.readLine()) != null) {
                kafkaTopicEmitter.send(line);
                LOG.infof("Sent message to Kafka topic: %s", line);
            }
            return "Data sent to Kafka topic successfully!";
        } catch (Exception e) {
            //If an exception occurs, it is caught and logged as an error using the Logger object
            LOG.error("Error sending data to Kafka topic", e);
            return "Error sending data to Kafka topic: " + e.getMessage();
        }
    }
}