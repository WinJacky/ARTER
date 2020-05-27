package test.java;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.PatternSyntaxException;

public class TestIsExternal {
    public static void main(String[] args) {
        String location = "http://localhost/phpshe/index.php?mod=user&act=orderlist";
//        String link = "http://localhost/phpshe/user/logout";
        String link = "http://localhost/phpshe/index.php?mod=order&act=cartlist";
        try {
            URL locationUrl = new URL(location);
            try {
                URL linkUrl = new URL(link);
                if (linkUrl.getHost().equals(locationUrl.getHost())) {
                    String linkPath = getBasePath(linkUrl);
                    String locationPath = getBasePath(locationUrl);
                    System.out.println(linkPath.startsWith(locationPath));
                }
            } catch (MalformedURLException e) {
            }
        } catch (MalformedURLException e) {
        }
    }

    private static String getBasePath(URL url) {
        String file = url.getFile().replaceAll("\\*", "");

        try {
            return url.getPath().replaceAll(file, "");
        } catch (PatternSyntaxException pe) {
            return "";
        }

    }
}
