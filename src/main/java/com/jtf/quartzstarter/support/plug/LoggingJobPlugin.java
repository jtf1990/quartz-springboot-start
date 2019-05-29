package com.jtf.quartzstarter.support.plug;

import com.jtf.quartzstarter.ApplicationHolder;
import com.jtf.quartzstarter.utils.LogFileUtil;
import org.quartz.*;
import org.quartz.plugins.history.LoggingJobHistoryPlugin;
import org.quartz.spi.ClassLoadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * 任务日志插件
 * 通过监听任务来保存
 * @author jiangtaofeng
 */
public class LoggingJobPlugin extends LoggingJobHistoryPlugin {

    private Logger logger = LoggerFactory.getLogger(LoggingJobPlugin.class);

    private static ThreadLocal<JobRunInfo> logThreadLocal;

    private static Map<JobKey, JobRunInfo> jobKeyJobRunInfoMap;



    @Override
    public void initialize(String pname, Scheduler scheduler, ClassLoadHelper classLoadHelper) throws SchedulerException {
        super.initialize(pname, scheduler, classLoadHelper);
        if(logThreadLocal == null) {
            logThreadLocal = new ThreadLocal<JobRunInfo>();
        }
        if(jobKeyJobRunInfoMap == null){
            jobKeyJobRunInfoMap = new HashMap<>();
        }
    }

    public void shutdown() {
        super.shutdown();
        logThreadLocal = null;
    }
    public void jobToBeExecuted(JobExecutionContext context) {
        super.jobToBeExecuted(context);
        JobKey key = context.getJobDetail().getKey();
        Date fireTime = context.getFireTime();
        JobRunInfo jobRunInfo = new JobRunInfo(key, fireTime);
        logThreadLocal.set(jobRunInfo);
        jobKeyJobRunInfoMap.put(key,jobRunInfo);

    }

    /**
     * @see org.quartz.JobListener#jobWasExecuted(JobExecutionContext, JobExecutionException)
     */
    public void jobWasExecuted(JobExecutionContext context,
                               JobExecutionException jobException) {
        super.jobWasExecuted(context, jobException);
        afterExecuteJobRunInfo();

    }

    /**
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    public void jobExecutionVetoed(JobExecutionContext context) {
        super.jobExecutionVetoed(context);
        afterExecuteJobRunInfo();

    }

    public void afterExecuteJobRunInfo() {
        JobRunInfo jobRunInfo = logThreadLocal.get();
        if(Objects.isNull(jobRunInfo)){
            return;
        }
        String[] beanNamesForType = ApplicationHolder.getApplicationContext().getBeanNamesForType(LoggingPersister.class);
        if(beanNamesForType == null || beanNamesForType.length == 0){
            return;
        }
        Set<LoggingPersister> set = new HashSet<>();
        for (String beanName : beanNamesForType) {
            set.add(ApplicationHolder.getApplicationContext().getBean(beanName, LoggingPersister.class));
        }
        for (LoggingPersister loggingPersister : set) {
            try{
                loggingPersister.persister(jobRunInfo);
            }catch (Exception ex){
               logger.warn("定时任务持久化处理异常", ex);
            }
        }


        try {
            jobRunInfo.getOutputStream().close();
        } catch (IOException e) {
            // ignore
        }
        jobKeyJobRunInfoMap.remove(jobRunInfo.getJobKey());
        logThreadLocal.remove();
    }

    /**
     * 获取当前线程的job运行信息
     * @return
     */
    public static JobRunInfo getJobRunInfo() {
        if(logThreadLocal != null){
            return logThreadLocal.get();
        }
        return null;
    }


    public static JobRunInfo getJobRunInfo(JobKey jobKey){
        return jobKeyJobRunInfoMap.get(jobKey);
    }

    public static class LoggingOutputStream extends ByteArrayOutputStream{
        private boolean closed = false;

        public boolean isClosed(){
            return closed;
        }

        public int read(int index){
            if(closed){
                return -1;
            }
            if(this.count>index){
                return this.buf[index];
            }
            while (true){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(closed){
                    return -1;
                }
                if(this.count>index){
                    return this.buf[index];
                }
            }
        }

        /**
         * 当获取正在执行的任务日志时使用
         * @param index
         * @param timeout
         * @return
         * @throws TimeoutException
         */
        public int read(int index,long timeout) throws TimeoutException{
            if(closed){
                return -1;
            }
            if(this.count>index){
                return this.buf[index];
            }
            long now = System.currentTimeMillis();
            while (true){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(System.currentTimeMillis()-now>timeout){
                    throw new TimeoutException("读超时");
                }
                if(closed){
                    return -1;
                }
                if(this.count>index){
                    return this.buf[index];
                }
            }
        }


        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }
    }

    public static class JobRunInfo {

        private JobKey jobKey;

        private Date fireTime;

        private boolean success=true;


        private LoggingOutputStream outputStream = new LoggingOutputStream();

        public JobRunInfo() {


        }


        public JobRunInfo(JobKey jobKey, Date fireTime) {
            this.jobKey = jobKey;
            this.fireTime = fireTime;
        }



        public JobKey getJobKey() {
            return jobKey;
        }

        public void setJobKey(JobKey jobKey) {
            this.jobKey = jobKey;
        }

        public Date getFireTime() {
            return fireTime;
        }

        public void setFireTime(Date fireTime) {
            this.fireTime = fireTime;
        }

        public LoggingOutputStream getOutputStream() {
            return outputStream;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

    }


}
