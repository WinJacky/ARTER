package main.java.util;

import main.java.config.Settings;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;

/**
 * This class refers to the same class in visualrepair.
 */
public class UtilsComputerVision {

    protected static WebDriver driver;
    protected static String screenshotFolder;

    private static Logger log = LoggerFactory.getLogger(UtilsComputerVision.class);

    static {
        try {
//            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the visual locator.
     *
     * @param d
     * @param element
     * @throws IOException
     */
    public static void getScaledSubImage(WebDriver d, BufferedImage img, WebElement element, File visualLocator, int scale) throws IOException {

        org.openqa.selenium.Point elementCoordinates = null;
        driver = d;

        try {
            elementCoordinates = element.getLocation();
        } catch (StaleElementReferenceException e) {
            if(Settings.VERBOSE) {
                log.info("Test might have changed its state");
            }
        }

        try {
            highlightElement(element);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        java.awt.Rectangle rect = new java.awt.Rectangle(width, height);
        BufferedImage subImage = null;

        int min_offset_x = Math.min(element.getLocation().x, img.getHeight() - rect.width - element.getLocation().x);
        int min_offset_y = Math.min(element.getLocation().y, img.getHeight() - rect.height - element.getLocation().y);
        int offset = Math.min(min_offset_x, min_offset_y);

        offset = offset / scale;

        try {
            if(element.getTagName().equals("option")) {

                WebElement thisShouldBeTheSelect = element.findElement(By.xpath(".."));
                new Actions(driver).moveToElement(thisShouldBeTheSelect).perform();

                elementCoordinates = thisShouldBeTheSelect.getLocation();
                subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset, 2 * offset + rect.width, 2 * offset + rect.height);

            } else {
                subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            }
        } catch (RasterFormatException e) {
            log.error("WARNING: " + e.getMessage());
        }

        ImageIO.write(subImage, "png", visualLocator);
        subImage.flush();
    }

    /**
     * This method highlights the web element on which PESTO is currently performing.
     *
     * @param element
     * @throws InterruptedException
     */
    private static void highlightElement(WebElement element) throws InterruptedException {

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: yellow; border: 2px solid yellow;");
        Thread.sleep(100);
        js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
    }

    /**
     * Save a precise crop for the web element (might not be unique).
     *
     * @param d
     * @param s
     * @param we
     * @param vl
     */
    public static void saveVisualCrop(WebDriver d, String s, WebElement we, String vl) {

        try {
            UtilsComputerVision.getPreciseElementVisualCrop(d, s, we, vl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

    /**
     * Get the precise crop for the web element.
     *
     * @param d
     * @param filename
     * @param element
     * @param webElementImageName
     * @throws IOException
     */
    public static void getPreciseElementVisualCrop(WebDriver d, String filename, WebElement element, String webElementImageName) throws IOException {

        File destFile = new File(filename);
        BufferedImage img = ImageIO.read(destFile);

        File visualLocator = new File(webElementImageName);

        getPreciseSubImage(d, img, element, visualLocator);
    }

    /**
     * Get the visual locator.
     *
     * @param d
     * @param element
     * @throws IOException
     */
    public static void getPreciseSubImage(WebDriver d, BufferedImage img, WebElement element, File visualLocator) throws IOException {

        org.openqa.selenium.Point elementCoordinates = null;
        driver = d;

        try {
            elementCoordinates = element.getLocation();
        } catch (StaleElementReferenceException e) {
            if (Settings.VERBOSE)
                log.info("test might have changed its state");
        }

        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        java.awt.Rectangle rect = new java.awt.Rectangle(width, height);
        BufferedImage subImage = null;

        int offset = 0;

        try {
            if (element.getTagName().equals("option")) {

                WebElement thisShouldBeTheSelect = element.findElement(By.xpath(".."));
                new Actions(driver).moveToElement(thisShouldBeTheSelect).perform();

                if (Settings.VERBOSE) {
                    log.error("thisShouldBeTheSelect.getLocation(): " + thisShouldBeTheSelect.getLocation());
                    log.error("element.getLocation(): " + element.getLocation());
                }

                elementCoordinates = thisShouldBeTheSelect.getLocation();
                subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            } else {
                subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset, 2 * offset + rect.width, 2 * offset + rect.height);
            }
        } catch (RasterFormatException e) {
            log.error("WARNING: " + e.getMessage());
        }

        ImageIO.write(subImage, "png", visualLocator);
        subImage.flush();
    }

    /**
     * Save rendered webpage path.
     */
    public static File saveHTMLPage(String urlString, String path) throws IOException {

        File savedHTML = new File(path);

        if(savedHTML.exists()) {
            UtilsFile.deleteDirectory(savedHTML);
        }

        /* wget to save html page. */
        Runtime runtime = Runtime.getRuntime();
        Process p = runtime.exec("E:/learn/vista/Ryan Addition/wget-1.19.4-win64/wget -p -k -E -nd -P " + path + " " + urlString);

        StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERR");
        StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUT");
        errorGobbler.start();
        outputGobbler.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            p.destroy();
        }

        return savedHTML;
    }
}
