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

## Install

Download the latest `gremlins-<version>.jar` from the
[Releases page](https://github.com/Graham-Williams/gremlins-minecraft-mods/releases/latest).
The mod is distributed only through GitHub releases; it is not on Modrinth or CurseForge.

**You need**

- Minecraft **Java Edition 26.3** with **Fabric Loader 0.19.5** or newer.
- [**Fabric API**](https://modrinth.com/mod/fabric-api) for 26.3.
- Java 25, which current launchers install for you.

| Gremlins | Minecraft | Fabric Loader |
|---|---|---|
| 0.2.0 | 26.3 | 0.19.5+ |

**Single player**

1. Create or open a Fabric 26.3 instance in your launcher.
2. Add Fabric API if the instance does not have it.
3. Add the Gremlins jar. In the Modrinth App: open the instance, go to **Content**, click
   **Upload files** and select the jar. In other launchers, put the jar in the instance's
   `mods` folder.
4. Start the game and run `/gremlins` in a world. It replies with the version and the
   loaded modules.

**Multiplayer**

The mod adds items, so it must be installed on the **server and on every player's
client**. Keep everyone on the same version. A player whose client lacks the mod, or
lacks an item the server's version adds, is disconnected when joining.

1. On a Fabric 26.3 server running Java 25, put the Gremlins jar and Fabric API in the
   `mods` folder and restart it.
2. Every player installs the same jar as in the single-player steps.
3. Run `/gremlins` on the server console and in game. Both should report the same version.

**Removing the mod**

A fused Wither Wings chestplate is a vanilla netherite chestplate carrying the vanilla
glider component, so it keeps working without the mod. Wither's Crowns and Wither Wing
Templates are the mod's own items, so expect them to be lost from a world opened
without it.

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

## License

MIT — see `LICENSE`.
