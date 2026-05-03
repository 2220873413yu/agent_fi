package com.xms.common.thread;

import java.util.concurrent.ThreadFactory;


public final class FixedNameThreadFactory extends ThreadCreator implements ThreadFactory {
    public FixedNameThreadFactory(String threadNamePrefix) {
        super(threadNamePrefix);
        this.setDaemon(true);
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return createThread(runnable);
    }

    @Override
    protected String nextThreadName() {
        return this.getThreadNamePrefix();
    }
}
