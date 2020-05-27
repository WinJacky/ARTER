package main.resources.PHPSHE;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;

import java.util.HashMap;
import java.util.Map;

public class TC2 {

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
    public void tC2() {
        driver.get("http://localhost/phpshe/");;
        driver.findElement(By.linkText("登录")).click();
        driver.findElement(By.name("user_name")).click();
        driver.findElement(By.name("user_name")).sendKeys("heli123");
        driver.findElement(By.name("user_pw")).click();
        driver.findElement(By.name("user_pw")).sendKeys("heli123");
        driver.findElement(By.cssSelector(".loginbtn1")).click();
        {
            WebElement element = driver.findElement(By.cssSelector(".loginbtn1"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        driver.findElement(By.linkText("[退出]")).click();
    }

    public WebDriver getDriver() {
        return this.driver;
    }
}
