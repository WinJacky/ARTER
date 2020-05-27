package main.java.runner;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TracerRunner {

    private static Logger log = LoggerFactory.getLogger(TracerRunner.class);

    static long startTime;
    static long stopTime;
    static long elapsedTime;

    public static Result runTestSuite(Class<?> testSuite) {
        Result result = JUnitCore.runClasses(testSuite);
        return result;
    }

    /**
     * Run a single JUnit test case or an entire test suite if a runner class is
     * specified.
     *
     * @param testSuite
     * @param testCase
     */
    public static void runTest(String testSuite, String testCase) {

        /* Build the class runner. */
        String testCaseToRun = testSuite + "." + testCase;

        /* Runn the test programmatically. */
        Result result = null;
        try {
            log.info("Running test " + testCaseToRun);
            startTime = System.currentTimeMillis();

            result = JUnitCore.runClasses(Class.forName(testCaseToRun));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        /* If the test failed, save the exception. */
        if (!result.wasSuccessful()) {

            log.info("Test " + testCaseToRun + " failed, saving the exception.");

            /* For each breakage, save the exception on the filesystem. */
            for (Failure fail: result.getFailures()) {
                log.error(String.valueOf(fail));

//                EnhancedException ea = UtilsRepair.saveExceptionFromFailure(fail);
//                String path = Settings.testingTestSuiteVisualTraceExecutionFolder
//                                + UtilsRepair.capitalizeFirstLetter(ea.getFailedTest()) + Settings.JAVA_EXT;
//                String jsonPath = UtilsRepair.toJsonPath(path);
//                UtilsParser.serializeException(ea, jsonPath);
            }
        } else {
            log.info("Test " + testCaseToRun + " passed");
        }

        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        log.info("Visual trace collected in {} s", String.format("%.3f", elapsedTime / 1000.0f));
    }
}
