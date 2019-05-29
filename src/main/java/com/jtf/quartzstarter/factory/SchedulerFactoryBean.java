package com.jtf.quartzstarter.factory;


import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Properties;

/**
 * Scheduler 工厂 bean
 * @author jiangtaofeng
 */
public class SchedulerFactoryBean implements FactoryBean<Scheduler>,DisposableBean, ApplicationListener<ContextRefreshedEvent> {


    private SchedulerFactoryBuilder schedulerFactoryBuilder;

    private SchedulerFactory schedulerFactory;

    private Logger logger = LoggerFactory.getLogger(SchedulerFactoryBean.class);

    // jdk 动态代理
    private Scheduler scheduler ;






    public SchedulerFactoryBean(SchedulerFactoryBuilder schedulerFactoryBuilder) throws SchedulerException {
        this.schedulerFactoryBuilder = schedulerFactoryBuilder;
    }



    @Override
    public Scheduler getObject() throws Exception {
        if(Objects.isNull(scheduler)){
           scheduler = (Scheduler) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                   new Class<?>[]{Scheduler.class},
                   new SchedulerProxy());
        }
        return scheduler;
    }

    public Properties findConfig(){
        return schedulerFactoryBuilder.getConfigProperties();
    }
    @Override
    public Class<?> getObjectType() {
        return Scheduler.class;
    }



    private Scheduler getSchedulerImpl() throws SchedulerException {
        if(Objects.isNull(schedulerFactory)){
            flushFactory();
        }
        return schedulerFactory.getScheduler();
    }

    public void flush() {
        if(Objects.isNull(schedulerFactory)){
            flushFactory();
        }
        startScheduler();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        flush();

    }

    public void flushFactory(){
        if(Objects.nonNull(schedulerFactory)){
            try {
                schedulerFactory.getScheduler().shutdown();
            } catch (SchedulerException e) {
                logger.warn("Quartz shutdown ", e);
            }
        }
        schedulerFactory = schedulerFactoryBuilder.builder();
    }

    private void startScheduler(){
        try {
            schedulerFactory.getScheduler().start();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if(Objects.nonNull(scheduler)){
            scheduler.shutdown();
        }
    }

    // JDK 动态代理
    public class SchedulerProxy implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(getSchedulerImpl(), args);
        }
    }


}
