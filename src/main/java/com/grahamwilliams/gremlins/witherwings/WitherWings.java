package com.grahamwilliams.gremlins.witherwings;

import com.grahamwilliams.gremlins.Gremlins;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Wither Wings module.
 *
 * <p>Adds two items -- the {@code Wither's Crown} (dropped when a player kills a
 * Wither) and the {@code Wither Wing Template} (a smithing template). The actual
 * Wither Wings result item is produced entirely by data-driven recipes
 * (see {@code data/gremlins/recipe/}); the smithing recipe transforms a
 * netherite chestplate + elytra into a chestplate carrying the vanilla
 * {@code minecraft:glider} data component, so gliding + durability behaviour is
 * fully native and needs no Java.
 */
public final class WitherWings {
    private WitherWings() {
    }

    /** {@code gremlins:withers_crown} -- dropped on a player-credited Wither kill. */
    public static final Item WITHERS_CROWN = register("withers_crown");

    /** {@code gremlins:wither_wing_template} -- the smithing template. */
    public static final Item WITHER_WING_TEMPLATE = register("wither_wing_template");

    private static Item register(String path) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Gremlins.id(path));
        Item item = new Item(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void init() {
        // Touching the static fields above (done by class load) performs item
        // registration. Now wire up the drop-on-kill behaviour.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Only Withers.
            if (!(entity instanceof WitherBoss)) {
                return;
            }
            // Cheese-proof: the killing blow must be attributed to a player
            // (mirrors vanilla kill-credit -- getEntity() is the ultimate
            // attacker, e.g. the player who fired an arrow). Excludes
            // environmental kills such as lava or suffocation.
            if (!(damageSource.getEntity() instanceof ServerPlayer)) {
                return;
            }
            if (!(entity.level() instanceof ServerLevel level)) {
                return;
            }
            // Drop one Wither's Crown at the Wither's death location. It lies on
            // the ground, so a full player inventory is a non-issue.
            ItemStack stack = new ItemStack(WITHERS_CROWN);
            ItemEntity drop = new ItemEntity(
                    level, entity.getX(), entity.getY() + 0.5, entity.getZ(), stack);
            level.addFreshEntity(drop);
        });
        Gremlins.LOGGER.info("[Gremlins] Wither Wings module initialized");
    }
}
