package test.java;

import com.crawljax.core.state.Identification;

public class TestIdentification {
    public static void main(String[] args) {
        Identification id = new Identification(Identification.How.xpath, "");
        System.out.println(id.getHow().toString());
    }
}
