package config;

import com.google.inject.AbstractModule;
import driver.EmbeddedMediaDriverProvider;
import messaging.AeronPublisher;
import messaging.AeronSubscriber;

public class AeronModule extends AbstractModule {
    @Override
    public void configure() {
        bind(EmbeddedMediaDriverProvider.class).asEagerSingleton();
        bind(AeronPublisher.class).asEagerSingleton();
        bind(AeronSubscriber.class).asEagerSingleton();
    }
}
