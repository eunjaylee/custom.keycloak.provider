package kafka.event;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Producer {

    private String topic;

    private Properties properties;

    public Producer(String topic, String broker) {
        this.topic = topic;
        this.init(broker);
    }

    public void publishEvent(String value){
        //reset thread context
        resetThreadContext();
        // create the producer
        System.out.println("event : ------------" +value);
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        // create a producer record
        ProducerRecord<String, String> eventRecord = new ProducerRecord<String, String>(topic, value);

        // send data - asynchronous
        producer.send(eventRecord);

        // flush data
        producer.flush();
        // flush and close producer
        producer.close();
    }

    private void resetThreadContext() {
        Thread.currentThread().setContextClassLoader(null);
    }

    public void init(String broker) {
        properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker); //Configuration.getOptionalValue("kafkaServerConfig").orElse("")
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    }

}
