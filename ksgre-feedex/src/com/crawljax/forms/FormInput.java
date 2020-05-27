package com.crawljax.forms;

import com.crawljax.core.state.Eventable;
import com.crawljax.core.state.Identification;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashSet;
import java.util.Set;

public class FormInput {
    private long id;
    private String type = "text";

    private Identification identification;
    private Set<InputValue> inputValues = new HashSet<InputValue>();
    private Eventable eventable;

    private boolean multiple;

    public FormInput() {
        super();
    }

    public FormInput(String type, Identification identification, String value) {
        this.type = type;
        this.identification = identification;
        inputValues.add(new InputValue(value, value.equals("1")));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (!"".equals(type)) {
            this.type = type;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FormInput)) {
            return false;
        }

        if (this == obj) {
            return true;
        }
        final FormInput rhs = (FormInput) obj;

        return new EqualsBuilder().append(this.identification, rhs.getIdentification()).append(
                this.type, rhs.getType()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.identification).append(this.type).toHashCode();
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static boolean containsInput(Set<FormInput> inputs, Identification identification) {
        for (FormInput input : inputs) {
            if (input.getIdentification().equals(identification)) {
                return true;
            }
        }

        return false;
    }

    public static FormInput getInput(Set<FormInput> inputs, Identification identification) {
        for (FormInput input : inputs) {
            if (input.getIdentification().equals(identification)) {
                return input;
            }
        }

        return null;
    }

    public Identification getIdentification() {
        return identification;
    }

    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    public Set<InputValue> getInputValues() {
        return inputValues;
    }

    public void setInputValues(Set<InputValue> inputValues) {
        this.inputValues = inputValues;
    }

    public Eventable getEventable() {
        return eventable;
    }

    public void setEventable(Eventable eventable) {
        this.eventable = eventable;
    }
}
