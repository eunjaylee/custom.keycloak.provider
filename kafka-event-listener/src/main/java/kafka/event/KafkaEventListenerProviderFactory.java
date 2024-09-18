package kafka.event;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.quarkus.runtime.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {
    public static final String ID = "kafka-event-listener";

    public KafkaEventListener SINGLETON = null;

    public KafkaEventListenerProviderFactory() {
    }

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        if(SINGLETON == null) {
            String broker = Configuration.getOptionalValue("broker").orElse("");
            String topic = Configuration.getOptionalValue("topic").orElse("");
            SINGLETON = new KafkaEventListener(session, new Producer(topic, broker));
        }
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public int order() {
        return EventListenerProviderFactory.super.order();
    }
}
