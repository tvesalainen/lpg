/*
 * Copyright (C) 2013 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.parser.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Timo Vesalainen
 */
public class DelayedExecutor<T> implements InvocationHandler
{
    private T proxy;
    private Deque<Invokation> queue = new ArrayDeque<>();
    
    public DelayedExecutor(Class<T> intf)
    {
        for (Method method : intf.getMethods())
        {
            if (!void.class.equals(method.getReturnType()))
            {
                throw new IllegalArgumentException(method+" return other than void");
            }
        }
        proxy = (T) Proxy.newProxyInstance(DelayedExecutor.class.getClassLoader(), new Class<?>[] {intf}, this);
    }

    public T getProxy()
    {
        return proxy;
    }
    
    public void append(DelayedExecutor<T> target)
    {
        queue.addAll(target.queue);
    }
    public void execute(T target) throws ReflectiveOperationException
    {
        for (Invokation invokation : queue)
        {
            invokation.invoke(target);
        }
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        queue.add(new Invokation(method, args));
        return null;
    }

    public class Invokation
    {
        private Method method;
        private Object[] args;

        public Invokation(Method method, Object[] args)
        {
            this.method = method;
            this.args = args;
        }
        
        public void invoke(T proxy) throws ReflectiveOperationException
        {
            method.invoke(proxy, args);
        }
    }
}
