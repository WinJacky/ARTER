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
 * This test case is meant to Send Messages
 */
public class TC4 {

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
    public void tC4() throws InterruptedException {
        driver.get("http://localhost/claroline90/");;
        driver.findElement(By.id("login")).clear();
        driver.findElement(By.id("login")).sendKeys("admin");
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("admin");
        driver.findElement(By.name("submitAuth")).click();
        driver.findElement(By.linkText("Platform administration")).click();
        driver.findElement(By.linkText("Send a message to all users")).click();
        driver.findElement(By.id("message_subject")).clear();
        driver.findElement(By.id("message_subject")).sendKeys("Examination Inform");
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/div[2]/div[1]/span[2]/a[3]")).click();
        driver.findElement(By.linkText("Logout")).click();
    }
}
