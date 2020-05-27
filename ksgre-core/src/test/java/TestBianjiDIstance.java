package test.java;

import com.crawljax.core.oraclecomparator.comparators.EditDistanceComparator;
import com.crawljax.util.SimHelper;
import org.apache.commons.lang3.StringUtils;

public class TestBianjiDIstance {
    public static void main(String[] args) {
        String xpath1 = "/HTML[1]/BODY[1]/FORM[1]/DIV[1]/DIV[1]/DIV[1]/INPUT[1]";
        String xpath2 = "html[1]/body[1]/form[1]/div[1]/div[1]/div[1]";

        double dis1 = SimHelper.simOfXpath(xpath2, xpath1);
        System.out.println(1.0/7.0);
        System.out.println(dis1);
        double dis = (double) (1.0 - dis1)
                / Math.max(xpath2.length(), xpath2.length());
        System.out.println(dis);
    }
}
