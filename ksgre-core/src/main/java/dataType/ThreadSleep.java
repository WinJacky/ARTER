package main.java.dataType;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class ThreadSleep extends Statement {

    private static final long serialVersionUID = 1L;

    public WebElement getWebElement() {
        return null;
    }

    public Select getSelect() {
        return null;
    }

    public void setWebElement(WebElement we) {
        webElement = null;
    }

    public void setSelect(Select we) {
        select = null;
    }

    @Override
    public String toString() {
        return "Thread." + getAction() + "(500)";
    }
}
