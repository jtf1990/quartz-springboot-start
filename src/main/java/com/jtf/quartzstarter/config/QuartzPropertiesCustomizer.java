package com.jtf.quartzstarter.config;

import java.util.Properties;

/**
 * quartz properties customizer<br/>
 * 用户自定义配置<br/>
 * @author jiangtaofeng
 */
public interface QuartzPropertiesCustomizer {

    void customize(Properties properties);
}
