package main.java.dataType;

import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionTreeNode {

    private List<String> hiddenElements;
    private List<String> candiClickElements;
    private List<ActionTreeNode> parentActionTreeNodes = new ArrayList<ActionTreeNode>();
    private Map<String, ActionTreeNode> childNodes = new HashMap<String, ActionTreeNode>();

    private int layoutLevel;
    private int index;

    public ActionTreeNode(int layoutLevel, int index, List<String> hiddenElements, List<String> candiClickElements) {
        this.hiddenElements = hiddenElements;
        this.candiClickElements = candiClickElements;
        this.layoutLevel = layoutLevel;
        this.index = index;
    }

    public ActionTreeNode(int layoutLevel, int index) {
        this.layoutLevel = layoutLevel;
        this.index = index;
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

    public List<ActionTreeNode> getParentActionTreeNodes() {
        return parentActionTreeNodes;
    }

    public void setParentActionTreeNodes(List<ActionTreeNode> parentActionTreeNodes) {
        this.parentActionTreeNodes = parentActionTreeNodes;
    }

    public Map<String, ActionTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(Map<String, ActionTreeNode> childNodes) {
        this.childNodes = childNodes;
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

    public void addParentActionTreeNode(ActionTreeNode actionTreeNode) {
        this.parentActionTreeNodes.add(actionTreeNode);
    }

    public void addChildNodes(String clickPath, ActionTreeNode actionTreeNode) {
        this.childNodes.put(clickPath, actionTreeNode);
    }
}
