# Warden's Wings — Design Spec (working draft)

> **Status: DESIGN PHASE.** This spec is provisional. It gets finalized from the
> answers to the design questionnaire before any datapack code is written.
>
> **Questionnaire (fill this out first):**
> https://docs.google.com/document/d/1TeOZeDG0EbZIEAzj1WBkkF7eXXxRHl0dt830ISoOszE/edit

## Goal

One item — **Warden's Wings** — that combines a Netherite Chestplate and an Elytra
(netherite-tier armor + gliding), earnable only by completing a hard end-game gauntlet.

## Provisional design (pending questionnaire)

**The gauntlet (candidate trophies, each forcing distinct content):**

| Trophy | Forces |
|---|---|
| Elytra | End City + Ship (the base, consumed at the final step) |
| Warden kill → special relic | Deep Dark / Ancient City (signature challenge) |
| Heavy Core | Ominous Trial Chamber vault |
| Totem of Undying | Woodland Mansion (Evoker) |
| Sponge | Ocean Monument |
| Nether Star | The Wither (fortress skulls, deep Nether) |
| Netherite Block | Ancient-debris grind |
| Dragon Head | A rare End City ship |

**The combine (two-stage forge):**
1. Craft the trophies together into a **Warden's Core** (3×3 crafting; needing all of
   them at once is what enforces the full tour).
2. Smithing table: **Netherite Chestplate + Elytra + Warden's Core → Warden's Wings.**

**Item behavior:** netherite armor values + elytra flight via the `minecraft:glider`
component; epic rarity + name; enchantable like a chestplate. Extra perks TBD.

**Progress tracking:** flavor advancements per trophy so players can see how close
they are. TBD per questionnaire.

## Open decisions (resolved by the questionnaire)

- Final naming (item, core, datapack).
- Which challenges are in, and whether each is proven by *holding an item* vs *killing a boss*
  (notably: require an actual **Warden kill** vs just an Echo Shard).
- Whether trophies are consumed; quantities; co-op requirement; total challenge count.
- The combine mechanic's feel (two-stage forge vs single recipe vs auto-grant).
- Item powers beyond the baseline; enchant/repair rules.
- Datapack-only vs bundling a resource pack for the custom look; timeline to the full mod.
- Modrinth/publishing and server-deployment logistics.

## Technical notes (Minecraft 26.1 datapack)

- The `minecraft:glider` item data component (added 1.21.2) makes a non-elytra item glide
  when worn in the chest slot — this is what lets a *chestplate* fly. Confirm exact
  component/recipe syntax against 26.1 at build time.
- `pack_format` in `pack.mcmeta` must match MC 26.1 — verify the correct number at build time.
- Bosses with no unique drop (e.g. the Warden) are handled by a kill-triggered advancement
  that grants a custom relic item usable as a recipe ingredient.
- Fully custom worn appearance (wings + texture) is a resource-pack / mod concern, not
  achievable with a server-side datapack alone.
