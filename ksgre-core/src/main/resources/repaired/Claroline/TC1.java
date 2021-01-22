package main.resources.repaired.Claroline;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This test case is meant to Update User Info
 */
public class TC1 {

    private WebDriver driver;

    private Map<String, Object> vars;

    JavascriptExecutor js;

    @Before
    public void setUp() {
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void tC1() {
        driver.get("http://localhost/claroline100/");
        driver.findElement(By.id("login")).clear();
        driver.findElement(By.id("login")).sendKeys("login");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[2]/div[1]/div[1]/p[1]/a[2]")).click();
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[3]/div[1]/ul[1]/li[2]/a[1]")).click();
        driver.findElement(By.id("officialCode")).clear();
        driver.findElement(By.id("officialCode")).sendKeys("123456");
        driver.findElement(By.id("applyChange")).click();
    }
}
