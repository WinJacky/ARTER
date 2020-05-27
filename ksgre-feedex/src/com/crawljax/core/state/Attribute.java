package com.crawljax.core.state;

/**
 * 表示一个element的一个attribute.
 */
public class Attribute {
    private long id;
    private String name;
    private String value;

    public Attribute() {
    }

    public Attribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        if (name != null) {
            return name.trim();
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        if (value != null) {
            return value.trim();
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getName() + "=\"" + getValue() + "\"";
    }
}
