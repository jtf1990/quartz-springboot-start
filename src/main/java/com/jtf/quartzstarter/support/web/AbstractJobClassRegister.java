package com.jtf.quartzstarter.support.web;

import java.util.Objects;

public abstract class AbstractJobClassRegister implements JobClassRegister {

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(!(obj instanceof JobClassRegister)){
            return false;
        }
        return Objects.equals(getExecuteClass(), ((JobClassRegister) obj).getExecuteClass());
    }

    public int hashCode(){
        return getExecuteClass().hashCode();
    }


}
