package com.doctordark.hcf.module;

public interface Module {

    /**
     * Gets the id of this {@link Module}
     *
     * @return the id
     */
    String getId();

    /**
     * Loads the module
     */
    void load();

    /**
     * Unloads the module
     */
    void unload();

}
