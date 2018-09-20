package demo;

import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import demo.test.TodoEventProducer;

import org.junit.Test;
//import demo.Application.test.TodoEventProducer;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = test.class)
public class testT {

	private static final Logger LOGGER = LoggerFactory.getLogger(testT.class);

    @Autowired
    TodoEventProducer todoEventProducer;

    @Test
    public void createEvent() throws InterruptedException {

        LOGGER.info("Publishing event...");
        todoEventProducer.create("foo");
        todoEventProducer.create("foo2");

        // A chance to see the logging message produced by LoggingErrorHandler before the JVM exists.
        Thread.sleep(1000);

        LOGGER.info("Finished publishing event");
    }
}
