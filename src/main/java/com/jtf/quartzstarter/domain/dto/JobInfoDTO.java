package com.jtf.quartzstarter.domain.dto;


import java.util.*;

/**
 * job Info
 * @author jiangtaofeng
 */
public class JobInfoDTO {

    /**
     * 名称
     */
    protected String name;

    /**
     * 分组
     */
    protected String group;

    /**
     * 描述
     */
    protected String description;

    /**
     * 任务类
     */
    protected String jobClass;

    protected Map<String, Object> requiredParam = new HashMap<>();

    protected Map<String, Object> optionalParam = new HashMap<>();

    protected Map<String, Object> customerParam = new HashMap<>();

    protected List<JobTriggerInfoDTO> jobTriggerInfoDTOS = new ArrayList<>();


    /**
     * 下次执行成功时间
     */
    protected String nextExecTime;

    /**
     * 上次执行成功时间
     */
    protected String upExecSuccessTime;


    /**
     * 上次执行失败时间
     */
    protected String upExecFailTime;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }


    public String getNextExecTime() {
        return nextExecTime;
    }

    public void setNextExecTime(String nextExecTime) {
        this.nextExecTime = nextExecTime;
    }

    public String getUpExecSuccessTime() {
        return upExecSuccessTime;
    }

    public void setUpExecSuccessTime(String upExecSuccessTime) {
        this.upExecSuccessTime = upExecSuccessTime;
    }

    public String getUpExecFailTime() {
        return upExecFailTime;
    }

    public void setUpExecFailTime(String upExecFailTime) {
        this.upExecFailTime = upExecFailTime;
    }

    public Map<String, Object> getRequiredParam() {
        return requiredParam;
    }

    public void setRequiredParam(Map<String, Object> requiredParam) {
        this.requiredParam = requiredParam;
    }

    public void addRequeiredParam(String key, Object value){
        this.requiredParam.put(key, value);
    }


    public Map<String, Object>getOptionalParam() {
        return optionalParam;
    }

    public void setOptionalParam(Map<String, Object> optionalParam) {
        this.optionalParam = optionalParam;
    }

    public void addOptionaldParam(String key, Object value){
        this.optionalParam.put(key, value);
    }

    public Map<String, Object> getCustomerParam() {
        return customerParam;
    }

    public void setCustomerParam(Map<String, Object> customerParam) {
        this.customerParam = customerParam;
    }

    public void addCustomerParam(String key, Object value){
        this.customerParam.put(key, value);
    }


    public List<JobTriggerInfoDTO> getJobTriggerInfoDTOS() {
        return jobTriggerInfoDTOS;
    }

    public void setJobTriggerInfoDTOS(List<JobTriggerInfoDTO> jobTriggerInfoDTOS) {
        this.jobTriggerInfoDTOS = jobTriggerInfoDTOS;
    }


    public void addJobTriggerInfoDTO(JobTriggerInfoDTO jobTriggerInfoDTO){
        this.jobTriggerInfoDTOS.add(jobTriggerInfoDTO);
    }
}
