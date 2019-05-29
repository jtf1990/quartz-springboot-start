package com.jtf.quartzstarter.utils;

import org.quartz.JobKey;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志文件工具
 */
public class LogFileUtil {

//    /**
//     * 生成日志文件名称的工具
//     * @param jobKey 任务key
//     * @param fireTime 执行时间
//     * @param success 是否执行成功
//     * @return 日志
//     */
//    public static String product(JobKey jobKey, Date fireTime, boolean success)throws UnsupportedOperationException{
//        String group = Objects.isNull(jobKey.getGroup())? "":jobKey.getGroup();
//        String name = Objects.isNull(jobKey.getName()) ? "" : jobKey.getName();
//        validateJobKey(jobKey);
//        long executeTime = fireTime.getTime();
//        return group+"_"+name+"_"+ executeTime+"_"+success+".log";
//    }
//
//    /**
//     * 解析log名称
//     * @param logName
//     * @return
//     * @throws UnsupportedOperationException
//     */
//    public static LogFileName parseJobKey(String logName) throws UnsupportedOperationException{
//        Pattern pattern = Pattern.compile("_");
//        Matcher matcher = pattern.matcher(logName);
//        String group = "";
//        String name = "";
//        Date fireTime = null;
//        Boolean success = null;
//        int end = 0;
//        if(matcher.find()){
//            int start = matcher.start();
//            group = logName.substring(end,start);
//            end = matcher.end();
//        }else{
//            throw new UnsupportedOperationException("解析logName失败:"+logName);
//        }
//        if(matcher.find()){
//            int start = matcher.start();
//            name = logName.substring(end,start);
//            end = matcher.end();
//        }else{
//            throw new UnsupportedOperationException("解析logName失败:"+logName);
//        }
//        if(matcher.find()){
//            int start = matcher.start();
//            fireTime = new Date(Long.parseLong(logName.substring(end,start)));
//            end = matcher.end();
//        }else{
//            throw new UnsupportedOperationException("解析logName失败:"+logName);
//        }
//        int start = logName.indexOf(".");
//        success = Boolean.valueOf(logName.substring(end,start));
//        LogFileName logFileName = new LogFileName();
//        logFileName.setFireTime(fireTime);
//        logFileName.setJobKey(new JobKey(name, group));
//        logFileName.setSuccess(success);
//        return logFileName;
//
//    }
//
//    public static File getLogFileDir(JobKey jobKey) {
//        URL resource = Thread.currentThread().getContextClassLoader().getResource("");
//        String protocol = resource.getProtocol();
//        String path = resource.getPath();
//        File baseDir = null;
//        if ("file".equals(protocol)) {
//            baseDir = new File(path);
//        } else if ("jar".equals(protocol)) {
//            try {
//                URL url = new URL(path);
//                String p = url.getPath();
//                System.out.println(path.substring(0, path.indexOf("!")));
//                int i = path.indexOf("!");
//                if (i < 0) {
//                    baseDir = new File(p).getParentFile();
//                } else {
//                    baseDir = new File(p.substring(0, p.indexOf("!"))).getParentFile();
//                }
//            } catch (MalformedURLException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        String group = Objects.isNull(jobKey.getGroup())? "":jobKey.getGroup();
//        String name = Objects.isNull(jobKey.getName()) ? "" : jobKey.getName();
//        validateJobKey(jobKey);
//        String dirName = group+"_"+name;
//        File target = new File(new File(baseDir,"quartz_logs"),dirName);
//        if(!target.exists()){
//            target.mkdirs();
//        }
//        return target;
//    }
//
//
//    private static void validateJobKey(JobKey jobKey){
//        String group = Objects.isNull(jobKey.getGroup())? "":jobKey.getGroup();
//        String name = Objects.isNull(jobKey.getName()) ? "" : jobKey.getName();
//        if(group.contains("_")||group.contains(".")){
//            throw new UnsupportedOperationException("不支持的jobGroup:"+group);
//        }
//        if(name.contains("_")||name.contains(".")){
//            throw new UnsupportedOperationException("不支持的jobName:"+name);
//        }
//    }
//
//    public static class LogFileName{
//        private JobKey jobKey;
//
//        private Date fireTime;
//
//        private Boolean success;
//
//        public JobKey getJobKey() {
//            return jobKey;
//        }
//
//        public void setJobKey(JobKey jobKey) {
//            this.jobKey = jobKey;
//        }
//
//        public Date getFireTime() {
//            return fireTime;
//        }
//
//        public void setFireTime(Date fireTime) {
//            this.fireTime = fireTime;
//        }
//
//        public Boolean getSuccess() {
//            return success;
//        }
//
//        public void setSuccess(Boolean success) {
//            this.success = success;
//        }
//
//        @Override
//        public String toString() {
//            return "LogFileName{" +
//                    "jobKey=" + jobKey +
//                    ", fireTime=" + fireTime +
//                    ", success=" + success +
//                    '}';
//        }
//    }
}
