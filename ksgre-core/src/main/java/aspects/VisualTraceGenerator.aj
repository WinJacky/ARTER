package main.java.aspects;

import main.java.config.Settings;
import main.java.dataType.KeyText;
import main.java.util.UtilsAspect;
import main.java.util.UtilsParser;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public aspect VisualTraceGenerator {

    private static Logger log = LoggerFactory.getLogger("VisualTraceGenerator.aj");

    static WebDriver d;
    static String testFolder;
    static String testCaseName;
    static String mainPage;

    /* Statement information. */
    static String statementName;
    static int line;

    /* DOM information. */
    static String htmlPath;
    static String domInfoJsonFile;

    /* Html all tags information. */
    static String htmlTagsInfoJsonFile;

    /* 关键词信息 */
    static List<KeyText> keyTexts;
    static String keyTextsInfoJsonFile;

    /* Intercept the calls to findElement methods. */
    pointcut logFindElementCalls() : call(* org.openqa.selenium.WebDriver.findElement(..));

    /* Intercept the executions of findElement methods. */
    pointcut catchFindElementExecutions() : execution(* org.openqa.selenium.WebDriver.findElement(..));

    /* Intercept the calls to WebElement methods. */
    pointcut logSeleniumCommands() : call(* org.openqa.selenium.WebElement.click()) ||
                                    call(* org.openqa.selenium.WebElement.sendKeys(..)) ||
                                    call(* org.openqa.selenium.WebElement.getText()) ||
                                    call(* org.openqa.selenium.WebElement.clear()) ||
                                    call(* org.openqa.selenium.support.ui.Select.selectByVisibleText(..)) ||
                                    call(* org.openqa.selenium.Alert.accept());

    /* 拦截JUnit框架测试用例中的tearDown()方法 */
    pointcut logTearDownCommands() : call(* org.openqa.selenium.WebDriver.quit());

    /* Create output folders before calling the method. */
    // 在执行命令之前进行初始化工作
    before() : logFindElementCalls() {
        if (Settings.aspectActive) {
            log.info("execute logFindElementCalls...");

            d = (WebDriver) thisJoinPoint.getTarget();

            String withinType = thisJoinPoint.getStaticPart().getSourceLocation().getWithinType().toString();
            String testSuiteName = UtilsParser.getTestSuiteNameFromWithinType(withinType);
            UtilsAspect.createTestFolder(Settings.outputPath + Settings.sep + testSuiteName);
            testFolder = Settings.outputPath + Settings.sep + testSuiteName + Settings.sep
                            + thisJoinPoint.getStaticPart().getSourceLocation().getFileName().replace(Settings.JAVA_EXT, "");
            testCaseName = UtilsAspect.getTestCaseNameFromJoinPoint(thisJoinPoint);

            UtilsAspect.createTestFolder(testFolder);
        }
    }

    /**
     * Save DOM and visual information before executing the method.
     */
    before() : logSeleniumCommands() {
        if(Settings.aspectActive) {
            WebElement we = null;
            Select sel = null;

            if(thisJoinPoint.getTarget() instanceof WebElement) {
                we = (WebElement) thisJoinPoint.getTarget();
            } else if(thisJoinPoint.getTarget() instanceof Select) {
                sel = (Select) thisJoinPoint.getTarget();
                we = (WebElement) sel.getOptions().get(0);
            } else if (thisJoinPoint.getTarget() instanceof Alert) {
                we = null;
            }


            // 获取当前测试语句及其所在行号
            statementName = UtilsAspect.getStatementNameFromJoinPoint(thisJoinPoint);
            line = UtilsAspect.getStatementLineFromJoinPoint(thisJoinPoint);
            // 获取保存目标元素DOM信息
            domInfoJsonFile = testFolder + Settings.sep + line + "-domInfo-" + testCaseName + Settings.JSON_EXT;
            UtilsAspect.saveDOMInformation(d, we, domInfoJsonFile);
            /*
            // 获取并保存当前状态的各类元素信息
            mainPage = d.getWindowHandle();
            htmlTagsInfoJsonFile = testFolder + Settings.sep + line + "-taginfo-" + testCaseName + Settings.JSON_EXT;
            UtilsAspect.saveHTMLTagsInformation(d, htmlTagsInfoJsonFile);
            */

            String methodName = thisJoinPoint.getSignature().getName();
            String type = null;
            if (methodName.equals("sendKeys")) {
                return;
            } else {
                if (methodName.equals("click")) {
                    String typeAttribute = we.getAttribute("type");
                    if (typeAttribute != null && typeAttribute.equalsIgnoreCase("submit")) {
                        type = "submit";
                    } else {
                        type = "click";
                    }
                }
            }
            // 获取并保存当前元素的text内容，仅限click事件的目标元素
            String textContent = UtilsAspect.getKeyTextFromJointPoint(thisJoinPoint);
            if (textContent!=null && !textContent.equals("")) {
                if (keyTexts == null) {
                    keyTexts = new ArrayList<>();
                }
                keyTexts.add(new KeyText(line, textContent, type));
            }

            log.info("@Before " + statementName);
        }
    }

    before() : logTearDownCommands() {
        if (keyTexts!=null && keyTexts.size()!=0) {
            keyTextsInfoJsonFile = testFolder + "-keyTextsListInfo-" + testCaseName + Settings.JSON_EXT;
            UtilsAspect.saveKeyTextsInformation(keyTexts, keyTextsInfoJsonFile);
        }
    }
}
