package com.atlassian.connect.spring.test.descriptor;

import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class AddonDescriptorControllerTest extends BaseApplicationTest {

    @Value("${add-on.key}")
    private String addonKey;

    @Value("${add-on.name}")
    private String addonName;

    @Test
    public void shouldRedirectRootToDescriptor() throws Exception {
        mvc.perform(get("/")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/atlassian-connect.json"));
    }

    @Test
    public void shouldReturnDescriptor() throws Exception {
        mvc.perform(get("/atlassian-connect.json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value(is(addonKey)));
    }

    @Test
    public void shouldReplacePropertyPlaceholdersInDescriptor() throws Exception {
        mvc.perform(get("/atlassian-connect.json")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(is(addonName)));
    }
}
