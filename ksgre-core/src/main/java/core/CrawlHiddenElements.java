package main.java.core;

import main.java.dataType.ActionTreeNode;
import main.java.util.UtilsFile;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CrawlHiddenElements {

    private Logger log = LoggerFactory.getLogger(CrawlHiddenElements.class);

    private String pageUrl;
    private WebDriver driver;
    private ActionTreeNode root;

    private String excludeTagsJS;
    private String getClickElementOfDOMJS;
    private String getElementXPathJS;
    private String getHiddenElementOfDOMJS;
    private String isDisPlayedJS;

    private List<ActionTreeNode> actionTreeNodes = new ArrayList<ActionTreeNode>();

    private Queue<String> clickPath = new LinkedList<String>();

    public CrawlHiddenElements(WebDriver driver, String pageUrl) {
        this.driver = driver;
        this.pageUrl = pageUrl;
    }

    public ActionTreeNode getRoot() {
        return root;
    }

    public void setRoot(ActionTreeNode root) {
        this.root = root;
    }

    public void beginCrawl() {
        root = new ActionTreeNode(1, 1);
        readJSCodeToString();
        root.setHiddenElements(getHidElementsOfDOM(this.driver.findElement(By.tagName("body"))));
        root.setCandiClickElements(getClickElementsOfDOM(this.driver.findElement(By.tagName("body"))));
        this.actionTreeNodes.add(root);

        ActionTreeNode currNode;
        int indexOfATInArray = 0;
        while (indexOfATInArray < this.actionTreeNodes.size()) {

            currNode = this.actionTreeNodes.get(indexOfATInArray);

            if (currNode.getCandiClickElements().size() > 0) {
                goToOneATFromRootAT(currNode);
                int index = 1;
                for (String eleXpath : currNode.getCandiClickElements()) {
                    index = traverse(eleXpath, currNode.getLayoutLevel(), index, currNode);
                }
            }
            this.clickPath.clear();
            indexOfATInArray++;
        }
    }

    /**
     * To read the javascript code file.
     */
    public void readJSCodeToString() {
        String currProjPath = System.getProperty("user.dir");
        String JSDirPath = "/src/main/js";
        this.excludeTagsJS = UtilsFile.readFileToString(currProjPath + JSDirPath + "/ExcludeTags.js");
        this.getClickElementOfDOMJS = UtilsFile.readFileToString(currProjPath + JSDirPath + "/GetClickElementOfDOM.js");
        this.getElementXPathJS = UtilsFile.readFileToString(currProjPath + JSDirPath + "/GetElementXPath.js");
        this.getHiddenElementOfDOMJS = UtilsFile.readFileToString(currProjPath + JSDirPath + "/GetHiddenElementsOfDOM.js");
        this.isDisPlayedJS = UtilsFile.readFileToString(currProjPath + JSDirPath + "/IsDisplayed.js");
    }

    /**
     * Get hidden elements' XPath under a root element node.
     *
     * @param rootElement
     * @return
     */
    public List<String> getHidElementsOfDOM(WebElement rootElement) {

        List<String> hiddenElements = new ArrayList<String>();

        hiddenElements = (List<String>) ((JavascriptExecutor) this.driver).executeScript(
                this.excludeTagsJS + this.isDisPlayedJS + this.getElementXPathJS + this.getHiddenElementOfDOMJS +
                        "return getHiddenElementsOfDOM(arguments[0]);",
                rootElement
        );
        return hiddenElements;
    }

    /**
     * Get clickable elements under a root element node.
     *
     * @param rootElement
     * @return
     */
    public List<String> getClickElementsOfDOM(WebElement rootElement) {

        List<String> clickElements = new ArrayList<String>();
        clickElements = (List<String>) ((JavascriptExecutor) this.driver).executeScript(
                this.excludeTagsJS + this.isDisPlayedJS + this.getElementXPathJS + this.getClickElementOfDOMJS +
                        "return getClickElementsOfDOM(arguments[0]);",
                rootElement
        );

        return clickElements;
    }

    /**
     * Go to one state from the root node.
     *
     * @param endNode
     */
    public void goToOneATFromRootAT(ActionTreeNode endNode) {
        Stack<String> clickPathToCurrAT = new Stack<String>();
        ActionTreeNode tempAT = endNode;
        while (tempAT.getParentActionTreeNodes().size() > 0) {

            /* Find the shorted path to the root ActionTreeNode. */
            Map<Integer, ActionTreeNode> pairOfLayerLevel = new HashMap<Integer, ActionTreeNode>();
            for (ActionTreeNode atn : tempAT.getParentActionTreeNodes()) {
                pairOfLayerLevel.put(atn.getLayoutLevel(), atn);
            }

            Object[] keySet = pairOfLayerLevel.keySet().toArray();
            Arrays.sort(keySet);
            ActionTreeNode parAT = pairOfLayerLevel.get(keySet[0]);

            for (String key : parAT.getChildNodes().keySet()) {
                if (parAT.getChildNodes().get(key).equals(tempAT)) {
                    clickPathToCurrAT.push(key);
                    break;
                }
            }
            tempAT = parAT;
        }
        backToOneState(clickPathToCurrAT);
    }

    public void backToOneState(Stack<String> clickPathToCurrAT) {
        this.clickPath.clear();
        while (!clickPathToCurrAT.isEmpty()) {
            this.clickPath.offer(clickPathToCurrAT.pop());
        }

        this.driver.get(this.pageUrl);

        String xpath;
        while ((xpath=this.clickPath.poll()) != null) {
            this.driver.findElement(By.xpath(xpath)).click();
        }
    }

    public int traverse(String clickXPath, int lastLayoutLevel, int index, ActionTreeNode lastAT) {
        String currentUrl = "";
        try {
            driver.findElement(By.xpath(clickXPath)).click();
            currentUrl = this.driver.getCurrentUrl();
        } catch (UnhandledAlertException e) {
            try {
                driver.switchTo().alert().accept();
            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                goToOneATFromRootAT(lastAT);
                return index;
            }
        } catch (Exception e) {
            if (e.getMessage().contains("is not clickable at point")) {
                return index;
            } else {
                e.printStackTrace();
                goToOneATFromRootAT(lastAT);
                return index;
            }
        }

        if (currentUrl.contains("#")) {
            currentUrl = currentUrl.substring(0, currentUrl.indexOf("#"));
        }

        if (!currentUrl.equals(this.pageUrl)) {
            goToOneATFromRootAT(lastAT);
            return index;
        }

        List<String> hiddenElements = getHidElementsOfDOM(driver.findElement(By.tagName("body")));
        for (ActionTreeNode atn : this.actionTreeNodes) {
            if (atn.getHiddenElements().equals(hiddenElements)) {
                if (!atn.equals(lastAT) && !atn.equals(this.actionTreeNodes.get(0))) {
                    atn.addParentActionTreeNode(lastAT);
                }

                goToOneATFromRootAT(lastAT);
                return index;
            }
        }

        ActionTreeNode atn = new ActionTreeNode(++lastLayoutLevel, index);
        atn.setHiddenElements(hiddenElements);

        List<String> elementsFromHidToVis = new ArrayList<String>();
        for (String ele : lastAT.getHiddenElements()) {
            if (!hiddenElements.contains(ele)) {
                elementsFromHidToVis.add(ele);
            }
        }
        List<String> tempElements = new ArrayList<String>();
        for (String ele : elementsFromHidToVis) {
            tempElements.addAll(getClickElementsOfDOM(driver.findElement(By.xpath(ele))));
        }
        atn.setCandiClickElements(tempElements);
        this.actionTreeNodes.add(atn);

        atn.addParentActionTreeNode(lastAT);
        lastAT.addChildNodes(clickXPath, atn);

        goToOneATFromRootAT(lastAT);
        return ++index;
    }
}
