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
        Thread.sleep(500);
        driver.findElement(By.id("oldpassword")).sendKeys("admin123");
        driver.findElement(By.id("pwd")).sendKeys("admin123");
        driver.findElement(By.id("pwd1")).sendKeys("admin123");
        driver.findElement(By.xpath("/html[1]/body[1]/div[1]/div[3]/div[1]/div[1]/form[1]/div[1]/div[1]/button[2]")).click();
    }
}
