package main.java.core;

import main.java.config.AppEnum;
import main.java.config.Settings;
import main.java.runner.TracerRunner;

import java.io.IOException;

public class VisualExecutionTracer {

    public static void main(String[] args) throws IOException {

        /* enable the AspectJ module. */
        Settings.aspectActive = true;

        Settings tracerSetting = new Settings(AppEnum.MIAOSHA);

        TracerRunner.runTest(tracerSetting.testSuite, "TC1");
    }
}
