package main.java.config;

public enum AppEnum {

    PHPSHE("PHPSHE", "main.resources.PHPSHE", "http://localhost/phpshe/"),

    PPMA("PPMA", "main.resources.PPMA", "http://localhost/ppma/"),

    ESHOP("EShop","main.resources.EShop","http://localhost:8080/EShop/"),

    ADDR("ADDR","main.resources.ADDR","http://localhost/addr/"),

    MIAOSHA("MIAOSHA", "main.resources.MIAOSHA", "http://localhost:8080/miaosha/login");

    private String appName;

    // 旧版本测试用例存放的包名
    private String testSuite;

    private String url;

    AppEnum(String appName, String testSuite, String url) {
        this.appName = appName;
        this.testSuite = testSuite;
        this.url = url;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(String testSuite) {
        this.testSuite = testSuite;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
