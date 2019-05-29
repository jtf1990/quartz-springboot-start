package com.jtf.quartzstarter.support.plug;

import org.quartz.JobKey;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件持久化
 * @author jiangtaofeng
 */
public class FileLoggingPersister implements LoggingPersister{

    private String logDir = null;


    public FileLoggingPersister(String logDir) {
        this.logDir = logDir;
    }

    @Override
    public void init() {
        // ignore
    }

    @Override
    public void persister(LoggingJobPlugin.JobRunInfo jobRunInfo) {
        LoggingJobPlugin.LoggingOutputStream outputStream = jobRunInfo.getOutputStream();
        File dir = getLogFileDir(jobRunInfo.getJobKey());
        File target = new File(dir, product(jobRunInfo.getJobKey(),jobRunInfo.getFireTime(),jobRunInfo.isSuccess()));
        if(!target.exists()){
            try {
                target.createNewFile();
            } catch (IOException e) {
                //
            }
        }
        OutputStream outputStream1 = null;
        try{
            outputStream1 = new FileOutputStream(target, true);
            StreamUtils.copy(outputStream.toByteArray(), outputStream1);
        }catch (Exception ex){

        }finally {
            try {
                outputStream1.close();
            } catch (Exception e) {
                //
            }
        }
    }

    @Override
    public long deleteLogging(JobKey jobKey) {
        File dir = getLogFileDir(jobKey);
        if(!dir.exists()){
            return 0L;
        }
        long result = 0L;
        File[] files = dir.listFiles();
        for (File file : files) {
            try{
                file.delete();
                result++;
            }catch (Exception ex){
                //
            }
        }
        return  result;
    }

    @Override
    public void deleteLogging(JobKey jobKey, Date date,boolean success) {
        File dir = getLogFileDir(jobKey);
        String fileName = product(jobKey, date, success);
        FileSystemUtils.deleteRecursively(new File(dir,fileName));
    }

    @Override
    public List<JobRunHistory> findAllHistory(JobKey jobKey) {
        List<JobRunHistory> result = new ArrayList<>();
        File logFileDir = getLogFileDir(jobKey);
        File[] files = logFileDir.listFiles();
        for (File file : files) {
            try{
                result.add(parseJobKey(file.getName()));
            }catch (Exception ex){
                //
            }
        }
        result.sort((t1, t2)->t2.getFireTime().compareTo(t1.getFireTime()));
        return result;
    }

    @Override
    public String findHistoryDetail(JobKey jobKey, Date date, boolean success) {
        File dir = getLogFileDir(jobKey);
        String fileName = product(jobKey, date, success);
        File file = new File(dir, fileName);
        if(!file.exists()){
            return "";
        }
        try(InputStream inputStream = new FileInputStream(file)){
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }catch (IOException ex){
            // ignore
            return "";
        }
    }


    @Override
    public long deleteLogging(JobKey jobKey, Date before) {
        File dir = getLogFileDir(jobKey);
        return deleteLogging(before, dir);
    }


    @Override
    public long deleteLogging(Date before) {
        long result = 0L;
        File dir = getLogFileDir();
        File[] logDirs = dir.listFiles();
        for (File logDir : logDirs) {
            result += deleteLogging(before, logDir);
        }
        return result;
    }


    private long deleteLogging(Date before, File dir){
        long result = 0L;
        if(dir.isDirectory()){
            File[] files = dir.listFiles();
            for (File file : files) {
                String name = file.getName();
                try{
                    JobRunHistory jobRunHistory = parseJobKey(name);
                    if(jobRunHistory.getFireTime().compareTo(before)<0){
                        file.delete();
                        result++;
                    }
                }catch (Exception ex){
                    continue;
                }
            }
        }
        return result;
    }

    /**
     * 获取总的日志文件路径
     * @return
     */
    private File getLogFileDir(){
        if(StringUtils.hasText(logDir)){
            return new File(logDir);
        }
        URL resource = Thread.currentThread().getContextClassLoader().getResource("");
        String protocol = resource.getProtocol();
        String path = resource.getPath();
        File baseDir = null;
        if ("file".equals(protocol)) {
            baseDir = new File(path);
        } else if ("jar".equals(protocol)) {
            try {
                URL url = new URL(path);
                String p = url.getPath();
                System.out.println(path.substring(0, path.indexOf("!")));
                int i = path.indexOf("!");
                if (i < 0) {
                    baseDir = new File(p).getParentFile();
                } else {
                    baseDir = new File(p.substring(0, p.indexOf("!"))).getParentFile();
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return new File(baseDir,"quartz_logs");
    }


    /**
     * 获取jobKey 的日志文件目录
     * @param jobKey
     * @return
     */
    private  File getLogFileDir(JobKey jobKey) {
        String group = Objects.isNull(jobKey.getGroup())? "":jobKey.getGroup();
        String name = Objects.isNull(jobKey.getName()) ? "" : jobKey.getName();
        validateJobKey(jobKey);
        String dirName = group+"_"+name;
        File target = new File(getLogFileDir(), dirName);
        if(!target.exists()){
            target.mkdirs();
        }
        return target;
    }

    /**
     * 验证jobKey是否可用
     * @param jobKey
     */
    private void validateJobKey(JobKey jobKey){
        String group = Objects.isNull(jobKey.getGroup())? "":jobKey.getGroup();
        String name = Objects.isNull(jobKey.getName()) ? "" : jobKey.getName();
        if(group.contains("_")||group.contains(".")){
            throw new UnsupportedOperationException("不支持的jobGroup:"+group);
        }
        if(name.contains("_")||name.contains(".")){
            throw new UnsupportedOperationException("不支持的jobName:"+name);
        }
    }

    /**
     * 解析日志文件名
     * @param logName
     * @return
     * @throws UnsupportedOperationException
     */
    private JobRunHistory parseJobKey(String logName) throws UnsupportedOperationException{
        Pattern pattern = Pattern.compile("_");
        Matcher matcher = pattern.matcher(logName);
        String group = "";
        String name = "";
        Date fireTime = null;
        Boolean success = null;
        int end = 0;
        if(matcher.find()){
            int start = matcher.start();
            group = logName.substring(end,start);
            end = matcher.end();
        }else{
            throw new UnsupportedOperationException("解析logName失败:"+logName);
        }
        if(matcher.find()){
            int start = matcher.start();
            name = logName.substring(end,start);
            end = matcher.end();
        }else{
            throw new UnsupportedOperationException("解析logName失败:"+logName);
        }
        if(matcher.find()){
            int start = matcher.start();
            fireTime = new Date(Long.parseLong(logName.substring(end,start)));
            end = matcher.end();
        }else{
            throw new UnsupportedOperationException("解析logName失败:"+logName);
        }
        int start = logName.indexOf(".");
        success = Boolean.valueOf(logName.substring(end,start));
        JobRunHistory jobRunHistory = new JobRunHistory();
        jobRunHistory.setFireTime(fireTime);
        jobRunHistory.setSuccess(success);
        return jobRunHistory;

    }

    /**
     * 生成日志文件名, 其名称为jobKey[group]_jobKey[name]_fireTime_success.log
     * @param jobKey 任务key
     * @param fireTime 执行时间
     * @param success 是否执行成功
     * @return
     * @throws UnsupportedOperationException
     */
    private String product(JobKey jobKey, Date fireTime, boolean success)throws UnsupportedOperationException{
        String group = Objects.isNull(jobKey.getGroup())? "":jobKey.getGroup();
        String name = Objects.isNull(jobKey.getName()) ? "" : jobKey.getName();
        validateJobKey(jobKey);
        long executeTime = fireTime.getTime();
        return group+"_"+name+"_"+ executeTime+"_"+success+".log";
    }
}
