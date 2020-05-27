package main.java.dataType;

import java.io.Serializable;

/**
 * This class is used to save the ancestor(not always direct) information of one element.
 * E.g. input -> form li -> ul
 */
public class ElementApendInformation implements Serializable {

    private String ancestorTagName;
    private int sameSiblingNum;
    private String locator;

    public ElementApendInformation(String ancestorTagName, int sameSiblingNum, String locator) {
        this.ancestorTagName = ancestorTagName;
        this.sameSiblingNum = sameSiblingNum;
        this.locator = locator;
    }

    public String getAncestorTagName() {
        return ancestorTagName;
    }

    public void setAncestorTagName(String ancestorTagName) {
        this.ancestorTagName = ancestorTagName;
    }

    public int getSameSiblingNum() {
        return sameSiblingNum;
    }

    public void setSameSiblingNum(int sameSiblingNum) {
        this.sameSiblingNum = sameSiblingNum;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }
}
