package main.resources.EShop;// Generated by Selenium IDE
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.hamcrest.CoreMatchers.theInstance;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
public class TC1 {
  private WebDriver driver;
  private Map<String, Object> vars;
  JavascriptExecutor js;
  @Before
  public void setUp() {
    driver = new FirefoxDriver();
    js = (JavascriptExecutor) driver;
    vars = new HashMap<String, Object>();
  }
  @After
  public void tearDown() {
    driver.quit();
  }
  @Test
  public void test() {
    driver.get("http://localhost:8080/EShop/mer.do?method=browseIndexMer");
    driver.manage().window().setSize(new Dimension(550, 692));
    driver.findElement(By.name("loginName")).click();
    driver.findElement(By.name("loginName")).sendKeys("heli");
    driver.findElement(By.name("loginPwd")).click();
    driver.findElement(By.name("loginPwd")).sendKeys("heli");
    driver.findElement(By.cssSelector(".UserRegster > input:nth-child(2)")).click();
    driver.findElement(By.cssSelector("tr:nth-child(3) > td > a > .blueText")).click();
  }

  public WebDriver getDriver() {
    return this.driver;
  }
}