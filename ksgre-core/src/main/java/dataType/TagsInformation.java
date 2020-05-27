package main.java.dataType;

import main.java.util.UtilsXPath;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class saves all relevant tags' information of the html state of one statement in a test case.
 */
public class TagsInformation {

    private String xpath;
    private String tagName;
    private String id;
    private String classAttribute;
    private String nameAttribute;
    private String textualContent;
    private List<TagsInformation> tagsInformation = new ArrayList<TagsInformation>();
    private String valueAttribute;

    public TagsInformation(JavascriptExecutor js, WebElement e) {
        this.xpath = UtilsXPath.getElementXPath(js, e).substring(1);
        this.tagName = e.getTagName();
        this.id = e.getAttribute("id");
        this.classAttribute = e.getAttribute("class");
        this.nameAttribute = e.getAttribute("name");
        this.textualContent = e.getAttribute("textContent").trim();
    }

    public TagsInformation() {

    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassAttribute() {
        return classAttribute;
    }

    public void setClassAttribute(String classAttribute) {
        this.classAttribute = classAttribute;
    }

    public String getNameAttribute() {
        return nameAttribute;
    }

    public void setNameAttribute(String nameAttribute) {
        this.nameAttribute = nameAttribute;
    }

    public String getTextualContent() {
        return textualContent;
    }

    public void setTextualContent(String textualContent) {
        this.textualContent = textualContent;
    }

    public List<TagsInformation> getTagsInformation() {
        return tagsInformation;
    }

    public void setTagsInformation(List<TagsInformation> tagsInformation) {
        this.tagsInformation = tagsInformation;
    }

    public void addTagsInformation(TagsInformation tagsInformation) {
        this.tagsInformation.add(tagsInformation);
    }

    public String getValueAttribute() {
        return valueAttribute;
    }

    public void setValueAttribute(String valueAttribute) {
        this.valueAttribute = valueAttribute;
    }

    @Override
    public String toString() {
        return "TagsInformation [xpath=" + xpath + ", tagName=" + tagName + ", id=" + id + ", classAttribute=" + classAttribute + ", nameAttribute" + nameAttribute + ", textualcontent=" + textualContent +"]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int resutlt = 1;
        resutlt = prime * resutlt + ((xpath == null) ? 0 : xpath.hashCode());
        return resutlt;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        TagsInformation other = (TagsInformation) obj;
        if(xpath == null) {
            if(other.xpath != null)
                return false;
        } else if(!xpath.equals(other.xpath))
            return false;
        return true;
    }
}
