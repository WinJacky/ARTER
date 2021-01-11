package test.java;

import com.crawljax.core.oraclecomparator.comparators.EditDistanceComparator;
import com.crawljax.util.SimHelper;
import org.apache.commons.lang3.StringUtils;

public class TestXpathDistance {
    public static void main(String[] args) {
        String xpath1 = "html[1]/body[1]/table[1]/tbody[1]/tr[2]/td[2]/table[1]/tbody[1]/tr[12]/td[1]/a[1]";
        String xpath2 = "html[1]/body[1]/div[1]/div[3]/ul[1]/li[3]/a[1]";

        double dis1 = SimHelper.simOfXpath(xpath2, xpath1);
        System.out.println(dis1);
        double dis = (double) (1.0 - dis1);
        System.out.println(dis);
    }
}
