package com.atlassian.connect.spring.internal.descriptor;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A loader of Atlassian Connect add-on descriptors, providing replacement of configuration placeholders.
 */
@Component
public class AddonDescriptorLoader implements ResourceLoaderAware {

    public static final String DESCRIPTOR_FILENAME = "atlassian-connect.json";

    public static final String DESCRIPTOR_RESOURCE_PATH = "classpath:" + DESCRIPTOR_FILENAME;

    private ResourceLoader resourceLoader;

    @Autowired
    private ConfigurableEnvironment configurableEnvironment;

    private ObjectMapper mapper;

    public AddonDescriptorLoader() {
        mapper = createObjectMapper();
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public AddonDescriptor getDescriptor() {
        String descriptor = getRawDescriptor();
        try {
            return mapper.readValue(descriptor, AddonDescriptor.class);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public String getRawDescriptor() {
        String rawDescriptor = getRawDescriptorQuietly();
        return configurableEnvironment.resolvePlaceholders(rawDescriptor);
    }

    private String getRawDescriptorQuietly() {
        try {
            return getDescriptorResourceContents();
        } catch (IOException e) {
            throw new AssertionError("Could not load add-on descriptor", e);
        }
    }

    private String getDescriptorResourceContents() throws IOException {
        Resource resource = resourceLoader.getResource(DESCRIPTOR_RESOURCE_PATH);
        if (!resource.exists()) {
            throw new IOException(String.format("No add-on descriptor found (%s)", DESCRIPTOR_FILENAME));
        }
        return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setVisibility(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
        return mapper;
    }
}
