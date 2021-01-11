package com.nibado.example.saga.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemOrderActivitiesImpl implements ItemOrderActivities {
    private static final Logger log = LoggerFactory.getLogger(ItemOrderActivitiesImpl.class);

    @Override
    public void debug() {
        log.info("Debug activity!");
    }
}
