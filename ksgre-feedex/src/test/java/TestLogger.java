package test.java;

import org.apache.log4j.Logger;

public class TestLogger {
    private static Logger logger = Logger.getLogger(TestCrawler.class);
    public static void main(String[] args) {
        logger.info("log is printing...");
    }
}
