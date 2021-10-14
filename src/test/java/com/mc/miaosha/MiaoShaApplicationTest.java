package com.mc.miaosha;

import com.mc.miaosha.dao.ItemStockDOMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class MiaoShaApplicationTest {
    private static final Logger logger = LoggerFactory.getLogger(MiaoShaApplicationTest.class);
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Test
    public void test(){
        redisTemplate.opsForValue().set("1","1");
        String o = (String)redisTemplate.opsForValue().get("1");
        System.out.println(o);
    }

    @Test
    public void test2(){
        int i = itemStockDOMapper.decreaseStock(14, 1);
        System.out.println(i);
    }

    @Test
    public void test3(){
    }
}
