/*
 * Copyright 2012-2014 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
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

package fr.insalyon.citi.golo.runtime;

import java.lang.invoke.*;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.constant;
import static java.lang.invoke.MethodType.genericMethodType;

public class ClosureReferenceSupport {

  public static CallSite bootstrap(MethodHandles.Lookup caller, String name, MethodType type, String moduleClass, int arity, int varargs) throws Throwable {
    Class<?> module = caller.lookupClass().getClassLoader().loadClass(moduleClass);
    Method function = module.getDeclaredMethod(name, genericMethodType(arity, varargs == 1).parameterArray());
    function.setAccessible(true);
    return new ConstantCallSite(constant(MethodHandle.class, caller.unreflect(function)));
  }
}
