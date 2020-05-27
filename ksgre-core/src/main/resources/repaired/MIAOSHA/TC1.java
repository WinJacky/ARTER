package main.resources.repaired.MIAOSHA;

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
    public void tC1() {
        driver.get("http://localhost:8080/miaosha/login");;
        driver.findElement(By.xpath("/HTML[1]/BODY[1]/DIV[1]/DIV[2]/BUTTON[1]")).click();
        driver.findElement(By.xpath("/html[1]/body[1]/form[1]/div[1]/div[1]/div[1]/input[1]")).sendKeys("18362903235");
        driver.findElement(By.id("password")).sendKeys("123456");
        driver.findElement(By.xpath("/html[1]/body[1]/form[1]/div[3]/div[2]/button[1]")).click();
    }

    public WebDriver getDriver() {
        return this.driver;
    }
}
