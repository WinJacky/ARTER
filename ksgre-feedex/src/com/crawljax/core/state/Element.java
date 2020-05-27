package com.crawljax.core.state;

import com.crawljax.util.Helper;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示一个element，通过node name以及node text contents创建。
 */
public class Element implements Serializable {
    private static final long serialVersionUID = -1608999189549530008L;
    private static final int HASHCONST = 32;

    private Node node;
    private long id;
    private String tag;
    private String text;
    private List<Attribute> attributes = new ArrayList<Attribute>();

    public Element() {
    }

    public Element(Node node) {
        if (node != null) {
            this.node = node;
            this.tag = node.getNodeName();
            if (node.getTextContent() == null) {
                this.text = "";
            } else {
                this.text = Helper.removeNewLines(node.getTextContent()).trim();
            }
            attributes = new ArrayList<Attribute>();
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                Node attr = node.getAttributes().item(i);
                Attribute attribute = new Attribute(attr.getNodeName(), attr.getNodeValue());
                attributes.add(attribute);
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        if (!this.getText().equals("")) {
            str.append("\"");
            str.append(getText());
            str.append("\" ");
        }
        str.append(getTag().toUpperCase());
        str.append(":");
        if (getAttributes() != null) {
            for (Attribute attribute : getAttributes()) {
                str.append(" ");
                str.append(attribute.toString());
            }
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object instanceof Element)) {
            return false;
        }
        return toString().equals(((Element) object).toString());
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result;
        if (attributes != null) {
            result += attributes.hashCode();
        }
        result = prime * result + (int) (id ^ (id >>> HASHCONST));
        result = prime * result;
        if (node != null) {
            result += node.hashCode();
        }
        result = prime * result;
        if (tag != null) {
            result += tag.hashCode();
        }
        result = prime * result;
        if (text != null) {
            result += text.hashCode();
        }
        return result;
    }

    /**
     * 判断两个elements的属性是否完全相同.
     * @param otherElement
     * @return
     */
    public boolean equalAttributes(Element otherElement) {
        return getAttributes().toString().equalsIgnoreCase(
                otherElement.getAttributes().toString());
    }

    /**
     * 判断element的id是否相同.
     * @param otherElement
     * @return
     */
    public boolean equalId(Element otherElement) {
        if (getElementId() == null || otherElement.getElementId() == null) {
            return false;
        }
        return getElementId().equalsIgnoreCase(otherElement.getElementId());
    }

    public boolean equalText(Element otherElement) {
        return getText().equalsIgnoreCase(otherElement.getText());
    }

    /**
     * 获取元素id
     * @return
     */
    public String getElementId() {
        for (Attribute attribute : getAttributes()) {
            if (attribute.getName().equalsIgnoreCase("id")) {
                return attribute.getValue();
            }
        }
        return null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public Node getNode() {
        return node;
    }
}
