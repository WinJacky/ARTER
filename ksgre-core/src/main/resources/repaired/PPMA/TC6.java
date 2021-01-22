package main.resources.repaired.PPMA;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
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
import java.util.concurrent.TimeUnit;

/**
 * This test case is meant to Change Password
 */
public class TC6 {

    private WebDriver driver;

    private Map<String, Object> vars;

    JavascriptExecutor js;

    @Before
    public void setUp() {
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void tC6() throws InterruptedException {
        driver.get("http://localhost/PPMA/Password-Manager-9.08/");
        driver.findElement(By.id("user")).sendKeys("admin123");
        driver.findElement(By.id("pwd")).sendKeys("admin123");
        driver.findElement(By.id("chk")).click();
        Thread.sleep(500);
        driver.findElement(By.linkText("Change Password")).click();
        driver.findElement(By.id("oldpassword")).sendKeys("admin123");
        driver.findElement(By.id("pwd")).sendKeys("admin123");
        driver.findElement(By.id("pwd1")).sendKeys("admin123");
        driver.findElement(By.id("changepw")).click();
    }
}