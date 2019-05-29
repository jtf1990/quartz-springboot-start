package com.jtf.quartzstarter.support.job;

import com.jtf.quartzstarter.ApplicationHolder;
import com.jtf.quartzstarter.support.plug.LoggingPersister;
import com.jtf.quartzstarter.support.web.AbstractJobClassRegister;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 删除任务日志的定时任务
 * @author jiangtaofeng
 */
public class DeleteJobLoggerJob extends AbstractJobClassRegister implements Job {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final String KEY_BEFORE = "before";

    @Override
    public Class<? extends Job> getExecuteClass() {
        return DeleteJobLoggerJob.class;
    }

    @Override
    public List<Param> getRequiredParam() {
        return Arrays.asList(new Param(KEY_BEFORE, "删除此参数之前的日志", "天数"));
    }

    @Override
    public List<Param> getOptionalParam() {
        return Collections.emptyList();
    }

    @Override
    public String validate(Map<String, String> param) {
        String s = param.get(KEY_BEFORE);
        if(!StringUtils.hasText(s)){
            return "天数必须";
        }
        try{
            Integer integer = Integer.valueOf(s);
            if(integer<1){
                return "天数必须大于等于1";
            }
        }catch (Exception ex){
            return "天数必须是一个大于0的整数";
        }
        return null;
    }

    @Override
    public String getDescribe() {
        return "删除定时器日志";
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try{
            logger.debug("删除日志定时器开始执行");
            long now = System.currentTimeMillis();
            Integer integerFromString = context.getJobDetail().getJobDataMap().getIntegerFromString(KEY_BEFORE);
            LoggingPersister bean = ApplicationHolder.getApplicationContext().getBean(LoggingPersister.class);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -integerFromString);
            long l = bean.deleteLogging(calendar.getTime());
            context.setResult(l);
            logger.info("删除日志定时器执行结束:删除{}之前的日志, 执行时间:{}毫秒, 删除数量:{}", integerFromString, System.currentTimeMillis()-now, l);
        }catch (Exception ex){
            throw new JobExecutionException(ex);
        }

    }
}
