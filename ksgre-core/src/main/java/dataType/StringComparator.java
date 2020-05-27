package main.java.dataType;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {

        if (o1.length() != o2.length())
            return o1.length() < o2.length() ? -1 : 1;
        else {
            for (int i=0; i<o1.length(); i++) {
                if (o1.charAt(i) == o2.charAt(i))
                    continue;
                if (o1.charAt(i) < o2.charAt(i)) {
                    return -1;
                }
                if (o1.charAt(i) > o2.charAt(i)) {
                    return 1;
                }
            }
            return 0;
        }
    }

}
