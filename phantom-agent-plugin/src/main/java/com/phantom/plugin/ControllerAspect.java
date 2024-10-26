package com.phantom.plugin;

import com.phantom.core.aspect.IAspectDefinition;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * Created by pphh on 2022/8/5.
 */
public class ControllerAspect implements IAspectDefinition {

    private static final String ENHANCE_CLASS = "com.phantom.demo.DemoController";
    private static final String ENHANCE_METHOD = "sayHello";
    private static final String INTERCEPT_CLASS = "com.phantom.plugin.TomcatEnhancer";

    @Override
    public ElementMatcher.Junction enhanceClass() {
        return named(ENHANCE_CLASS).and(not(isInterface()));
    }

    @Override
    public ElementMatcher<MethodDescription> getMethodsMatcher() {
        return nameStartsWith("say");
    }

    @Override
    public String getMethodsEnhancer() {
        return INTERCEPT_CLASS;
    }

}
