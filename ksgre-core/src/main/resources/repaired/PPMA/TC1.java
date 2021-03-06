package main.resources.repaired.PPMA;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;

/**
   * This test case is meant to Log Out
   */
public class TC1 {

    private WebDriver driver;

    private Map<String, Object> vars;

    JavascriptExecutor js;

    @Before
    public void setUp() {
        driver = new FirefoxDriver();
        driver.manage().window().maximize();
        js = (JavascriptExecutor) driver;
        vars = new HashMap<String, Object>();
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void tC1() throws InterruptedException {
        driver.get("http://localhost/Password-Manager-5.12/");;
        driver.findElement(By.id("user")).clear();
        driver.findElement(By.id("user")).sendKeys("admin123");
        driver.findElement(By.id("pwd")).clear();
        driver.findElement(By.id("pwd")).sendKeys("admin123");
        driver.findElement(By.id("chk")).click();
        Thread.sleep(500);;
        driver.findElement(By.xpath("/html[1]/body[1]/nav[1]/div[1]/div[2]/div[1]/p[1]/a[1]")).click();
    }
}
