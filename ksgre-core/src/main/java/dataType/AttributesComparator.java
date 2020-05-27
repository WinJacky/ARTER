package main.java.dataType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class AttributesComparator implements Comparator<String> {

    List<String> definedOrder = Arrays.asList("id", "name", "class", "title", "alt", "value");

    @Override
    public int compare(String o1, String o2) {
        return Integer.valueOf(definedOrder.indexOf(o1)).compareTo(Integer.valueOf(definedOrder.indexOf(o2)));
    }
}
