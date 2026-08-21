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

Pinned versions live in `gradle.properties`.

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
     `minecraft:glider={}`, `minecraft:custom_name="Wither Wings"`,
     `minecraft:custom_data={wither_wings:true}`.

**Why vanilla smithing preserves enchants:** `SmithingTransformRecipe#assemble` calls
`TransmuteRecipe.createWithOriginalComponents(result, base)`, which builds the output
as `new ItemStack(resultItem, base.getComponentsPatch())` and *then* applies the
result JSON's declared components on top. So the base chestplate's **enchantments and
damage/durability carry over**, and the glider component + custom name + custom-data
flag are layered on. No custom `SmithingRecipe` in Java was needed. The elytra's own
enchantments are consumed — expected.

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
