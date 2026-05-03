package com.xms.common.thread;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * copy from springframework
 *
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThreadCreator {

    final AtomicInteger threadCount = new AtomicInteger();
    String threadNamePrefix;
    int threadPriority = Thread.NORM_PRIORITY;
    boolean daemon = false;
    ThreadGroup threadGroup;

    public ThreadCreator(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public void setThreadGroupName(String name) {
        this.threadGroup = new ThreadGroup(name);
    }

    public Thread createThread(Runnable runnable) {
        Thread thread = new Thread(getThreadGroup(), runnable, nextThreadName());
        thread.setPriority(threadPriority);
        thread.setDaemon(daemon);
        return thread;
    }

    protected String nextThreadName() {
        String format = "%s-%d";
        return String.format(format, this.threadNamePrefix, this.threadCount.incrementAndGet());
    }
}
