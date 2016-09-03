package com.atlassian.connect.spring.it;

import com.atlassian.connect.spring.AddonInstalledEvent;
import com.atlassian.connect.spring.AddonUninstalledEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

@Component
public class AddonLifecycleEventCollector {

    private static final int POLL_TIMEOUT_SECONDS = 5;

    private TransferQueue<AddonInstalledEvent> installedEvents = new LinkedTransferQueue<>();

    private TransferQueue<AddonUninstalledEvent> uninstalledEvents = new LinkedTransferQueue<>();

    @EventListener
    public void handleAddonInstalledEvent(AddonInstalledEvent installedEvent) throws InterruptedException {
        installedEvents.add(installedEvent);

        // An exception thrown here will not fail the request to the /installed resource
        throw new LifecycleEventException();
    }

    @EventListener
    public void handleAddonUninstalledEvent(AddonUninstalledEvent uninstalledEvent) throws InterruptedException {
        uninstalledEvents.add(uninstalledEvent);

        // An exception thrown here will not fail the request to the /uninstalled resource
        throw new LifecycleEventException();
    }

    public AddonInstalledEvent takeInstalledEvent() throws InterruptedException {
        AddonInstalledEvent event = installedEvents.poll(POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assert installedEvents.isEmpty();
        return event;
    }

    public AddonUninstalledEvent takeUninstalledEvent() throws InterruptedException {
        AddonUninstalledEvent event = uninstalledEvents.poll(POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assert uninstalledEvents.isEmpty();
        return event;
    }

    public void clearEvents() {
        installedEvents.clear();
        uninstalledEvents.clear();
    }

    public static class LifecycleEventException extends RuntimeException {}
}
