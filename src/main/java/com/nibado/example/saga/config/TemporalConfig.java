package com.nibado.example.saga.config;

import com.nibado.example.saga.mock.CreditClient;
import com.nibado.example.saga.mock.StockClient;
import com.nibado.example.saga.workflow.ItemOrderActivitiesImpl;
import com.nibado.example.saga.workflow.ItemOrderWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class TemporalConfig {
    private static final String ITEM_ORDER_QUEUE = "ItemOrderQueue";
    private static final Logger log = LoggerFactory.getLogger(TemporalConfig.class);

    public static final WorkflowOptions ITEM_ORDER_OPTIONS = WorkflowOptions.newBuilder()
        .setWorkflowRunTimeout(Duration.ofSeconds(10))
        .setWorkflowTaskTimeout(Duration.ofSeconds(10))

        .setTaskQueue(ITEM_ORDER_QUEUE).build();

    @Bean
    public WorkflowClient client() {
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
            .build();

        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(options);
        return WorkflowClient.newInstance(service);
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient client) {
        return WorkerFactory.newInstance(client);
    }

    @Bean
    public Worker worker(WorkerFactory factory, CreditClient creditClient, StockClient stockClient) {
        var worker = factory.newWorker(ITEM_ORDER_QUEUE);
        worker.registerWorkflowImplementationTypes(ItemOrderWorkflowImpl.class);
        worker.registerActivitiesImplementations(new ItemOrderActivitiesImpl(creditClient, stockClient));

        factory.start();

        log.info("Worker Factory started");

        return worker;
    }
}
