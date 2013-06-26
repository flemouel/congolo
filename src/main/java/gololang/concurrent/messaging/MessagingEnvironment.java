/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gololang.concurrent.messaging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A messaging environment is an abstraction over a set of spawned functions that can asynchronously process messages
 * sent through topics.
 * <p/>
 * Each topic is internally associated to a set of messaging functions and a messages queue. The messaging environment maintains
 * an executor that dispatches message processing jobs over its thread pool.
 */
public final class MessagingEnvironment {

    private final ExecutorService executor;

    /**
     * Creates a new messaging environment using an executor.
     *
     * @param executor the executor.
     */
    public MessagingEnvironment(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * @return a new messaging environment with a cached thread pool.
     * @see java.util.concurrent.Executors#newCachedThreadPool()
     */
    public static MessagingEnvironment newMessagingEnvironment() {
        return new MessagingEnvironment(Executors.newCachedThreadPool());
    }

    /**
     * @return a messaging environment builder object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * messaging environment builder objects exist mostly to provide a good-looking API in Golo.
     */
    public static class Builder {

        /**
         * @return a messaging environment with a cached thread pool.
         * @see java.util.concurrent.Executors#newCachedThreadPool()
         */
        public MessagingEnvironment withCachedThreadPool() {
            return newMessagingEnvironment();
        }

        /**
         * @param size the thread pool size.
         * @return a messaging environment with a fixed-size thread pool.
         * @see java.util.concurrent.Executors#newFixedThreadPool(int)
         */
        public MessagingEnvironment withFixedThreadPool(int size) {
            return new MessagingEnvironment(Executors.newFixedThreadPool(size));
        }

        /**
         * @return a messaging environment with a fixed-size thread pool in the number of available processors.
         * @see java.util.concurrent.Executors#newFixedThreadPool(int)
         * @see Runtime#availableProcessors()
         */
        public MessagingEnvironment withFixedThreadPool() {
            return withFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }

        /**
         * @return a messaging environment with a single executor thread.
         */
        public MessagingEnvironment withSingleThreadExecutor() {
            return new MessagingEnvironment(Executors.newSingleThreadExecutor());
        }
    }

    /**
     * Spawns a messaging topic.
     *
     * @return a topic to send messages.
     */
    public Topic spawn() {
        return new Topic(executor);
    }

    /**
     * Shutdown the messaging environment.
     *
     * @return the same messaging environment object.
     * @see java.util.concurrent.ExecutorService#shutdown()
     */
    public MessagingEnvironment shutdown() {
        executor.shutdown();
        return this;
    }

    /**
     * Waits until all remaining messages have been processed.
     *
     * @param millis the delay.
     * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    public boolean awaitTermination(int millis) throws InterruptedException {
        return awaitTermination((long) millis);
    }

    /**
     * Waits until all remaining messages have been processed.
     *
     * @param millis the delay.
     * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    public boolean awaitTermination(long millis) throws InterruptedException {
        return awaitTermination(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Waits until all remaining messages have been processed.
     *
     * @param timeout the delay.
     * @param unit    the delay time unit.
     * @see java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    /**
     * @see java.util.concurrent.ExecutorService#isShutdown()
     */
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    /**
     * @see java.util.concurrent.ExecutorService#isTerminated()
     */
    public boolean isTerminated() {
        return executor.isTerminated();
    }
}
