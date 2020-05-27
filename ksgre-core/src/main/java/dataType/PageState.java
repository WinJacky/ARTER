package main.java.dataType;

import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageState {

    private List<String> hiddenElements = new ArrayList<String>();
    private List<String> candiClickElements = new ArrayList<String>();
    private String hashOfDriver;

    private int layoutLevel;
    private int index;

    private List<PageState> parentPageStates = new ArrayList<PageState>();

    private Map<String, PageState> childElements = new HashMap<String, PageState>();

    public PageState(int layoutLevel, int index, List<String> hiddenElements, List<String> candiClickElements) {
        this.hiddenElements = hiddenElements;
        this.candiClickElements = candiClickElements;
        this.layoutLevel = layoutLevel;
        this.index = index;
    }

    public PageState(int layoutLevel, int index) {
        this.layoutLevel = layoutLevel;
        this.index = index;
    }

    public PageState() {

    }

    public List<String> getHiddenElements() {
        return hiddenElements;
    }

    public void setHiddenElements(List<String> hiddenElements) {
        this.hiddenElements = hiddenElements;
    }

    public List<String> getCandiClickElements() {
        return candiClickElements;
    }

    public void setCandiClickElements(List<String> candiClickElements) {
        this.candiClickElements = candiClickElements;
    }

    public String getHashOfDriver() {
        return hashOfDriver;
    }

    public void setHashOfDriver(String hashOfDriver) {
        this.hashOfDriver = hashOfDriver;
    }

    public int getLayoutLevel() {
        return layoutLevel;
    }

    public void setLayoutLevel(int layoutLevel) {
        this.layoutLevel = layoutLevel;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List<PageState> getParentPageStates() {
        return parentPageStates;
    }

    public void setParentPageStates(List<PageState> parentPageStates) {
        this.parentPageStates = parentPageStates;
    }

    public void addParentPageState(PageState pageState) {
        this.parentPageStates.add(pageState);
    }

    public Map<String, PageState> getChildElements() {
        return childElements;
    }

    public void setChildElements(Map<String, PageState> childElements) {
        this.childElements = childElements;
    }

    public void addChildElement(String clickPath, PageState pageState) {
        this.childElements.put(clickPath, pageState);
    }

    public String getLayoutLevelIndex() {
        return this.layoutLevel + "_" + this.index;
    }
}
