package com.example.redisdemo.lock;

import java.util.concurrent.TimeUnit;

/**
 * @ProjectName redisdemo
 * @Author: lancx
 * @Date: 2019/12/15 17:32
 * @Description:
 */
public interface RedisLock {
    Boolean tryLock(String key, Long timeout , TimeUnit timeUtil);

    void releaseLock(String key);
}
