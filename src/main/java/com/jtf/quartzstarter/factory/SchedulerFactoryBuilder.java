package com.jtf.quartzstarter.factory;

import org.quartz.SchedulerFactory;

import java.util.Properties;

/**
 * SchedulerFactory Builder
 */
public interface SchedulerFactoryBuilder {

    /**
     * 创建一个新的SchedulerFactry
     * @return
     */
    SchedulerFactory builder();



    /**
     * 获取实际配置
     * @return
     */
    Properties getConfigProperties();


}
