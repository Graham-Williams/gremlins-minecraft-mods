# Gremlins Minecraft Mods

An umbrella collection of **Fabric** gameplay modules for Minecraft, published under
the "Gremlins" banner. One mod jar, many self-contained features.

- **Platform:** Java / **Fabric** only. This is **not** a Bedrock mod and there is no
  Bedrock support (the required item data components and Fabric APIs are Java-only).
- **Minecraft:** **26.3** (calendar versioning — 26.3 is a real release).
- **Fabric Loader:** 0.19.5 · **Fabric API:** 0.162.0+26.3 · **Java:** 25 (required).

> Minecraft 26.1+ ships **unobfuscated**, so this mod uses the non-remapping Loom
> plugin (`net.fabricmc.fabric-loom`) and Mojang's official names throughout. There
> are no Yarn/Mojang mapping downloads — see `DESIGN.md` for the toolchain notes.

## Modules

### Wither Wings (shipped)

Fuse an **Elytra** onto a **Netherite Chestplate** to get netherite-tier protection
**and** flight in one chest slot — earned by defeating the Wither.

1. **Kill the Wither** (the killing blow must be credited to a player) → it drops a
   **Wither's Crown**.
2. **Craft** `Wither's Crown + Phantom Membrane` (shapeless) → a **Wither Wing Template**.
3. **Smith** `Wither Wing Template` + `Netherite Chestplate` + `Elytra` at a smithing
   table → a netherite chestplate carrying the vanilla `minecraft:glider` component.

The fused chestplate **keeps its own name** — "Netherite Chestplate", or whatever you
anvil-named the base item — and is marked by a gray italic **Wither Wings** lore line
underneath. In the inventory it shows a **winged icon** (the netherite chestplate
with elytra wings behind it), and armour trim still renders on it. Its
**enchantments, durability, anvil name and armour trim are all preserved**; the
elytra's enchantments are consumed.

Gliding, firework boosting, and durability drain are all **native** vanilla behaviour
of the `minecraft:glider` component — no custom logic. It is a one-way transform (no
recipe splits it back apart).

See `DESIGN.md` for the full design and future modules (a Warden-gated safe-zone mod;
a shared/team Ender Chest).

## The `/gremlins` command

A **smoke test / tracer bullet** for verifying a deploy — it has **zero gameplay
impact**. Run `/gremlins` and it replies (to you only) with the mod's name, its version
and the loaded feature modules:

```
Gremlins v0.3.0 — modules: Wither Wings
```

Seeing that in chat proves three things at once: the jar is actually loaded **on the
server**, the version running is the version you think it is, and server→client
messaging works. Notes:

- **Available to everyone** — no operator/permission requirement, since it is purely
  informational.
- The version is read at runtime from the mod's own metadata
  (`FabricLoader … getMetadata().getVersion()`), never hardcoded, so it can't go stale.
- Works from the **server console** too, not just from a player.

Registered in `com.grahamwilliams.gremlins.command.GremlinsCommand`. When you add a new
feature module, add its display name to the `MODULES` list there (a one-line change).

## Build

Requires **JDK 25** (MC 26.3 enforces it). The build points Gradle at a JDK 25 via
`org.gradle.java.home` in `gradle.properties` — edit that path if your JDK 25 lives
elsewhere (e.g. `brew install openjdk@25`).

```bash
./gradlew build
```

The mod jar is produced at `build/libs/gremlins-<version>.jar` (ignore the
`-sources.jar`).

## Test

`./gradlew build` also runs the mod's game tests: it boots a headless dedicated server
on the target Minecraft version and checks the items, both recipes, the Wither's Crown
drop rules, the `/gremlins` command, and that the winged icon's definition still
matches vanilla's armour-trim materials. A failing test fails the build. To run only
the tests:

```bash
./gradlew runGameTest
```

The tests live in `src/gametest/` and are not included in the mod jar.

## Run a local test client / server

```bash
./gradlew runClient   # launches a dev Minecraft client with the mod loaded
./gradlew runServer   # launches a dev dedicated server (loads recipes at startup)
```

## Install into the Gremlins Modrinth profile

Copy the built jar into the profile's mods folder:

```bash
cp build/libs/gremlins-*.jar \
  ~/Library/Application\ Support/ModrinthApp/profiles/<Gremlins profile>/mods/
```

You also need **Fabric API** (`fabric-api-0.162.0+26.3`) in that mods folder. Launch
the profile from the Modrinth app.

## License

MIT — see `LICENSE`.
