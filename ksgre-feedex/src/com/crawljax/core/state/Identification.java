package com.crawljax.core.state;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openqa.selenium.By;

import java.io.Serializable;

/**
 * 表示一个特殊的元素，指定一个method以及value.
 */
public class Identification implements Serializable {
    private static final long serialVersionUID = 1608879189549535808L;

    public enum How {
        xpath, name, id, tag, text, partialText, css, clazz
    }

    private long id;
    private How how;
    private String value;

    public Identification() {
    }

    public Identification(How how, String value) {
        this.how = how;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public How getHow() {
        return how;
    }

    public void setHow(How how) {
        this.how = how;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 将一个 Identification 转换成一个 By.
     * @return
     */
    public By getWebDriverBy() {
        switch (how) {
            case name:
                return By.name(this.value);

            case id:
                return By.id(this.value);

            case xpath:
                return By.xpath(this.value.replaceAll("/BODY\\[1\\]/", "/BODY/"));

            case tag:
                return By.tagName(this.value);

            case text:
                return By.linkText(this.value);

            case partialText:
                return By.partialLinkText(this.value);

            case css:
                return By.cssSelector(this.value);

            case clazz:
                return By.className(this.value);

            default:
                return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Identification)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        final Identification rhs = (Identification) obj;

        return new EqualsBuilder().append(this.how, rhs.getHow()).append(this.value, rhs.getValue()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.how).append(this.value).toHashCode();
    }
}
