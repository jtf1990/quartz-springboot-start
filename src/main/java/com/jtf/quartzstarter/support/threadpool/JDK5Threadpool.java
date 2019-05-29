package com.jtf.quartzstarter.support.threadpool;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * quartz jdk5 的线程池
 * @author jiangtaofeng
 */
public class JDK5Threadpool implements ThreadPool {
    private ThreadPoolExecutor threadPoolExecutor ;
    /**
     * 线程池数量
     */
    private Integer poolSize = -1;

    private String instanceId;

    private String instanceName;

    @Override
    public boolean runInThread(Runnable runnable) {
        if(Objects.isNull(runnable)){
            return false;
        }
        threadPoolExecutor.submit(runnable);
        return true;
    }

    @Override
    public int blockForAvailableThreads() {
        if(Objects.nonNull(this.threadPoolExecutor)){
            return getPoolSize() - threadPoolExecutor.getActiveCount();
        }
        return 0;
    }

    @Override
    public void initialize() throws SchedulerConfigException {
        // already initialize;
        if(Objects.nonNull(this.threadPoolExecutor)){
            return ;
        }
        // init
        if (getPoolSize() <= 0) {
            throw new SchedulerConfigException(
                    "Thread count must be > 0");
        }
        ThreadFactory threadFactory = new DefaultThreadFactory(getInstanceName());
        this.threadPoolExecutor = new ThreadPoolExecutor(
                getPoolSize(),
                getPoolSize(),
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);
    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        ThreadPoolExecutor executor = this.threadPoolExecutor;
        this.threadPoolExecutor = null;
        if(waitForJobsToComplete){
            executor.shutdown();
        }else{
            executor.shutdown();
        }


    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }

    @Override
    public void setInstanceId(String schedInstId) {
        this.instanceId = schedInstId;
    }

    @Override
    public void setInstanceName(String schedName) {
        this.instanceName = schedName;

    }

    public void setThreadCount(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }


    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix+"_JDK5pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
