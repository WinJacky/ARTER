package test.java;

import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class TestXpath {
    public static void main(String[] args) throws IOException, SAXException, XPathExpressionException {
        WebDriver driver = new FirefoxDriver();
        driver.get("http://localhost:8080/miaosha/login");
        Document document = Helper.getDocument(driver.getPageSource());
        NodeList nodeList = XPathHelper.evaluateXpathExpression(document, "html/body/div/div[2]/button".toUpperCase());
        System.out.println(nodeList.getLength());
        Element element = (Element) nodeList.item(0);
    }
}
