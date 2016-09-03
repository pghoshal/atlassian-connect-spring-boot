package com.atlassian.connect.spring.internal.lifecycle;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LifecycleControllerTest {

    @Test
    public void shouldReturnInstalledMethodReference() {
        assertThat(LifecycleController.getInstalledMethod().getName(), is("installed"));
    }

    @Test
    public void shouldReturnUninstalledMethodReference() {
        assertThat(LifecycleController.getUninstalledMethod().getName(), is("uninstalled"));
    }
}
