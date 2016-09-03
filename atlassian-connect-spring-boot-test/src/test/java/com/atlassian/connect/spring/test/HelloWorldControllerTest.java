package com.atlassian.connect.spring.test;

import com.atlassian.connect.spring.test.util.BaseApplicationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class HelloWorldControllerTest extends BaseApplicationTest {

    @Test
    public void shouldRenderPage() throws Exception {
        String userName = "charlie";
        mvc.perform(get("/hello-world")
                .param("username", "charlie")
                .accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(xpath("//*[@id = 'username']").string(userName));
    }
}
