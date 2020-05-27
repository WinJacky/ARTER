package main.java.util;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.Keyword;
import com.crawljax.core.state.Identification;
import com.crawljax.dataType.PlainElement;
import com.crawljax.util.Helper;
import com.crawljax.util.SimHelper;
import main.java.config.Settings;
import main.java.dataType.EnhancedTestCase;
import main.java.dataType.SeleniumLocator;
import main.java.dataType.Statement;
import main.java.nlp.word2vec.core.WordsSplit;
import main.java.runner.RepaireRunner;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.How;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UtilsRepaire {
    private static Logger log = LoggerFactory.getLogger(UtilsRepaire.class);

    /**
     * clone an object
     * @param object
     * @return
     */
    public static Object deepClone(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bias = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bias);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("an error occurred when cloning an object");
            return null;
        }
    }

    public static WebElement retrieveWebElementFromDomLocator(EmbeddedBrowser browser, SeleniumLocator domSelector) throws NoSuchElementException {
        String strategy = domSelector.getStrategy();
        String locator = domSelector.getValue();
        WebElement element = null;

        if (strategy.equalsIgnoreCase("xpath")) {
            element = browser.getWebElement(new Identification(Identification.How.xpath, locator));
        } else if (strategy.equalsIgnoreCase("name")) {
            element = browser.getWebElement(new Identification(Identification.How.name, locator));
        } else if (strategy.equalsIgnoreCase("id")) {
            element = browser.getWebElement(new Identification(Identification.How.id, locator));
        } else if (strategy.equalsIgnoreCase("linkText")) {
            element = browser.getWebElement(new Identification(Identification.How.text, locator));
        } else if (strategy.equalsIgnoreCase("cssSelector")) {
            element = browser.getWebElement(new Identification(Identification.How.css, locator));
        }

        return element;
    }

    public static WebElement checkRepairedMap(Map<String, String> recordMap, String oldXpath, EmbeddedBrowser browser) {
        if (recordMap.keySet().contains(oldXpath)) {
            return browser.getWebElement(new Identification(Identification.How.xpath, oldXpath));
        }
        return null;
    }

    public static double checkElementByCollectedInfo(EmbeddedBrowser browser, WebElement element, Statement collectedInfo) {
        if (StringUtils.equalsIgnoreCase(element.getTagName(), collectedInfo.getTagName())) {
            String xpath1 = UtilsXPath.getElementXPath((JavascriptExecutor) browser.getWebDriver(), element);
            String xpath2 = collectedInfo.getXpath();
            if (!xpath1.startsWith("/")) xpath1 = "/" + xpath1;
            if (!xpath2.startsWith("/")) xpath2 = "/" + xpath2;
            double pho1 = (double) SimHelper.simOfXpath(xpath1, xpath2);
            double pho2 = 0.0;
            double divid = 6.0;
            String temp;
            temp = element.getAttribute("id");
            if (temp == null && collectedInfo.getId() == null) divid--;
            else if (temp != null && temp.equalsIgnoreCase(collectedInfo.getId())) pho2++;
            temp = element.getAttribute("class");
            if (temp == null && collectedInfo.getClassAttribute() == null) divid--;
            else if (temp != null && temp.equalsIgnoreCase(collectedInfo.getClassAttribute())) pho2++;
            temp = element.getAttribute("name");
            if (temp == null && collectedInfo.getNameAttribute() == null) divid--;
            else if (temp != null && temp.equalsIgnoreCase(collectedInfo.getNameAttribute())) pho2++;
            temp = element.getAttribute("type");
            if (temp == null && collectedInfo.getTypeAttribute() == null) divid--;
            else if (temp != null && temp.equalsIgnoreCase(collectedInfo.getTypeAttribute())) pho2++;
            temp = element.getAttribute("value");
            if (temp == null && collectedInfo.getValueAttribute() == null) divid--;
            else if (temp != null && temp.equalsIgnoreCase(collectedInfo.getValueAttribute())) pho2++;
            temp = (UtilsXPath.getInnerTextOnce((JavascriptExecutor) browser.getWebDriver(), element));
            if (temp == null && collectedInfo.getText() == null) divid--;
            else if (temp != null && temp.equalsIgnoreCase(collectedInfo.getText())) pho2++;
            if (divid <= 0) pho2 = 0;
            else pho2 = pho2 / 6.0;
            double alpha = 0.8;
            return (pho1 * alpha + (pho2) * (1 - alpha));
        }
        return 0.0;
    }

    public static double checkElementBySemantic(WebElement element, Statement statement) {
        // 提取元素关键信息
        // 此处元素关键信息和关键词相比较
        Set<String> set = new HashSet<>();
        String temp = null;
        temp = element.getText();
        if (StringUtils.isNotBlank(temp)) set.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = element.getAttribute("id");
        if (StringUtils.isNotBlank(temp)) set.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = element.getAttribute("name");
        if (StringUtils.isNotBlank(temp)) set.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = element.getAttribute("value");
        if (StringUtils.isNotBlank(temp)) set.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = element.getAttribute("title");
        if (StringUtils.isNotBlank(temp)) set.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));

        Set<String> set1 = new HashSet<>();
        temp = statement.getText();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = statement.getId();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = statement.getNameAttribute();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = statement.getValueAttribute();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = statement.getTitleAttribute();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));

        return computeSimilarity(set, set1);
    }

    private static double computeSimilarity(Set<String> set, Set<String> set1) {
        // TODO: 2020/1/15 相似度计算规则要重新定义
        double sumScore = 0.0;
        for (String s1: set) {
            double maxScore = 0.0;
            for (String s2: set1) {
                maxScore = Math.max(maxScore, RepaireRunner.word2Vec.getSimWith2Words(s1, s2));
            }
            sumScore += maxScore;
        }
        return sumScore / set.size();
    }

    public static void printTestCaseWithLineNumbers(EnhancedTestCase tc) {
        for (Integer i: tc.getStatements().keySet()) {
            System.out.println(tc.getStatements().get(i).getLine() + ":\t" + tc.getStatements().get(i) + ";");
        }
    }

    public static void cloneStatementToPlainElement(Statement statement, PlainElement plainElement) {
        if (statement.getSelect() != null) plainElement.setSelect(statement.getSelect());
        if (statement.getSeleniumAction() != null) plainElement.setSeleniumAction(statement.getSeleniumAction());
        if (statement.getValue() != null) plainElement.setValue(statement.getValue());
        if (statement.getName() != null) plainElement.setName(statement.getName());
        plainElement.setLine(statement.getLine());
        if (statement.getHtmlPage() != null) plainElement.setHtmlPage(statement.getHtmlPage());
        if (statement.getCoordinates() != null) plainElement.setCoordinates(statement.getCoordinates());
        if (statement.getDimension() != null) plainElement.setDimension(statement.getDimension());
        if (statement.getDomBefore() != null) plainElement.setDomBefore(statement.getDomBefore());
        if (statement.getDomAfter() != null) plainElement.setDomAfter(statement.getDomAfter());
        if (statement.getXpath() != null) plainElement.setXpath(statement.getXpath());
        if (statement.getTagName() != null) plainElement.setTagName(statement.getTagName());
        if (statement.getId() != null) plainElement.setId(statement.getId());
        if (statement.getClassAttribute() != null) plainElement.setClassAttribute(statement.getClassAttribute());
        if (statement.getNameAttribute() != null) plainElement.setNameAttribute(statement.getNameAttribute());
        if (statement.getText() != null) plainElement.setText(statement.getText());
        if (statement.getValueAttribute() != null) plainElement.setValueAttribute(statement.getValueAttribute());
        if (statement.getTypeAttribute() != null) plainElement.setTypeAttribute(statement.getTypeAttribute());
        if (statement.getTitleAttribute() != null) plainElement.setTitleAttribute(statement.getTitleAttribute());
    }

    /**
     * 将修复后的测试用例持久化，保存到java文件
     * @param prefix
     *          文件路径
     * @param className
     *          类名
     * @param temp
     *          修复后的测试语句
     */
    public static void saveTest(String prefix, String className, EnhancedTestCase temp) {
        String oldPath = Settings.resourcesFolder + prefix.replace(".", "\\") + className + Settings.JAVA_EXT;
        String newPath = Settings.resourcesFolder + "repaired\\" + prefix.replace(".", "\\") + className + Settings.JAVA_EXT;

        try {
            ParseTest.parseAndSaveToJava(temp, oldPath, newPath);
        } catch (IOException e) {
            log.error("an error occurred when saving the repaired test into java file");
            e.printStackTrace();
        }
    }
}
