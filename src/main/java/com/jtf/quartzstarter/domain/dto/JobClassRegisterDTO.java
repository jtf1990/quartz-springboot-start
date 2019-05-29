package com.jtf.quartzstarter.domain.dto;

import com.jtf.quartzstarter.support.web.JobClassRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * quartz 任务的描述类
 * @author jiangtaofeng
 */
public class JobClassRegisterDTO {

    /**
     * 任务类
     */
    private String jobClass;

    /**
     * 任务类描述
     */
    private String jobClassDescribe;



    /**
     * 必须参数
     */
    private List<JobClassRegister.Param> requiredParam = new ArrayList<>();

    /**
     * 可选参数
     */
    private List<JobClassRegister.Param> optionalParam = new ArrayList<>();


    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public String getJobClassDescribe() {
        return jobClassDescribe;
    }

    public void setJobClassDescribe(String jobClassDescribe) {
        this.jobClassDescribe = jobClassDescribe;
    }

    public List<JobClassRegister.Param> getRequiredParam() {
        return requiredParam;
    }

    public void setRequiredParam(List<JobClassRegister.Param> requiredParam) {
        this.requiredParam = requiredParam;
    }

    public List<JobClassRegister.Param> getOptionalParam() {
        return optionalParam;
    }

    public void setOptionalParam(List<JobClassRegister.Param> optionalParam) {
        this.optionalParam = optionalParam;
    }


}
