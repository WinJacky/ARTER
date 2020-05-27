package main.java.dataType;

public class EnhancedAlert extends EnhancedWebElement {
    @Override
    public String toString() {
        return "driver.switchTo().alert().accept()";
    }
}
