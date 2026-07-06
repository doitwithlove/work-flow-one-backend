package com.touchmind.work.flow.one.sample.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "workflowone.sample.seed")
public class SampleDataSeederProperties {

    private boolean enabled = true;

    private String sourceDirs = "../turmik_q15_sample_package,../hurco_m10_sample_package";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSourceDirs() {
        return sourceDirs;
    }

    public void setSourceDirs(String sourceDirs) {
        this.sourceDirs = sourceDirs;
    }
}
