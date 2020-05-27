package test.java;

import main.java.config.AppEnum;

import static main.java.runner.RepaireRunner.readKeyText;

public class testReadKeyText {
    public static void main(String[] args) {
        AppEnum appEnum = AppEnum.PHPSHE;
        readKeyText(appEnum, "TC1");
    }
}
