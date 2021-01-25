package com.nibado.example.saga.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TopicFactoryTest {
    private TopicFactory factory;

    @BeforeEach
    void setup() {
        factory = new TopicFactory();
    }

    @Test
    void topicShouldProcessMessages() {
        var topicA = factory.getTopic("a");
        var topicB = factory.getTopic("b");

        var dataA = new byte[] {1};
        var dataB = new byte[] {2};

        var counterA = new AtomicInteger();
        var counterB = new AtomicInteger();

        factory.subScribe("a", m -> {
            counterA.incrementAndGet();
            assertThat(m.data()).containsExactly(1);
        });
        factory.subScribe("a", m -> {
            counterA.incrementAndGet();
            assertThat(m.data()).containsExactly(1);
        });
        factory.subScribe("b", m -> {
            counterB.incrementAndGet();
            assertThat(m.data()).containsExactly(2);
        });

        topicA.write("1", dataA);
        topicA.write("2", dataA);
        topicB.write("3", dataB);
        topicB.write("4", dataB);

        factory.process();

        assertThat(counterA).hasValue(2);
        assertThat(counterB).hasValue(1);

        for(int i = 0;i< 10;i++) {
            factory.process();
        }

        assertThat(counterA).hasValue(4);
        assertThat(counterB).hasValue(2);
    }
}
