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

import java.lang.invoke.MethodHandle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.invoke.MethodHandleProxies.asInterfaceInstance;

/**
 * A topic is the communication endpoint to a set of registered listener messaging functions.
 * <p>
 * A topic is obtained from a messaging environment when spawned. It can then be used to send messages that
 * will be eventually processed by the set of target registered functions. Messages are being put in a first-in, first-out queue.
 */
public final class Topic {

  private final ExecutorService executor;

  private final ConcurrentHashMap<MessagingFunction, MessagingFunction> functions = new ConcurrentHashMap<>();
  private final ConcurrentLinkedQueue<Object> queue = new ConcurrentLinkedQueue<>();

  private final AtomicBoolean running = new AtomicBoolean(false);

  /**
   * Topic constructor.
   *
   * @param executor the executor to dispatch the asynchronous message handling jobs to.
   */
  public Topic(ExecutorService executor) {
    this.executor = executor;
  }

  /**
    * Registers an new listener function.
    *
    * @param handle the listener messaging function target.
    */
  public void register(MethodHandle handle) {
    registerListener(asInterfaceInstance(MessagingFunction.class, handle));
  }

  /**
   * Registers an new listener function.
   *
   * @param function the listener messaging function target.
   */
  public void registerListener(MessagingFunction function) {
      functions.putIfAbsent(function, function);
  }

    /**
     * Unregisters a listener function.
     *
     * @param handle the listener messaging function target.
     */
    public void unregister(MethodHandle handle) {
        unregisterListener(asInterfaceInstance(MessagingFunction.class, handle));
    }

  /**
    * Unregisters a listener function.
    *
    * @param function the listener messaging function target.
    */
  public void unregisterListener(MessagingFunction function) {
        functions.remove(function);
  }

  private class Runner implements Runnable {
    MessagingFunction function;
    Object message;
    public Runner(MessagingFunction function, Object message) {
        this.function = function;
        this.message = message;
    }
    public void run() {
            try {
                function.apply(message);
            } finally {
                scheduleNext();
            }
        }
    }

  private void scheduleNext() {
      if (!queue.isEmpty() && running.compareAndSet(false, true)) {
      try {
          Object message = queue.poll();
          for (MessagingFunction function : functions.keySet()) {
            executor.execute(new Runner(function, message));
          }
          running.set(false);
      } catch (Throwable t) {
          running.set(false);
        throw t;
      }
      }
  }

  /**
   * Sends a message to the set of listener messaging functions. This method returns immediately as message processing is
   * asynchronous.
   *
   * @param message the message of any type.
   * @return the same topic object.
   */
  public Topic send(Object message) {
    queue.offer(message);
    scheduleNext();
    return this;
  }
}
