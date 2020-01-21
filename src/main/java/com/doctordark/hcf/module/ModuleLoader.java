package com.doctordark.hcf.module;

import com.doctordark.hcf.HCF;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ModuleLoader {

    private final HCF plugin;
    private List<Module> modules;

    public ModuleLoader(HCF plugin) {
        this.plugin = plugin;

        this.modules = new ArrayList<>();
    }

    /**
     * Loads a module
     */
    public void load(Module module) {
        this.modules.add(module);

        module.load();
    }

}
