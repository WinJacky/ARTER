package com.crawljax.core.state;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawljaxException;
import com.crawljax.forms.FormInput;
import com.crawljax.util.XPathHelper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jgrapht.graph.DefaultEdge;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Eventable extends DefaultEdge implements Serializable {
    private static final long serialVersionUID = 3229708706467350994L;
    private long id;
    private EventType eventType;
    private Identification identification;
    private Element element;
    private List<FormInput> relatedFormInputs = new ArrayList<FormInput>();
    private String relatedFrame = "";

    public enum EventType {
        click, hover
    }

    public Eventable() {

    }

    public Eventable(Identification identification, EventType eventType) {
        this.identification = identification;
        this.eventType = eventType;
    }

    public Eventable(Identification identification, EventType eventType, String relatedFrame) {
        this(identification, eventType);
        this.relatedFrame = relatedFrame;
    }

    public Eventable(Node node, EventType eventType) {
        this(new Identification(Identification.How.xpath, XPathHelper.getXPathExpression(node)),
                eventType);
        this.element = new Element(node);
    }

    public Eventable(CandidateElement candidateElement, EventType eventType) {
        this(candidateElement.getIdentification(), eventType);
        if (candidateElement.getElement() != null) {
            this.element = new Element(candidateElement.getElement());
        }
        this.relatedFormInputs = candidateElement.getFormInputs();
        this.relatedFrame = candidateElement.getRelatedFrame();
    }

    /**
     * {@inheritDoc}
     *
     * @return the String representation of this Eventable
     */
    @Override
    public String toString() {
        String str = "";
        if (this.getElement() != null) {
            str = this.getElement().toString();
        }
        str += " " + this.eventType + " " + this.identification.toString();
        return str;
    }

    /**
     * Returns a hashcode. Uses reflection to determine the fields to test.
     *
     * @return the hashCode
     */
    @Override
    public int hashCode() {
        String[] exclude = new String[1];
        exclude[0] = "id";

        return HashCodeBuilder.reflectionHashCode(this, exclude);
    }

    /**
     * Return true if equal. Uses reflection.
     *
     * @param obj
     *            the object to compare to of type Eventable
     * @return true if both Objects are equal
     */

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Eventable)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        Eventable rhs = (Eventable) obj;

        return new EqualsBuilder().append(toString(), rhs.toString()).isEquals();
    }

    /**
     * @return the eventType.
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the identification
     */
    public Identification getIdentification() {
        return identification;
    }

    /**
     * @param identification
     *            the identification to set
     */
    public void setIdentification(Identification identification) {
        this.identification = identification;
    }

    /**
     * @param eventType
     *            the eventType to set
     */
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * @return the element
     */
    public Element getElement() {
        return element;
    }

    /**
     * @param element
     *            the element to set
     */
    public void setElement(Element element) {
        this.element = element;
    }

    /**
     * Retrieve the related form inputs.
     *
     * @return the formInputs
     */
    public List<FormInput> getRelatedFormInputs() {
        return relatedFormInputs;
    }

    /**
     * Set the list of formInputs.
     *
     * @param relatedFormInputs
     *            the list of formInputs
     */
    public void setRelatedFormInputs(List<FormInput> relatedFormInputs) {
        this.relatedFormInputs = relatedFormInputs;
    }

    /* Horrible stuff happening below, don't look at it! */

    /**
     * @return the source state.
     * @throws CrawljaxException
     *             if the source cannot be found.
     */
    public StateVertix getSourceStateVertix() throws CrawljaxException {
        return getSuperField("source");
    }

    /**
     * @return the target state.
     * @throws CrawljaxException
     *             if the target cannot be found.
     */
    public StateVertix getTargetStateVertix() throws CrawljaxException {
        return getSuperField("target");
    }

    private StateVertix getSuperField(String name) throws CrawljaxException {
        try {
            return (StateVertix) searchSuperField(name).get(this);
        } catch (IllegalArgumentException e) {
            throw new CrawljaxException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new CrawljaxException(e.getMessage(), e);
        }

    }

    private Field searchSuperField(String name) {
        Class<?> clazz = this.getClass().getSuperclass().getSuperclass();
        Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);

        for (Field field : fields) {
            String fieldName = field.getName();

            if (name.equals(fieldName)) {
                return field;
            }
        }
        throw new InternalError("Field was not found!");
    }

    /**
     * @return the relatedFrame
     */
    public String getRelatedFrame() {
        return relatedFrame;
    }
}
