package com.doctordark.hcf.configuration;

import com.doctordark.hcf.HCF;
import com.doctordark.hcf.module.Module;

public class ConfigurationModule implements Module {

    private final HCF plugin;

    public ConfigurationModule(HCF plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "configuration";
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }
}
