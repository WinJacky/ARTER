package com.crawljax.dataType;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.support.ui.Select;

import java.io.File;

public class PlainElement {
    Select select;

    /* Statement's information. */
    private String seleniumAction;
    private String value;
    private String name;
    private int line;

    /* DOM-based information. */
    private File htmlPage;
    private Point coordinates;
    private Dimension dimension;
    private File domBefore;
    private File domAfter;
    private String xpath;
    private String tagName;
    private String id;
    private String classAttribute;
    private String nameAttribute;
    private String text;
    private String valueAttribute;
    private String typeAttribute;
    private String titleAttribute;

    public PlainElement() {}

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public String getSeleniumAction() {
        return seleniumAction;
    }

    public void setSeleniumAction(String seleniumAction) {
        this.seleniumAction = seleniumAction;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public File getHtmlPage() {
        return htmlPage;
    }

    public void setHtmlPage(File htmlPage) {
        this.htmlPage = htmlPage;
    }

    public Point getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Point coordinates) {
        this.coordinates = coordinates;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public File getDomBefore() {
        return domBefore;
    }

    public void setDomBefore(File domBefore) {
        this.domBefore = domBefore;
    }

    public File getDomAfter() {
        return domAfter;
    }

    public void setDomAfter(File domAfter) {
        this.domAfter = domAfter;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValueAttribute() {
        return valueAttribute;
    }

    public void setValueAttribute(String valueAttribute) {
        this.valueAttribute = valueAttribute;
    }

    public String getTypeAttribute() {
        return typeAttribute;
    }

    public void setTypeAttribute(String typeAttribute) {
        this.typeAttribute = typeAttribute;
    }

    public String getTitleAttribute() {
        return titleAttribute;
    }

    public void setTitleAttribute(String titleAttribute) {
        this.titleAttribute = titleAttribute;
    }
}
