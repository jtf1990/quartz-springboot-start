package com.jtf.quartzstarter.support.web;

import java.util.*;

/**
 * 任务注册器容器
 * @author jiangtaofeng
 */
public class JobClassRegisterContext {

    private Set<JobClassRegister> set = new TreeSet<>((t1,t2)->t1.getExecuteClass().getName().compareTo(t2.getExecuteClass().getName()));

    public JobClassRegisterContext add(JobClassRegister jobClassRegister){
        set.add(jobClassRegister);
        return this;
    }

    public JobClassRegisterContext addAll(Collection<JobClassRegister> jobClassRegisterCollection){
        jobClassRegisterCollection.forEach(t->add(t));
        return this;
    }

    public Collection<JobClassRegister> getJobClassRegisterCollection(){
        return Collections.unmodifiableCollection(set);
    }



}
