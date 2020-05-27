package test.java;

import com.crawljax.core.Crawler;

public class TestIndic {
    public static void main(String[] args) {
        TestIndic crawler = new TestIndic();
        assignToC(crawler);
        System.out.println(crawler.toString());
    }

    public static void assignToC(TestIndic c) {
        System.out.println(c.toString());
        c = new TestIndic();
        System.out.println(c.toString());
    }

}
