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
 * This test case is meant to Add a new account
 */
public class TC2 {

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
    public void tC2() throws InterruptedException {
        driver.get("http://localhost/PPMA/Password-Manager-5.0/");
        driver.findElement(By.id("user")).sendKeys("admin123");
        driver.findElement(By.id("pwd")).sendKeys("admin123");
        driver.findElement(By.id("chk")).click();
        Thread.sleep(500);
        driver.findElement(By.id("srch-term")).sendKeys("null");
        driver.findElement(By.id("newiteminput")).sendKeys("Google");
        driver.findElement(By.id("newiteminputuser")).sendKeys("null");
        driver.findElement(By.id("newiteminputpw")).sendKeys("Google123");
        driver.findElement(By.id("newiteminputurl")).sendKeys("null");
        driver.findElement(By.id("newiteminputtags")).sendKeys("null");
        driver.findElement(By.id("edititeminput")).sendKeys("null");
        driver.findElement(By.id("edititeminputuser")).sendKeys("null");
        driver.findElement(By.id("edititeminputpw")).sendKeys("null");
        driver.findElement(By.id("edititeminputurl")).sendKeys("null");
        driver.findElement(By.id("edititeminputtags")).sendKeys("null");
        driver.findElement(By.id("pinxx")).sendKeys("null");
        driver.findElement(By.id("importTypeBackup")).sendKeys("null");
        driver.findElement(By.id("importTypeCSV")).sendKeys("null");
        driver.findElement(By.id("oldpassword")).sendKeys("null");
        driver.findElement(By.id("pwd")).sendKeys("heli123");
        driver.findElement(By.id("pwd1")).sendKeys("heli123");
        driver.findElement(By.id("newiteminputcomment")).sendKeys("null");
        driver.findElement(By.id("edititeminputcomment")).sendKeys("null");
        driver.findElement(By.id("importc")).sendKeys("null");
        driver.findElement(By.xpath("/HTML[1]/BODY[1]/NAV[1]/DIV[1]/DIV[2]/UL[1]/LI[2]/A[1]")).click();
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[3]/div[1]/div[1]/div[1]/div[2]/form[1]/div[1]/input[1]")).sendKeys("Google");
        driver.findElement(By.id("newiteminputpw")).sendKeys("Google123");
        driver.findElement(By.id("newbtn")).click();
    }
}
