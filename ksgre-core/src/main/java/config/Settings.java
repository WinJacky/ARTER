package main.java.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings {

    public Settings(AppEnum app) {
        this.appName = app.getAppName();
        this.testSuite = app.getTestSuite();
        this.referenceTestSuiteVisualTraceExecutionFolder = outputPath + sep + testSuite.substring(testSuite.lastIndexOf(".") + 1) + sep;
    }

    public String appName;
    public String testSuite;
    public String referenceTestSuiteVisualTraceExecutionFolder;

    public static String driverName = "firefox";  // firefox, chrome

    public static String sep = File.separator;

    public static String outputPath = "output";

    public static String resourcesFolder = "ksgre-core" + sep + "src" + sep + "main" + sep + "resources" + sep;

    /* Package name of the broken/regressed test suite. */
    public static String testSuiteBroken = "main.resources.PPMANew";

    /* Path to the test suite used as a reference. */
    public static String pathToReferenceTestSuite = resourcesFolder;

    /* Path to the test suite under test. */
    public static String pathToTestSuiteUnderTest = resourcesFolder;

    /* These tags will be ignored in the page html code. */
    // 排除在外的html元素
    public static String[] excludeTags = new String[]{"script", "style", "meta", "html", "head", "link"};

    /* These tags will be clicked to explore the layout hierarchy. */
    // 触发事件的html元素，重点关注
    public static String[] candiClickTags = new String[]{"a", "input", "button"};

    /* The name of the directory to save document of different layout hierarchies. */
    public static String DOMDirectoryName = "doms";

    /* The path of the folder that saves the js methods. */
    public static String JSDirPath = "/ksgre-core/src/main/js";

    /* The path of word2vec model */
    public static String Word2VecModelPath = "src/main/resources/Word2VecModel/wiki.en.text.vector.bin";

    /* File extensions. */
    public static final String HTML_EXT = ".html";
    public static final String JSON_EXT = ".json";
    public static final String JAVA_EXT = ".java";

    /* The page url to be extracted */
    public static String crawlPageUrl = "";

    /* Specify if AspectJ is active. */
    public static boolean aspectActive = true;

    /* Folder containing the visual execution trace of the test suite under test. */
    public static String testingTestSuiteVisualTraceExecutionFolder = outputPath + sep + testSuiteBroken.substring(testSuiteBroken.lastIndexOf(".") + 1) + sep;

    public static boolean VERBOSE = false;

    public static enum RepairMode {
        VISUAL, DOM, HYBRID
    }

    /* Tags that are used to show things. */
    public static List<String> contentTags = Arrays.asList("title", "header", "nav", "section", "aside", "footer", "h1", "h2",
                                                            "h3", "h4", "h5", "h6", "article", "address", "hgroup", "main",
                                                            "div", "p", "pre", "ol", "ul", "li", "figure", "figcaption",
                                                            "blockquote", "hr", "span", "strong", "b", "i", "u", "img",
                                                            "map", "area", "iframe", "source", "script", "table", "caption",
                                                            "thead", "tbody", "tfoot", "tr", "col", "colgroup", "th", "td",
                                                            "label", "legend", "output");

    /* Tags that are used to be interacted with users. */
    public static List<String> interTags = Arrays.asList("form", "input", "textarea", "button", "datalist", "fieldset",
                                                            "optgroup", "progress", "select");

    /* The tags to be ignored when save the tags' information of an html state. */
    public static List<String> excludeTages = Arrays.asList("script", "style", "meta", "html", "head", "link");

    public static final String[] ATTRIBUTES_WHITELIST = new String[]{"id", "name", "class", "title", "alt", "value"};

    /* Regex to match the English word in NLP. */
    public static  String PAT_ALPHBETIC = "(((?![\\d])\\w)+)";
}
