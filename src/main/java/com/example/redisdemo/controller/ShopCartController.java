package com.example.redisdemo.controller;

import com.example.redisdemo.lock.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.TimeUnit;

/**
 * @ProjectName redisdemo
 * @Author: lancx
 * @Date: 2019/12/15 16:35
 * @Description:
 */
public class ShopCartController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisLock redisLock;

    protected static final String product = "12345678";

    @RequestMapping("/submitOrder")
    public String submitOrder() {
        redisLock.tryLock(product,30L,TimeUnit.SECONDS);
        try{
            //redis 中查询商品库存 stock = 50
            int stock = Integer.parseInt(stringRedisTemplate.opsForValue().get("stock"));
            if (stock > 0) {
                // TODO: 2019/12/15  下单
                stock = stock - 1;
                stringRedisTemplate.opsForValue().set("stock", stock + "");
                System.out.println("扣减成功，当前库存：" + stock);
            } else {
                System.out.println("扣减失败，库存不足");
            }
        }finally {
            redisLock.releaseLock(product);
        }
        return "end";
    }
}
