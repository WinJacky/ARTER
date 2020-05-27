package main.resources.PHPSHE;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import java.util.HashMap;
import java.util.Map;

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
    public void tC1() {
        driver.get("http://localhost/phpshe/");;
        driver.findElement(By.linkText("免费注册")).click();
        driver.findElement(By.name("user_name")).sendKeys("heli123");
        driver.findElement(By.name("user_pw")).sendKeys("heli123");
        driver.findElement(By.name("user_pw1")).sendKeys("heli123");
        driver.findElement(By.name("user_email")).sendKeys("123@email.com");
        driver.findElement(By.xpath("//*[@id='form']/table/tbody/tr[6]/td[2]/input[2]")).click();
        driver.findElement(By.linkText("退出")).click();
    }

    public WebDriver getDriver() {
        return this.driver;
    }
}
