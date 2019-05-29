package com.jtf.quartzstarter.domain.dto;

/**
 * api 返回信息
 * @author jiangtaofeng
 * @param <T>
 */
public class ResDTO<T> {

    /**
     * 是否执行成功
     */
    private boolean flag;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 成功返回的数据
     */
    private T data;

    public ResDTO() {
    }

    public ResDTO(boolean flag, String message, T data) {
        this.flag = flag;
        this.message = message;
        this.data = data;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <V> ResDTO<V> success(V data){
        return new ResDTO<V>(true, "OK", data);
    }

    public static <V> ResDTO<V> error(String message){
        return new ResDTO<V>(false, message, null);
    }
}
