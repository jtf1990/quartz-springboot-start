package com.jtf.quartzstarter.config;



import com.jtf.quartzstarter.factory.SchedulerFactoryBean;
import com.jtf.quartzstarter.support.job.DeleteJobLoggerJob;
import com.jtf.quartzstarter.support.job.SpringJob;
import com.jtf.quartzstarter.support.job.SpringStateFullJob;
import com.jtf.quartzstarter.support.web.*;
import org.quartz.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * web 自动配置<br/>
 * 默认web url 前缀为/quartz<br/>
 * 可以在配置文件中配置 quartz.config.web-prefix 来修改url前缀
 * @author jiangtaofeng
 */
@Configuration
@ConditionalOnBean(value = {Scheduler.class, SchedulerFactoryBean.class})
@ConditionalOnClass(value = {WebMvcAutoConfiguration.class})
@AutoConfigureAfter(value = QuartzAutoConfigure.class)
@AutoConfigureBefore(WebMvcAutoConfiguration.class)
public class QuartzWebAutoConfigure {


    public static final String KEY_WEB_PREFIX="quartz.config.web-prefix";

    public static final String DEFAULT_WEB_PREFIX = "/quartz";

    public QuartzWebAutoConfigure(){
    }

    /**
     * web controller
     * @param environment
     * @return
     */
    @Bean()
    public BaseQuartzWebController baseQuartzWebController(Environment environment){

        if(StringUtils.hasText(environment.getProperty(KEY_WEB_PREFIX))){
            return new CustomerQuartzWebController();
        }
        return new DefaultQuartzWebController();
    }

    /**
     * 注册quartz Job
     * @param objectProvider
     * @return
     */
    @Bean
    public JobClassRegisterContext jobClassRegisterContext(ObjectProvider<JobClassRegister[]> objectProvider ){
        JobClassRegisterContext jobClassRegisterContext = new JobClassRegisterContext();
        jobClassRegisterContext.add(new SpringJob()).add(new SpringStateFullJob()).add(new DeleteJobLoggerJob());
        JobClassRegister[] jobClassRegisterArray = objectProvider.getIfAvailable();
        if(Objects.nonNull(jobClassRegisterArray)){
            for (JobClassRegister jobClassRegister : jobClassRegisterArray) {
                if(Objects.nonNull(jobClassRegister)){
                    jobClassRegisterContext.add(jobClassRegister);
                }
            }
        }
        return jobClassRegisterContext;
    }


}
