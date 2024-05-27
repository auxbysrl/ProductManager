package com.auxby.productmanager.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    private final OfferScheduler offerScheduler;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Start performing tasks.");
        try {
            offerScheduler.triggerTaskOnDemand();
        } catch (Exception e) {
            log.info("Failed to perform tasks on startup:" + e.getLocalizedMessage());
        }
        log.info("Finished performing tasks.");
    }
}
