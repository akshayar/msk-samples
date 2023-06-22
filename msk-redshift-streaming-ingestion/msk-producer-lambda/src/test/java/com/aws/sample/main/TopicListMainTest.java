package com.aws.sample.main;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.Context;

class TopicListMainTest {
    private static final Logger logger = LoggerFactory.getLogger(TopicListMainTest.class);
    @Test
    void testHandler() {
        logger.info("Invoke TEST - Handler");
        Context context = new TestContext();
        //System.getenv().put("BOOTSTRAP_SERVER","localhost:9092");
        //System.getenv().put("REPLICATION_FACTOR","1");
        TopicListMain handler = new TopicListMain();
        assertNull(handler.handleRequest(context));
    }
}