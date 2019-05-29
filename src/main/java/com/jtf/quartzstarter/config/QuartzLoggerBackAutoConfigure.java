package com.jtf.quartzstarter.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import com.jtf.quartzstarter.support.plug.LoggingJobPlugin;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


/**
 * Quartz Auto Configure
 * 日志自动配置<br/>
 * 增加一个日志拦截器,拦截quartz线程日志,并写入日志
 * @author
 * @see LoggingJobPlugin
 */
@Configuration
@ConditionalOnClass(value = LoggerContext.class)
@ConditionalOnBean(value = QuartzAutoConfigure.class)
public class QuartzLoggerBackAutoConfigure  {

    QuartzLoggerBackAutoConfigure() {

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.addTurboFilter(new TurboFilter() {
            @Override
            public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
                // 获取当前线程的日志插件
                LoggingJobPlugin.JobRunInfo jobRunInfo = LoggingJobPlugin.getJobRunInfo();
                if(Objects.nonNull(jobRunInfo)){
                    try{// 获取输出流
                        LoggingJobPlugin.LoggingOutputStream outputStream = jobRunInfo.getOutputStream();
                        // 如果流已关闭则直接返回;
                        if(outputStream.isClosed()){
                            return FilterReply.NEUTRAL;
                        }

                        String name = logger.getName();
                        String levelStr = level.levelStr;
                        // 设置是否有成功
                        if(level.levelInt>Level.INFO_INT|| Objects.nonNull(t)){
                            jobRunInfo.setSuccess(false);
                        }
                        // 组装日志内容
                        if(!Objects.isNull(format)){
                            String[] result = format.split("\\{\\}");
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())
                            ).append(" [").append(name).append("] ").append(levelStr).append(":");
                            for(int i=0;i< result.length;i++){
                                stringBuilder.append(result[i]);
                                if(params != null && i< params.length){
                                    stringBuilder.append(params[i]);
                                }else if(i+1 != result.length){
                                    stringBuilder.append("{}");
                                }
                            }
                            stringBuilder.append(System.lineSeparator());
                            // 写入输出流
                            try {
                                outputStream.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
                            } catch (IOException e) {
                                //
                            }

                        }
                        // 异常写入输出流
                        if(Objects.nonNull(t)){
                            t.printStackTrace(new PrintStream(outputStream));
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }



                }
                return FilterReply.NEUTRAL;
            }
        });
    }

}
