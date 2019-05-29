package com.jtf.quartzstarter.support.plug;

import org.quartz.JobKey;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日志持久化
 * @author jiangtaofeng
 */
public interface LoggingPersister {

    /**
     * 初始化
     */
    void init();


    /**
     * 持久化
     */
    void persister(LoggingJobPlugin.JobRunInfo jobRunInfo);

    /**
     * 删除此jobKey的所有日志
     * @param jobKey
     * @return 删除数量
     */
    long deleteLogging(JobKey jobKey);

    /**
     * 删除此jobKey 在特定时间的日志
     * @param jobKey
     * @param date
     * @param success
     *
     */
    void deleteLogging(JobKey jobKey, Date date, boolean success);

    /**
     * 获取此jobKey 的执行历史
     * @param jobKey
     * @return
     */
    List<JobRunHistory> findAllHistory(JobKey jobKey);

    /**
     * 获取此jobKey 的历史执行数据
     * @param  jobKey
     * @param date
     * @param success
     * @return
     */
    String findHistoryDetail(JobKey jobKey, Date date, boolean success);


    /**
     * 删除jobKey 在 before 之前的日志
     * @param jobKey
     * @param before
     * @return 删除数量
     */
    long deleteLogging(JobKey jobKey, Date before);


    default Date getUpSuccessDate(JobKey jobKey) {
        List<JobRunHistory> findAllHistory = findAllHistory(jobKey);
        List<JobRunHistory> collect = findAllHistory.stream()
                .filter(JobRunHistory::isSuccess)
                .sorted((t1, t2) -> t2.getFireTime().compareTo(t1.getFireTime()))
                .collect(Collectors.toList());
        if(collect.isEmpty()){
            return null;
        }
        return collect.get(0).getFireTime();
    }

    default Date getUpFailDate(JobKey jobKey) {
        List<JobRunHistory> findAllHistory = findAllHistory(jobKey);
        List<JobRunHistory> collect = findAllHistory.stream()
                .filter(t->!t.isSuccess())
                .sorted((t1, t2) -> t2.getFireTime().compareTo(t1.getFireTime()))
                .collect(Collectors.toList());
        if(collect.isEmpty()){
            return null;
        }
        return collect.get(0).getFireTime();
    }





    /**
     * 删除在 before 之前的日志
     * @param before
     * @return 删除记录数
     */
    long deleteLogging(Date before);

    class JobRunHistory{

        private Date fireTime;

        private boolean success=true;

        public JobRunHistory(Date fireTime, boolean success) {
            this.fireTime = fireTime;
            this.success = success;
        }

        public JobRunHistory() {
        }


        public Date getFireTime() {
            return fireTime;
        }

        public void setFireTime(Date fireTime) {
            this.fireTime = fireTime;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

    }
}
