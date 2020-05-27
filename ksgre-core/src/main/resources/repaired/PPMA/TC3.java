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

public class TC3 {

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
    public void tC3() throws InterruptedException {
        driver.get("http://localhost/ppma/");;
        driver.findElement(By.id("user")).sendKeys("heli123");
        driver.findElement(By.id("pwd")).sendKeys("heli123");
        driver.findElement(By.id("chk")).click();
        driver.findElement(By.id("newiteminput")).sendKeys("Google");
        driver.findElement(By.id("newiteminputpw")).sendKeys("Google123");
        driver.findElement(By.id("oldpassword")).sendKeys("null");
        driver.findElement(By.id("pwd")).sendKeys("heli123");
        driver.findElement(By.id("pwd1")).sendKeys("heli123");
        driver.findElement(By.id("npin")).sendKeys("null");
        driver.findElement(By.id("importc")).sendKeys("null");
        driver.findElement(By.xpath("/HTML[1]/BODY[1]/NAV[1]/DIV[1]/DIV[2]/UL[1]/LI[2]/A[1]")).click();
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[3]/div[1]/div[1]/div[1]/div[3]/button[2]")).click();
        driver.findElement(By.cssSelector(".btn-info")).click();
    }
}
