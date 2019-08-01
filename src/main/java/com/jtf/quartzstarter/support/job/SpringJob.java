package com.jtf.quartzstarter.support.job;

import com.jtf.quartzstarter.ApplicationHolder;
import com.jtf.quartzstarter.support.web.AbstractJobClassRegister;
import com.jtf.quartzstarter.utils.ResourceUtil;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * spring 容器任务
 * @author jiangtaofeng
 * @since
 */
public class SpringJob extends AbstractJobClassRegister implements Job {

    private Logger logger = LoggerFactory.getLogger(SpringJob.class);

    public static final String SERVICE_CLASS_NAME_KEY="serviceClass";

    public static final String METHOD_NAME="methodName";

    public static final String METHOD_PARAM_CLASSES="methodParamClasses";

    private static List<Param> requiredParam =  Arrays.asList(new Param(SERVICE_CLASS_NAME_KEY,"业务类class","类class"),
            new Param(METHOD_NAME,"业务类methodName","方法名称"));

    private static List<Param> optionalParam =
            Arrays.asList(new Param(METHOD_PARAM_CLASSES,"方法参数类型,以英文逗号分割, 其值的key为:methodParam + 参数下标","参数class"));

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.debug("定时任务springjob开始执行,应该执行时间:{}, 实际执行时间:{}, jobKey:{}, 重试次数:{}",
                context.getFireTime(), new Date(), context.getJobDetail().getKey(), context.getRefireCount());
        // 当前时间戳, 记录用时使用
        long now = System.currentTimeMillis();
        // 获取执行参数
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        String serverClass = jobDataMap.getString(SpringJob.SERVICE_CLASS_NAME_KEY);
        if(serverClass == null){
            throw new JobExecutionException("springJob serviceClass is null, jobKey:"+ jobDetail.getKey());
        }
        Class<?> clazz = null;
        try {
            clazz = Class.forName(serverClass);
        } catch (ClassNotFoundException e) {
            throw new JobExecutionException("springJob servicelass is not found, jobKey:"+ jobDetail.getKey()+", serviceClass: "+serverClass, e);
        }
        ApplicationContext applicationContext = ApplicationHolder.getApplicationContext();
        Object bean = applicationContext.getBean(clazz);

        String methodName = jobDataMap.getString(SpringJob.METHOD_NAME);
        if(serverClass == null){
            throw new JobExecutionException("springJob methodName is null, jobKey:"+ jobDetail.getKey()+", serviceClass: "+serverClass);
        }
        Class[] paramClass = null;
        Object[] paramValue = null;
        String methodParamClasses = jobDataMap.getString(SpringJob.METHOD_PARAM_CLASSES);
        if(methodParamClasses != null && methodParamClasses.length()!=0){
            String[] methodParamClassArray = methodParamClasses.split(",");
            paramClass = new Class[methodParamClassArray.length];
            paramValue = new Object[methodParamClassArray.length];
            for (int i = 0; i < methodParamClassArray.length; i++) {
                try{
                    Class paramClazz =  Class.forName(methodParamClassArray[i]);
                    paramClass[i] = paramClazz;
                }catch (Exception ex){
                    throw new JobExecutionException("springJob methodParamClass not Found, jobKey:"+ jobDetail.getKey()+", serviceClass: "+serverClass, ex);
                }
            }
            for (int i = 0; i < methodParamClassArray.length; i++) {
                String param = jobDataMap.getString("methodParam"+i);
                if(Objects.isNull(param)){
                    paramValue[i] = param;
                }else if(paramClass[i].equals(JobExecutionContext.class)){
                    paramValue[i] = context;
                }else{
                    paramValue[i] = DefaultConversionService.getSharedInstance().convert(param, paramClass[i]);
                }
            }
        }
        Method method = null;
        try{
            if(paramClass == null){
                method = clazz.getDeclaredMethod(methodName);
            }else {
                method = clazz.getDeclaredMethod(methodName, paramClass);
            }
        }catch (Exception ex){
            throw new JobExecutionException("springJob method not Found, jobKey:"+ jobDetail.getKey()+", serverClass: "+serverClass, ex);
        }
        logger.debug("定时任务springjob开始执行, className:{}, methodName:{}, paramClass:{}, paramValue:{}",
                serverClass,methodName, Arrays.toString(paramClass), Arrays.toString(paramValue));


        Object result = null;
        try{
            if(paramClass == null){
               result = method.invoke(bean);
            }else {
                result = method.invoke(bean, paramValue);
            }
        }catch (Exception ex){
            throw new JobExecutionException("springJob method execute fail, jobKey:"+ jobDetail.getKey()+", serverClass: "+serverClass, ex);
        }
        context.setResult(result);
        logger.debug("定时任务springjob开始执行,jobKey:{},执行完成, 执行时间:{}",jobDetail.getKey(),System.currentTimeMillis() - now);
    }





    @Override
    public Class<? extends Job> getExecuteClass() {
        return SpringJob.class;
    }

    @Override
    public List<Param> getRequiredParam() {
        return requiredParam;
    }

    @Override
    public List<Param> getOptionalParam() {
        return optionalParam;
    }

    @Override
    public String getDescribe() {
        return "从spring容器中获取执行任务";
    }


    @Override
    public String validate(Map<String, String> param) {
        String className = param.get(SERVICE_CLASS_NAME_KEY);
        if(!StringUtils.hasText(className)){
            return "业务类名称不能为空";
        }
        Class clazz = null;
        try{
            clazz = ResourceUtil.loadClass(className);
        }catch (ClassNotFoundException ex){
            return "业务类不存在";
        }
        String methodName = param.get(METHOD_NAME);
        if(!StringUtils.hasText(methodName)){
            return "业务类执行方法不能为空";
        }

        String methodParamClasses = param.get(METHOD_PARAM_CLASSES);
        Class<?>[] paramClassArray = null;
        if(StringUtils.hasText(methodParamClasses)){
            String[] split = methodParamClasses.split(",");
            paramClassArray = new Class<?>[split.length];
            for (int i = 0; i < split.length; i++) {
                Class<?> methodParam = null;
                try{
                    methodParam = ResourceUtil.loadClass(split[i]);
                }catch (ClassNotFoundException ex){
                    return "参数:"+split[i]+" 不存在";
                }
                paramClassArray[i] = methodParam;
                String s = param.get("methodParam" + i);
                if(Objects.nonNull(s)){
                    try{
                        DefaultConversionService.getSharedInstance().convert(s, paramClassArray[i]);
                    }catch (Exception ex){
                        return "参数:"+split[i]+"转换失败";
                    }

                }

            }
        }
        if(paramClassArray != null){
            try {
                clazz.getDeclaredMethod(methodName, paramClassArray);
            } catch (NoSuchMethodException e) {
                return "方法不存在";
            }
            for (int i = 0; i < paramClassArray.length; i++) {

            }

        }else{
            try {
                clazz.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                return "方法不存在";
            }
        }
        return null;

    }
}
