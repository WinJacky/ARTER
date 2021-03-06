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
 * This test case is meant to Create New Member
 */
public class TC5 {

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
    public void tC5() {
        driver.get("http://localhost/NucleusCMS-Nucleus-3-40/nucleus/");;
        driver.findElement(By.name("login")).clear();
        driver.findElement(By.name("login")).sendKeys("admin");
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys("admin");
        driver.findElement(By.xpath("//input[@value='Log In']")).click();
        driver.findElement(By.linkText("Members")).click();
        driver.findElement(By.name("name")).clear();
        driver.findElement(By.name("name")).sendKeys("root");
        driver.findElement(By.name("realname")).clear();
        driver.findElement(By.name("realname")).sendKeys("root");
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys("root123");
        driver.findElement(By.name("repeatpassword")).clear();
        driver.findElement(By.name("repeatpassword")).sendKeys("root123");
        driver.findElement(By.name("email")).clear();
        driver.findElement(By.name("email")).sendKeys("123@163.com");
        driver.findElement(By.xpath("//input[@value='Add Member']")).click();
    }
}
