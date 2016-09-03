package com.atlassian.connect.spring.test.descriptor;

import com.atlassian.connect.spring.internal.descriptor.AddonDescriptor;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AddonDescriptorLoaderTest extends BaseApplicationTest {

    @Value("${add-on.key}")
    private String addonKey;

    @Autowired
    private AddonDescriptorLoader addonDescriptorLoader;

    @Test
    public void shouldReturnRawDescriptor() {
        String rawDescriptor = addonDescriptorLoader.getRawDescriptor();
        assertThat(rawDescriptor, startsWith("{"));
    }

    @Test
    public void shouldReturnAddonKey() {
        AddonDescriptor descriptor = addonDescriptorLoader.getDescriptor();
        assertThat(descriptor.getKey(), is(addonKey));
    }

    @Test
    public void shouldReturnBaseUrl() {
        assertThat(addonDescriptorLoader.getDescriptor().getBaseUrl(), is("http://localhost:8081"));
    }

    @Test
    public void shouldReturnAuthenticationType() {
        assertThat(addonDescriptorLoader.getDescriptor().getAuthenticationType(), is("jwt"));
    }

    @Test
    public void shouldReturnLifecycleUrls() {
        assertThat(addonDescriptorLoader.getDescriptor().getInstalledLifecycleUrl(), is("/hello-installed"));
        assertThat(addonDescriptorLoader.getDescriptor().getEnabledLifecycleUrl(), nullValue());
        assertThat(addonDescriptorLoader.getDescriptor().getDisabledLifecycleUrl(), nullValue());
        assertThat(addonDescriptorLoader.getDescriptor().getUninstalledLifecycleUrl(), is("/hello-uninstalled"));
    }
}
