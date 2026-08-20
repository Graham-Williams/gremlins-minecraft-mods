package com.grahamwilliams.gremlins;

import com.grahamwilliams.gremlins.command.GremlinsCommand;
import com.grahamwilliams.gremlins.witherwings.WitherWings;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the Gremlins Minecraft mod collection.
 *
 * <p>This is an umbrella mod: each gameplay feature lives in its own sub-package
 * and is wired up from {@link #onInitialize()}. The first module is
 * {@link WitherWings}.
 */
public class Gremlins implements ModInitializer {
    public static final String MOD_ID = "gremlins";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Helper for building {@code gremlins:*} identifiers. */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("[Gremlins] initializing");
        // Register the Wither Wings module (items + drop-on-kill behaviour).
        WitherWings.init();
        // Register /gremlins -- the smoke-test command (no gameplay impact).
        GremlinsCommand.init();
    }
}
