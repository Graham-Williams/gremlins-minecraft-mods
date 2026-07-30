# CLAUDE.md — Gremlins Minecraft Mods

Guidance for Claude Code (and any agent) working in this repo.

## What this is

`gremlins` — an umbrella **Fabric** mod bundling independent gameplay modules.
**Java / Fabric only. There is NO Bedrock support** (do not claim any). First shipped
module: **Wither Wings**.

- **MC 26.1.2**, Fabric Loader 0.19.3, Fabric API 0.153.0+26.1.2, **Java 25**.
- Base package `com.grahamwilliams.gremlins`; each feature is a sub-package wired up in
  `Gremlins#onInitialize()`.

## Toolchain — read before touching the build

MC 26.1+ is **unobfuscated**. This repo therefore:

- Uses the **non-remapping** Loom plugin **`net.fabricmc.fabric-loom`** (v1.17.17), not
  the legacy `fabric-loom`. **No `mappings` line**; deps are `implementation`, not
  `modImplementation`.
- Uses **Mojang official names**. Gotcha: `ResourceLocation` → **`Identifier`**
  (`net.minecraft.resources.Identifier`). When in doubt about an API/name, decompile:
  `./gradlew genSources` then read the sources jar under
  `.gradle/loom-cache/minecraftMaven/.../*-sources.jar`, or `javap -classpath` the
  merged jar at `~/.gradle/caches/fabric-loom/26.1.2/minecraft-merged.jar`.
- Requires **JDK 25**. `gradle.properties` sets `org.gradle.java.home` to the
  brew `openjdk@25` path — update it if your JDK 25 is elsewhere. Do **not** rely on
  the machine default Java. Gradle wrapper is **9.5.0** (Loom 1.17 needs the 9.5 plugin
  API); wrapper was generated in a scratch dir and copied in (the wrapper task can't run
  while the loom plugin is applied).

## Build / run / test

```bash
./gradlew build       # -> build/libs/gremlins-<version>.jar
./gradlew runClient   # dev client with the mod loaded
./gradlew runServer   # dev dedicated server (validates datapack recipe JSON at startup)
./gradlew genSources  # decompile MC for API inspection
```

Install into the Gremlins Modrinth profile: copy `build/libs/gremlins-*.jar` (plus
`fabric-api-0.153.0+26.1.2`) into that profile's `mods/` folder.

## Wither Wings design (summary)

`gremlins:withers_crown` (Wither drop) + Phantom Membrane → `gremlins:wither_wing_template`
→ smith with a Netherite Chestplate + Elytra → a netherite chestplate carrying the
vanilla `minecraft:glider` component ("Wither Wings").

- **Drop is cheese-proof:** only on a Wither death whose `DamageSource.getEntity()` is a
  `ServerPlayer` (player-credited kill, projectiles included); environmental kills give
  nothing. Implemented via `ServerLivingEntityEvents.AFTER_DEATH`.
- **Enchants/durability preserved:** vanilla `smithing_transform` copies the base item's
  component patch, then layers the result JSON's components (glider, custom_name,
  custom_data) on top — so no custom Java recipe is needed. The elytra's enchants are
  consumed (expected).
- **Durability/flight are native** to the glider component — write no durability code.
- One-way (no un-smithing recipe).

Full detail in `DESIGN.md`.

## Self-maintenance

When you add or change a capability, module, dependency, command, or architectural
decision, **update this file, `README.md`, and `DESIGN.md`** before considering the
task done. This is how context persists for the next agent/session in this repo. Keep
all docs free of anything sensitive (paths under a real home dir are fine; no secrets).

## Git workflow

`main` is protected — only Graham merges, via a PR he reviews. Do real work on feature
branches (e.g. `feature/wither-wings`); commit/push feature branches freely. Never push
to `main`.
