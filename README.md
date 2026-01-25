# Cobbled Counter

v1.7.2-1.9

[Modrinth](https://modrinth.com/mod/cobblemon-counter)

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/cobblemon-counter)

[GitHub](https://github.com/timinc-cobble/cobblemon-counter)

## What if…

…the game kept track of how many times you did a thing, in Cobblemon?

## Features

- Tracks various player action stats including:
    - Captures
    - Knock Outs (in-battle)
    - Resurrections
    - Fish-ups
    - Egg hatchings
    - Pokémon eating a snack nearby
- Tracks both counts and streaks for every stat. Counts are for the lifetime of the player in a given world, and streaks are broken if the player performs that action on another species/form.
    - Some odd variants are mapped back to the Normal variant.
- Adds a Counter item to view your current stats.
- Commands to get and modify scores for a given player.

## Dependencies

- [Cobblemon](https://www.notion.so/Cobblemon-22157e0d4afd80a49896c70a775a3c7f?pvs=21)
- [Tim Core](https://www.notion.so/Tim-Core-22057e0d4afd809b9c02e78f26805376?pvs=21)

## Testing

As a quick test for the counting functionality, jump into a fresh world and give yourself some Master Balls. Spawn in a Caterpie, catch it. Observe the message in the chat telling you that you have captured a Caterpie, and have a Capture Count of 1 and Capture Streak of 1 for Caterpie. Spawn in another Caterpie, catch it. Observe the message in the chat telling you that you have a Capture Count and Streak of 2 for Caterpie. Spawn in a Weedle, catch it. Observe the message in the chat telling you that you have a Capture Count and Streak of 1 for Weedle. Spawn in a final Caterpie, catch it. Observer the message in the chat telling you that you have a Capture Count of 3 for Caterpie, but a Capture Streak of only 1 for Caterpie. This is because you have captured a total of 3 Caterpie overall, but you broke your original streak of 2 Caterpie by capturing that Weedle, and also broke that Weedle streak by capturing that final Caterpie.

## Player Help

[Commands](https://www.notion.so/Commands-2f357e0d4afd81ea96d2fec2dd3acfd0?pvs=21)

[Config Options](https://www.notion.so/Config-Options-2f357e0d4afd8120b19ace44690c8a6e?pvs=21)

## Addon Dev Help

### Data Pack Help

[Spawning Conditions](https://www.notion.so/Spawning-Conditions-2f357e0d4afd81bab1d7f650d877aabe?pvs=21)

[Making an Addon](https://www.notion.so/Making-an-Addon-2f357e0d4afd81bc808cc50a5b4c3bb7?pvs=21)

### Resource Pack Help

[Translations](https://www.notion.so/Translations-2f357e0d4afd81e988add3c189b58657?pvs=21)

[Resources](https://www.notion.so/Resources-2f357e0d4afd81d5b0fcf6fb93afc042?pvs=21)

## Mod Dev Help

[Events](https://www.notion.so/Events-2f357e0d4afd815eb7bae9c332b49e59?pvs=21)

[CounterManager](https://www.notion.so/CounterManager-2f357e0d4afd81908eb7e40e906a7d1d?pvs=21)

## Roadmap

If you’d like to keep up with the work being done on the mod, please join [the Discord](https://discord.com/invite/WKAR27SdSv) and subscribe to notifications on the channel for this content. You can also keep track of the to do list available on the mod’s main page ([Counter](https://www.notion.so/Counter-21d57e0d4afd80d0815fc97b89368998?pvs=21)).

## Known Issues

- None. Why? Who’s asking? 👀

## Feedback

If you have any questions or requests concerning the mod, or just want to drop by and say hi, visit us over at [the Discord](https://discord.com/invite/WKAR27SdSv)!

## Support

If I've made something you enjoyed or helped you make something, please consider [dropping a tip in the cup](https://ko-fi.com/timsminecraftmods) and mention how I helped if you'd like!