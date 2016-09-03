package com.atlassian.connect.spring.internal.lifecycle;

import com.atlassian.connect.spring.internal.descriptor.AddonDescriptor;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A {@link RequestMappingHandlerMapping} that provides handler mappings for the add-on installation and uninstallation
 * lifecycle callbacks.
 *
 * @see LifecycleController
 */
@Component
public class LifecycleControllerHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * An ordinal before that used by {@link RequestMappingHandlerMapping} (0).
     *
     * @see WebMvcConfigurationSupport#requestMappingHandlerMapping()
     */
    private static final int ORDER = -100;

    @Autowired
    private AddonDescriptorLoader addonDescriptorLoader;

    private Map<Method, Function<AddonDescriptor, String>> methodUrlResolvers = new HashMap<>();

    public LifecycleControllerHandlerMapping() {
        setOrder(ORDER);
        methodUrlResolvers.put(LifecycleController.getInstalledMethod(), AddonDescriptor::getInstalledLifecycleUrl);
        methodUrlResolvers.put(LifecycleController.getUninstalledMethod(), AddonDescriptor::getUninstalledLifecycleUrl);
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return super.isHandler(beanType) && LifecycleController.class.equals(beanType);
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        return Optional.ofNullable(methodUrlResolvers.get(method)).map(this::getRequestMappingInfo).orElse(null);
    }

    private RequestMappingInfo getRequestMappingInfo(Function<AddonDescriptor, String> methodUrlResolver) {
        String url = methodUrlResolver.apply(addonDescriptorLoader.getDescriptor());
        return RequestMappingInfo.paths(url).methods(RequestMethod.POST).build();
    }
}
