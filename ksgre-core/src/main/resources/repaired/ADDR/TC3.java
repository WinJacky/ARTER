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

public class TC3 {

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
    public void tC3() {
        driver.get("http://localhost/addr/");;
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[1]/a[3]/img[1]")).click();
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[4]/form[2]/table[1]/tbody[1]/tr[2]/td[6]/a[1]/img[1]")).click();
        driver.findElement(By.name("modifiy")).click();
        driver.findElement(By.name("bday")).click();
        driver.findElement(By.xpath("//option[. = '9']")).click();
        driver.findElement(By.name("bmonth")).click();
        driver.findElement(By.xpath("//option[. = 'April']")).click();
        driver.findElement(By.name("byear")).sendKeys("1987");
        driver.findElement(By.name("update")).click();
    }
}
