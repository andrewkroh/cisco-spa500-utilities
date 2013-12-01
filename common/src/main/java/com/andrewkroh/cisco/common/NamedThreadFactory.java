package com.andrewkroh.cisco.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

/**
 * ThreadFactory that creates named threads and wraps Runnables with
 * RuntimeExceptions handling.
 *
 * @author akroh
 */
public class NamedThreadFactory implements ThreadFactory
{
    private final String namePrefix;

    private final AtomicInteger count = new AtomicInteger();

    private final ExceptionHandler handler;

    public NamedThreadFactory(String namePrefix, ExceptionHandler handler)
    {
        this.namePrefix = Preconditions.checkNotNull(namePrefix,
                "Name prefix cannot be null.");
        this.handler = Preconditions.checkNotNull(handler,
                "ExceptionHandler cannot be null.");
    }

    @Override
    public Thread newThread(Runnable r)
    {
        int currentCount = count.incrementAndGet();
        return new Thread(new ExceptionHandlingRunnable(r, handler),
                          namePrefix + "-" + currentCount);
    }

    private static class ExceptionHandlingRunnable implements Runnable
    {
        private final Runnable runnable;

        private final ExceptionHandler handler;

        public ExceptionHandlingRunnable(Runnable runnable, ExceptionHandler handler)
        {
            this.runnable = Preconditions.checkNotNull(runnable,
                    "Runnable cannot be null.");
            this.handler = Preconditions.checkNotNull(handler,
                    "ExceptionHandler cannot be null.");
        }

        @Override
        public void run()
        {
            try
            {
                runnable.run();
            }
            catch (RuntimeException e)
            {
                handler.handle(e, "Runnable throw an exception.");
            }
        }
    }
}
