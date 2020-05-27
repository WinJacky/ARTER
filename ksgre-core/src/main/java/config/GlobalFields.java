package main.java.config;

import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Record the resource in the runtime.
 */
public class GlobalFields {

    private static String appDirectoryPath;
    private static String currentSavedDirectoryPath;

    public static String getAppDirectoryPath() {
        return appDirectoryPath;
    }

    public static void setAppDirectoryPath(String appDirectoryPath) {
        GlobalFields.appDirectoryPath = appDirectoryPath;
    }

    public static String getCurrentSavedDirectoryPath() {
        return currentSavedDirectoryPath;
    }

    public static void setCurrentSavedDirectoryPath(String currentSavedDirectoryPath) {
        GlobalFields.currentSavedDirectoryPath = currentSavedDirectoryPath;
    }
}
