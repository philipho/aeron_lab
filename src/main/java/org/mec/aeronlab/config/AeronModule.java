package org.mec.aeronlab.config;

import com.google.inject.AbstractModule;
import org.mec.aeronlab.driver.EmbeddedMediaDriverProvider;
import org.mec.aeronlab.messaging.AeronPublisher;
import org.mec.aeronlab.messaging.AeronSubscriber;

public class AeronModule extends AbstractModule {
    @Override
    public void configure() {
        bind(EmbeddedMediaDriverProvider.class).asEagerSingleton();
        bind(AeronPublisher.class).asEagerSingleton();
        bind(AeronSubscriber.class).asEagerSingleton();
    }
}
