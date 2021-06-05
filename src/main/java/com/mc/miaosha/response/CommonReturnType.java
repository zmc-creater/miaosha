package com.mc.miaosha.response;

/**
 * 对返回的响应做处理
 */
public class CommonReturnType {
    //表明对应请求的返回处理结果，"success","fail"
    private String status;
    //若status为success，则返回json字符串
    //若status为fail，则返回通用的错误码格式
    private Object data;

    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result,"success");
    }

    public static CommonReturnType create(Object result, String status){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
