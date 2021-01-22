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
 * This test case is meant to Add Comment
 */
public class TC2 {

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
    public void tC2() throws InterruptedException {
        driver.get("http://localhost/NucleusCMS-Nucleus-3-40/");;
        driver.findElement(By.id("nucleus_lf_name")).clear();
        driver.findElement(By.id("nucleus_lf_name")).sendKeys("admin");
        driver.findElement(By.id("nucleus_lf_pwd")).clear();
        driver.findElement(By.id("nucleus_lf_pwd")).sendKeys("admin");
        driver.findElement(By.xpath("//input[@value='Log In']")).click();
        driver.findElement(By.linkText("Add comment")).click();
        driver.findElement(By.id("nucleus_cf_body")).clear();
        driver.findElement(By.id("nucleus_cf_body")).sendKeys("You are right!");
        driver.findElement(By.xpath("//input[@value='Add Comment']")).click();
    }
}
