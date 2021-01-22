package main.resources.repaired.ADDR;

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
    public void tC1() throws InterruptedException {
        driver.get("http://localhost/addressbookv1.0");;
        driver.findElement(By.linkText("add new")).click();
        Thread.sleep(500);;
        driver.findElement(By.name("address")).sendKeys("null");
        driver.findElement(By.xpath("/HTML[1]/BODY[1]/DIV[1]/DIV[4]/FORM[1]/INPUT[1]")).click();
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[4]/form[1]/input[3]")).sendKeys("Joe");
        driver.findElement(By.name("lastname")).sendKeys("Yue");
        driver.findElement(By.name("address")).sendKeys("Nanjing");
        driver.findElement(By.name("home")).sendKeys("SEU");
        driver.findElement(By.name("submit")).click();
    }
}
