package main.java.runner;

import com.alibaba.fastjson.JSONObject;
import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.config.AppConfig;
import com.crawljax.core.*;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.core.oraclecomparator.comparators.PlainStructureComparator;
import com.crawljax.core.plugin.ScreenShotPlugin;
import com.crawljax.core.state.*;
import com.crawljax.dataType.PlainElement;
import com.crawljax.forms.FormInput;
import com.crawljax.forms.InputValue;
import com.crawljax.util.Helper;
import com.crawljax.util.XPathHelper;
import main.java.config.AppConfigFactory;
import main.java.config.AppEnum;
import main.java.config.Settings;
import main.java.dataType.*;
import main.java.nlp.word2vec.core.Word2Vec;
import main.java.nlp.word2vec.core.WordsSplit;
import main.java.nlp.word2vec.domain.WordEntry;
import main.java.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.Select;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class RepaireRunner {
    private static Logger log = Logger.getLogger(RepaireRunner.class);
    private static final int MAX_NUMBER_STATES = 30000;
    private static final int MAX_DEPTH = 100;
    private static AppConfig appConfig = null;
    private static AppEnum appEnum;

    private static CrawljaxController crawljaxController;

    private static long startTime;
    private static long stopTime;
    private static long elapsedTime;

    // SFG info
    private static StateFlowGraph sfg = null;
    private static StateMachine stateMachine = null;
    private static CrawlSession crawlSession = null;
    private static StateVertix indexState = null;
    private Crawler crawler;

    // nlp
    private static List<Keyword> keywordList = new ArrayList<>();
    public static Word2Vec word2Vec = null;
    public static Word2Vec word2Vec1 = null;

    // 修复的语句个数
    private static int repairedStatNum;

    private static EmbeddedBrowser browser;

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
        specification.setCrawlStrategy(CrawlStrategy.KSG);

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

    public static void main(String[] args) throws IOException {
        RepaireRunner repaireRunner = new RepaireRunner();
        // 待测用例的类名
        String testcaseName = "TC1";
        repairedStatNum = 0;

        // 1. 获取app配置
        appEnum = AppEnum.MIAOSHA;
        appConfig = AppConfigFactory.getInstance(appEnum);
        // 2. 配置app默认配置
        CrawljaxConfiguration configuration = getCrawljaxConfiguration(appEnum);
        // 3. 启动CrawljaxController
        crawljaxController = new CrawljaxController(configuration);
        // 4. 读取Word2Vec模型
        word2Vec = new Word2Vec("ksgre-core/src/main/resources/Word2VecModel/hanlp-wiki-vec-zh.txt");
        word2Vec.loadModel();
        CrawljaxController.word2Vec = word2Vec;
        // 5. 读取测试用例关键词序列，分词、扩词
        List<KeyText> keyTextList = readKeyText(appEnum, testcaseName);
        for (KeyText text: keyTextList) {
            Keyword temp = new Keyword(text.getLineNumber(), WordsSplit.getWords(text.getText()));
            temp.addToExtendedSeq(extendKeywords(temp.getKeywords(), 5));
            keywordList.add(temp);
        }
        CrawljaxController.keywords = keywordList;
        for (int i=0; i<keywordList.size(); i++) {
            Keyword temp = keywordList.get(i);
            System.out.println("----------");
            System.out.println("--行号：" + temp.getLineNumber());
            System.out.println("--关键词：" + temp.getKeywords());
            System.out.println("--扩展关键词: " + temp.getExtendedSeq());
            System.out.println("----------");
        }
        // 6. 启动修复
        repaireRunner.run(testcaseName);
    }

    public static List<KeyText> readKeyText(AppEnum appEnum, String testcaseName) {
        String appName = appEnum.getAppName();
        String fileName = Settings.outputPath + Settings.sep + appName + Settings.sep + testcaseName
                            + "-keyTextsListInfo-" + testcaseName + ".json";
        File file = new File(fileName);
        List<KeyText> result = null;
        try {
            String content = FileUtils.readFileToString(file, "UTF-8");
            result = JSONObject.parseArray(content, KeyText.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // 扩词
    public static List<String> extendKeywords(List<String> words, int number) {
        List<String> keywordSeq = new ArrayList<>();
        for (String str : words) {
            Set<WordEntry> set = word2Vec.getTopNSimilarWords(str, number);
            for (WordEntry entry : set) {
                keywordSeq.add(entry.name);
            }
            keywordSeq.add(str);
        }
        return keywordSeq;
    }

    public void run(String testcaseName) {
        startTime = System.currentTimeMillis();
        try {
            startRepair(testcaseName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        stopTime = System.currentTimeMillis();
        elapsedTime = stopTime - startTime;
        System.out.format("%.3f s", elapsedTime / 1000.0f);
    }

    private void startRepair(String testcaseName) throws IOException {
        String pathToTestBroken = UtilsFileGetters.getTestFile(testcaseName, ("ksgre-core.src." + appEnum.getTestSuite()).replaceAll("\\.", "\\\\"));

        log.info("Verifying test " + appEnum.getTestSuite() + " " + testcaseName);

        Class<?> clazz = null;
        Object inst = null;

        try {
            clazz = Class.forName(appEnum.getTestSuite() + "." + testcaseName);
            inst = clazz.newInstance();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InstantiationException e3) {
            e3.printStackTrace();
        }

        // 运行测试用例中
        try {
            browser = crawljaxController.getBrowserPool().requestBrowser();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // parse the tests
        ParseTest pt = null;
        EnhancedTestCase testBroken = null;
        EnhancedTestCase testCorrect = null;

        try {
            pt = new ParseTest(Settings.outputPath + Settings.sep + appEnum.getAppName());
            testBroken = pt.parseAndSerialize(pathToTestBroken);
            testCorrect = pt.parseAndSerialize(UtilsFileGetters.getTestFile(testcaseName, pathToTestBroken.substring(0, pathToTestBroken.lastIndexOf("\\"))));

            UtilsParser.sanityCheck(testBroken, testCorrect);
        } catch (NullPointerException e) {
            log.error("Errors occurred while initializing the tests. " + "Verify that the settings are correct and that the test classes start with the same line number.");
            // cleanup();
            shutDown();
            e.printStackTrace();
            System.exit(1);
        }

        /* Map of the original statements. */
        Map<Integer, Statement> statementMap = testBroken.getStatements();
        /* Map of the repaired statements. */
        Map<Integer, Statement> repairedTest = new LinkedHashMap<>();
        List<Integer> deletedSteps = new ArrayList<>();
        Map<Integer, List<Eventable>> repairedPathList = new LinkedHashMap<>();

        boolean noSuchelementException;

        /* For each statement. */
        int keywordPos = 0;
        Keyword keyword = null;
        Statement previousStatement = null;
        for (int statementNumber: statementMap.keySet()) {
            noSuchelementException = true;
            Statement statement = statementMap.get(statementNumber);
            log.info("Check statement " + statementNumber + ": " + statement.toString());
            WebElement webElementFromDomLocator = null;
            List<Eventable> repairedPath = new ArrayList<>();

            Statement repairedStatement = (Statement) UtilsRepaire.deepClone(statement);

            CandidateElement candidateElement = null;
            try {
                // ignore the driver.get(xx)
                if (statement.getAction() == "get") {
                    initSFG(browser, statement);
                    previousStatement = statement;
                    repairedTest.put(statementNumber, repairedStatement);
                    continue;
                }

                // check the alert window content
                if (statement.getAction() == "closeAlertAndGetItsText" || statement.getAction() == "alert") {
                    String value = statement.getValue();
                    try {
                        browser.switchToAlert().accept();
//                        String alertText = alert.getText();
//
//                        if (!value.equals(alertText)) {
//                            repairedStatement.setValue(alertText);
//                            repairedTest.put(statementNumber, repairedStatement);
//                        }
//                        alert.accept();
                        previousStatement = statement;
                        continue;
                    } catch (NoAlertPresentException e) {
                        continue;
                    }
                }

                // 移动关当前键词索引
                keyword = keywordList.get(keywordPos);
                while (keyword.getLineNumber() < statementNumber) {
                    keywordPos++;
                    keyword = keywordList.get(keywordPos);
                }

                // 先检查元素是否被修复过
                if (identicalWithLastStatement(statement, previousStatement)) {
                    repairedStatement = (Statement) UtilsRepaire.deepClone(repairedTest.get(statementNumber - 1));
                    repairedStatement.setAction(statement.getAction());
                    repairedStatement.setValue(statement.getValue());
                    repairedStatement.setLine(statementNumber);
                    repairedTest.put(statementNumber, repairedStatement);
                    webElementFromDomLocator = UtilsRepaire.retrieveWebElementFromDomLocator(browser, repairedStatement.getDomLocator());
                    noSuchelementException = false;
                    repairedStatNum++;
                } else {
                    // 使用旧元素locator进行查找
                    webElementFromDomLocator = UtilsRepaire.retrieveWebElementFromDomLocator(browser, statement.getDomLocator());

                    // 对元素进行检查
                    if (webElementFromDomLocator != null && webElementFromDomLocator.isDisplayed()) {
                        // 先检查元素结构相似度
                        double collectedInfoSim = UtilsRepaire.checkElementByCollectedInfo(browser, webElementFromDomLocator,
                                testCorrect.getStatements().get(statementNumber));
                        // 如果相似度达到0.8，则认为一样
                        // TODO: 2020/1/9 阈值
                        if (collectedInfoSim > 0.8) {
                            // 直接放入修复队列
                            String tarXpath = UtilsXPath.getElementXPath((JavascriptExecutor) browser.getWebDriver(), webElementFromDomLocator);
                            if (!tarXpath.equalsIgnoreCase(repairedStatement.getXpath())) repairedStatement.setXpath(tarXpath);
                            repairedTest.put(statementNumber, repairedStatement);
                            noSuchelementException = false;
                        } else {
                            double semanticSim = UtilsRepaire.checkElementBySemantic(webElementFromDomLocator, statement);
                            // TODO: 2020/1/15 the threshold needs to be redefined
                            if (semanticSim > 0.8) {
                                String tarXpath = UtilsXPath.getElementXPath((JavascriptExecutor) browser.getWebDriver(), webElementFromDomLocator);
                                if (!tarXpath.equalsIgnoreCase(repairedStatement.getXpath())) repairedStatement.setXpath(tarXpath);
                                repairedTest.put(statementNumber, repairedStatement);
                                noSuchelementException = false;
                            }
                        }
                    } else {
                        log.info("Direct breakage detected at line " + statement.getLine());
                        log.info("Cause: Non-selection of elements by the locator " + statement.getDomLocator() + " in the current DOM state");
                        candidateElement = repairWithKSG(keywordPos, statement, repairedPath);
                        if (candidateElement == null) {
                            noSuchelementException = true;
                            if (repairedPath.size() > 0) repairedPathList.put(statementNumber, repairedPath);
                        }
                        else noSuchelementException = false;
                    }
                }
            } catch (NoSuchElementException e) {
                log.info("Direct breakage detected at line " + statement.getLine());
                log.info("Cause: Non-selection of elements by the locator " + statement.getDomLocator() + " in the current DOM state");

                candidateElement = repairWithKSG(keywordPos, statement, repairedPath);
                if (candidateElement == null) noSuchelementException = true;
                else noSuchelementException = false;
            }

            if (noSuchelementException) {
                log.info("this test cannot be repaired");
            } else {
                // 执行事件
                // 状态转换
                CandidateCrawlAction action = null;
                if (candidateElement != null) {
                    // 修复
                    SeleniumLocator domLocator = new SeleniumLocator("xpath", candidateElement.getIdentification().getValue());
                    repairedStatement.setDomLocator(domLocator);
                    repairedStatement.setXpath(candidateElement.getIdentification().getValue());
                    repairedTest.put(statementNumber, repairedStatement);
                    if (repairedPath.size() > 0) {
                        repairedPathList.put(statementNumber, repairedPath);
                    }

                    if (statement.getAction().equalsIgnoreCase("click") ||
                            statement.getAction().equalsIgnoreCase("hover")) {
                        if (statement.getAction().equalsIgnoreCase("click")) {
                            action = new CandidateCrawlAction(candidateElement, Eventable.EventType.click);
                        } else {
                            action = new CandidateCrawlAction(candidateElement, Eventable.EventType.hover);
                        }

                        crawler.setOnlyFireSpecificEvent(true);
                        if (!this.crawler.fireSpecificEvent(action)) {
                            log.error("an error occurred...");
                        }
                    } else {
                        userWebDriverToFireEvent(statement, webElementFromDomLocator, repairedStatement);
                    }
                } else if (repairedTest.get(statementNumber) != null) {
                    useCrawlerToFireEvent(statement, webElementFromDomLocator, repairedStatement, browser);
                } else {
                    log.info("current statement can not be repaired, it will be deleted from the test");
                    repairedTest.put(statementNumber, null);
                    deletedSteps.add(statementNumber);
                }
            }
            previousStatement = statement;
        }
        testCorrect = getNewTest(testBroken, repairedTest, repairedPathList);
        testCorrect.setName(testBroken.getName());
        UtilsRepaire.saveTest(appEnum.getAppName() + ".", testcaseName, testCorrect);
        shutDown();
    }

    private boolean identicalWithLastStatement(Statement statement, Statement previousStatement) {
        if (previousStatement == null) return false;

        SeleniumLocator target = statement.getDomLocator();
        SeleniumLocator src = previousStatement.getDomLocator();
        if (target == null || src == null) return false;
        if (target.getStrategy().equalsIgnoreCase(src.getStrategy()) && target.getValue().equalsIgnoreCase(src.getValue())) {
            return true;
        }

        return false;
    }

    private void useCrawlerToFireEvent(Statement statement, WebElement webElementFromDomLocator, Statement repairedStatement, EmbeddedBrowser browser) {
        if (statement.getAction().equalsIgnoreCase("click") ||
                statement.getAction().equalsIgnoreCase("hover")) {
            CandidateCrawlAction action = null;
            try {
                Document dom = Helper.getDocument(browser.getDomWithoutIframeContent());
                NodeList nodeList = XPathHelper.evaluateXpathExpression(dom, repairedStatement.getXpath().toUpperCase());
                if (nodeList.getLength() > 1) {
                    log.error("found two more nodes...");
                    return;
                }
                Element element = (Element) nodeList.item(0);
                CandidateElement candidateElement = new CandidateElement(element, statement.getXpath());
                if (statement.getAction().equalsIgnoreCase("click")) {
                    action = new CandidateCrawlAction(candidateElement, Eventable.EventType.click);
                } else {
                    action = new CandidateCrawlAction(candidateElement, Eventable.EventType.hover);
                }
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e1) {
                e1.printStackTrace();
            }

            crawler.setOnlyFireSpecificEvent(true);
            if (action == null || !crawler.fireSpecificEvent(action)) {
                log.error("an error occurred...");
            }
        } else {
            userWebDriverToFireEvent(statement, webElementFromDomLocator, repairedStatement);
        }
    }

    private EnhancedTestCase getNewTest(EnhancedTestCase testBroken, Map<Integer, Statement> repairedTest, Map<Integer, List<Eventable>> repairedPathList) {
        UtilsRepaire.printTestCaseWithLineNumbers(testBroken);

//        EnhancedTestCase temp = (EnhancedTestCase) UtilsRepaire.deepClone(testBroken);
        EnhancedTestCase temp = new EnhancedTestCase();
        int currentLine = 0;
        for (Integer i : repairedTest.keySet()) {
            if (currentLine == 0) currentLine = i;
            // 检查在该步骤前是否有新增步骤
            if (repairedPathList.keySet().contains(i)) {
                List<Eventable> addEvents = repairedPathList.get(i);
                for (Eventable event: addEvents) {
                    // 处理关联的formInput信息
                    List<FormInput> relatedForms = event.getRelatedFormInputs();
                    if (relatedForms != null && relatedForms.size() > 0) {
                        for (FormInput input: relatedForms) {
                            EnhancedWebElement statement = new EnhancedWebElement();
                            SeleniumLocator locator = new SeleniumLocator(input.getIdentification().getHow().toString(), input.getIdentification().getValue());
                            statement.setDomLocator(locator);
                            statement.setLine(currentLine);
                            statement.setAction("sendKeys");
                            Iterator<InputValue> it = input.getInputValues().iterator();
                            if (it.hasNext()) {
                                statement.setValue(it.next().getValue());
                            }
                            temp.addAndReplaceStatement(currentLine++, statement);
                        }
                    }
                    EnhancedWebElement statement = new EnhancedWebElement();
                    SeleniumLocator locator = new SeleniumLocator("xpath", event.getIdentification().getValue());
                    statement.setDomLocator(locator);
                    statement.setAction("click");
                    statement.setValue("");
                    statement.setLine(currentLine);
                    temp.addAndReplaceStatement(currentLine++, statement);
                }
            }
            Statement stament = repairedTest.get(i);
            if (stament == null) continue;
            stament.setLine(currentLine);
            temp.addAndReplaceStatement(currentLine++, stament);
        }

        System.out.println("\n after repairing....");

        UtilsRepaire.printTestCaseWithLineNumbers(temp);
        return temp;
    }

    private void userWebDriverToFireEvent(Statement statement, WebElement element, Statement repairedStatement) {
        try {
            if (element == null) element = UtilsRepaire.retrieveWebElementFromDomLocator(browser, repairedStatement.getDomLocator());
            if (statement.getAction().equalsIgnoreCase("click")) {
                element.click();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("an interruptedException occurred...");
                }
            } else if (statement.getAction().equalsIgnoreCase("clear")) {
                element.clear();
            } else if (statement.getAction().equalsIgnoreCase("sendkeys")) {
                element.sendKeys(statement.getValue());
            } else if (statement.getAction().equalsIgnoreCase("selectByVisibleText")) {
                new Select(element).selectByVisibleText(statement.getValue());
            } else if (statement.getAction().equalsIgnoreCase("getText")) {
                if (element.getText().equals(statement.getValue())) {
                } else {
                    repairedStatement.setValue(element.getText());
                }
            }
        } catch (Exception e) {
            log.error("an exception occurred when execute the command...");
            e.printStackTrace();
        }
    }

    private CandidateElement repairWithKSG(int keywordPos, Statement statement, List<Eventable> clickPath) {
        log.info("Start to repair the web element...");

        repairedStatNum++;
        CandidateElement element = null;
        try {
//            c.init();
            PlainElement targetElement = new PlainElement();
            UtilsRepaire.cloneStatementToPlainElement(statement, targetElement);
            crawler.setOnlyFireSpecificEvent(false);
            crawler.clearCrawlStack();
            element = this.crawler.guidedCrawlForTargetElement(targetElement, keywordPos, clickPath);
            if (element != null) {
                log.info("find target element...");
            } else {
                log.info("target element has moved...");
            }
        } catch (CrawljaxException e) {
            log.error("\nan occurred when execute the method guidedCawlForTagetElement of Crawler");
            e.printStackTrace();
        }
        return element;
    }

    /**
     * 处理一些后续事件，如：关闭浏览器，文件句柄等
     */
    private void shutDown() {
        log.info("正在处理退出信息...........");
        log.info("正在关闭浏览器...");
        crawljaxController.getBrowserPool().shutdown();
        log.info("浏览器已安全关闭");
        log.info("已成功退出.................");

        // 输出统计信息
        log.info("\n修复的语句个数：" + repairedStatNum);
        log.info("触发事件数：" + crawlSession.fireEventNum);
        log.info("探测的状态数：" + crawlSession.crawlStateNum);
        log.info("总状态数：" + sfg.getAllStates().size());
    }

    private void initSFG(EmbeddedBrowser browser, Statement statement) {
        String indexUrl = statement.getValue();
        browser.goToUrl(indexUrl);
        browser.maximizeWindow();
        // 创建第一个状态
        StateVertix indexState = new StateVertix(indexUrl, "index", browser.getDom(), crawljaxController.getStrippedDom(browser), null);
        this.indexState = indexState;
        if (CrawljaxController.checkKSG()) {
            StateVertix.setKeyContent(null, indexState);
        }
        // 创建状态流图
        sfg = new StateFlowGraph(indexState);
        // 创建StateMachine
        stateMachine = new StateMachine(sfg, indexState, crawljaxController.getInvariantList());
        // 创建crawlSession
        crawlSession = new CrawlSession(crawljaxController.getBrowserPool(), sfg, indexState, crawljaxController.getStartCrawl(), crawljaxController.getConfigurationReader());
        crawljaxController.setSession(crawlSession);
        crawljaxController.setStartCrawl(System.currentTimeMillis());
        // 创建crawler
        crawler = new Crawler(crawljaxController,
                crawljaxController.getSession().getCurrentCrawlPath().immutableCopy(true), 0);
        try {
            crawler.init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
