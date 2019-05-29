//package com.jtf.quartzstarter.support.job;
//
//import com.jtf.quartzstarter.support.web.AbstractJobClassRegister;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.methods.*;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.HttpClients;
//import org.quartz.Job;
//import org.quartz.JobDataMap;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.util.StringUtils;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
//public class HttpJob extends AbstractJobClassRegister implements Job {
//
//
//    /**
//     * 浏览器头
//     */
//    public static final String KEY_USER_AGENT = "userAgent";
//
//    public static final String KEY_URL = "url";
//
//    public static final String KEY_METHOD = "method";
//
//    public static final String[] SUPPORT_METHOD = new String[]{
//      HttpGet.METHOD_NAME, HttpPost.METHOD_NAME, HttpDelete.METHOD_NAME,
//            HttpPut.METHOD_NAME, HttpHead.METHOD_NAME, HttpOptions.METHOD_NAME,
//            HttpPatch.METHOD_NAME, HttpTrace.METHOD_NAME
//    };
//
//
//
//    private static List<Param> requiredParam =  Arrays.asList(new Param(KEY_URL,"请求的url地址","url"));
//
//    private static List<Param> optionalParam =  Arrays.asList(new Param(KEY_USER_AGENT,"模拟浏览器头","浏览器userAgent"),
//            new Param(KEY_METHOD, "请求方式,默认get,可选值:"+Arrays.toString(SUPPORT_METHOD), "请求方式"));
//
//    private Logger logger = LoggerFactory.getLogger(getClass());
//
//    @Override
//    public void execute(JobExecutionContext context) throws JobExecutionException {
//        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
//        String url = jobDataMap.getString(KEY_USER_AGENT);
//        String method = jobDataMap.getString(KEY_METHOD);
//        String userAgent = jobDataMap.getString(KEY_USER_AGENT);
//
//        // clientBuiler
//        HttpClientBuilder custom = HttpClients.custom();
//        if(StringUtils.hasText(userAgent)){
//            custom.setUserAgent(userAgent);
//        }
//        RequestConfig.custom()
//        HttpRequestBase http = createHttpRequestBase(url, StringUtils.hasText(method)?method:"get");
//        http.setConfig(.setLocalAddress().build());
//        CloseableHttpClient client = custom.build();
//
//    }
//
//    public HttpRequestBase createHttpRequestBase(String url, String method){
//        HttpGet.METHOD_NAME, HttpPost.METHOD_NAME, HttpDelete.METHOD_NAME,
//                HttpPut.METHOD_NAME, HttpHead.METHOD_NAME, HttpOptions.METHOD_NAME,
//                HttpPatch.METHOD_NAME, HttpTrace.METHOD_NAME
//        if(HttpGet.METHOD_NAME.equalsIgnoreCase(method)){
//            return new HttpGet(url);
//        }else if(HttpPost.METHOD_NAME.equalsIgnoreCase(method)){
//            return new HttpPost(url);
//        }else if(HttpDelete.METHOD_NAME.equalsIgnoreCase(method)){
//            return new HttpDelete(url);
//        }else if(HttpPut.METHOD_NAME.equalsIgnoreCase(method)){
//            return new HttpPut(url);
//        }else if(HttpHead.METHOD_NAME.equalsIgnoreCase(method)){
//            return new HttpHead(url);
//        }else if(HttpOptions.METHOD_NAME.equalsIgnoreCase(method)){
//            return new HttpOptions(url);
//        }else if(HttpPatch.METHOD_NAME.equalsIgnoreCase(method)){
//            return new HttpPatch(url);
//        }else if(HttpTrace.METHOD_NAME.equalsIgnoreCase(method)){
//            return new HttpTrace(url);
//        }else{
//            throw new RuntimeException(String.format("不支持的请求方式:%s", method));
//        }
//    }
//
//    @Override
//    public Class<? extends Job> getExecuteClass() {
//        return HttpJob.class;
//    }
//
//    @Override
//    public List<Param> getRequiredParam() {
//        return requiredParam;
//    }
//
//    @Override
//    public List<Param> getOptionalParam() {
//        return optionalParam;
//    }
//
//    @Override
//    public String validate(Map<String, String> param) {
//        return null;
//    }
//
//    @Override
//    public String getDescribe() {
//        return "执行http或https请求";
//    }
//}
