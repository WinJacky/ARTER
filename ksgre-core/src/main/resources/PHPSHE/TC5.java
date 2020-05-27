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

public class TC5 {

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
    public void tC5() {
        driver.get("http://localhost/phpshe/");;
        driver.findElement(By.linkText("登录")).click();
        driver.findElement(By.name("user_name")).sendKeys("heli123");
        driver.findElement(By.name("user_pw")).sendKeys("heli123");
        driver.findElement(By.className("loginbtn1")).click();
        driver.findElement(By.linkText("首页")).click();
        driver.findElement(By.linkText("iPhone 8")).click();
        driver.findElement(By.linkText("加入购物车")).click();
        driver.findElement(By.xpath("/HTML[1]/BODY[1]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/TABLE[1]/TBODY[1]/TR[2]/TD[2]/DIV[1]/DIV[1]/A[1]")).click();
        driver.findElement(By.linkText("退出")).click();
    }

    public WebDriver getDriver() {
        return this.driver;
    }
}
