# TE Config Copier (Minecraft 1.6.4)

Copy a Thermal Expansion machine's settings and paste them onto another: side configuration,
redstone mode, access, and a Tesseract's frequency and modes. No more clicking through the same
side colours on dozens of Pulverizers or Tesseracts.

- **Client side only.** Install it in your own game. The server doesn't need it, so it works on
  any server running Thermal Expansion.
- **It only does what the GUI does.** Pasting makes the same calls as clicking the buttons in the
  machine's GUI, and the server applies its usual checks.

Requires Thermal Expansion 3.0.0.6 and CoFH Core 2.0.0.5 (Forge 9.11.1.965).

## How to use

Hold a piece of **paper**:

| | |
|---|---|
| **Copy** | look at a machine and press **C** |
| **Paste** | **sneak + right-click** another machine |

A chat message confirms each copy and paste, and says what was skipped. The copy key can be changed
in *Options → Controls* ("Copy machine settings (with paper)"). The copied settings stay until you
copy something else or quit the game.

Copying uses a key, not a click, because Forge 1.6.4 only reports left-clicks on blocks to the
server, where a client-side mod can't see them.

## What's copied

| Setting | Notes |
|---|---|
| **Side configuration** | Relative to the machine's front: "input on its left" stays on its left, whichever way the new machine faces. The front face itself can't be configured, as in the GUI. |
| **Redstone mode** | ignored / low / high |
| **Access** | public / restricted / private. Only pasted onto blocks **you own**. |
| **Tesseract** | frequency, and the item, fluid and energy modes |

Pasting onto a different kind of machine (a Pulverizer's settings onto a Redstone Furnace, say)
applies what fits and skips side settings the target doesn't have.

## Rules it follows

- **It only copies blocks you could open.** A secured block you can't access can't be copied, so it
  never shows you more than the GUI would.
- **It only changes access on blocks you own**, as the GUI's security tab does.
- **It can't be used to tap into someone else's private Tesseract.** Thermal Expansion keeps
  frequencies per owner: a public Tesseract uses the shared public channels, a private or
  restricted one its owner's own channels. Frequency 42 on someone's private Tesseract and
  frequency 42 on yours are different channels, so there's nothing to gain from copying a frequency
  anyway.

## How it works

Thermal Expansion's GUI changes settings by calling methods on the client's copy of the machine,
which then send the change to the server: `setSide`, `setRedstoneConfig`, `setAccess`, and for
Tesseracts the mode buttons plus `setTileInfo`. TE Config Copier reads the settings the client
already has (they're sent to every client that can see the block) and pastes through those same
methods. The right-click that triggers a paste is cancelled on the client, so the server never
sees it.

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
