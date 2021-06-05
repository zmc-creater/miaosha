package com.mc.miaosha;

import com.mc.miaosha.dao.UserDOMapper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages={"com.mc.miaosha"})
@RestController
@MapperScan("com.mc.miaosha.dao")
public class MiaoShaApplication {
    @Autowired
    private UserDOMapper userDOMapper;

    @RequestMapping("/")
    public String home(){
        return "Welcome！";
    }

    public static void main(String[] args) {
        System.out.println("hello world");
        SpringApplication.run(MiaoShaApplication.class, args);

    }

}
