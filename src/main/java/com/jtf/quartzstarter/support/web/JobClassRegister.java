package com.jtf.quartzstarter.support.web;

import org.quartz.Job;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 任务类注册教书
 */
public interface JobClassRegister {

    /**
     * 获取执行类
     * @return
     */
    Class<? extends Job> getExecuteClass();


    /**
     * 获取必须的参数
     * @return
     */
    List<Param> getRequiredParam();

    /**
     * 获取可选的参数
     * @return
     */
    List<Param> getOptionalParam();

    /**
     * 验证参数是否异常
     * @param param
     * @return 异常信息,如果没有返回空
     */
    String validate(Map<String, String> param);

    /**
     * 获取任务类描述
     * @return
     */
    String getDescribe();

    class Param{

        private String key;

        private String describe;

        private String name;

        public Param() {
        }

        public Param(String key, String describe, String name) {
            this.key = key;
            this.describe = describe;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Param param = (Param) o;
            return Objects.equals(key, param.key);

        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public String toString() {
            return "Param{" +
                    "key='" + key + '\'' +
                    ", describe='" + describe + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
