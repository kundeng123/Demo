package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import demo.test.TodoEventProducer;



public class Application {
	private static final Logger LOGGER = LoggerFactory.getLogger(testT.class);

    @Autowired
	public static TodoEventProducer todoEventProducer;
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		
		LOGGER.info("Publishing event...");
        todoEventProducer.create("foo");
        todoEventProducer.create("foo2");

        // A chance to see the logging message produced by LoggingErrorHandler before the JVM exists.
        Thread.sleep(1000);

        LOGGER.info("Finished publishing event");
	}

}
