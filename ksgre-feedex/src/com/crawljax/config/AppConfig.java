package com.crawljax.config;

import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.plugin.Plugin;

/**
 * 被测App配置
 */
public interface AppConfig {

    /**
     * 设置点击哪些元素
     * @param crawler
     */
    void setClick(CrawlSpecification crawler);

    /**
     * 设置表单输入，表单是否输入随机值
     * @param crawler
     */
    void setInputSpecification(CrawlSpecification crawler);

    /**
     * 状态之间相似性比较插件
     * @return
     */
    Plugin getDomChangedPlugin();

    /**
     * 有些事件的执行是有先后约束关系的，比如点击立即购买之前，先要选择商品型号，
     * 而且这些事件不以表单提交
     */
    void setPreClickConstraints(CrawlSpecification crawler);

    /**
     * 配置阈值
     */
    void setThreshold();
}
