package test.java.config.impl;

import com.crawljax.condition.UrlCondition;
import com.crawljax.config.AbstractAppConfig;
import com.crawljax.core.TagElement;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;

public class PHPSHEConfig extends AbstractAppConfig {

    @Override
    public void setClick(CrawlSpecification crawler) {
        crawler.addCrawlCondition("Only PHPSHE", new UrlCondition("phpshe.com/demo/phpshe"));
        crawler.click("span").withAttribute("class", "js_ruledata prodata_span");
        crawler.click("a").withAttribute("title", "登录");
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
//        InputSpecification input = new InputSpecification();
//        Form form1 = new Form();
//        form1.field("user_name").setValues("jayshawn");
//        form1.field("user_pw").setValues("gao963680393");
//        input.setValuesInForm(form1).beforeClickElement("input").withAttribute("class", "loginbtn1");
//        Form form2 = new Form();
//        form2.field("cart_id.*").setValue(true);
//        input.setValuesInForm(form2).beforeClickElement("input").withAttribute("value", "结算");
//        crawler.setInputSpecification(input);

    }

    @Override
    public void setPreClickConstraints(CrawlSpecification crawler) {
        super.setPreClickConstraints(crawler);
        TagElement t1 = new TagElement("a");
        t1.addAttribute("class", "fl ljgm");
        TagElement t2 = new TagElement("a");
        t2.addAttribute("class", "fl jiagwc");
        TagElement t3 = new TagElement("span");
        t3.addAttribute("class", "js_ruledata prodata_span");
    }

    @Override
    public void setThreshold() {
        super.setThreshold();
    }
}
