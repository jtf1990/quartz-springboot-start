package com.jtf.quartzstarter.utils;


import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class ResourceUtil {
    public static final int BUFFER_SIZE = 4096;
    /**
     * 从类路径下获取资源
     * @param path 类路径下的资源
     * @return
     */
    public static InputStream getResourceFromClasspath(String path){
        if(Objects.isNull(path)){
            return null;
        }
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }


    /**
     * 从类路径下获取必须的资源，如果没有将跑出异常
     * @param path
     * @return
     */
    public static InputStream getRequiredResourceFromClasspath(String path){
        InputStream inputStream = getResourceFromClasspath(path);
        if(Objects.nonNull(inputStream)){
            return inputStream;
        }
        throw new NullPointerException("classpath 下 资源:"+path+" 不存在");
    }

    /**
     * 复制流
     * @param in
     * @param out
     * @return
     * @throws IOException
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }

    /**
     * 关闭可关闭的资源
     * @param closeable
     */
    public static void close(Closeable closeable){
        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }
}
