package com.phantom.demo;

import com.phantom.core.aspect.IAspectEnhancer;
import com.phantom.core.context.TraceContext;
import com.phantom.core.log.Logger;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.apache.catalina.connector.Request;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

/**
 * Created by pphh on 2022/8/4.
 */
public class RecallEnhancer implements IAspectEnhancer {

    private static ThreadLocal<Span> spanThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<Integer> sizeChanged = new ThreadLocal<>();
    
    Stack<Integer> stackStart = new Stack<>();
    Stack<List<Unit>> subRangeList = new Stack<>();

    
    class Unit {
        int start;
        int end;

        public Unit(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            // System.out.println(start+ "  ,  " +end);
            return String.format("%d , %d", start, end);
        }
    }

    @Override
    public void beforeMethod(Object objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object result) throws Throwable {
        Logger.info("[trace-plus]beforeMethod(), method = %s.%s", method.getDeclaringClass().getName(), method.getName());
        
        if(objInst instanceof DemoController) {
            DemoController controller = (DemoController) objInst;
            
            stackStart.push(controller.l.size());
            subRangeList.push(new ArrayList<>());
        }

        if (allArguments.length <= 0) {
            return;
        }
        Request request = (Request) allArguments[0];
        TextMapGetter<Request> getter =
                new TextMapGetter<Request>() {
                    @Override
                    public String get(Request carrier, String key) {
                        return carrier.getHeader(key);
                    }

                    @Override
                    public Iterable<String> keys(Request carrier) {
                        List<String> keys = new ArrayList<>();
                        for (final Enumeration<String> headers = carrier.getParameterNames(); headers.hasMoreElements(); ) {
                            final String name = headers.nextElement();
                            keys.add(name);
                        }
                        return keys;
                    }
                };

        Context extractedContext = TraceContext.TextPropagator().extract(Context.current(), request, getter);
        extractedContext.makeCurrent();

        String operationName = String.format("%s %s", request.getMethod(), request.getRequestURI());
        Span span = TraceContext.TRACER().spanBuilder(operationName)
                .setSpanKind(SpanKind.PRODUCER)
                .startSpan()
                .setAttribute("URL", request.getRequestURL().toString())
                .setAttribute("METHOD", request.getMethod())
                .setAttribute("URI", request.getRequestURI())
                .setAttribute("RemoteAddr", request.getRemoteAddr())
                .setAttribute("RemoteHost", request.getRemoteHost())
                .setAttribute("RemotePort", request.getRemotePort())
                .setAttribute("RemoteUser", request.getRemoteUser())
                .setAttribute("Component", "Tomcat");
        span.makeCurrent();
        spanThreadLocal.set(span);
    }

    @Override
    public Object afterMethod(Object objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object result) throws Throwable {
        Logger.info("[trace-off]afterMethod(), method = %s.%s", method.getDeclaringClass().getName(), method.getName());
        endCurrenSpan();
        
        if(objInst instanceof DemoController) {
            DemoController controller = (DemoController) objInst;
            
            // todo some thing with the list of subrange
            List<Unit> lOfUnit = subRangeList.pop();
            System.out.println(lOfUnit);
            int my_start = stackStart.pop();
            int my_end = controller.l.size();
            if(!subRangeList.isEmpty()) {
                subRangeList.peek().add(new Unit(my_start, my_end));
            }
            
            Collections.reverse(lOfUnit);//反转
            List<String> toRemove = new ArrayList<>();
            lOfUnit.forEach(u -> {
                for(int i=u.end-1;i>=u.start;i--) {
                    toRemove.add(controller.l.get(i));
                }
            });
            List<String> myResult = new ArrayList<>(controller.l.subList(my_start, my_end));
            myResult.removeAll(toRemove);
            System.out.println("zhouxiang===my===>"+method.getName() + myResult);
        }


        return null;
    }

    @Override
    public void handleMethodException(Object objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {
        Logger.info("[trace]handleMethodException(), method = %s.%s", method.getDeclaringClass().getName(), method.getName());
        endCurrenSpan();
    }

    private void endCurrenSpan() {
        Span span = spanThreadLocal.get();
        if (span != null) {
            span.end();
            spanThreadLocal.remove();
        }
    }

}

