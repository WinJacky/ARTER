package main.java.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import japa.parser.ast.stmt.Statement;
import main.java.config.Settings;
import main.java.dataType.EnhancedException;
import main.java.dataType.EnhancedTestCase;
import main.java.dataType.SeleniumLocator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.notification.Failure;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class refers to the class int the visualrepair.
 */
public class UtilsParser {

    private static Logger log = LoggerFactory.getLogger(UtilsParser.class);

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Save the current html code.
     * @param d
     * @param path
     * @param fileName
     * @return
     */
    public static File saveHTMLPage(WebDriver d, String path, String fileName) {

        File savedHTML = new File(path + Settings.sep + fileName + Settings.HTML_EXT);

        String html = d.getPageSource();

        try {
            PrintStream ps = new PrintStream(new FileOutputStream(savedHTML));
            ps.println(html);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return savedHTML;
    }

    public static String getExceptionFromFailure(Failure f) {

        String s = null;

        try {
            s = f.getException().toString().substring(0, f.getException().toString().indexOf("For documentation", 0));
        } catch (StringIndexOutOfBoundsException e) {
            log.error("Exception not supported by the current implementation");
            log.error(f.getMessage());
            System.exit(1);
        }

        return s;
    }

    public static String getFailedTestFromFailure(Failure f) {

        String s = f.getTestHeader().substring(0, f.getTestHeader().indexOf("("));

        return s;
    }

    public static String getMessageFromFailure(Failure f) {
        String s;

        if (f.getMessage().contains("Cannot locate element with text:")) {
            s = f.getMessage().toString().substring(0, f.getException().toString().indexOf("For documentation", 0));
            s = s.substring(0, s.indexOf("For documentation"));
        } else {
            s = f.getMessage().toString().substring(0, f.getMessage().toString().indexOf("Command"));
        }

        return s;
    }

    public static String getLineFromfailure(Failure f) {

        String s = f.getTrace();
        int begin = s.indexOf(getFailedTestFromFailure(f), 0);
        s = s.substring(begin, s.indexOf(System.getProperty("line.separator"), begin));

        return s.replaceAll("\\D+", "");
    }

    public static void serializeException(EnhancedException ex, String path) {

        try {
            FileUtils.write(new File(path), gson.toJson(ex, EnhancedException.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Exception saved: " + path);
    }

    public static String getTestSuiteNameFromWithinType(String withinType) {

        if (withinType.contains("main.java")) {
            withinType = withinType.replaceAll("class ", "");
            withinType = withinType.replaceAll("main.java.", "");
        } else if(withinType.contains("main.resources")) {
            withinType = withinType.replaceAll("class ", "");
            withinType = withinType.replaceAll("main.resources.", "");
        } else {
            withinType = withinType.replaceAll("class ", "");
        }

        withinType = withinType.substring(0, withinType.indexOf("."));
        return withinType;
    }

    public static SeleniumLocator extractSeleniumLocatorFromWebElement(WebElement webElement) {

        String res = webElement.toString();
        res = res.substring(res.indexOf("-> ") + 3, res.length());
        res = res.substring(0, res.length() - 1);
        String strategy = res.split(":")[0].trim();
        String value = res.split(":")[1].trim();
        value = value.replaceAll("\"", "").trim();
        return new SeleniumLocator(strategy, value);
    }

    /**
     * Save the test case in JSON format.
     *
     * @param tc
     * @param path
     * @param folder
     */
    public static void serializeTestCase(EnhancedTestCase tc, String path, String folder) {

        int lastSlash = path.lastIndexOf("\\");
        int end = path.indexOf(".java");
        String testName = path.substring(lastSlash + 1, end);
        String newPath = folder + testName + Settings.sep + testName + Settings.JSON_EXT;

        try {
            FileUtils.write(new File(newPath), gson.toJson(tc));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Get class name from path e.g. src/PPMA/TC5.java
     * => TC5
     *
     * @param arg
     * @return
     * @throws Exception
     */
    public static String getClassNameFromPath(String arg) throws Exception {

        if (arg.length() == 0 || arg.isEmpty() || !arg.contains(Settings.JAVA_EXT)) {
            throw new Exception("[ERR]\tmalformed classname path");
        }

        return arg.substring(arg.lastIndexOf("\\") + 1).replace(Settings.JAVA_EXT, "");
    }

    /**
     * Auxiliary method to get the value for the get statement (i.e., the URL).
     *
     * @param s
     * @return
     * @throws Exception
     */
    public static String getUrlFromDriverGet(String s) throws Exception {

        if (s.length() == 0 || s.isEmpty() || !s.contains("\"")) {
            throw new Exception("[ERR]\tdriver get statement malformed");
        }

        String[] valuesInQuotes = StringUtils.substringsBetween(s, "\"", "\"");

        if (valuesInQuotes.length != 1) {
            throw new Exception("[ERR]\tdriver get statement malformed");
        }

        return valuesInQuotes[0];
    }

    /**
     * Auxiliary method to extract the DOM locator used by the web element.
     *
     * @param s
     * @return
     * @throws Exception
     */
    public static SeleniumLocator getDomLocator(String s) throws Exception {

        if (s.length() == 0 || s.isEmpty() || !s.contains("driver.findElement")) {
            throw new Exception("[ERR]\tdriver findElement statement malformed");
        }

        String[] valuesInQuotes = StringUtils.substringsBetween(s, "By.", ")");

        if (valuesInQuotes.length != 1) {
            throw new Exception("[ERR]\tdriver findElement statement malformed");
        }

        String[] splitted = StringUtils.split(valuesInQuotes[0], "(");

        String strategy = splitted[0].trim();
        String value = splitted[1].replaceAll("\"", "").trim();

        return new SeleniumLocator(strategy, value);
    }

    /**
     * Auxiliary method to get the value for the sendKeys statement.
     *
     * @param s
     * @return
     * @throws Exception
     */
    public static String getValueFromSendKeys(String s) throws Exception {

        if (s.length() == 0 || s.isEmpty() || !s.contains("sendKeys")) {
            throw new Exception("[ERR]\tsendKeys statement malformed");
        }

        String[] valuesInQuotes = StringUtils.substringsBetween(s, "sendKeys(\"", "\"");

        if (valuesInQuotes.length != 1) {
            throw new Exception("[ERR]\\tsendKeys statement malformed");
        }

        return valuesInQuotes[0];
    }

    public static String getValueFromSelect(Statement st) {

        String value = st.toString(); // new
        // Select(driver.findElement(By.id("course_category"))).selectByVisibleText("(SC)
        // Sciences");
        value = value.substring(value.indexOf("selectBy"), value.length()); // selectByVisibleText("(SC)
        // Sciences");
        value = value.substring(value.indexOf("(") + 1, value.indexOf("\");") + 1); // (SC)
        // Sciences
        return value;
    }

    public static String getAssertion(Statement st) {

        // assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
        // Doe"));
        String a = st.toString();
        int begin = a.indexOf("assert");
        int end = a.indexOf("(", begin);
        a = a.substring(begin, end);
        return a;
    }

    /**
     * Return the predicate used in the assertion.
     *
     * @param st
     * @return
     */
    public static String getPredicate(Statement st) {

        // assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
        // Doe"));
        if (st.toString().contains("assert") && st.toString().contains("getText()")) {

            String a = st.toString();
            int begin = a.indexOf("getText().");
            a = a.substring(begin + "getText().".length(), a.length() - 2);
            a = a.substring(0, a.indexOf("("));
            return a;
        } else if (st.toString().contains("assert") && st.toString().contains("closeAlertAndGetItsText")) {

            String a = st.toString();
            int begin = a.indexOf("(\"");
            int end = a.lastIndexOf("\",");
            a = a.substring(begin, end);
            return a;
        }
        else {
            System.err.println("[WARNING]\tUnable to extract predicate from assertion");
            return "";
        }

    }

    /**
     * Return the value used in the assertion.
     *
     * @param st
     * @return
     */
    public static String getValueFromAssertion(Statement st) {

        // assertTrue(driver.findElement(By.xpath("//*[@class='userName']")).getText().contains("John
        // Doe"));
        if (st.toString().contains("assert") && st.toString().contains("getText()")) {

            String a = st.toString();
            int begin = a.indexOf("getText().");
            a = a.substring(begin + "getText().".length(), a.length() - 2);
            a = a.substring(a.indexOf("("), a.length());
            a = a.replaceAll("\"", "");
            a = a.replaceAll("\\(", "");
            a = a.replaceAll("\\)", "");
            a = a.trim();
            return a;
        } else if (st.toString().contains("assert") && st.toString().contains("closeAlertAndGetItsText")) {

            String a = st.toString();
            int begin = a.indexOf("(\"");
            int end = a.lastIndexOf("\",");
            a = a.substring(begin, end);
            return a;
        } else {
            System.err.println("[WARNING]\tUnable to extract predicate from assertion");
            return "";
        }

    }

    public static void sanityCheck(EnhancedTestCase etc, EnhancedTestCase testCorrect) {

        Map<Integer, main.java.dataType.Statement> a = etc.getStatements();
        Map<Integer, main.java.dataType.Statement> b = testCorrect.getStatements();

        ArrayList<Integer> list1 = new ArrayList<Integer>(a.keySet());
        ArrayList<Integer> list2 = new ArrayList<Integer>(b.keySet());

        if (list1.get(0) != list2.get(0)) {
            log.error("Tests numbering is not aligned: " + list1.get(0) + "!=" + list2.get(0));
            throw new NullPointerException();
        }

    }

    public static String getPackageName(String newclazz) {
        return "main.resources.repaired.MIAOSHA";
    }
}
