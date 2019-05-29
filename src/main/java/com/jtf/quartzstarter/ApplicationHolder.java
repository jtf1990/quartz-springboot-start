package com.jtf.quartzstarter;


import org.springframework.context.ApplicationContext;

/**
 * 持有Application 供用户使用
 * @author jiangtaofeng
 */
public class ApplicationHolder {

    private static ApplicationContext applicationContext;


    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext){
        ApplicationHolder.applicationContext = applicationContext;
    }

}
