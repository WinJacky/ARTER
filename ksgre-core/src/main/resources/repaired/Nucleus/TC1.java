package main.resources.repaired.Nucleus;

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
 * This test case is meant to Add Item
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
    public void tC1() throws InterruptedException {
        driver.get("http://localhost/NucleusCMS-Nucleus-3-40/nucleus/");;
        driver.findElement(By.name("login")).clear();
        driver.findElement(By.name("login")).sendKeys("admin");
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys("admin");
        driver.findElement(By.xpath("//input[@value='Log In']")).click();
        driver.findElement(By.linkText("Add Item")).click();
        driver.findElement(By.id("inputtitle")).clear();
        driver.findElement(By.id("inputtitle")).sendKeys("South East University");
        driver.findElement(By.id("inputbody")).clear();
        driver.findElement(By.id("inputbody")).sendKeys("SEU is an university.");
        driver.findElement(By.xpath("//input[@value='Add Item']")).click();
    }
}
