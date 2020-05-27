package com.crawljax.config;

import com.crawljax.config.AppConfig;
import com.crawljax.core.configuration.CrawlSpecification;
import com.crawljax.core.oraclecomparator.comparators.SimpleComparator;
import com.crawljax.core.plugin.DomChangeNotifierPlugin;
import com.crawljax.core.plugin.Plugin;
import com.crawljax.core.state.Eventable;

public class AbstractAppConfig implements AppConfig {

    @Override
    public void setClick(CrawlSpecification crawler) {
        crawler.clickDefaultElements();
    }

    @Override
    public void setInputSpecification(CrawlSpecification crawler) {
        crawler.setRandomInputInForms(false);
    }

    @Override
    public Plugin getDomChangedPlugin() {
        return new DomChangeNotifierPlugin() {
            @Override
            public boolean isDomChanged(String domBefore, Eventable e, String domAfter) {
                SimpleComparator comparator = new SimpleComparator();
                comparator.setOriginalDom(domBefore);
                comparator.setNewDom(domAfter);
                return !comparator.isEquivalent();
            }
        };
    }

    @Override
    public void setPreClickConstraints(CrawlSpecification crawler) {}

    @Override
    public void setThreshold() {
        // enum默认值
    }
}
