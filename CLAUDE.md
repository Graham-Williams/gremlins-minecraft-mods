# CLAUDE.md — Gremlins Minecraft Mods

Guidance for Claude Code (and any agent) working in this repo.

## What this is

`gremlins` — an umbrella **Fabric** mod bundling independent gameplay modules.
**Java / Fabric only. There is NO Bedrock support** (do not claim any). First shipped
module: **Wither Wings**.

- **MC 26.3**, Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, **Java 25**.
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
  merged jar at `~/.gradle/caches/fabric-loom/26.3/minecraft-merged.jar`.
- **26.3 gotcha:** the entity type constants moved from `EntityType` to
  **`net.minecraft.world.entity.EntityTypes`** (`EntityTypes.WITHER`); `EntityType` is
  now only the type class.
- Requires **JDK 25**. `gradle.properties` sets `org.gradle.java.home` to the
  brew `openjdk@25` path — update it if your JDK 25 is elsewhere. Do **not** rely on
  the machine default Java. Gradle wrapper is **9.5.0** (Loom 1.17 needs the 9.5 plugin
  API); wrapper was generated in a scratch dir and copied in (the wrapper task can't run
  while the loom plugin is applied).

## Build / run / test

```bash
./gradlew build       # -> build/libs/gremlins-<version>.jar (also runs the game tests)
./gradlew runGameTest # just the game tests: headless server, ~10 s, non-zero exit on failure
./gradlew runClient   # dev client with the mod loaded
./gradlew runServer   # dev dedicated server (validates datapack recipe JSON at startup)
./gradlew genSources  # decompile MC for API inspection
```

Install into the Gremlins Modrinth profile: copy `build/libs/gremlins-*.jar` (plus
`fabric-api-0.162.0+26.3`) into that profile's `mods/` folder.

### Game tests

`src/gametest/` is a separate source set and mod (`gremlins-test`, never packaged into
the mod jar) holding Fabric GameTest API tests, wired up by `fabricApi.configureTests`
in `build.gradle`. They run on a real headless dedicated server of the target Minecraft
version, so they check the real registries, datapack recipe loader and command
dispatcher. Loom hooks `runGameTest` into `check`, so **`./gradlew build` fails when a
test fails**. Per-test results land in `build/gametest/junit.xml`; the run directory is
`build/run/gameTest`.

- Covered: both items are registered; the template recipe and the smithing recipe
  match and assemble (glider, gray lore line, `custom_data`, **no** `custom_name`, base
  name and damage preserved); a player-credited Wither kill drops exactly one crown and
  environmental or mob kills drop none; `/gremlins` runs for a source with no
  permissions and prints the version from `mod_version`.
- **Porting to a new Minecraft version:** bump `gradle.properties` and
  `fabric.mod.json`, then run `./gradlew build`. A green build means the mod loads and
  behaves on that version; it does not cover the client (icons, tooltip rendering,
  flight feel), which still needs an in-game look.
- A recipe JSON that fails to parse is **fatal** on 26.3 — the server stops at
  "Registry loading errors" instead of logging and carrying on — so a bad recipe shows
  up as the test server failing to start rather than as a failed test.
- When you add behaviour, add a test for it, and check the test fails when the
  behaviour is broken.

## Wither Wings design (summary)

`gremlins:withers_crown` (Wither drop) + Phantom Membrane → `gremlins:wither_wing_template`
→ smith with a Netherite Chestplate + Elytra → a netherite chestplate carrying the
vanilla `minecraft:glider` component.

- **Drop is cheese-proof:** only on a Wither death whose `DamageSource.getEntity()` is a
  `ServerPlayer` (player-credited kill, projectiles included); environmental kills give
  nothing. Implemented via `ServerLivingEntityEvents.AFTER_DEATH`.
- **Enchants/durability preserved:** vanilla `smithing_transform` copies the base item's
  component patch, then layers the result JSON's components (glider, lore,
  custom_data) on top — so no custom Java recipe is needed. The elytra's enchants are
  consumed (expected). **Armour trim carries over too** — it is just another component.
- **The item keeps its own name** (issue #5). The result sets no `custom_name`, so it
  reads "Netherite Chestplate", or whatever Graham anvil-named the base chestplate; a
  gray italic `minecraft:lore` line marks it as Wither Wings. Do **not** re-add
  `custom_name` — it would clobber his anvil name. A custom *icon* was considered and
  rejected (his call, recorded on issue #5): it needs a `minecraft:item_model`
  override, which replaces the whole icon definition and so drops the armour-trim
  overlay. Trim could be replicated under a custom icon — it is 11 trim-material cases
  (still 11 in 26.3), not impossible — but the lore line is the marker that keeps trim for free.
- **Only affects items fused AFTER this jar.** Recipe results are baked into the stack
  at craft time, so any Wither Wings crafted before v0.1.1 still carries the old
  `custom_name` and has no lore. Expect this during in-game QA — seeing the old name on
  an old item does NOT mean the change failed; fuse a fresh one to check.
- **Durability/flight are native** to the glider component — write no durability code.
- One-way (no un-smithing recipe).

Full detail in `DESIGN.md`.

## `/gremlins` command (smoke test)

`com.grahamwilliams.gremlins.command.GremlinsCommand` — a **tracer bullet** registered via
Fabric's `CommandRegistrationCallback` (`fabric-command-api-v2`), wired up from
`Gremlins#onInitialize()`. Replies to the sender only with
`Gremlins v<version> — modules: <list>`. **Zero gameplay impact.**

- **No `.requires(...)`** — deliberately available to all players, not just ops. Don't add
  a permission level; it is purely informational.
- Version comes from `FabricLoader.getInstance().getModContainer("gremlins")` →
  `getMetadata().getVersion().getFriendlyString()`. **Never hardcode it** — the point is
  that it can't lie about what's actually running.
- Uses `source.sendSuccess(() -> msg, false)` and never touches `getPlayerOrException()`,
  so the **server console** can run it too.
- **Adding a module:** append its display name to the `MODULES` list in that class.
- Use it to verify a deploy: if `/gremlins` prints the expected version, the jar is loaded
  server-side and server→client messaging works.
- Source files are kept **pure ASCII** (the em dash is written as the escape `\u2014`) since javac's
  default source encoding is platform-dependent.

## Self-maintenance

When you add or change a capability, module, dependency, command, or architectural
decision, **update this file, `README.md`, and `DESIGN.md`** before considering the
task done. This is how context persists for the next agent/session in this repo. Keep
all docs free of anything sensitive (paths under a real home dir are fine; no secrets).

## Git workflow

`main` is protected — only Graham merges, via a PR he reviews. Do real work on feature
branches (e.g. `feature/wither-wings`); commit/push feature branches freely. Never push
to `main`.
