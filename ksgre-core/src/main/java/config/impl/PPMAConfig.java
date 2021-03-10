package main.java.config.impl;

import com.crawljax.condition.UrlCondition;
import com.crawljax.config.AbstractAppConfig;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.configuration.Form;
import com.crawljax.core.configuration.InputSpecification;

public class PPMAConfig extends AbstractAppConfig {

    @Override
    public void setClick(CrawlSpecification crawler) {
        crawler.addCrawlCondition("PPMA", new UrlCondition("http://localhost/PPMA/Password-Manager"));
        crawler.click("a");
        crawler.click("input").withAttribute("type", "button");
        crawler.click("input").withAttribute("type", "submit");
    }

    @Override
    public void setInputSpecification(CrawlSpecification crawler) {
        super.setInputSpecification(crawler);
        InputSpecification input = new InputSpecification();
        Form form1 = new Form();
        form1.field("user").setValues("admin123");
        form1.field("pwd").setValues("admin123");
        form1.field("pwd1").setValues("admin123");
        form1.field("email").setValues("admin123");
        input.setValuesInForm(form1).beforeClickElement("input").withAttribute("id", "chk");

        Form form2 = new Form();
        form2.field("newiteminput").setValues("Google");
        form2.field("newiteminputpw").setValues("Google123");
        input.setValuesInForm(form2).beforeClickElement("input").withAttribute("id", "newbtn");

        Form form3 = new Form();
        form3.field("user").setValues("admin123");
        form3.field("pwd").setValues("admin123");
        input.setValuesInForm(form3).beforeClickElement("input").withAttribute("value", "Login");

        Form form4 = new Form();
        form4.field("oldpassword").setValues("admin123");
        form4.field("pwd").setValues("admin123");
        form4.field("pwd1").setValues("admin123");
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
