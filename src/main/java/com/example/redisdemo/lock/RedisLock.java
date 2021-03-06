package com.example.redisdemo.lock;

import java.util.concurrent.TimeUnit;

/**
 * @ProjectName redisdemo
 * @Author: lancx
 * @Date: 2019/12/15 17:32
 * @Description:
 */
public interface RedisLock {
    /**
     * 加锁
     * @param key
     * @param timeout
     * @param timeUtil
     * @return
     */
    Boolean tryLock(String key, Long timeout, TimeUnit timeUtil);

    /**
     * 解锁
     * @param key
     */
    void releaseLock(String key);
}
