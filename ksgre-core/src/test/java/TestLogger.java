package test.java;

import org.apache.log4j.Logger;

public class TestLogger {
    private static final Logger logger = Logger.getLogger(TestLogger.class);

    public static void main(String[] args) {
        logger.info("logger is print....");
    }
}
