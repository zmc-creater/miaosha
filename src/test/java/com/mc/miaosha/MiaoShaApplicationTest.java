package com.mc.miaosha;

import com.mc.miaosha.dao.ItemStockDOMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class MiaoShaApplicationTest {
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

}
