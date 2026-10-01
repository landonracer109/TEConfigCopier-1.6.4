# TE Config Copier (Minecraft 1.6.4)

Copy a Thermal Expansion machine's settings and paste them onto another: side configuration,
redstone mode, access, and a Tesseract's frequency and modes. No more clicking through the same
side colours on dozens of Pulverizers or Tesseracts.

- **Client side only.** Install it in your own game. The server doesn't need it, so it works on
  any server running Thermal Expansion.
- **It only does what the GUI does.** Pasting makes the same calls as clicking the buttons in the
  machine's GUI, and the server applies its usual checks.

Requires Thermal Expansion 3.0.0.6.

## How to use

Hold a piece of **paper**:

| | |
|---|---|
| **Copy** | look at a machine and press **C** |
| **Paste** | **sneak + right-click** another machine |

Chat confirms each copy and paste. The copy key can be changed in *Options → Controls*.

## What's copied

| Setting | Notes |
|---|---|
| **Side configuration** | relative to the machine's front, so it fits machines facing any way |
| **Redstone mode** | ignored / low / high |
| **Access** | public / restricted / private |
| **Tesseract** | frequency, and the item, fluid and energy modes |

On a different kind of machine, it pastes what fits and skips the rest.

## Rules

- Only copies blocks you can open.
- Only changes access on blocks you own.
- Can't reach someone else's private Tesseract: private frequencies are per owner.

## How it works

It pastes through the same calls Thermal Expansion's GUI makes (`setSide`, `setRedstoneConfig`,
`setAccess`, `setTileInfo`), so the server treats a paste like clicking the buttons.

## Building

`./build.sh` builds `build/TEConfigCopier-<version>.jar` with Java 8. It needs these jars in
`tools/`, which aren't in this repository:
- **`ecj.jar`:** the Eclipse compiler, `org.eclipse.jdt:ecj` 3.x.
- **`mc-1.6.4-srg.jar`** and **`forge-srg.jar`:** Minecraft 1.6.4 and Forge 9.11.1.965 universal,
  remapped to SRG names. See
  [Lag Monitor's README](https://github.com/landonracer109/LagMonitor-1.6.4#building) for how to
  make them.
- **`CoFHCore-2.0.0.5.jar`** and **`ThermalExpansion-3.0.0.6.jar`** from the pack.

## License

MIT, see [LICENSE](LICENSE).
