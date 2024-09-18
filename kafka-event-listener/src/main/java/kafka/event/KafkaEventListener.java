package kafka.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.keycloak.Config;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaEventListener implements EventListenerProvider {

    private final KeycloakSession session;
    private final ObjectMapper mapper = new ObjectMapper();

    Producer producer;

    public KafkaEventListener(KeycloakSession session, Producer producer) {
        this.session = session;
        this.producer = producer;
    }

    @Override
    public void onEvent(Event event) {
        String json = null;
        try {
            json = mapper.writeValueAsString(event);
            this.producer.publishEvent(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        String json = null;
        try {
            json = mapper.writeValueAsString(adminEvent);
            this.producer.publishEvent(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }
}
