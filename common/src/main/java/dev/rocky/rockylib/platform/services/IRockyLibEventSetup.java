package dev.rocky.rockylib.platform.services;

public interface IRockyLibEventSetup {
    /**
     * Registers common event handlers for the Amber mod.
     */
    void registerCommon();

    /**
     * Registers client-specific event handlers for the Amber mod.
     */
    void registerClient();

    /**
     * Registers server-specific event handlers for the Amber mod.
     */
    void registerServer();
}
