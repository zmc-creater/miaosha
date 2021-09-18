package com.mc.miaosha.error;

public enum EmBusinessError implements CommonError{
    //通用错误码10001
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知的错误"),

    //20000开头为用户信息相关错误定义
    USER_NOT_EXIST(20001,"用户不存在"),
    USER_LOGIN_ERROR(20002,"用户电话或密码错误"),
    USER_NOT_LOGIN(20003,"用户未登录"),

    //30000开头的为商品相关错误定义
    ITEM_NOT_EXIST(30000,"商品不存在"),
    ITEM_AMOUNT_NOT_ENOUGH(30001,"商品数量不足"),
    MQ_SEND_FAIL(30002,"库存异步消息失败"),
    ;

    EmBusinessError(int errCode,String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    private int errCode;
    private String errMsg;

    @Override
    public int getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
