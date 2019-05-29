package com.jtf.quartzstarter.domain.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * job 分组信息
 * @author jiangtaofeng
 */
public class JobGroupInfoDTO {

    /**
     * 组名称
     */
    protected String groupName;

    /**
     * 此组下的定时任务
     */
    protected List<JobInfoDTO> jobInfoList = new ArrayList<>();

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<JobInfoDTO> getJobInfoList() {
        return jobInfoList;
    }

    public void setJobInfoList(List<JobInfoDTO> jobInfoList) {
        this.jobInfoList = jobInfoList;
    }
}
