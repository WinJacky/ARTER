package com.crawljax.forms;

public class InputValue {
    private long id;
    private String value;
    private boolean checked = false;

    public InputValue() {

    }

    public InputValue(String value) {
        this(value, true);
    }

    public InputValue(String value, boolean checked) {
        this.value = value;
        this.checked = checked;
    }

    @Override
    public String toString() {
        return getValue();
        // + " formInput " + formInput.getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
