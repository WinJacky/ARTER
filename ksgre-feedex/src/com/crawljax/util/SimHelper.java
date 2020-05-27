package com.crawljax.util;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.Keyword;
import com.crawljax.dataType.PlainElement;
import main.java.nlp.word2vec.core.WordsSplit;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 与相似度计算的相关方法
 */
public class SimHelper {
    private static final Logger logger = Logger.getLogger(SimHelper.class);

    public static double getSimilarityOfSemantic(WebElement element, PlainElement targetElement, EmbeddedBrowser browser) {
        // 提取元素关键信息
        // 此处元素关键信息和关键词相比较
        Set<String> set = new HashSet<>();
        String temp = null;
        temp = getInnerTextOnce((JavascriptExecutor)browser.getWebDriver(), element);
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
        temp = targetElement.getText();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = targetElement.getId();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = targetElement.getNameAttribute();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = targetElement.getValueAttribute();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));
        temp = targetElement.getTitleAttribute();
        if (StringUtils.isNotBlank(temp)) set1.addAll(WordsSplit.getWords(Helper.removeNewLines(temp.trim())));

        return computeSimilarity(set, set1);
    }

    public static double getSimilarityOfStructure(WebElement srcElement, PlainElement tarElement, EmbeddedBrowser browser) {
//        String srcXpath = XPathHelper.getXPathExpression(srcElement);
        String srcXpath = getElementXPath(browser, srcElement);
        String tarXpath = tarElement.getXpath();
        // 统一xpath
        if (!srcXpath.startsWith("/")) srcXpath = "/" + srcXpath;
        if (!tarXpath.startsWith("/")) tarXpath = "/" + tarXpath;
        double pho1 = simOfXpath(srcXpath, tarXpath);
        double pho2 = 0.0;
        double divid = 6.0;
        String tmp;

        tmp = srcElement.getAttribute("id");
        if (tmp == null && tarElement.getId() == null) divid--;
        else if (tmp != null && tmp.equalsIgnoreCase(tarElement.getId())) pho2++;

        tmp = srcElement.getAttribute("class");
        if (tmp == null && tarElement.getClassAttribute() == null) divid--;
        else if (tmp != null && tmp.equalsIgnoreCase(tarElement.getClassAttribute())) pho2++;

        tmp = srcElement.getAttribute("name");
        if (tmp == null && tarElement.getNameAttribute() == null) divid--;
        else if (tmp != null && tmp.equalsIgnoreCase(tarElement.getNameAttribute())) pho2++;

        tmp = srcElement.getAttribute("type");
        if (tmp == null && tarElement.getTypeAttribute() == null) divid--;
        else if (tmp != null && tmp.equalsIgnoreCase(tarElement.getTypeAttribute())) pho2++;

        tmp = srcElement.getAttribute("value");
        if (tmp == null && tarElement.getValueAttribute() == null) divid--;
        else if (tmp != null && tmp.equalsIgnoreCase(tarElement.getValueAttribute())) pho2++;

        tmp = getInnerTextOnce((JavascriptExecutor)browser.getWebDriver(), srcElement);
        if (tmp == null && tarElement.getText() == null) divid--;
        else if (tmp != null && tmp.equalsIgnoreCase(tarElement.getText())) pho2++;

        if (divid <= 0.0) pho2 = 0;
        else pho2 = pho2 / divid;
        double alpha = 0.8;
        return (pho1 * alpha + (pho2) * (1 - alpha));
    }

    public static String getInnerTextOnce(JavascriptExecutor js, WebElement element) {
        String result = (String) js.executeScript("var getInnerTextOnce = function (element) {" +
                "if(element.childNodes.length > 0) {\n" +
                "            try {\n" +
                "                return element.childNodes[0].nodeValue.trim();\n" +
                "            } catch (e) {\n" +
                "                return \"\";\n" +
                "            }\n" +
                "\n" +
                "        }" +
                "};" +
                "return getInnerTextOnce(arguments[0]);", element);
        return result == null ? result : result.trim();
    }

    public static String getElementXPath(EmbeddedBrowser browser, WebElement element) {
        return (String) ((JavascriptExecutor)browser.getWebDriver()).executeScript(
                "var getElementXPath = function(element) {" + "return getElementTreeXPath(element);"
                        + "};" + "var getElementTreeXPath = function(element) {" + "var paths = [];"
                        + "for (; element && element.nodeType == 1; element = element.parentNode) {" + "var index = 0;"
                        + "for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {"
                        + "if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE) {" + "continue;" +"}"
                        + "if (sibling.nodeName == element.nodeName) {" + "++index;" + "}" + "}"
                        + "var tagName = element.nodeName.toLowerCase();"
                        + "var pathIndex = (\"[\" + (index+1) + \"]\");" + "paths.splice(0, 0, tagName + pathIndex);"
                        + "}" + "return paths.length ? \"/\" + paths.join(\"/\") : null;" + "};"
                        + "return getElementXPath(arguments[0]);", element
        );
    }

    private static double computeSimilarity(Set<String> set, Set<String> set1) {
        // TODO: 2020/1/15 相似度计算规则要重新定义
        double sumScore = 0.0;
        for (String s1: set) {
            double maxScore = 0.0;
            for (String s2: set1) {
                maxScore = Math.max(maxScore, CrawljaxController.word2Vec.getSimWith2Words(s1, s2));
            }
            sumScore += maxScore;
        }
        return sumScore / set.size();
    }

    // 计算xpath的编辑距离
    public static double simOfXpath(String xpath1, String xpath2) {
        // 计算编辑距离
        if (xpath1 != null && xpath2 != null) {
            int k;
            for (k=0; k<xpath1.length(); k++) {
                if (xpath1.charAt(k) != '/') break;
            }
            xpath1 = xpath1.substring(k);
            for (k=0; k<xpath2.length(); k++) {
                if (xpath2.charAt(k) != '/') break;
            }
            xpath2 = xpath2.substring(k);

            String str1[] = xpath1.split("/");
            String str2[] = xpath2.split("/");
            int n = str1.length;
            int m = str2.length;
            if (n == 0) {
                return m;
            } else if (m == 0) {
                return n;
            } else {
                if (n > m) {
                    String[] tmp = str1;
                    str1 = str2;
                    str2 = tmp;
                    n = m;
                    m = tmp.length;
                }

                int[] p = new int[n + 1];

                int i;
                for (i = 0; i <= n; p[i] = i++) {
                }

                for (int j = 1; j <= m; ++j) {
                    int upper_left = p[0];
                    String s_j = str2[j - 1];
                    p[0] = j;

                    for (i = 1; i <= n; ++i) {
                        int upper = p[i];
                        int cost = str1[i - 1].equalsIgnoreCase(s_j) ? 0 : 1;
                        p[i] = Math.min(Math.min(p[i - 1] + 1, p[i] + 1), upper_left + cost);
                        upper_left = upper;
                    }
                }

                return  1.0 - (double) p[n] / m;
            }
        } else {
            logger.error("Strings must not be null");
        }

        return 0.0;
    }
}
