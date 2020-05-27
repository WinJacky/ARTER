package main.java.config.impl;

import com.crawljax.condition.UrlCondition;
import com.crawljax.config.AbstractAppConfig;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;

public class PPMAConfig extends AbstractAppConfig {

    @Override
    public void setClick(CrawlSpecification crawler) {
        crawler.addCrawlCondition("PPMA", new UrlCondition("http://localhost/ppma"));
        crawler.click("a");
        crawler.click("input").withAttribute("type", "button");
        crawler.click("input").withAttribute("type", "submit");
//        crawler.click("span").withAttribute("class", "js_ruledata prodata_span");
//        crawler.dontClick("a").withAttribute("title", "注册");
//        crawler.dontClick("a").withAttribute("title", "退出");
//        crawler.dontClick("a").withAttribute("title", "我的订单");
//        crawler.dontClick("a").withAttribute("title", "签到有礼");
//        crawler.dontClick("a").withAttribute("title", "资讯中心");
//        crawler.dontClick("a").withAttribute("title", "领券中心");
//        crawler.dontClick("a").withText("购买授权");
//        crawler.dontClick("a").withText("简好网络");
//        crawler.dontClick("a").underXPath("//div[@class='foot']");
//        crawler.dontClick("a").underXPath("//div[@style='background:#fff; padding-bottom:20px;']");
//        crawler.dontClick("a").underXPath("//div[@class='celan']");
//        crawler.dontClick("a").underXPath("//div[@id='phone_html']");
//        crawler.dontClick("a").underXPath("//div[@class='huiyuan_left']");
//        crawler.dontClick("a").withText("最新订单");
//        crawler.dontClick("a").underXPath("//div[@class='brand_zm']");
//        crawler.dontClick("a").underXPath("//div[@class='now']");
//        crawler.dontClick("a").underXPath("//div[@class='pinpai_list']");
//        crawler.dontClick("a").underXPath("//div[@class='list_sx']");
//        crawler.dontClick("a").withText("继续购物");
//        crawler.dontClick("a").withText("+ 新增收货地址");
//        crawler.dontClick("a").underXPath("//div[@class='fr huiyuan_main']");
//        crawler.dontClick("a").withAttribute("title", "GXG");
    }

    @Override
    public void setInputSpecification(CrawlSpecification crawler) {
        super.setInputSpecification(crawler);
        InputSpecification input = new InputSpecification();
        Form form1 = new Form();
        form1.field("user").setValues("heli123");
        form1.field("pwd").setValues("heli123");
        form1.field("pwd1").setValues("heli123");
        form1.field("email").setValues("heli123");
        input.setValuesInForm(form1).beforeClickElement("input").withAttribute("id", "chk");

        Form form2 = new Form();
        form2.field("newiteminput").setValues("Google");
        form2.field("newiteminputpw").setValues("Google123");
        input.setValuesInForm(form2).beforeClickElement("input").withAttribute("id", "newbtn");

        Form form3 = new Form();
        form3.field("user").setValues("heli123");
        form3.field("pwd").setValues("heli123");
        input.setValuesInForm(form3).beforeClickElement("input").withAttribute("value", "Login");

        Form form4 = new Form();
        form4.field("oldpassword").setValues("heli123");
        form4.field("pwd").setValues("heli456");
        form4.field("pwd1").setValues("heli456");
        input.setValuesInForm(form3).beforeClickElement("button").withAttribute("id", "changepw");

        crawler.setInputSpecification(input);
    }

    @Override
    public void setPreClickConstraints(CrawlSpecification crawler) {
        super.setPreClickConstraints(crawler);
//        TagElement t1 = new TagElement("a");
//        t1.addAttribute("class", "fl ljgm");
//        TagElement t2 = new TagElement("a");
//        t2.addAttribute("class", "fl jiagwc");
//        TagElement t3 = new TagElement("span");
//        t3.addAttribute("class", "js_ruledata prodata_span");
    }

    @Override
    public void setThreshold() {
        super.setThreshold();
    }
}
