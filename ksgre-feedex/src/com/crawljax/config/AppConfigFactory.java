package com.crawljax.config;

import org.apache.log4j.Logger;

public class AppConfigFactory {

    private static Logger LOGGER = Logger.getLogger(AppConfigFactory.class);
    public static AppConfig getInstance(AppEnum appEnum) {
        if (appEnum == null) {
            return null;
        }
        AppConfig appConfig = null;
        try {
            appConfig = (AppConfig) Class.forName("com.crawljax.example.config.impl." + appEnum + "Config").getConstructor().newInstance();
        } catch (Exception e) {
            LOGGER.info(appEnum + " 未实现AppConfig，默认使用AbstractAppConfig");
        }
        if (appConfig == null) {
            return new AbstractAppConfig();
        }

        return appConfig;

    }
}
