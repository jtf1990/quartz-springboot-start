package com.jtf.quartzstarter.config;

import com.jtf.quartzstarter.ApplicationHolder;
import com.jtf.quartzstarter.factory.SchedulerFactoryBean;
import com.jtf.quartzstarter.factory.SchedulerFactoryBuilder;
import com.jtf.quartzstarter.support.plug.DbLoggingPersister;
import com.jtf.quartzstarter.support.plug.FileLoggingPersister;
import com.jtf.quartzstarter.support.plug.LoggingPersister;
import com.jtf.quartzstarter.utils.JDBCUtil;
import com.jtf.quartzstarter.utils.ResourceUtil;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.jdbcjobstore.Constants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Quartz Auto Configure<br/>
 * quartz 自动配置,其在有dataSource 时启用,需要数据库支持,现在仅仅支持mysql 和oracle<br/>
 * 其功能:<br/>
 * 1. 初始化数据库表结构,
 * 2. 初始化quartz配置, 如果需要修改配置,实现{@code QuartzPropertiesCustomizer}并注册springbean即可,默认配置在com/jtf/quartzstarter/config/jdbc.properties;<br/>
 * 3. 创建 {@code SchedulerFactoryBean} 其用来实例化Scheduler;<br/>
 * 4. 初始化 {@code ApplicationHolder} 其静态持有applicationContext , 供程序以后获取spring bean 使用<br/>
 * 5. 生成日志持久化类,{@code LoggingPersister},其具有日志的增删查作用,默认实现为 {@ com.jtf.quartzstarter.support.plug.DbLoggingPersister},
 * 可以通过在配置文件中配置 {@code quartz.config.logging-persister} 来定制实现<br/>
 * @see SchedulerFactoryBean
 * @see LoggingPersister
 * @see ApplicationHolder
 * @see #initDB(Connection) ;<br/>
 * @author jiangtaofeng
 */
@Configuration
@ConditionalOnClass(value = {SchedulerFactory.class, Scheduler.class})
@AutoConfigureAfter(value = DataSourceAutoConfiguration.class)
@ConditionalOnBean(value = DataSource.class)
public class QuartzAutoConfigure implements SchedulerFactoryBuilder, ApplicationContextAware, DisposableBean {


    public static final String KEY_LOGGING_PERSISTER = "quartz.config.logging-persister";

    public static final String DEFAULT_LOGGING_PERSISTER = "com.jtf.quartzstarter.support.plug.DbLoggingPersister";

    public static final String FILE_LOGGING_PERSISTER = "com.jtf.quartzstarter.support.plug.FileLoggingPersister";

    public static final String KEY_FILE_LOGGING_PERSISTER_DIR="quartz.config.logging-persister.file.dir";

    // 容器引用
    private ApplicationContext applicationContext;
    // 实际配置
    private Properties configProperties = new Properties();


    public QuartzAutoConfigure(ObjectProvider<QuartzPropertiesCustomizer[]> quartzPropertiesCustomizers,
                               DataSource dataSource){

        try{
            Connection connection = dataSource.getConnection();
            initDB(connection);
            connection.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        QuartzPropertiesCustomizer[] customizers = quartzPropertiesCustomizers.getIfAvailable();
        try(InputStream inputStream = ResourceUtil.getRequiredResourceFromClasspath("com/jtf/quartzstarter/config/jdbc.properties")){
            this.configProperties.load(inputStream);
            if(Objects.nonNull(customizers)){
                for (QuartzPropertiesCustomizer customizer : customizers) {
                    if(Objects.nonNull(customizer)){
                        customizer.customize(this.configProperties);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Bean
    public LoggingPersister loggingPersister(Environment environment, DataSource dataSource){

        String property = environment.getProperty(KEY_LOGGING_PERSISTER);

        if(!StringUtils.hasText(property)){
            property = DEFAULT_LOGGING_PERSISTER;
        }
        LoggingPersister loggingPersister = null;
        if(DEFAULT_LOGGING_PERSISTER.endsWith(property)){
            loggingPersister = new  DbLoggingPersister(dataSource);
        }else if(FILE_LOGGING_PERSISTER.equals(property)){
            loggingPersister = new FileLoggingPersister(environment.getProperty(KEY_FILE_LOGGING_PERSISTER_DIR));
        }else{
            try{
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                Class<?> aClass = contextClassLoader.loadClass(property);
                loggingPersister = (LoggingPersister) aClass.getConstructor().newInstance();
            }catch (Exception ex){
                throw new RuntimeException(ex);
            }
        }
        loggingPersister.init();
        return loggingPersister;

    }

    // 创建SchedulerFactoryBean
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() throws SchedulerException {
        return new SchedulerFactoryBean(this);
    }

    @Override
    public SchedulerFactory builder() {
        try {
            return new StdSchedulerFactory(configProperties);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Properties getConfigProperties() {
        return configProperties;
    }

    @Override
    public void destroy() throws Exception {
        this.applicationContext = null;
        ApplicationHolder.setApplicationContext(null);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        ApplicationHolder.setApplicationContext(applicationContext);
    }


    /**
     * 判断数据库是否有表，如果没有新建表并且初始化默认数据
     * @param connection
     * @throws SQLException
     * @throws IOException
     */
    private void initDB(Connection connection) throws SQLException, IOException {

        boolean exist = JDBCUtil.tableExist(connection, Constants.DEFAULT_TABLE_PREFIX+"JOB_DETAILS");
        if(!exist){
            JDBCUtil.DbType dbType = JDBCUtil.getDbType(connection);
            if(dbType.equals(JDBCUtil.DbType.MYSQL)){
                JDBCUtil.executeBatch(connection, ResourceUtil.getRequiredResourceFromClasspath("com/jtf/quartzstarter/config/mysql.sql"));
            }else if(dbType.equals(JDBCUtil.DbType.ORACLE)){
                JDBCUtil.executeBatch(connection, ResourceUtil.getRequiredResourceFromClasspath("com/jtf/quartzstarter/config/oracle.sql"));
            }
        }
    }

}
