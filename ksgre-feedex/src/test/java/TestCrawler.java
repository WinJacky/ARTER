package test.java;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.browser.WebDriverBackedEmbeddedBrowser;
import com.crawljax.config.AppConfig;
import com.crawljax.core.*;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.core.oraclecomparator.comparators.PlainStructureComparator;
import com.crawljax.core.plugin.ScreenShotPlugin;
import com.crawljax.core.state.StateFlowGraph;
import com.crawljax.core.state.StateMachine;
import com.crawljax.forms.FormInput;
import com.crawljax.util.Helper;
import com.crawljax.util.HtmlNamespace;
import main.java.nlp.word2vec.core.Word2Vec;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import test.java.config.AppConfigFactory;
import test.java.config.AppEnum;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.List;

public class TestCrawler {
    private static Logger log = Logger.getLogger(TestCrawler.class);
    private static final int MAX_NUMBER_STATES = 30000;
    private static final int MAX_DEPTH = 100;
    private static AppConfig appConfig = null;
    private static AppEnum appEnum;

    private static CrawljaxController crawljaxController;

    private static long startTime;
    private static long stopTime;
    private static long elapsedTime;


    private static CrawljaxConfiguration getCrawljaxConfiguration(AppEnum appEnum) {
        CrawljaxConfiguration config = new CrawljaxConfiguration();
        CrawlSpecification specification = getCrawljaxSpecification(appEnum);
        config.setCrawlSpecification(specification);
        config.setThreadConfiguration(getThreadConfiguration());
        config.setBrowser(EmbeddedBrowser.BrowserType.firefox);
        config.addPlugin(new ScreenShotPlugin());
        config.addPlugin(appConfig.getDomChangedPlugin());
        return config;
    }

    private static ThreadConfiguration getThreadConfiguration() {
        ThreadConfiguration tc = new ThreadConfiguration();
        tc.setBrowserBooting(true);
        tc.setNumberBrowsers(1);
        tc.setNumberThreads(1);
        return tc;
    }

    private static CrawlSpecification getCrawljaxSpecification(AppEnum appEnum) {
        CrawlSpecification specification = new CrawlSpecification(appEnum.getUrl());
        // 这里设置的OracleComparator会用来初始化StateComparator，而StateComparator的作用是剥离DOM树
        specification.addOracleComparator("1", new PlainStructureComparator());
        specification.setMaximumStates(MAX_NUMBER_STATES);
        specification.setDepth(MAX_DEPTH);
        specification.setCrawlStrategy(CrawlStrategy.BFS);

        // 每个可点击事件是否仅点击一次
        specification.setClickOnce(true);

        // 表单输入
        appConfig.setInputSpecification(specification);
        // 设置可点击元素
        appConfig.setClick(specification);
        // 设置事件触发先后的关系约束
        appConfig.setPreClickConstraints(specification);
        // 配置阈值
        appConfig.setThreshold();

        return specification;
    }

    public static void main(String[] args) {

//        WebDriver driver = new FirefoxDriver();
//        driver.manage().window().maximize();
//        driver.get("http://phpshe.com/demo/phpshe");
//        driver.findElement(By.xpath("/html/body/div[1]/div/div[1]/a[1]")).click();
//
//        EmbeddedBrowser browser = new WebDriverBackedEmbeddedBrowser(driver);
//
//        Document dom;
//        try {
//            dom = Helper.getDocument(browser.getDomWithoutIframeContent());
//            XPath xp = XPathFactory.newInstance().newXPath();
//            xp.setNamespaceContext(new HtmlNamespace());
//
//            String xpath = "//INPUT[@name='user_name' or @id='user_name']";
//            WebElement element = driver.findElement(By.xpath(xpath));
//            Element element1 = (Element) xp.evaluate(xpath, dom, XPathConstants.NODE);
//            System.out.println(element.getTagName());
//        } catch (SAXException e) {
//            System.out.println(e);
//        } catch (IOException e) {
//            System.out.println(e);
//        } catch (XPathExpressionException e) {
//            System.out.println(e);
//        }

        // 1. 获取app配置
        appEnum = AppEnum.PHPSHE;
        appConfig = AppConfigFactory.getInstance(appEnum);
        // 2. 配置app默认配置
        CrawljaxConfiguration configuration = getCrawljaxConfiguration(appEnum);
        // 3. 启动CrawljaxController
        crawljaxController = new CrawljaxController(configuration);

        Crawler initialCrawler = new InitialCrawler(crawljaxController);

        //Crawler crawler = new Crawler(crawljaxController, null, "init", 0, null);
        crawljaxController.addWorkToQueue(initialCrawler);
    }
}
