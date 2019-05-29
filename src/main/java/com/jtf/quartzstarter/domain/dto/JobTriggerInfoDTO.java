package com.jtf.quartzstarter.domain.dto;

import org.quartz.*;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 任务触发器DTO
 * @author jiangtaofeng
 */
public class JobTriggerInfoDTO {

    /**
     * 名称
     */
    protected String name;

    /**
     * 分组名
     */
    protected String group;

    /**
     * 描述
     */
    protected String describe;

    /**
     * 开始时间
     */
    protected String startTime;

    /**
     * 结束时间
     */
    protected String endTime;

    /**
     * 最后执行时间
     */
    protected String finalFireTime;

    /**
     * 下次执行时间
     */
    protected String nextFireTime;

    /**
     * 任务状态
     */
    protected Trigger.TriggerState state;

    protected Integer misfireInstruction;

    /**
     *
     */
    protected SimpleTriggerInfo simpleTriggerInfo;

    protected CronTriggerInfo cronTriggerInfo;

    protected CalTriggerInfo calTriggerInfo;

    protected DailyTriggerInfo dailyTriggerInfo;

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

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFinalFireTime() {
        return finalFireTime;
    }

    public void setFinalFireTime(String finalFireTime) {
        this.finalFireTime = finalFireTime;
    }

    public String getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(String nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public Trigger.TriggerState getState() {
        return state;
    }

    public void setState(Trigger.TriggerState state) {
        this.state = state;
    }

    public SimpleTriggerInfo getSimpleTriggerInfo() {
        return simpleTriggerInfo;
    }

    public void setSimpleTriggerInfo(SimpleTriggerInfo simpleTriggerInfo) {
        this.simpleTriggerInfo = simpleTriggerInfo;
    }

    public CronTriggerInfo getCronTriggerInfo() {
        return cronTriggerInfo;
    }

    public void setCronTriggerInfo(CronTriggerInfo cronTriggerInfo) {
        this.cronTriggerInfo = cronTriggerInfo;
    }

    public CalTriggerInfo getCalTriggerInfo() {
        return calTriggerInfo;
    }

    public void setCalTriggerInfo(CalTriggerInfo calTriggerInfo) {
        this.calTriggerInfo = calTriggerInfo;
    }

    public DailyTriggerInfo getDailyTriggerInfo() {
        return dailyTriggerInfo;
    }

    public void setDailyTriggerInfo(DailyTriggerInfo dailyTriggerInfo) {
        this.dailyTriggerInfo = dailyTriggerInfo;
    }

    public Integer getMisfireInstruction() {
        return misfireInstruction;
    }

    public void setMisfireInstruction(Integer misfireInstruction) {
        this.misfireInstruction = misfireInstruction;
    }

    public void withTrigger(Trigger source){
        if(source instanceof SimpleTrigger){
            SimpleTrigger builder = SimpleTrigger.class.cast(source);
            SimpleTriggerInfo simpleTriggerInfo = new SimpleTriggerInfo();
            simpleTriggerInfo.setInterval(builder.getRepeatInterval());
            simpleTriggerInfo.setRepeatCount(builder.getRepeatCount());
            simpleTriggerInfo.setTimesTriggered(builder.getTimesTriggered());
            this.simpleTriggerInfo = simpleTriggerInfo;

        }else if(source instanceof CronTrigger){
            CronTrigger builder = CronTrigger.class.cast(source);
            CronTriggerInfo cronTriggerInfo = new CronTriggerInfo();
            cronTriggerInfo.setCronExpression(builder.getCronExpression());
            cronTriggerInfo.setTimeZone(builder.getTimeZone().getID());
            this.cronTriggerInfo = cronTriggerInfo;
        }else if(source instanceof CalendarIntervalTrigger){
            CalendarIntervalTrigger builder = CalendarIntervalTrigger.class.cast(source);
            CalTriggerInfo calTriggerInfo = new CalTriggerInfo();
            calTriggerInfo.setInterval(builder.getRepeatInterval());
            calTriggerInfo.setIntervalUnit(builder.getRepeatIntervalUnit());
            calTriggerInfo.setPreserveHourOfDayAcrossDaylightSavings(builder.isPreserveHourOfDayAcrossDaylightSavings());
            calTriggerInfo.setSkipDayIfHourDoesNotExist(builder.isSkipDayIfHourDoesNotExist());
            calTriggerInfo.setTimeZone(builder.getTimeZone().getID());
            calTriggerInfo.setTimesTriggered(builder.getTimesTriggered());
            builder.getTimesTriggered();
            this.calTriggerInfo = calTriggerInfo;
        }else if(source instanceof DailyTimeIntervalTrigger){
            DailyTimeIntervalTrigger builder = DailyTimeIntervalTrigger.class.cast(source);
            DailyTriggerInfo info = new DailyTriggerInfo();
            info.setInterval(builder.getRepeatInterval());
            info.setIntervalUnit(builder.getRepeatIntervalUnit());
            info.setRepeatCount(builder.getRepeatCount());
            List<Integer> days = new ArrayList<>(builder.getDaysOfWeek());
            Collections.sort(days);
            StringBuilder stringBuilder = new StringBuilder();
            days.forEach(t ->stringBuilder.append(t).append(","));
            info.setDaysOfWeek(stringBuilder.substring(0, stringBuilder.length()-1));
            TimeOfDay start = builder.getStartTimeOfDay();
            if(Objects.nonNull(start)){
                info.setStartTimeOfDay(LocalTime.of(start.getHour(),start.getMinute(),start.getSecond()).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
            TimeOfDay end = builder.getEndTimeOfDay();
            if(Objects.nonNull(end)){
                info.setEndTimeOfDay(LocalTime.of(end.getHour(),start.getMinute(),start.getSecond()).format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
            info.setTimesTriggered(builder.getTimesTriggered());

            this.dailyTriggerInfo = info;

        }
    }


    private Object getField(Object obj, String field){
        try{
            Class<?> clazz = obj.getClass();
            Field declaredField = clazz.getDeclaredField(field);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        }catch (Exception ex){
            // igonre
            return null;
        }

    }

    public class SimpleTriggerInfo{
        private long interval = 0;
        private int repeatCount = 0;
        private int timesTriggered =0;

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public int getRepeatCount() {
            return repeatCount;
        }

        public void setRepeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
        }

        public int getTimesTriggered() {
            return timesTriggered;
        }

        public void setTimesTriggered(int timesTriggered) {
            this.timesTriggered = timesTriggered;
        }
    }

    public class CronTriggerInfo{

        private String cronExpression;

        private String timeZone;

        public String getCronExpression() {
            return cronExpression;
        }

        public void setCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }


    }

    public class CalTriggerInfo{
        private int timesTriggered =0;
        private int interval = 1;
        private DateBuilder.IntervalUnit intervalUnit = DateBuilder.IntervalUnit.DAY;

        private String timeZone;
        private boolean preserveHourOfDayAcrossDaylightSavings;
        private boolean skipDayIfHourDoesNotExist;

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public DateBuilder.IntervalUnit getIntervalUnit() {
            return intervalUnit;
        }

        public void setIntervalUnit(DateBuilder.IntervalUnit intervalUnit) {
            this.intervalUnit = intervalUnit;
        }


        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }

        public boolean isPreserveHourOfDayAcrossDaylightSavings() {
            return preserveHourOfDayAcrossDaylightSavings;
        }

        public void setPreserveHourOfDayAcrossDaylightSavings(boolean preserveHourOfDayAcrossDaylightSavings) {
            this.preserveHourOfDayAcrossDaylightSavings = preserveHourOfDayAcrossDaylightSavings;
        }

        public boolean isSkipDayIfHourDoesNotExist() {
            return skipDayIfHourDoesNotExist;
        }

        public void setSkipDayIfHourDoesNotExist(boolean skipDayIfHourDoesNotExist) {
            this.skipDayIfHourDoesNotExist = skipDayIfHourDoesNotExist;
        }
        public int getTimesTriggered() {
            return timesTriggered;
        }

        public void setTimesTriggered(int timesTriggered) {
            this.timesTriggered = timesTriggered;
        }
    }

    public class DailyTriggerInfo{
        private int interval = 1;
        private DateBuilder.IntervalUnit intervalUnit = DateBuilder.IntervalUnit.MINUTE;
        private String daysOfWeek;
        private String startTimeOfDay;
        private String endTimeOfDay;
        private int repeatCount = DailyTimeIntervalTrigger.REPEAT_INDEFINITELY;
        private int timesTriggered =0;
        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public DateBuilder.IntervalUnit getIntervalUnit() {
            return intervalUnit;
        }

        public void setIntervalUnit(DateBuilder.IntervalUnit intervalUnit) {
            this.intervalUnit = intervalUnit;
        }

        public String getDaysOfWeek() {
            return daysOfWeek;
        }

        public void setDaysOfWeek(String daysOfWeek) {
            this.daysOfWeek = daysOfWeek;
        }

        public String getStartTimeOfDay() {
            return startTimeOfDay;
        }

        public void setStartTimeOfDay(String startTimeOfDay) {
            this.startTimeOfDay = startTimeOfDay;
        }

        public String getEndTimeOfDay() {
            return endTimeOfDay;
        }

        public void setEndTimeOfDay(String endTimeOfDay) {
            this.endTimeOfDay = endTimeOfDay;
        }

        public int getRepeatCount() {
            return repeatCount;
        }

        public void setRepeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
        }

        public int getTimesTriggered() {
            return timesTriggered;
        }

        public void setTimesTriggered(int timesTriggered) {
            this.timesTriggered = timesTriggered;
        }
    }
}
