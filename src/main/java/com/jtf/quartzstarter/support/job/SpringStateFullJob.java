package com.jtf.quartzstarter.support.job;


import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.PersistJobDataAfterExecution;

/**
 * 从spring容器中获取执行任务,不能同时进行
 * @author jiangtaofeng
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SpringStateFullJob extends SpringJob {

    @Override
    public Class<? extends Job> getExecuteClass() {
        return SpringStateFullJob.class;
    }
    @Override
    public String getDescribe() {
        return "从spring容器中获取执行任务,不能同时进行";
    }
}
