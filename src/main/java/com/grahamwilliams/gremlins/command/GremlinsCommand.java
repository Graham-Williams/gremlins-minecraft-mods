package com.grahamwilliams.gremlins.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.grahamwilliams.gremlins.Gremlins;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * The {@code /gremlins} command -- a tracer bullet / smoke test.
 *
 * <p>Reports the mod name, its version (read from the mod's own metadata, never
 * hardcoded) and the list of loaded feature modules. Running it proves the mod is
 * actually loaded <em>on the server</em> and that server-to-client messaging works.
 *
 * <p>Deliberately has <strong>zero gameplay impact</strong> and <strong>no permission
 * requirement</strong> -- every player can run it, it is purely informational.
 */
public final class GremlinsCommand {
    private GremlinsCommand() {
    }

    /**
     * Feature modules wired up in {@link Gremlins#onInitialize()}, in display order.
     * Adding a future module is a one-line change here.
     */
    private static final List<String> MODULES = List.of(
            "Wither Wings");

    /** Shown if the mod container is somehow missing (should never happen in practice). */
    private static final String UNKNOWN_VERSION = "unknown";

    public static void init() {
        // No .requires(...) -- available to ALL players, not just operators.
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal(Gremlins.MOD_ID)
                        .executes(GremlinsCommand::run)));
        Gremlins.LOGGER.info("[Gremlins] /{} command registered", Gremlins.MOD_ID);
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        Component message = buildMessage();
        // Feedback to the sender only (false = do not broadcast to other operators).
        // Works for a non-player source too: the dedicated-server console source simply
        // logs the message, so this never assumes (or dereferences) a player.
        context.getSource().sendSuccess(() -> message, false);
        return Command.SINGLE_SUCCESS;
    }

    /** e.g. {@code Gremlins v0.1.0 - modules: Wither Wings} (with colour). */
    private static Component buildMessage() {
        String modules = MODULES.isEmpty() ? "none" : String.join(", ", MODULES);
        // Built off an empty root so each sibling carries only its own style
        // (a styled root would leak e.g. bold into every child).
        return Component.empty()
                .append(Component.literal("Gremlins")
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                .append(Component.literal(" v" + version())
                        .withStyle(ChatFormatting.GRAY))
                // \u2014 is an em dash, escaped so the source stays pure ASCII
                // (javac's default source encoding is platform-dependent).
                .append(Component.literal(" \u2014 modules: ")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(modules)
                        .withStyle(ChatFormatting.AQUA));
    }

    /** The running mod's version, read dynamically so it can never go stale. */
    private static String version() {
        return FabricLoader.getInstance()
                .getModContainer(Gremlins.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse(UNKNOWN_VERSION);
    }
}
