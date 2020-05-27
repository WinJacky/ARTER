package main.java.util;

import main.java.config.Settings;
import main.java.dataType.DOMInformation;
import main.java.dataType.KeyText;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static main.java.util.UtilsParser.gson;

public class UtilsAspect {

    private static Logger log = LoggerFactory.getLogger(UtilsAspect.class);

    /**
     * Create a directory in the project workspace.
     *
     */
    public static void createTestFolder(String path) {

        File theDir = new File(path);
        if (!theDir.exists()) {

            if(Settings.VERBOSE)
                log.info("creating directory " + path + "...");

            boolean result = theDir.mkdirs();
            if (result) {
                if (Settings.VERBOSE)
                    log.info("done");
            } else {
                if(Settings.VERBOSE)
                    log.info("failed");
                System.exit(1);
            }
        }
    }

    /**
     * Return an identifier for the statement in the form <testname>-<line> from a joinPoint of type WebElement.
     *
     * @param joinPoint
     * @return String
     */
    public static String getStatementNameFromJoinPoint(JoinPoint joinPoint) {
        String name = "";
        name = joinPoint.getStaticPart().toString();

        return name;
    }


    /**
     * 返回测试用例类名
     * @param joinPoint
     * @return String
     */
    public static String getTestCaseNameFromJoinPoint(JoinPoint joinPoint) {
        String name = "";
        name = joinPoint.getStaticPart().getSourceLocation().getFileName().replace(".java", "");
        return name;
    }

    /**
     * Return the statement line from a joinPoint of type WebElement
     *
     * @param joinPoint
     * @return int
     */
    public static int getStatementLineFromJoinPoint(JoinPoint joinPoint) {
        return joinPoint.getStaticPart().getSourceLocation().getLine();
    }

    /**
     * Save pertinent DOM information for the given web element and stores them in JSON file.
     *
     * @param d
     * @param we
     * @param domInfoJsonFile
     */
    public static void saveDOMInformation(WebDriver d, WebElement we, String domInfoJsonFile) {
        JavascriptExecutor js = (JavascriptExecutor) d;
        DOMInformation webElementWithDomInfo = new DOMInformation(js, we);

        try {
            FileUtils.writeStringToFile(new File(domInfoJsonFile), gson.toJson(webElementWithDomInfo, DOMInformation.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save pertinent html tags information for the given web element and stores them in JSON file.
     *
     * @param d
     * @param htmlTagsInfoJsonFile
     */
    public static void saveHTMLTagsInformation(WebDriver d, String htmlTagsInfoJsonFile) {
        JavascriptExecutor js = (JavascriptExecutor) d;
        WebElement root = d.findElement(By.tagName("body"));

        String JSCode = UtilsFile.readGetTagsInfoJS(Arrays.asList("GetTagsInformation", "ExcludeTags", "TagsInformation"));
        Object tagsInformationInJSON = js.executeScript(JSCode + "return getTagsInformation(arguments[0]);", root);

        try {
            FileUtils.writeStringToFile(new File(htmlTagsInfoJsonFile), String.valueOf(gson.toJsonTree(tagsInformationInJSON)));
            log.info("run...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 提取目标元素包裹的text内容，作为关键词进行保存
     * @param joinPoint
     * @return
     */
    public static String getKeyTextFromJointPoint(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String textualContent = "";
        // 如果是点击事件
        // 则可能是跳转、弹出事件（对话框、表单、目录等）、表单提交
        // 点击事件是关键事件
        if (methodName.equals("click")) {
            WebElement we = (WebElement) joinPoint.getTarget();
            textualContent = we.getAttribute("textContent");
            if (textualContent == null || textualContent.trim().equals("")) {
                textualContent = we.getAttribute("title");
                if (textualContent == null || textualContent.trim().equals("")) {
                    textualContent = we.getAttribute("name");
                    if (textualContent == null || textualContent.trim().equals("")) {
                        textualContent = we.getAttribute("value");
                    }
                }
            }
        }

        return textualContent == null ? "" : textualContent.trim();
    }

    public static void saveKeyTextsInformation(List<KeyText> keyTextsList, String keyTextsInfoJsonFile) {
        try {
            FileUtils.writeStringToFile(new File(keyTextsInfoJsonFile), gson.toJson(keyTextsList, List.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
