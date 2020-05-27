package main.java.util;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * This class refers to the same class in visualrepair.
 */
public class UtilsXPath {

    /**
     * Given an HTML element, retrieve its XPath.
     *
     * @param js
     *      Selenium JavaScriptExecutor object to execute javascript.
     * @param element
     *      Selenium WebElement corresponding to the HTML element.
     * @return XPath of the given element.
     */
    public static String getElementXPath(JavascriptExecutor js, WebElement element) {
        return (String) js.executeScript(
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
}
