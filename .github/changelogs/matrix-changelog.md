# 1.0.1
First normal release with fixed multiple bugs.
## Added
- [1.21.4, 1.21.11, 26.1.x] Added support for this Minecraft versions of the mod (resolves #2 issue).
- Added more support for a server version of mod:
    * Added lock at client-side for a few options when mod is installed on the server-side.
    * Server config now determines options for client-side mod.
    * When a server doesn't have installed mod, everything acts normally.
- Added elements on the XP bar option in config for classic style.
- [1.21.11, 26.1.x] Hidden default Minecraft locator bar.
## Changed
- Config screen now hides unavailable options for chosen style.
- Changed classic locator bar texture color.
- Redesigned project to stonecutter multiversion support using kotlin code and multiple Gradle DSL buildscripts.
## Fixed
- Fixed [#1](https://github.com/FuzjaJadrowa/LocatorBar/issues/1) issue.
- [NeoForge] Fixed [#3](https://github.com/FuzjaJadrowa/LocatorBar/issues/3) issue.