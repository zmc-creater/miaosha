package com.mc.miaosha.controller;

import com.mc.miaosha.controller.viewobject.UserVO;
import com.mc.miaosha.error.BusinessException;
import com.mc.miaosha.error.EmBusinessError;
import com.mc.miaosha.response.CommonReturnType;
import com.mc.miaosha.service.UserService;
import com.mc.miaosha.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


@Controller("user")
@RequestMapping(value = "/user")
public class UserController extends BaseController{
    @Autowired
    UserService userService;

    @Autowired
    HttpServletRequest httpServletRequest;


    //用户登录
    @RequestMapping(value = "/login",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                      @RequestParam(name = "password") String encrpPassword) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(encrpPassword)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户名或密码为空");
        }

        //验证登录是否成功
        UserModel userModel = userService.validLogin(telphone, this.EncodeByMD5(encrpPassword));

        //dataobject-->model
        UserVO userVO = convertFromModel(userModel);
        httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        httpServletRequest.getSession().setAttribute("LOGIN_USER",userVO);
        System.out.println("登录成功");
        return CommonReturnType.create(null);
    }

    //用户注册
    @RequestMapping(value = "/register",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender") Byte gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "telphone")String phone,
                                     @RequestParam(name = "password")String password,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     HttpServletResponse httpServletResponse) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证码校验
        //edge和chrome浏览器获取不到session，火狐浏览器可以
        String inSessionOtpCode = (String)httpServletRequest.getSession().getAttribute(phone);

        if(!com.alibaba.druid.util.StringUtils.equals(otpCode,inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"验证码错误");
        }

        //用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(gender);
        userModel.setAge(age);
        userModel.setPhone(phone);

        //JDK提供的MD5方法之只能加密16位
        //userModel.setEncrptPaaword(MD5Encoder.encode(password.getBytes()));
        userModel.setEncrptPaaword(this.EncodeByMD5(password));
        userModel.setRegisterMode("byphone");
        userService.register(userModel);

        return CommonReturnType.create(null);
    }


    @ResponseBody
    @RequestMapping(value = "/getotp",method = {RequestMethod.POST},consumes = {BaseController.CONTENT_TYPE_FORMED})
    public CommonReturnType getOpt(@RequestParam(name = "telphone")String telphone){
        //按照一定规则生成opt验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码与用户的手机号关联,使用http的session
        httpServletRequest.getSession().setAttribute(telphone,otpCode);
        //将OTP验证码通过短信发给用户

        System.out.println("telphone="+telphone+",otpCode="+otpCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id")Integer id) throws BusinessException {
        //获取对象核心模型
        UserModel userModel = userService.getUserById(id);
        if (userModel == null) {
            //throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
            userModel.setEncrptPaaword("333rfsf");
        }

        UserVO userVO = convertFromModel(userModel);
        return CommonReturnType.create(userVO);
    }


    public String EncodeByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        String newstr = base64Encoder.encode(md5.digest(str.getBytes("UTF-8")));
        return newstr;
    }

    private UserVO convertFromModel(UserModel userModel){
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel,userVO);
        return userVO;
    }

}
