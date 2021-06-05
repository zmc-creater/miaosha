package com.mc.miaosha.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class ValidationResult {
    private boolean hasErrors = false;
    private HashMap<String,String> errMsgMap = new HashMap<>();

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public HashMap<String, String> getErrMsgMap() {
        return errMsgMap;
    }

    public void setErrMsgMap(HashMap<String, String> errMsgMap) {
        this.errMsgMap = errMsgMap;
    }

    public String getErrMsg() {
        return StringUtils.join(errMsgMap.values().toArray(), ",");
    }
}
