package com.nibado.example.saga.config;

import com.nibado.example.saga.mock.AsyncService;
import com.nibado.example.saga.mock.CreditClient;
import com.nibado.example.saga.mock.StockClient;
import com.nibado.example.saga.workflow.ItemOrderActivitiesAsyncImpl;
import com.nibado.example.saga.workflow.ItemOrderActivitiesImpl;
import com.nibado.example.saga.workflow.ItemOrderWorkflowAsyncImpl;
import com.nibado.example.saga.workflow.ItemOrderWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class TemporalConfig {
    private static final String ITEM_ORDER_QUEUE = "ItemOrderQueue";
    private static final String ITEM_ORDER_ASYNC_QUEUE = "ItemOrderQueueAsync";
    private static final Logger log = LoggerFactory.getLogger(TemporalConfig.class);

    public static final WorkflowOptions ITEM_ORDER_OPTIONS = WorkflowOptions.newBuilder()
        .setWorkflowRunTimeout(Duration.ofSeconds(10))
        .setWorkflowTaskTimeout(Duration.ofSeconds(10))

        .setTaskQueue(ITEM_ORDER_QUEUE).build();

    public static final WorkflowOptions ITEM_ORDER_OPTIONS_ASYNC = WorkflowOptions.newBuilder()
        .setWorkflowRunTimeout(Duration.ofSeconds(10))
        .setWorkflowTaskTimeout(Duration.ofSeconds(10))

        .setTaskQueue(ITEM_ORDER_ASYNC_QUEUE).build();

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

    @Bean("workerSync")
    public DisposableBean syncWorker(
        WorkflowClient client,
        CreditClient creditClient,
        StockClient stockClient) {
        var factory = WorkerFactory.newInstance(client);
        var worker = factory.newWorker(ITEM_ORDER_QUEUE);

        worker.registerWorkflowImplementationTypes(ItemOrderWorkflowImpl.class);
        worker.registerActivitiesImplementations(new ItemOrderActivitiesImpl(creditClient, stockClient));

        factory.start();

        log.info("Sync Worker Factory started");

        return () -> {
            log.info("Shutting down Temporal Sync WorkerFactory.");
            factory.shutdown();
            factory.awaitTermination(10, TimeUnit.SECONDS);
        };
    }

    @Bean("workerAsync")
    public DisposableBean asyncWorker(
        AsyncService asyncService,
        WorkflowClient client) {
        var factory = WorkerFactory.newInstance(client);
        var worker = factory.newWorker(ITEM_ORDER_ASYNC_QUEUE);

        worker.registerWorkflowImplementationTypes(ItemOrderWorkflowAsyncImpl.class);
        worker.registerActivitiesImplementations(new ItemOrderActivitiesAsyncImpl(client.newActivityCompletionClient(), asyncService));

        factory.start();

        log.info("Async Worker Factory started");

        return () -> {
            log.info("Shutting down Temporal Async WorkerFactory.");
            factory.shutdown();
            factory.awaitTermination(10, TimeUnit.SECONDS);
        };
    }
}
