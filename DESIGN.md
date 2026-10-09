# Gremlins Minecraft Mods — Design

## What this is

A single Fabric mod (`gremlins`) that bundles several independent gameplay modules.
Java/Fabric only — **no Bedrock**. Each module lives in its own sub-package under
`com.grahamwilliams.gremlins` and is wired up from `Gremlins#onInitialize()`.

## Toolchain notes (Minecraft 26.1+)

Minecraft 26.1 was the **first unobfuscated** release. Consequences for this repo:

- Fabric no longer publishes Yarn or Mojang mappings for 26.1+. We use the
  **non-remapping** Loom plugin `net.fabricmc.fabric-loom` (**not** the legacy
  `fabric-loom` remapping plugin). There is **no `mappings` line** in `build.gradle`,
  and Minecraft/mod deps are plain `implementation` (not `modImplementation`).
- All code uses **Mojang's official names**. A few names differ from the old community
  mappings — notably `ResourceLocation` is now **`net.minecraft.resources.Identifier`**.
- **Java 25** is required (Loom enforces it; the version JSON declares
  `javaVersion.majorVersion = 25`). The Gremlins Modrinth profile bundles a Zulu 25
  runtime for playing; the build needs a full **JDK 25** (`brew install openjdk@25`).
- Loom **1.17.17**, Gradle **9.5.0** (Loom 1.17 requires the Gradle 9.5 plugin API).

Pinned versions live in `gradle.properties` (currently Minecraft **26.3**, Fabric
Loader 0.19.5, Fabric API 0.162.0+26.3).

### Porting 26.1.2 to 26.3 (mod v0.2.0)

Nothing in the mod's own code or data had to change. The smithing and shapeless recipe
codecs, the `minecraft:glider` / `minecraft:lore` / `minecraft:custom_data` components,
and the item-definition and model JSON shapes are the same in 26.3, and Loom 1.17.17 and
Gradle 9.5.0 handle 26.3 as they are. One API move to know about: the entity type
constants now live in `net.minecraft.world.entity.EntityTypes` rather than `EntityType`.

### Game tests

Behaviour is checked by Fabric GameTest API tests in `src/gametest/` (a separate
`gremlins-test` mod, not shipped). `./gradlew build` runs them on a headless dedicated
server of the target version, so a version port is verified by the build rather than by
hand: item registration, both recipes and the smithing result's components, the crown
drop rules, `/gremlins`, and the icon definition's one-to-one match with vanilla's
trim-material cases. Rendering itself is not covered.

## Module: Wither Wings

**Goal:** one item — **Wither Wings** — a netherite chestplate that also glides,
earned by beating the Wither.

### Items (registered in Java)

| Item | ID | Role |
|---|---|---|
| Wither's Crown | `gremlins:withers_crown` | Trophy dropped by the Wither |
| Wither Wing Template | `gremlins:wither_wing_template` | Smithing template |

Both are plain `Item`s with placeholder 16×16 textures/models and `en_us` lang
entries. (Art is intentionally placeholder.)

### Drop-on-kill (Java, cheese-proof)

`WitherWings.init()` registers a Fabric `ServerLivingEntityEvents.AFTER_DEATH`
listener. On a death it drops **one** Wither's Crown at the Wither's location **only
if**:

- the dying entity is a `WitherBoss`, **and**
- `damageSource.getEntity()` is a `ServerPlayer` — i.e. the killing blow is credited to
  a player (mirrors vanilla kill-credit, so projectile kills count). This deliberately
  **excludes environmental kills** (lava, suffocation, etc.), so you can't cheese the
  drop by letting the world kill the Wither.

Dropping as a ground `ItemEntity` (rather than into inventory) means a full inventory
is a non-issue.

### Recipes (data-driven JSON, `data/gremlins/recipe/`)

1. **Shapeless** (`wither_wing_template.json`): `Wither's Crown + Phantom Membrane`
   → `Wither Wing Template`.
2. **Smithing** (`wither_wings.json`, `minecraft:smithing_transform`):
   - template `gremlins:wither_wing_template`
   - base `minecraft:netherite_chestplate`
   - addition `minecraft:elytra`
   - result: `minecraft:netherite_chestplate` **+ components**
     `minecraft:glider={}`, `minecraft:item_model=gremlins:wither_wings`,
     `minecraft:lore=[…]`, `minecraft:custom_data={wither_wings:true}`.

**Why vanilla smithing preserves enchants:** `SmithingTransformRecipe#assemble` calls
`TransmuteRecipe.createWithOriginalComponents(result, base)`, which builds the output
as `ItemStackTemplate.apply(count, base.getComponentsPatch())` — which constructs
`new ItemStack(holder, count, basePatch)` and *then* applies the
result JSON's declared components on top. So the base chestplate's **enchantments,
damage/durability, custom name and armour trim all carry over**, and the glider
component, item model, lore and custom-data flag are layered on. No custom
`SmithingRecipe` in Java was needed. The elytra's own enchantments are consumed —
expected.

### Naming: lore, not `custom_name` (issue #5)

The result deliberately sets **no `minecraft:custom_name`**. Because the base item's
component patch is copied first, an absent `custom_name` means the item shows its own
name — "Netherite Chestplate", or whatever the player anvil-named the base chestplate.
Re-adding `custom_name` would silently clobber that name, which is what #5 was filed
about.

The marker is instead a single `minecraft:lore` line — a gray italic "Wither Wings"
under the real item name. Lore is a plain component, so it costs nothing else.

**`color: "gray"` is load-bearing — do not remove it as redundant.** Vanilla applies
`ItemLore.LORE_STYLE = Style.EMPTY.withColor(DARK_PURPLE).withItalic(true)` to every
lore line, and `ComponentUtils.mergeStyles` lets the line's *own* style win field by
field. With no explicit colour the line renders **dark purple**, not gray. (`italic:
true` genuinely *is* redundant — `LORE_STYLE` already sets it — and is kept only as
self-documentation. The two are not equally optional.)

Two consequences worth knowing: component patches overwrite per key rather than merging,
so the result's lore **replaces** any lore already on the base chestplate (irrelevant for
a normally-obtained chestplate); and recipe results are baked into the stack at craft
time, so **items fused before v0.1.1 keep their old `custom_name` and have no lore** —
the change is not retroactive.

### Inventory icon

The fused chestplate shows a bespoke icon: vanilla's netherite chestplate with elytra
wings behind it ("full wings"), with armour trim still drawn on top. The recipe result
sets `minecraft:item_model` to `gremlins:wither_wings`, and
`assets/gremlins/items/wither_wings.json` is that icon definition.

An `item_model` override replaces vanilla's *entire* icon definition, trim overlay
included. Vanilla draws inventory trim from a `minecraft:select` on
`minecraft:trim_material` in `assets/minecraft/items/netherite_chestplate.json` (11
cases, no per-pattern dimension), so our definition mirrors it case for case, in
vanilla's order: each case points at `models/item/wither_wings_<material>_trim.json`
and the fallback at `models/item/wither_wings.json`. Every model is
`minecraft:item/generated` with `layer0 = gremlins:item/wither_wings`,
`layer1 = minecraft:item/netherite_chestplate` and, on the trim models, `layer2` set
to the trim sprite vanilla's own model for that material uses.

**Why wings-only under vanilla's texture.** `textures/item/wither_wings.png` holds
only the wing pixels and is transparent wherever the chestplate is opaque; vanilla's
chestplate texture is drawn over it by name. The repo therefore ships no copy of
vanilla art, and the icon follows a vanilla chestplate retexture or resource pack on
its own. The trim sprites (`minecraft:trims/items/chestplate_trim_<material>`) are
generated into the item atlas by vanilla's `atlases/items.json`, so they too are
referenced, never copied.

**The netherite case.** Vanilla's netherite-trim-on-netherite model uses
`chestplate_trim_netherite_darker`, not `chestplate_trim_netherite`; ours does the
same. It is the kind of detail the sync test exists to catch.

**Upkeep contract.** The definition must match vanilla's one to one. When Mojang adds
a trim material, add one case and one model; when one is renamed or re-paletted,
rename or re-point it. `iconDefinitionMirrorsVanillaTrimCases` reads both definitions
off the classpath and fails the build at a version port until that is done, naming
the case or sprite that differs. Like the lore line, the icon only applies to items
fused after the jar that introduced it (v0.3.0). Not tried: a world opened without the
mod is expected to draw the fused item with the missing-model placeholder; it would
still work. A client without the mod cannot join a server running it at all.

**History.** A custom icon was rejected on issue #5 in September 2026 over the cost of
replicating the trim cases. Reversed in October 2026 (the PR that added this section
records the pick: the full-wings mockup).

### Durability & flight are native

The `minecraft:glider` component (added in 1.21.2) gives the chestplate elytra-style
gliding: it drains 1 durability/sec while gliding and refuses to glide at
`damage >= maxDamage - 1`, so gliding can never fully destroy it (survives at 1, like
an elytra). Armor/combat durability is the normal netherite-chestplate behaviour.
**No durability code is written or overridden.** Firework rockets boost a
glider-component chestplate exactly as they boost an elytra (the boost logic keys off
the glider component / gliding state, not the elytra item).

### One-way

There is intentionally no recipe to split Wither Wings back into a chestplate + elytra.

## Utility: the `/gremlins` command

Not a gameplay module — a **tracer bullet**. `GremlinsCommand` registers `/gremlins` via
Fabric's `CommandRegistrationCallback`; it prints
`Gremlins v<version> — modules: <list>` to whoever ran it and does nothing else.

Design constraints, deliberate:

- **Zero gameplay impact** and **no permission gate** (any player can run it) — it exists
  only to answer "is the mod actually loaded on the server, and which version?".
- The version is read at runtime from the mod's own metadata rather than a constant, so a
  stale jar can never report a version it isn't.
- Non-player sources (the dedicated-server console) are handled; the command never
  assumes a player.
- The module list is a single `List<String>` constant — a new module is one added line.

## Planned future modules

- **Warden safe-zone** (reward likely **"Warden's Eyes"**): a Warden-gated mod that
  establishes a protected zone. **Needs Java** — it hooks explosion handling to cancel
  damage inside the zone; not expressible as a datapack.
- **Shared / team Ender Chest**: a team-wide shared inventory, separate from the normal
  per-player ender chest.

Both are unstarted; captured here so the intent persists.
