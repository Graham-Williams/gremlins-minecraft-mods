package com.grahamwilliams.gremlins.test;

import com.grahamwilliams.gremlins.Gremlins;
import com.grahamwilliams.gremlins.witherwings.WitherWings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Server game tests for the gremlins mod, run headless by {@code ./gradlew runGameTest}
 * (which {@code ./gradlew build} depends on through {@code check}).
 *
 * <p>These run against a real dedicated-server instance of the targeted Minecraft
 * version, so they exercise the real registries, the real datapack recipe loader and
 * the real command dispatcher. They are what makes a Minecraft version port
 * checkable without launching a client.
 */
public class GremlinsGameTests {
    /** Set by the Gradle run config from {@code mod_version} in gradle.properties. */
    private static final String EXPECTED_VERSION_PROPERTY = "gremlins.expectedVersion";

    // ---------------------------------------------------------------- items

    @GameTest
    public void itemsAreRegistered(GameTestHelper helper) {
        for (String path : List.of("withers_crown", "wither_wing_template")) {
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(Gremlins.id(path));
            helper.assertTrue(item.isPresent(), "gremlins:" + path + " is not in the item registry");
        }
        helper.assertValueEqual(
                BuiltInRegistries.ITEM.getKey(WitherWings.WITHERS_CROWN),
                Gremlins.id("withers_crown"), "id of WITHERS_CROWN");
        helper.assertValueEqual(
                BuiltInRegistries.ITEM.getKey(WitherWings.WITHER_WING_TEMPLATE),
                Gremlins.id("wither_wing_template"), "id of WITHER_WING_TEMPLATE");
        helper.succeed();
    }

    // -------------------------------------------------------------- recipes

    @GameTest
    public void templateRecipeCraftsFromCrownAndMembrane(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getServer().getRecipeManager();
        helper.assertTrue(
                recipes.byKey(recipeKey("wither_wing_template")).isPresent(),
                "recipe gremlins:wither_wing_template was not loaded");

        CraftingInput input = CraftingInput.of(2, 1, List.of(
                new ItemStack(WitherWings.WITHERS_CROWN),
                new ItemStack(Items.PHANTOM_MEMBRANE)));
        Optional<RecipeHolder<CraftingRecipe>> match =
                recipes.getRecipeFor(RecipeType.CRAFTING, input, level);
        helper.assertTrue(match.isPresent(), "crown + phantom membrane matched no crafting recipe");
        helper.assertValueEqual(
                match.get().id(), recipeKey("wither_wing_template"), "matched crafting recipe");

        ItemStack result = match.get().value().assemble(input);
        helper.assertTrue(
                result.is(WitherWings.WITHER_WING_TEMPLATE),
                "crafting result is " + result + ", expected a Wither Wing Template");
        helper.assertValueEqual(result.getCount(), 1, "crafting result count");

        // The crown is required: a membrane alone must not make a template.
        CraftingInput membraneOnly = CraftingInput.of(1, 1, List.of(
                new ItemStack(Items.PHANTOM_MEMBRANE)));
        helper.assertFalse(
                recipes.getRecipeFor(RecipeType.CRAFTING, membraneOnly, level)
                        .filter(holder -> holder.id().equals(recipeKey("wither_wing_template")))
                        .isPresent(),
                "template recipe matched without a Wither's Crown");
        helper.succeed();
    }

    @GameTest
    public void smithingRecipeAddsGliderAndLoreWithoutRenaming(GameTestHelper helper) {
        ItemStack result = smith(helper, new ItemStack(Items.NETHERITE_CHESTPLATE));

        helper.assertTrue(
                result.is(Items.NETHERITE_CHESTPLATE),
                "smithing result is " + result + ", expected a netherite chestplate");
        helper.assertTrue(
                result.has(DataComponents.GLIDER), "smithing result has no minecraft:glider component");

        ItemLore lore = result.get(DataComponents.LORE);
        helper.assertTrue(lore != null, "smithing result has no minecraft:lore component");
        helper.assertValueEqual(lore.lines().size(), 1, "lore line count");
        helper.assertValueEqual(lore.lines().get(0).getString(), "Wither Wings", "lore line text");
        // styledLines() is what the tooltip renders: the line's own style merged over
        // vanilla's default lore style. Gray is load-bearing: without an explicit colour
        // the default (dark purple) wins.
        Component rendered = lore.styledLines().get(0);
        helper.assertValueEqual(
                rendered.getStyle().getColor(),
                TextColor.fromLegacyFormat(ChatFormatting.GRAY), "rendered lore line colour");
        helper.assertTrue(rendered.getStyle().isItalic(), "rendered lore line is not italic");

        CustomData data = result.get(DataComponents.CUSTOM_DATA);
        helper.assertTrue(data != null, "smithing result has no minecraft:custom_data component");
        helper.assertTrue(
                data.copyTag().getBooleanOr("wither_wings", false),
                "custom_data is missing wither_wings=true");

        // Issue #5: the result must keep the chestplate's own name.
        helper.assertFalse(
                result.has(DataComponents.CUSTOM_NAME),
                "smithing result sets minecraft:custom_name; it must keep the item's own name");
        helper.succeed();
    }

    @GameTest
    public void smithingRecipePreservesBaseNameAndDamage(GameTestHelper helper) {
        ItemStack base = new ItemStack(Items.NETHERITE_CHESTPLATE);
        base.set(DataComponents.CUSTOM_NAME, Component.literal("Old Faithful"));
        base.setDamageValue(37);

        ItemStack result = smith(helper, base);

        Component name = result.get(DataComponents.CUSTOM_NAME);
        helper.assertTrue(name != null, "the base chestplate's anvil name was dropped");
        helper.assertValueEqual(name.getString(), "Old Faithful", "preserved anvil name");
        helper.assertValueEqual(result.getDamageValue(), 37, "preserved damage value");
        helper.assertTrue(result.has(DataComponents.GLIDER), "renamed base lost the glider component");
        helper.succeed();
    }

    @GameTest
    public void smithingRecipePreservesEnchantmentsAndTrim(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        Holder<Enchantment> protection =
                registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
        ArmorTrim trim = new ArmorTrim(
                registries.lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(TrimMaterials.QUARTZ),
                registries.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(TrimPatterns.SENTRY));

        ItemStack base = new ItemStack(Items.NETHERITE_CHESTPLATE);
        base.enchant(protection, 4);
        base.set(DataComponents.TRIM, trim);

        ItemStack result = smith(helper, base);

        helper.assertValueEqual(
                EnchantmentHelper.getItemEnchantmentLevel(protection, result), 4,
                "Protection level on the smithing result");
        helper.assertTrue(result.has(DataComponents.TRIM), "the base chestplate's armour trim was dropped");
        helper.assertValueEqual(result.get(DataComponents.TRIM), trim, "armour trim on the smithing result");
        helper.assertTrue(result.has(DataComponents.GLIDER), "enchanted base lost the glider component");
        helper.succeed();
    }

    @GameTest
    public void smithingRecipeRequiresTheTemplate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        SmithingRecipeInput input = new SmithingRecipeInput(
                new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                new ItemStack(Items.NETHERITE_CHESTPLATE),
                new ItemStack(Items.ELYTRA));
        helper.assertFalse(
                level.getServer().getRecipeManager()
                        .getRecipeFor(RecipeType.SMITHING, input, level).isPresent(),
                "chestplate + elytra smithed without a Wither Wing Template");
        helper.succeed();
    }

    // ----------------------------------------------------------- wither drop

    @GameTest
    public void playerKilledWitherDropsOneCrown(GameTestHelper helper) {
        Player player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        WitherBoss wither = spawnWither(helper);
        killWith(helper, wither, helper.getLevel().damageSources().playerAttack(player));
        helper.assertValueEqual(crownsNear(helper, wither), 1, "Wither's Crowns dropped by a player kill");
        helper.succeed();
    }

    @GameTest
    public void playerArrowKilledWitherDropsOneCrown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        WitherBoss wither = spawnWither(helper);
        // The arrow is the direct entity; the player is only the credited owner.
        Arrow arrow = new Arrow(
                level, wither.getX(), wither.getY(), wither.getZ(), new ItemStack(Items.ARROW), null);
        killWith(helper, wither, level.damageSources().arrow(arrow, player));
        helper.assertValueEqual(crownsNear(helper, wither), 1, "Wither's Crowns dropped by a player's arrow kill");
        helper.succeed();
    }

    @GameTest
    public void environmentKilledWitherDropsNoCrown(GameTestHelper helper) {
        WitherBoss wither = spawnWither(helper);
        killWith(helper, wither, helper.getLevel().damageSources().genericKill());
        helper.assertValueEqual(crownsNear(helper, wither), 0, "Wither's Crowns dropped by an environmental kill");
        helper.succeed();
    }

    @GameTest
    public void mobKilledWitherDropsNoCrown(GameTestHelper helper) {
        IronGolem golem = helper.spawnWithNoFreeWill(EntityTypes.IRON_GOLEM, new Vec3(1.5, 1.0, 1.5));
        WitherBoss wither = spawnWither(helper);
        killWith(helper, wither, helper.getLevel().damageSources().mobAttack(golem));
        helper.assertValueEqual(crownsNear(helper, wither), 0, "Wither's Crowns dropped by a mob kill");
        golem.discard();
        helper.succeed();
    }

    // -------------------------------------------------------------- command

    @GameTest
    public void gremlinsCommandReportsVersionWithoutPermissions(GameTestHelper helper) {
        String expectedVersion = System.getProperty(EXPECTED_VERSION_PROPERTY);
        helper.assertTrue(
                expectedVersion != null && !expectedVersion.isBlank(),
                "system property " + EXPECTED_VERSION_PROPERTY + " is not set by the run config");

        List<String> output = new ArrayList<>();
        CommandSource capture = new CommandSource() {
            @Override
            public void sendSystemMessage(Component message) {
                output.add(message.getString());
            }

            @Override
            public boolean acceptsSuccess() {
                return true;
            }

            @Override
            public boolean acceptsFailure() {
                return true;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        };
        ServerLevel level = helper.getLevel();
        // A non-player source holding no permissions at all: the command must still run.
        CommandSourceStack source = level.getServer().createCommandSourceStack()
                .withSource(capture)
                .withPermission(PermissionSet.NO_PERMISSIONS);
        level.getServer().getCommands().performPrefixedCommand(source, "gremlins");

        helper.assertValueEqual(output.size(), 1, "messages sent by /gremlins (got " + output + ")");
        helper.assertValueEqual(
                output.get(0),
                "Gremlins v" + expectedVersion + " — modules: Wither Wings",
                "/gremlins output");
        helper.succeed();
    }

    // -------------------------------------------------------------- helpers

    private static ResourceKey<Recipe<?>> recipeKey(String path) {
        return ResourceKey.create(Registries.RECIPE, Gremlins.id(path));
    }

    /** Runs the real smithing lookup for template + base + elytra and returns the output. */
    private static ItemStack smith(GameTestHelper helper, ItemStack base) {
        ServerLevel level = helper.getLevel();
        RecipeManager recipes = level.getServer().getRecipeManager();
        helper.assertTrue(
                recipes.byKey(recipeKey("wither_wings")).isPresent(),
                "recipe gremlins:wither_wings was not loaded");

        SmithingRecipeInput input = new SmithingRecipeInput(
                new ItemStack(WitherWings.WITHER_WING_TEMPLATE), base, new ItemStack(Items.ELYTRA));
        Optional<RecipeHolder<SmithingRecipe>> match =
                recipes.getRecipeFor(RecipeType.SMITHING, input, level);
        helper.assertTrue(match.isPresent(), "template + chestplate + elytra matched no smithing recipe");
        helper.assertValueEqual(match.get().id(), recipeKey("wither_wings"), "matched smithing recipe");
        return match.get().value().assemble(input);
    }

    private static WitherBoss spawnWither(GameTestHelper helper) {
        return helper.spawnWithNoFreeWill(EntityTypes.WITHER, new Vec3(4.5, 2.0, 4.5));
    }

    private static void killWith(GameTestHelper helper, WitherBoss wither, DamageSource source) {
        helper.hurt(wither, source, Float.MAX_VALUE);
        helper.assertTrue(wither.isDeadOrDying(), "the Wither survived " + source.getMsgId());
    }

    /** Counts Wither's Crown item entities at the Wither's position, then clears all drops. */
    private static int crownsNear(GameTestHelper helper, WitherBoss wither) {
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(
                ItemEntity.class, wither.getBoundingBox().inflate(2.0));
        // The vanilla Nether Star always drops; seeing it proves the death was processed
        // and that this search box really does cover where death drops land.
        helper.assertTrue(
                drops.stream().anyMatch(drop -> drop.getItem().is(Items.NETHER_STAR)),
                "no Nether Star found near the dead Wither, so drops were not observed");
        int crowns = 0;
        for (ItemEntity drop : drops) {
            if (drop.getItem().is(WitherWings.WITHERS_CROWN)) {
                crowns += drop.getItem().getCount();
            }
            drop.discard();
        }
        wither.discard();
        return crowns;
    }
}
