package com.crawljax.config;

import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.config.AppEnum;
import com.crawljax.core.CrawljaxController;
import com.crawljax.core.CrawljaxException;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.CrawljaxConfiguration;
import com.crawljax.core.configuration.ThreadConfiguration;
import com.crawljax.core.oraclecomparator.comparators.PlainStructureComparator;
import com.crawljax.core.plugin.ScreenShotPlugin;

public class MainExample {
    private static final int MAX_NUMBER_STATES=30000;
    private static final int MAX_DEPTH=100;
    private static AppEnum appEnum = null;
    private static AppConfig appConfig = null;

    private static CrawljaxConfiguration getCrawljaxConfiguration() {
        CrawljaxConfiguration config = new CrawljaxConfiguration();
        config.setCrawlSpecification(getCrawlSpecification());
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

    private static CrawlSpecification getCrawlSpecification() {
        CrawlSpecification crawler = new CrawlSpecification(appEnum.url);
        // 这里设置的OracleComparator会用来初始化StateComparator，而StateComparator的作用是剥离DOM树
        crawler.addOracleComparator("1", new PlainStructureComparator());

        crawler.setMaximumStates(MAX_NUMBER_STATES);
        crawler.setMaximumRuntime(300);
        crawler.setDepth(0);

//        // 设置关键词
//        crawler.setKeywords(appEnum.keywords);
//
//        // 设置爬取算法
//        crawler.setCrawlStrategy(CrawlStrategy.KSG);

        // 每个可点击事件是否仅点击一次
        crawler.setClickOnce(true);

        // 表单输入
        appConfig.setInputSpecification(crawler);

        // 设置点击哪些元素
        appConfig.setClick(crawler);

        // 设置事件触发先后关系约束
        appConfig.setPreClickConstraints(crawler);

        // 配置阈值
        appConfig.setThreshold();

        return crawler;
    }

    public static void main(String[] args) {
        try
        {
            // 配置WebApp
            appEnum = AppEnum.PHPSHE;
            appConfig = AppConfigFactory.getInstance(appEnum);
            CrawljaxController crawl = new CrawljaxController(getCrawljaxConfiguration());

            crawl.run();


        }catch(Exception e)
        {
            System.out.println("Configuration Exception");
        }
    }
}
