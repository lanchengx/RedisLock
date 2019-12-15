package com.example.redisdemo.lock;

import ch.qos.logback.core.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @ProjectName redisdemo
 * @Author: lancx
 * @Date: 2019/12/15 17:32
 * @Description:
 */
public class RedisLockImpl implements RedisLock {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //线程安全  线程服用
    private ThreadLocal<String> threadLocal = new ThreadLocal<>();

    @Override
    public Boolean tryLock(String key, Long timeout, TimeUnit unit) {
        Boolean lock = false;
        if (threadLocal.get() == null) {
            //（异步）开启新线程更新过期时间
            while (true) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        stringRedisTemplate.expire(key, timeout, unit);
                    }

                };
                //拿到锁的线程启动续命线程
                thread.start();

                String uuid = thread.getId() + ":" + UUID.randomUUID().toString();
                //保证线程安全
                threadLocal.set(uuid);
                lock = stringRedisTemplate.opsForValue().setIfAbsent(key, uuid, timeout, unit);
                //cpu 性能   （while阻塞）
                while (true) {
                    lock = stringRedisTemplate.opsForValue().setIfAbsent(key, uuid, timeout, unit);
                    if (lock) {
                        break;
                    }
                }

            }
        } else if (threadLocal.get().equals(stringRedisTemplate.opsForValue().get(key))) {
            lock = true;
        }
        return lock;
    }

    @Override
    public void releaseLock(String key) {
        if (threadLocal.get().equals(stringRedisTemplate.opsForValue().get(key))) {
            //停止线程 更新过期时间线程
            findThread(Long.parseLong(threadLocal.get().split(":")[0])).interrupt();
            stringRedisTemplate.delete(key);
        }
    }

    public static Thread findThread(Long threadId) {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        while (group != null) {
            Thread[] threads = new Thread[(int) (group.activeCount() * 1.2)];
            int count = group.enumerate(threads, true);
            for (int i = 0; i < count; i++) {
                if (threadId == threads[i].getId()) {
                    return threads[i];
                }
            }
            group = group.getParent();
        }
        return null;
    }
}
