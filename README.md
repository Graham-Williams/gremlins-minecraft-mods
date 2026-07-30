# Gremlins Minecraft Mods

An umbrella collection of **Fabric** gameplay modules for Minecraft, published under
the "Gremlins" banner. One mod jar, many self-contained features.

- **Platform:** Java / **Fabric** only. This is **not** a Bedrock mod and there is no
  Bedrock support (the required item data components and Fabric APIs are Java-only).
- **Minecraft:** **26.1.2** (calendar versioning — 26.1.2 is a real release).
- **Fabric Loader:** 0.19.3 · **Fabric API:** 0.153.0+26.1.2 · **Java:** 25 (required).

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
   table → **Wither Wings**: a netherite chestplate with the vanilla
   `minecraft:glider` component. The base chestplate's **enchantments and durability
   are preserved**; the elytra's enchantments are consumed.

Gliding, firework boosting, and durability drain are all **native** vanilla behaviour
of the `minecraft:glider` component — no custom logic. It is a one-way transform (no
recipe splits it back apart).

See `DESIGN.md` for the full design and future modules (a Warden-gated safe-zone mod;
a shared/team Ender Chest).

## Build

Requires **JDK 25** (MC 26.1.2 enforces it). The build points Gradle at a JDK 25 via
`org.gradle.java.home` in `gradle.properties` — edit that path if your JDK 25 lives
elsewhere (e.g. `brew install openjdk@25`).

```bash
./gradlew build
```

The mod jar is produced at `build/libs/gremlins-<version>.jar` (ignore the
`-sources.jar`).

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

You also need **Fabric API** (`fabric-api-0.153.0+26.1.2`) in that mods folder. Launch
the profile from the Modrinth app.

## License

MIT — see `LICENSE`.
