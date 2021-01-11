package com.crawljax.browser;

import com.crawljax.core.configuration.CrawljaxConfigurationReader;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.util.List;

/**
 * This class represents the default Crawljax used implementation of the BrowserBuilder. It's based
 * on the WebDriver implementations offered by Crawljax.
 * 
 * @author Stefan Lenselink <S.R.Lenselink@student.tudelft.nl>
 * @version $Id: WebDriverBrowserBuilder.java 465M 2012-05-04 20:42:33Z (local) $
 */
public class WebDriverBrowserBuilder implements EmbeddedBrowserBuilder {

	/**
	 * Build a new WebDriver based EmbeddedBrowser.
	 * 
	 * @see EmbeddedBrowserBuilder#buildEmbeddedBrowser(CrawljaxConfigurationReader)
	 * @param configuration
	 *            the configuration object to read the config values from
	 * @return the new build WebDriver based embeddedBrowser
	 */
	@Override
	public EmbeddedBrowser buildEmbeddedBrowser(CrawljaxConfigurationReader configuration) {
		// Retrieve the config values used
		List<String> filterAttributes = configuration.getFilterAttributeNames();
		int crawlWaitReload = configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl();
		int crawlWaitEvent = configuration.getCrawlSpecificationReader().getWaitAfterEvent();

		// Determine the requested browser type
		switch (configuration.getBrowser()) {
			case firefox:
				System.setProperty("webdriver.firefox.driver","D:\\DevelopmentTool\\IDEA\\MavenWorkspace\\ARTER\\ksgre-core\\src\\main\\resources\\BrowserDriver\\geckodriver.exe");
				if (configuration.getProxyConfiguration() != null) {
					FirefoxProfile profile = new FirefoxProfile();

					profile.setPreference("network.proxy.http", configuration
					        .getProxyConfiguration().getHostname());
					profile.setPreference("network.proxy.http_port", configuration
					        .getProxyConfiguration().getPort());
					profile.setPreference("network.proxy.type", configuration
					        .getProxyConfiguration().getType().toInt());
					/* use proxy for everything, including localhost */
					profile.setPreference("network.proxy.no_proxies_on", "");

					return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(profile),
					        filterAttributes, crawlWaitReload, crawlWaitEvent);
				}

				return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
				        configuration.getFilterAttributeNames(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

//			case ie:
//				return WebDriverBackedEmbeddedBrowser.withDriver(new InternetExplorerDriver(),
//				        configuration.getFilterAttributeNames(), configuration
//				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
//				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case chrome:
				System.setProperty("webdriver.chrome.driver","D:\\DevelopmentTool\\IDEA\\MavenWorkspace\\ARTER\\ksgre-core\\src\\main\\resources\\BrowserDriver\\chromedriver.exe");
				return WebDriverBackedEmbeddedBrowser.withDriver(new ChromeDriver(),
				        configuration.getFilterAttributeNames(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			case remote:
				return WebDriverBackedEmbeddedBrowser.withRemoteDriver(
				        configuration.getRemoteHubUrl(), configuration.getFilterAttributeNames(),
				        configuration.getCrawlSpecificationReader().getWaitAfterEvent(),
				        configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl());

//			case htmlunit:
//				return WebDriverBackedEmbeddedBrowser.withDriver(new HtmlUnitDriver(true),
//				        configuration.getFilterAttributeNames(), configuration
//				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
//				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());
//
//			case iphone:
//				try {
//					return WebDriverBackedEmbeddedBrowser.withDriver(new IPhoneDriver(),
//					        configuration.getFilterAttributeNames(), configuration
//					                .getCrawlSpecificationReader().getWaitAfterEvent(),
//					        configuration.getCrawlSpecificationReader().getWaitAfterReloadUrl());
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			case android:
//				return WebDriverBackedEmbeddedBrowser.withDriver(new AndroidDriver(),
//				        configuration.getFilterAttributeNames(), configuration
//				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
//				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());

			default:
				return WebDriverBackedEmbeddedBrowser.withDriver(new FirefoxDriver(),
				        configuration.getFilterAttributeNames(), configuration
				                .getCrawlSpecificationReader().getWaitAfterEvent(), configuration
				                .getCrawlSpecificationReader().getWaitAfterReloadUrl());
		}
	}
}
