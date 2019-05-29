package com.jtf.quartzstarter.support.web;

import com.jtf.quartzstarter.ApplicationHolder;
import com.jtf.quartzstarter.domain.dto.*;
import com.jtf.quartzstarter.factory.SchedulerFactoryBean;
import com.jtf.quartzstarter.support.plug.LoggingPersister;
import com.jtf.quartzstarter.utils.DateUtil;
import com.jtf.quartzstarter.utils.ResourceUtil;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.MutableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Calendar;
import java.util.stream.Collectors;

/**
 * 基础的定时器web接口
 * @author jiangtaofeng
 * @since 1.0
 */
public class BaseQuartzWebController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private JobClassRegisterContext jobClassRegisterContext;

    @Autowired
    private LoggingPersister loggingPersister;

    /**
     * 定时任务前缀请求前缀
     */
    private String triggerPrefix = "trigger-";



    /**
     * 获取quartz 请求web前缀
     * @return
     */
    protected String getWebPrefix(){
        String string = ApplicationHolder.getApplicationContext().getBean(Environment.class).getProperty("quartz.config.web-prefix");
        if(!StringUtils.hasText(string)){
            string = "/quartz";
        }
        if(!string.startsWith("/")){
            return "/"+string;
        }
        return string;
    }

    /**
     * 获取定时任务所有的日志
     * @param name
     * @param group
     * @return
     */
    @PostMapping(value = "/api/findLogging")
    @ResponseBody
    public ResDTO<TreeMap<String, Boolean>> findLogging(String name, String group){
        List<LoggingPersister.JobRunHistory> allHistory = loggingPersister.findAllHistory(new JobKey(name, group));
        TreeMap<String, Boolean> treeMap = new TreeMap<>(Comparator.reverseOrder());
        for (LoggingPersister.JobRunHistory jobRunHistory : allHistory) {
            try{
                treeMap.put(DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS", jobRunHistory.getFireTime()),jobRunHistory.isSuccess());
            }catch (Exception ex){
                // ignore
            }
        }
        return ResDTO.success(treeMap);
    }

    /**
     * 新建一个任务
     * @param request 请求对象
     * @return 成功或失败
     */
    @PostMapping(value = "/api/saveJob")
    @ResponseBody
    public ResDTO<String> saveJob(HttpServletRequest request){
        String function = "新建任务";
        try{

            Map<String, String[]> parameterMap = request.getParameterMap();
            // 任务分组名
            String jobGroupName = parameterMap.get("job-groupName")[0];
            logger.debug("{}:任务分组名:{}", function, jobGroupName);

            // job名
            String jobName = parameterMap.get("job-name")[0];
            logger.debug("{}:任务名:{}", function, jobName);

            // 任务key
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            // 任务执行类
            String jobClass = parameterMap.get("job-class")[0];
            logger.debug("{}:任务执行类:{}", function, jobClass);

            String jobDescribe = parameterMap.get("job-describe")[0];
            logger.debug("{}:任务描述:{}", function, jobDescribe);

            Map<String, String> param = new HashMap<>();
            Map<String, String> requiredKey = getRequiredJobKey(parameterMap, request);
            for (Map.Entry<String, String> entry : requiredKey.entrySet()) {
                String oldValue = param.put(entry.getKey(), entry.getValue());
                logger.debug("{}:必要任务参数:{}->{}", function, entry.getKey(), entry.getValue());
                if(Objects.nonNull(oldValue)){
                    logger.warn("{}:必要任务参数:{}->{},已存在key", function, entry.getKey(), entry.getValue());
                    return ResDTO.error("参数:"+entry.getKey()+"已存在");
                }
            }
            Map<String, String> optional = getOptionalJobKey(parameterMap, request);
            for (Map.Entry<String, String> entry : optional.entrySet()) {
                String oldValue = param.put(entry.getKey(), entry.getValue());
                logger.debug("{}:可选任务参数:{}->{}", function, entry.getKey(), entry.getValue());
                if(Objects.nonNull(oldValue)){
                    logger.warn("{}:可选任务参数:{}->{}, 已存在key", function, entry.getKey(), entry.getValue());
                    return ResDTO.error("参数:"+entry.getKey()+"已存在");
                }
            }

            Map<String, String> customer = getCustomerJobKey(parameterMap, request);
            for (Map.Entry<String, String> entry : customer.entrySet()) {
                String oldValue = param.put(entry.getKey(), entry.getValue());
                logger.debug("{}:自定义任务参数:{}->{}", function, entry.getKey(), entry.getValue());
                if(Objects.nonNull(oldValue)){
                    logger.warn("{}:自定义任务参数:{}->{}, 已存在key", function, entry.getKey(), entry.getValue());
                    return ResDTO.error("参数:"+entry.getKey()+"已存在");
                }
            }

            Collection<JobClassRegister> jobClassRegisterCollection = jobClassRegisterContext.getJobClassRegisterCollection();
            JobClassRegister realJobClassRegister = null;
            for (JobClassRegister jobClassRegister : jobClassRegisterCollection) {
                if(jobClassRegister.getExecuteClass().getName().equals(jobClass)){
                    realJobClassRegister = jobClassRegister;
                    try{
                        String validate = jobClassRegister.validate(param);
                        if(StringUtils.hasText(validate)){
                            return ResDTO.error(validate);
                        }
                    }catch (Exception ex){
                        return ResDTO.error(ex.getMessage());
                    }
                }
            }
            if(Objects.isNull(realJobClassRegister)){
                logger.warn("{}:任务class未注册:{}", function, jobClass);
                return ResDTO.error("任务class未注册");
            }
            JobBuilder jobBuilder = JobBuilder.newJob()
                    .ofType(realJobClassRegister.getExecuteClass())
                    .withIdentity(jobKey)
                    .storeDurably(true)
                    .withDescription(jobDescribe);

            param.forEach((k,v)->jobBuilder.usingJobData(k,v));
            JobDetail jobDetail = jobBuilder.build();
            Set<Trigger> triggers = parseTrigger(parameterMap);
            if(triggers.isEmpty()){
                scheduler.addJob(jobDetail,false);
                logger.debug("{},保存任务成功,job:{},没有trigger", function, jobDetail);
            }else{
                scheduler.scheduleJob(jobDetail, triggers,false);
                logger.debug("{},保存任务成功,jobDetail:{},trigger:{}", function,jobDetail, triggers);
            }
            logger.info("{}完成, jobKey:{}", function, jobKey);
            return ResDTO.success("");
        }catch (Exception ex){
            logger.warn("{}异常", function, ex);
            return ResDTO.error(getExMessage(ex));
        }

    }


    /**
     * 获取可用的任务类
     * @return
     */
    @PostMapping(value = "/api/findAvailableJobClassRegister")
    @ResponseBody
    public ResDTO<List<JobClassRegisterDTO>> findAvailableJobClassRegister(){
        Collection<JobClassRegister> jobClassRegisterCollection = jobClassRegisterContext.getJobClassRegisterCollection();
        List<JobClassRegisterDTO> result = new ArrayList<>();
        for (JobClassRegister jobClassRegister : jobClassRegisterCollection) {
            JobClassRegisterDTO dto = new JobClassRegisterDTO();
            dto.setJobClass(jobClassRegister.getExecuteClass().getName());
            dto.setJobClassDescribe(jobClassRegister.getDescribe());
            dto.setOptionalParam(jobClassRegister.getOptionalParam());
            dto.setRequiredParam(jobClassRegister.getRequiredParam());
            result.add(dto);
        }
        return ResDTO.success(result);

    }

    /**
     * 中断任务
     * @param name 任务名称
     * @param group 任务分组
     * @see Scheduler#pauseJob(JobKey)
     * @return
     */
    @PostMapping(value = "/api/pauseJob")
    @ResponseBody
    public ResDTO<String> pauseJob(String name, String group){
        try {
            scheduler.pauseJob(new JobKey(name, group));
        } catch (Exception e) {
            return ResDTO.error(getExMessage(e));
        }
        logger.info("中断任务成功:jobName:{}, jobGroup:{}", name, group);
        return ResDTO.success("中断成功");
    }


    /**
     * 中断触发器
     * @param name
     * @param group
     * @return
     */
    @PostMapping(value = "/api/pauseTrigger")
    @ResponseBody
    public ResDTO<String> pauseTrigger(String name, String group){
        try {
            scheduler.pauseTrigger(new TriggerKey(name, group));
        } catch (Exception e) {
            return ResDTO.error(getExMessage(e));
        }
        logger.info("中断触发器成功:name:{}, group:{}", name, group);
        return ResDTO.success("中断成功");
    }


    /**
     * 恢复触发器
     * @param name
     * @param group
     * @return
     */
    @PostMapping(value = "/api/resumeTrigger")
    @ResponseBody
    public ResDTO<String> resumeTrigger(String name, String group){
        try {
            scheduler.resumeTrigger(new TriggerKey(name, group));
        } catch (Exception e) {
            return ResDTO.error(getExMessage(e));
        }
        logger.info("恢复触发器成功:name:{}, group:{}", name, group);
        return ResDTO.success("中断成功");
    }

    /**
     * 恢复中断的任务
     * @param name 任务名称
     * @param group 任务分组
     * @see Scheduler#resumeJob(JobKey)
     * @return
     */
    @PostMapping(value = "/api/resumeJob")
    @ResponseBody
    public ResDTO<String> resumeJob(String name, String group){
        try {
            scheduler.resumeJob(new JobKey(name, group));
        } catch (Exception e) {
            return ResDTO.error(getExMessage(e));
        }
        logger.info("中断恢复成功:jobName:{}, jobGroup:{}", name, group);
        return ResDTO.success("中断恢复成功");
    }


    /**
     * 删除任务
     * @param name 任务名称
     * @param group 任务分组
     * @see Scheduler#deleteJob(JobKey)
     * @return
     */
    @PostMapping(value = "/api/deleteJob")
    @ResponseBody
    public ResDTO<String> deleteJob(String name, String group){
        JobKey jobKey = new JobKey(name, group);
        try {
            scheduler.deleteJob(jobKey);
        }catch (Exception e) {
                return ResDTO.error(getExMessage(e));
        }
        try{
            loggingPersister.deleteLogging(jobKey);
        }catch (Exception ex){
            // ignore
        }
        logger.info("删除任务成功:jobName:{}, jobGroup:{}", name, group);
        return ResDTO.success("删除任务成功");
    }

    /**
     * 删除触发器
     * @param name
     * @param group
     * @return
     */
    @PostMapping(value = "/api/deleteTrigger")
    @ResponseBody
    public ResDTO<String> deleteTrigger(String name, String group){
        try{
            scheduler.unscheduleJob(new TriggerKey(name, group));
            logger.info("删除定时任务触发器,触发器name:{}, 触发器group:{}", name, group);
            return ResDTO.success("删除成功");
        }catch (Exception ex){
            return ResDTO.error(getExMessage(ex));
        }
    }


    /**
     * 删除日志
     * @param name
     * @param group
     * @return
     */
    @PostMapping(value = "/api/deleteLogging")
    @ResponseBody
    public ResDTO<String> deleteLogging(String name, String group, Boolean success, String fireTime){
        try{
            loggingPersister.deleteLogging(new JobKey(name,group),DateUtil.parse("yyyy-MM-dd HH:mm:ss.SSS", fireTime), success);
            return ResDTO.success("删除成功");
        }catch (Exception ex){
            return ResDTO.error(getExMessage(ex));
        }
    }

    /**
     * 删除所有日志
     * @param name
     * @param group
     * @return
     */
    @PostMapping(value = "/api/deleteAllLogging")
    @ResponseBody
    public ResDTO<String> deleteAllLogging(String name, String group){
        try{
            loggingPersister.deleteLogging(new JobKey(name,group));
            return ResDTO.success("删除成功");
        }catch (Exception ex){
            return ResDTO.error(getExMessage(ex));
        }
    }

    /**
     * 删除所有日志
     * @param name
     * @param group
     * @return
     */
    @PostMapping(value = "/api/deleteBeforeLogging")
    @ResponseBody
    public ResDTO<String> deleteBeforeLogging(String name, String group, Integer before){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -before);
        try{
            loggingPersister.deleteLogging(new JobKey(name,group),calendar.getTime());
            return ResDTO.success("删除成功");
        }catch (Exception ex){
            return ResDTO.error(getExMessage(ex));
        }
    }
    /**
     * 获取所有定时任务
     * @return
     */
    @PostMapping(value = "/api/findAllJob")
    @ResponseBody
    public ResDTO<List<JobGroupInfoDTO>> findAllJob(){
        try {
            // 定义结果
            List<JobGroupInfoDTO> result = new ArrayList<>();
            //获取所有的任务分组
            List<String> jobGroupNames = scheduler.getJobGroupNames();
            for (String jobGroupName : jobGroupNames) {
                // 定义分组结果
                JobGroupInfoDTO jobGroupInfoDTO = new JobGroupInfoDTO();
                // 设置分组名称
                jobGroupInfoDTO.setGroupName(jobGroupName);
                List<JobInfoDTO> jobInfoList = jobGroupInfoDTO.getJobInfoList();
                // 获取此分组下的所有任务key
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName));
                for (JobKey jobKey : jobKeys) {
                    JobInfoDTO jobInfoDTO = transferJobKeyToJobInfoDTO(jobKey);
                    if(Objects.nonNull(jobInfoDTO)){
                        jobInfoList.add(jobInfoDTO);
                    }
                }
                result.add(jobGroupInfoDTO);
            }

            return ResDTO.success(result);
        } catch (Exception ex) {
            return ResDTO.error(getExMessage(ex));
        }
    }

    /**
     * 静态资源处理
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/**")
    public void stactic(HttpServletRequest request, HttpServletResponse response) throws IOException {

        doStaticFile(request, response);
    }

    /**
     * 获取系统可用的TimeZoneIds
     * @return
     */
    @PostMapping(value = "/api/getTimeZoneIds")
    @ResponseBody
    public ResDTO<String[]> getTimeZoneIds(){
        return ResDTO.success(TimeZone.getAvailableIDs());
    }


    /**
     * 获取日志明细
     * @param name
     * @param group
     * @param success
     * @param fireTime
     * @return
     */
    @PostMapping(value = "/api/findLogDetail")
    @ResponseBody
    public ResDTO<String> findLogDetail(String name, String group, String success, String fireTime){
        try{
            return ResDTO.success(loggingPersister.findHistoryDetail(new JobKey(name, group),DateUtil.parse("yyyy-MM-dd HH:mm:ss.SSS", fireTime), Boolean.valueOf(success)));
        }catch (Exception ex){
            return ResDTO.error(getExMessage(ex));
        }
    }

    /**获取配置
     *
     * @return
     */
    @PostMapping(value = "/api/findConfig")
    @ResponseBody
    public ResDTO<Properties> findConfig(){
        return ResDTO.success(schedulerFactoryBean.findConfig());
    }


    /**
     * 为job增加触发器
     * @param request
     * @return
     */
    @PostMapping(value = "/api/addJobTriggers")
    @ResponseBody
    public ResDTO<String> addJobTriggers(HttpServletRequest request){
        String jobName = request.getParameter("job-name");
        String jobGroup = request.getParameter("job-group");
        JobKey jobKey = new JobKey(jobName, jobGroup);
        Set<Trigger> triggerSet = parseTrigger(request.getParameterMap());
        if(triggerSet.isEmpty()){
            return ResDTO.error("请增加触发器.");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Trigger trigger : triggerSet) {
            try{
                MutableTrigger t = MutableTrigger.class.cast(trigger);
                t.setJobKey(jobKey);
                scheduler.scheduleJob(trigger);
                stringBuilder.append("触发器:")
                        .append(t.getJobKey().getGroup())
                        .append(".")
                        .append(t.getJobKey().getName())
                        .append("保存成功;\n");
            }catch (Exception ex){
                return ResDTO.error(stringBuilder.append(getExMessage(ex)).toString());
            }

        }
        return ResDTO.success("保存成功");

    }

    private JobInfoDTO transferJobKeyToJobInfoDTO(JobKey jobKey) throws SchedulerException {
        // 获取任务key
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        String name = jobDetail.getKey().getName();
        String description = jobDetail.getDescription();
        String jobClass = jobDetail.getJobClass().getName();
        JobInfoDTO jobInfoDTO = new JobInfoDTO();
        jobInfoDTO.setDescription(description);
        jobInfoDTO.setJobClass(jobClass);
        jobInfoDTO.setName(name);
        jobInfoDTO.setGroup(jobKey.getGroup());
        jobInfoDTO.setUpExecFailTime(DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS", loggingPersister.getUpFailDate(jobKey)));
        jobInfoDTO.setUpExecSuccessTime(DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS", loggingPersister.getUpSuccessDate(jobKey)));

        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        Collection<JobClassRegister> jobClassRegisterCollection = jobClassRegisterContext.getJobClassRegisterCollection();
        List<JobClassRegister> collect = jobClassRegisterCollection.stream()
                .filter(t -> t.getExecuteClass().getName().equals(jobClass)).collect(Collectors.toList());
        if(!collect.isEmpty()){
            JobClassRegister jobClassRegister = collect.get(0);
            Set<String> keys = new HashSet<>();
            List<JobClassRegister.Param> requiredParam = jobClassRegister.getRequiredParam();
            if(Objects.nonNull(requiredParam)){
                requiredParam.forEach(t->{
                    String key = t.getKey();
                    keys.add(key);
                    jobInfoDTO.addRequeiredParam(key, jobDataMap.get(key));
                });
            }
            List<JobClassRegister.Param> optionalParam = jobClassRegister.getOptionalParam();
            if(Objects.nonNull(optionalParam)){
                optionalParam.forEach(t->{
                    String key = t.getKey();
                    keys.add(key);
                    jobInfoDTO.addOptionaldParam(key, jobDataMap.get(key));
                });
            }
            jobDataMap.forEach((k,v)->{
                if(keys.add(k)){
                    jobInfoDTO.addCustomerParam(k,v);
                }
            });


        }

        List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);

        jobInfoDTO.setNextExecTime(DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS" ,findMinNextFireDate(triggersOfJob)));

        for(Trigger trigger : triggersOfJob){
            TriggerKey key = trigger.getKey();
            String triggerName = key.getName();
            String triggerGroup = key.getGroup();
            String triggerDescribe = trigger.getDescription();
            Date startTime = trigger.getStartTime();
            Date endTime = trigger.getEndTime();
            Date finalFireTime = trigger.getFinalFireTime();
            Date nextFireTime = trigger.getNextFireTime();
            Trigger.TriggerState triggerState = scheduler.getTriggerState(key);
            ScheduleBuilder<? extends Trigger> scheduleBuilder = trigger.getScheduleBuilder();

            JobTriggerInfoDTO jobTriggerInfoDTO = new JobTriggerInfoDTO();
            jobTriggerInfoDTO.setName(triggerName);
            jobTriggerInfoDTO.setGroup(triggerGroup);
            jobTriggerInfoDTO.setDescribe(triggerDescribe);
            jobTriggerInfoDTO.setStartTime(DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS", startTime));
            jobTriggerInfoDTO.setEndTime(DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS", endTime));
            jobTriggerInfoDTO.setFinalFireTime(DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS", finalFireTime));
            jobTriggerInfoDTO.setNextFireTime(DateUtil.format("yyyy-MM-dd HH:mm:ss.SSS", nextFireTime));
            jobTriggerInfoDTO.setMisfireInstruction(trigger.getMisfireInstruction());
            jobTriggerInfoDTO.withTrigger(trigger);
            jobTriggerInfoDTO.setState(triggerState);
            jobInfoDTO.addJobTriggerInfoDTO(jobTriggerInfoDTO);

        }
        return jobInfoDTO;
    }

    /**
     * 获取最近的下次执行时间
     * @param source
     * @return
     */
    private Date findMinNextFireDate(List<? extends Trigger> source){
        Date nextExecTime = null;
        for(Trigger trigger : source){
            Date nextFireTime = trigger.getNextFireTime();
            if(Objects.nonNull(nextFireTime)&&
                    (Objects.isNull(nextExecTime)||nextFireTime.compareTo(nextExecTime)<0)){
                nextExecTime = nextFireTime;
            }
        }
        return nextExecTime;
    }

    /**
     * 将参数解析为触发器
     * @param param
     * @return
     */
    private Set<Trigger> parseTrigger(Map<String, String[]> param){
        Set<Trigger> result = new HashSet<>();
        Set<Integer> triggerKeys = new HashSet<>();
        String triggerPrefix = "trigger-";
        param.forEach((k,v)->{
                    if(k.startsWith(triggerPrefix)){
                        String substring = k.substring(triggerPrefix.length());

                        triggerKeys.add(Integer.valueOf(substring.split("\\-")[0]));
                    }
                } );
        logger.debug("解析触发器,触发器下标:{}", result);
        triggerKeys.forEach(t->{
            String type = param.get(triggerPrefix+t+"-type")[0];
            logger.debug("解析触发器,触发器下标:{}, 类型:{}", t, type);
            switch (type){
                case "simple":{
                    result.add(parseSimplerTrigger(param,t));
                    break;
                }
                case "cron":{
                    result.add(parseCornTrigger(param, t));
                    break;
                }
                case "cal":{
                    result.add(parseCalTrigger(param, t));
                    break;
                }
                case "daily" :{
                    result.add(parseDailyTrigger(param,t));
                    break;
                }
                default:{
                    //ignore
                }
            }
        });
        return result;
    }

    /**
     * 解析星期触发器
     * @param param
     * @param index
     * @return
     */
    private Trigger parseDailyTrigger(Map<String, String[]> param, Integer index) {
        TriggerBuilder<Trigger> triggerBuilder = getBaseTriggerBuilder(param,index);
        String intervalStr = param.get(triggerPrefix+index+"-interval")[0];
        String intervalUnitStr = param.get(triggerPrefix+index+"-intervalUnit")[0];
        String startTimeOfDayStr = param.get(triggerPrefix+index+"-startTimeOfDay")[0];
        String endTimeOfDayStr = param.get(triggerPrefix+index+"-endTimeOfDay")[0];
        String repeatCountStr = param.get(triggerPrefix+index+"-repeatCount")[0];
        String daysOfWeekStr = param.get(triggerPrefix+index+"-daysOfWeek")[0];
        String misfireInstructionStr = param.get(triggerPrefix+index+"-misfireInstruction")[0];
        DailyTimeIntervalScheduleBuilder scheduleBuilder = DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule();
        scheduleBuilder.withInterval(Integer.valueOf(intervalStr), DateBuilder.IntervalUnit.valueOf(intervalUnitStr));
        if(StringUtils.hasText(startTimeOfDayStr)){
            LocalTime localTime = LocalTime.parse(startTimeOfDayStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
            scheduleBuilder.startingDailyAt(new TimeOfDay(localTime.getHour(), localTime.getMinute(), localTime.getSecond()));
        }
        if(StringUtils.hasText(endTimeOfDayStr)){
            LocalTime localTime = LocalTime.parse(endTimeOfDayStr, DateTimeFormatter.ofPattern("HH:mm:ss"));
            scheduleBuilder.endingDailyAt(new TimeOfDay(localTime.getHour(), localTime.getMinute(), localTime.getSecond()));
        }
        if(StringUtils.hasText(repeatCountStr)){
            scheduleBuilder.withRepeatCount(Integer.valueOf(repeatCountStr));
        }
        if(StringUtils.hasText(daysOfWeekStr)){
            Set<Integer> set = new HashSet<>();
            String[] split = daysOfWeekStr.split(",");
            for (String s : split) {
                try{
                    set.add(Integer.valueOf(s));
                }catch (Exception ex){
                    // ignore
                }
            }
            if(!set.isEmpty()){
                scheduleBuilder.onDaysOfTheWeek(set);
            }
        }
        switch (Integer.valueOf(misfireInstructionStr)){
            case Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY:{
                scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                break;
            }
            case DailyTimeIntervalTrigger.MISFIRE_INSTRUCTION_DO_NOTHING:{
                scheduleBuilder.withMisfireHandlingInstructionDoNothing();
                break;
            }
            case CalendarIntervalTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW:{
                scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                break;
            }
            default:{
                //ignore
            }
        }
        triggerBuilder.withSchedule(scheduleBuilder);
        return triggerBuilder.build();

    }

    /**
     * 解析 日历触发器
     * @param param
     * @param index
     * @return
     */
    private Trigger parseCalTrigger(Map<String, String[]> param, Integer index) {
        TriggerBuilder<Trigger> triggerBuilder = getBaseTriggerBuilder(param,index);
        String intervalStr = param.get(triggerPrefix+index+"-interval")[0];
        String intervalUnitStr = param.get(triggerPrefix+index+"-intervalUnit")[0];
        String timeZoneStr = param.get(triggerPrefix+index+"-timeZone")[0];
        String preserveHourOfDayStr = param.get(triggerPrefix+index+"-preserveHourOfDay")[0];
        String skipDayskipDayStr = param.get(triggerPrefix+index+"-skipDay")[0];
        String misfireInstructionStr = param.get(triggerPrefix+index+"-misfireInstruction")[0];
        CalendarIntervalScheduleBuilder scheduleBuilder = CalendarIntervalScheduleBuilder.calendarIntervalSchedule();
        scheduleBuilder.withInterval(Integer.valueOf(intervalStr), DateBuilder.IntervalUnit.valueOf(intervalUnitStr));
        if(StringUtils.hasText(timeZoneStr)){
            scheduleBuilder.inTimeZone(TimeZone.getTimeZone(timeZoneStr));
        }
        scheduleBuilder.skipDayIfHourDoesNotExist(Boolean.valueOf(skipDayskipDayStr));
        scheduleBuilder.preserveHourOfDayAcrossDaylightSavings(Boolean.valueOf(preserveHourOfDayStr));
        switch (Integer.valueOf(misfireInstructionStr)){
            case Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY : {
                scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                break;
            }
            case CalendarIntervalTrigger.MISFIRE_INSTRUCTION_DO_NOTHING :{
                scheduleBuilder.withMisfireHandlingInstructionDoNothing();
                break;
            }
            case CalendarIntervalTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW : {
                scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                break;
            }
            default:{
                // ignore
            }
        }
        triggerBuilder.withSchedule(scheduleBuilder);
        return triggerBuilder.build();
    }

    /**
     * 解析corn 触发器
     * @param param
     * @param index
     * @return
     */
    private Trigger parseCornTrigger(Map<String, String[]> param, Integer index) {
        TriggerBuilder<Trigger> triggerBuilder = getBaseTriggerBuilder(param,index);


        String cronExpression = param.get(triggerPrefix+index+"-cronExpression")[0];
        String timeZoneStr = param.get(triggerPrefix+index+"-timeZone")[0];
        String misfireInstructionStr = param.get(triggerPrefix+index+"-misfireInstruction")[0];

        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        if(StringUtils.hasText(timeZoneStr)){
            cronScheduleBuilder.inTimeZone(TimeZone.getTimeZone(timeZoneStr));
        }
        switch (Integer.valueOf(misfireInstructionStr)){
            case Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY:{
                cronScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                break;
            }
            case CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING:{
                cronScheduleBuilder.withMisfireHandlingInstructionDoNothing();
                break;
            }
            case CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW:{
                cronScheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
                break;
            }
            default:{
                //ignore
            }
        }
        triggerBuilder.withSchedule(cronScheduleBuilder);
        return triggerBuilder.build();
    }

    /**
     * 解析简单触发器
     * @param param
     * @param index
     * @return
     */
    private Trigger parseSimplerTrigger(Map<String, String[]> param,Integer index) {
        TriggerBuilder<Trigger> triggerBuilder = getBaseTriggerBuilder(param,index);

        String intervalStr = param.get(triggerPrefix+index+"-interval")[0];
        String repeatCountStr = param.get(triggerPrefix+index+"-repeatCount")[0];
        String misfireInstructionStr = param.get(triggerPrefix+index+"-misfireInstruction")[0];

        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        simpleScheduleBuilder.withIntervalInMilliseconds(Long.valueOf(intervalStr));
        if(StringUtils.hasText(repeatCountStr)){
            simpleScheduleBuilder.withRepeatCount(Integer.valueOf(repeatCountStr));
        }else{
            simpleScheduleBuilder.repeatForever();
        }
        switch (Integer.valueOf(misfireInstructionStr)){
            case SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW :{
                simpleScheduleBuilder.withMisfireHandlingInstructionFireNow();
                break;
            }
            case SimpleTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY:{
                simpleScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
                break;
            }
            case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT :{
                simpleScheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount();
                break;
            }
            case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT:{
                simpleScheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount();
                break;
            }
            case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT :{
                simpleScheduleBuilder.withMisfireHandlingInstructionNowWithExistingCount();
                break;
            }
            case SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT:{
                simpleScheduleBuilder.withMisfireHandlingInstructionNowWithRemainingCount();
                break;
            }
            default:{
                //ignore
            }
        }
        triggerBuilder.withSchedule(simpleScheduleBuilder);
        return triggerBuilder.build();
    }


    private TriggerBuilder getBaseTriggerBuilder(Map<String, String[]> param,Integer index){
        String groupName = param.get(triggerPrefix+index+"-groupName")[0];
        String name = param.get(triggerPrefix+index+"-name")[0];
        String startDateStr = param.get(triggerPrefix+index+"-startDate")[0];
        String endDateStr = param.get(triggerPrefix+index+"-endDate")[0];
        String describe = param.get(triggerPrefix+index+"-describe")[0];
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
        triggerBuilder.withIdentity(name, groupName);
        if(StringUtils.hasText(startDateStr)){
            try {
                triggerBuilder.startAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startDateStr));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        if(StringUtils.hasText(endDateStr)){
            try {
                triggerBuilder.endAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endDateStr));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        triggerBuilder.withDescription(describe);
        return triggerBuilder;
    }

    /**
     * 获取必须的参数
     * @param param
     * @return
     */
    private Map<String, String> getRequiredJobKey(Map<String,String[]> param,HttpServletRequest request){
        Map<String, String> result = new HashMap<>();
        String keyPrefix = "job-key-required-";
        param.forEach((k,v)->{
            if(k.startsWith(keyPrefix)){
                String key = k.substring(keyPrefix.length());
                result.put(key, request.getParameter(k));
            }
        });
        return result;
    }

    /**
     * 获取可选的参数
     * @param param
     * @param request
     * @return
     */
    private Map<String, String> getOptionalJobKey(Map<String,String[]> param, HttpServletRequest request){
        Map<String, String> result = new HashMap<>();
        String keyPrefix = "job-key-optional-";
        param.forEach((k,v)->{
            if(k.startsWith(keyPrefix)){
                String key = k.substring(keyPrefix.length());
                result.put(key, request.getParameter(k));
            }
        });
        return result;
    }


    /**
     * 获取自定义参数
     * @param param
     * @param request
     * @return
     */
    private Map<String, String> getCustomerJobKey(Map<String, String[]> param, HttpServletRequest request) {
        Map<String, String> nameMap = new HashMap<>();
        Map<String, String> valueMap = new HashMap<>();
        String keyNamePrefix = "job-key-customer-name-";
        String keyValuePrefix = "job-key-customer-value-";
        param.forEach((k,v)->{
            if(k.startsWith(keyNamePrefix)){
                String key = k.substring(keyNamePrefix.length());
                nameMap.put(key, request.getParameter(k));
            }
            if(k.startsWith(keyValuePrefix)){
                String key = k.substring(keyValuePrefix.length());
                valueMap.put(key, request.getParameter(k));
            }
        });
        Map<String, String> result = new IdentityHashMap();
        nameMap.forEach((k,v)->{
            result.put(v, valueMap.get(k));
        });
        return result;
    }


    private void doStaticFile(HttpServletRequest request, HttpServletResponse response){
        String servletPath = request.getServletPath();
        String url = servletPath.substring(getWebPrefix().length());
        logger.debug("quartz请求静态资源:{}",url);
        if(Objects.isNull(url)|| url.length()==0|| url.equals("/")){
            try {
                response.sendRedirect(request.getContextPath()+request.getServletPath()+"/index.html");
            } catch (IOException e) {
                //
            }
            return;
        }
        String fileName = url.substring(url.lastIndexOf("/")+1);
        response.setCharacterEncoding("UTF-8");
        if(fileName.endsWith("js")){
            response.setContentType("application/x-javascript");
        }else if(fileName.endsWith("css")){
            response.setContentType("text/css");
        }else if(fileName.endsWith("jpg")||fileName.endsWith("png")||fileName.endsWith("gif")){
            response.setContentType("application/x-jpg");
        }else{
            response.setContentType("text/html");
        }
        doStaticFile(response,url);
    }

    private void doStaticFile(HttpServletResponse response, String fileName) {
        InputStream inputStream = ResourceUtil.getResourceFromClasspath("com/jtf/quartzstarter/config/web"+fileName);
        if(Objects.isNull(inputStream)){
            try {
                response.setContentType("text/html");
                response.sendError(404);
            } catch (IOException e) {
                // ignore
            }
            return;
        }
        try{
            ServletOutputStream outputStream = response.getOutputStream();
            ResourceUtil.copy(inputStream, outputStream);
        }catch (IOException ex){
            // ignore
        }finally {
            ResourceUtil.close(inputStream);
        }
    }

    /**
     * 获取异常信息
     * @param ex
     * @return
     */
    private String getExMessage(Throwable ex){
        Throwable cause = ex.getCause();
        if(Objects.nonNull(cause)){
            return getExMessage(cause);
        }
        return ex.getMessage();
    }
}
