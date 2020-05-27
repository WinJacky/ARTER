package com.crawljax.config;

import java.util.Arrays;
import java.util.List;

public enum AppEnum {

    PHPSHE("http://www.phpshe.com/demo/phpshe/",
            "登录", "商品详情", "购物车", "结算", "提交订单"
    ),

    DISCUZ("http://localhost/discuz/index.php",
        "登录", "版块", "发帖", "提交"
    ),

    WORDPRESS("http://localhost/wordpress/",
            "登录", "文章", "写文章", "发布"
    ),

    NUCLEUS("http://localhost/nucleus/html/index.php3",
            "login", "items", "delete"
    ),

    ESHOP("http://localhost:8080/EShop/mer.do?method=browseIndexMer",
            "登录", "商品详情", "购买", "提交订单"
    ),

    PROMONKEY("http://localhost/ProMonkey/my.php",
            "登录", "添加代码", "提交"
    ),

    SCHOOLMATE("http://localhost/schoolmate/index.php",
            "login", "term", "add"
    ),

    PERSONMANAGER("http://localhost:8080/PersonManager/",
            "登录", "部门", "增加"
    ),

    EXAM("http://localhost:8080/Exam/",
            "进入后台", "登录", "考试题目", "修改", "保存"
    ),

    DOMITORY("http://localhost:8080/domitory/",
            "登录", "宿舍", "添加"
    ),

    PHPMYADMIN("http://localhost/phpmyadmin/",
                       "数据库", "创建", "新建数据表", "执行"
    ),

    BUGFREE("http://localhost/bugfree/index.php/site/login",
                    ""
    );

    public String url;

    public List<String> keywords;

    AppEnum(String url, String ... keywords){
        this.url = url;
        this.keywords = Arrays.asList(keywords);
    }
}
