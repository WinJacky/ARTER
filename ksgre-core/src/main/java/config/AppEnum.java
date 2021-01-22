package main.java.config;

public enum AppEnum {

    //Password-Manager-5.12
    //Password-Manager-5.13
    //Password-Manager-6.0
    //Password-Manager-8.0
    //Password-Manager-9.0
    //Password-Manager-9.08
    //Password-Manager-9.09
    PPMA("PPMA", "main.resources.PPMA", "http://localhost/PPMA/Password-Manager-5.13/"),

    //addressbookv2.0
    //addressbookv3.0
    //addressbookv4.0
    //addressbookv5.0
    //addressbookv6.0
    //addressbookv7.0
    ADDR("ADDR","main.resources.ADDR","http://localhost/ADDR/addressbookv4.0/"),

    //NucleusCMS-Nucleus-3-41/nucleus
    //NucleusCMS-Nucleus-3-50/nucleus
    //NucleusCMS-Nucleus-3-51/nucleus
    //NucleusCMS-Nucleus-3-60/nucleus
    //NucleusCMS-Nucleus-3-62/nucleus
    //NucleusCMS-Nucleus-3-64/nucleus
    //NucleusCMS-Nucleus-3-65/nucleus
    //NucleusCMS-Nucleus-3-66/nucleus
    //NucleusCMS-Nucleus-3-70/nucleus
    Nucleus("Nucleus","main.resources.Nucleus","http://localhost/NucleusCMS-Nucleus-3-70/nucleus/"),

    //phpshe-free_v1.5
    //phpshe-free_v1.6
    //phpshe-free_v1.7
    PHPSHE("PHPSHE", "main.resources.PHPSHE", "http://localhost/phpshe-free_v1.6/"),

    //claroline90
    //claroline100
    //claroline110
    Claroline("Claroline","main.resources.Claroline","http://localhost/claroline100/"),

    ESHOP("EShop","main.resources.EShop","http://localhost:8080/EShop/"),

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
