# CLAUDE.md — Warden's Wings

Guidance for Claude Code (and any agent) working in this repo.

## What this is

A Minecraft **datapack** (later a **Fabric mod**) that adds one hard-to-earn item:
**Warden's Wings** — a netherite chestplate fused with an elytra (armor + flight),
craftable only after an end-game gauntlet across the game's major structures/bosses.

- **Target game version:** Minecraft **26.1** (Fabric loader on the server side).
- **Phase 1 — datapack** (current): server-side only, no client install needed to *play*.
  Uses vanilla item data components (notably `minecraft:glider`, added in 1.21.2) on a
  named netherite chestplate, plus custom crafting + smithing recipes and flavor
  advancements. Known limitation: a datapack alone can't give the item a fully custom
  worn *look* (custom wings/texture) — that needs a resource pack or the mod.
- **Phase 2 — Fabric mod** (future): a real registered item with its own model/texture,
  config, and a Modrinth release. Same game design as the datapack; port, don't rewrite.

## Current status

**Design phase — no datapack code yet.** The gauntlet, naming, and difficulty are being
locked via a questionnaire (see `DESIGN.md`) before implementation. Do not scaffold the
`data/` tree or `pack.mcmeta` until the questionnaire answers are in.

## Layout (planned)

```
pack.mcmeta                 # datapack manifest (pack_format must match MC 26.1 — verify at build time)
data/wardens_wings/
  recipe/                   # crafting (Warden's Core) + smithing (final item) recipes
  advancement/              # flavor "trophy" advancements + progress tracking
  function/                 # grant/give logic, kill-triggered relic grants (e.g. Warden)
  loot_table/ or item component defs for the item itself
DESIGN.md                   # the working design spec + questionnaire link
```

## How to build / test / deploy

- **Build:** zip the datapack contents (`pack.mcmeta` + `data/`) into `wardens-wings.zip`.
- **Test (do this before touching a real server):** drop the folder/zip into a throwaway
  **creative test world's** `datapacks/`, `/reload`, then `/give` the trophies and verify
  both recipes resolve and the final item actually glides + has netherite armor values.
- **Deploy:** place the datapack in the target server's `world/datapacks/` and run `/reload`
  (or restart). Confirm on the server world, not just locally.
- **Manual verification is required** before considering any change done — actually fly
  with the item in-game; automated checks are not a substitute.

## Git workflow

- Commit/push freely on **feature branches**. `main` is **protected** — changes reach it
  only through a PR that Graham reviews and merges himself. Never push to `main`, never
  merge your own PR, never force-push a shared/protected branch.
- Public repo: **never commit anything sensitive** — no server IPs/hostnames, no host
  panel credentials, no personal info. Keep server-specific details out of tracked files.

## Self-maintenance

When you add or change a capability, recipe, dependency, or the build/deploy process,
**update this file and `DESIGN.md` before the task is done.** These docs are how the next
agent/session picks up context — if it's not written here, it's lost.
