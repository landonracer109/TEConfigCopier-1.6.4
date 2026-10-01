package techit.teconfigcopier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cofh.api.tileentity.IReconfigurableFacing;
import cofh.api.tileentity.IReconfigurableSides;
import cofh.api.tileentity.IRedstoneControl;
import cofh.api.tileentity.ISecureTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

/**
 * A copied set of settings, and the copy and paste logic.
 *
 * Everything is read from, and written through, the client's copy of the machine with the same
 * calls Thermal Expansion's GUI makes: setSide, setRedstoneConfig, setAccess and, for Tesseracts,
 * the modes plus setTileInfo. Those send the change to the server, which applies its usual checks.
 *
 * Copying only works on blocks the player may open, so it never shows more than the GUI would.
 * Access is only pasted onto blocks the player owns, as in the GUI.
 */
final class Config {
    private static final String TESSERACT = "thermalexpansion.block.ender.TileTesseract";

    String name;
    /** Side settings by position relative to the machine's front: bottom, top, front, back, left, right. */
    byte[] sides;
    Boolean redstoneDisable;
    Boolean redstoneSetting;
    ISecureTile.AccessMode access;
    Integer frequency;
    byte[] tesseractModes; // item, fluid, energy

    static boolean isSupported(TileEntity te) {
        return te instanceof IReconfigurableSides || te instanceof IRedstoneControl || te instanceof ISecureTile || isTesseract(te);
    }

    /** Returns null if the player may not open this block (then nothing is copied). */
    static Config copy(TileEntity te, String name, String player) throws Exception {
        if (te instanceof ISecureTile && !((ISecureTile) te).canPlayerAccess(player)) {
            return null;
        }
        Config c = new Config();
        c.name = name;
        byte[] sideCache = sideCache(te);
        if (sideCache != null) {
            int facing = facing(te);
            c.sides = new byte[6];
            for (int rel = 0; rel < 6; rel++) {
                c.sides[rel] = sideCache[relativeToAbsolute(rel, facing)];
            }
        }
        if (te instanceof IRedstoneControl) {
            IRedstoneControl rs = (IRedstoneControl) te;
            c.redstoneDisable = rs.getControlDisable();
            c.redstoneSetting = rs.getControlSetting();
        }
        if (te instanceof ISecureTile) {
            c.access = ((ISecureTile) te).getAccess();
        }
        if (isTesseract(te)) {
            c.frequency = field(te, "frequency").getInt(te);
            c.tesseractModes = new byte[] {field(te, "modeItem").getByte(te), field(te, "modeFluid").getByte(te), field(te, "modeEnergy").getByte(te)};
        }
        return c;
    }

    /** Applies these settings to a machine. Returns a short summary ("Pasted sides, redstone onto"). */
    String paste(TileEntity te, String player) throws Exception {
        if (te instanceof ISecureTile && !((ISecureTile) te).canPlayerAccess(player)) {
            return "You can't access";
        }
        boolean owner = te instanceof ISecureTile && player.equalsIgnoreCase(((ISecureTile) te).getOwnerName());
        List<String> done = new ArrayList<String>();
        List<String> skipped = new ArrayList<String>();

        byte[] sideCache = sideCache(te);
        if (sides != null && sideCache != null) {
            IReconfigurableSides target = (IReconfigurableSides) te;
            int facing = facing(te);
            int changed = 0, unsupported = 0;
            for (int abs = 0; abs < 6; abs++) {
                if (abs == facing) {
                    continue; // the front face can't be configured
                }
                int value = sides[absoluteToRelative(abs, facing)];
                if (value >= target.getNumConfig(abs)) {
                    unsupported++;
                } else if (sideCache[abs] != value && target.setSide(abs, value)) {
                    changed++;
                }
            }
            done.add("sides");
            if (unsupported > 0) {
                skipped.add(unsupported + " side" + (unsupported == 1 ? "" : "s") + " this machine doesn't have");
            }
        }
        if (redstoneDisable != null && te instanceof IRedstoneControl) {
            IRedstoneControl rs = (IRedstoneControl) te;
            if (rs.getControlDisable() != redstoneDisable || rs.getControlSetting() != redstoneSetting) {
                rs.setRedstoneConfig(redstoneDisable, redstoneSetting);
            }
            done.add("redstone");
        }
        boolean pasteAccess = access != null && te instanceof ISecureTile;
        if (pasteAccess && !owner) {
            skipped.add("access, since you don't own it");
            pasteAccess = false;
        }
        if (isTesseract(te) && frequency != null) {
            // As the Tesseract GUI does: set the modes (and access) on the client copy, then
            // setTileInfo sends frequency, modes and access to the server in one packet.
            field(te, "modeItem").setByte(te, tesseractModes[0]);
            field(te, "modeFluid").setByte(te, tesseractModes[1]);
            field(te, "modeEnergy").setByte(te, tesseractModes[2]);
            if (pasteAccess) {
                field(te, "access").set(te, access);
                done.add("access");
            }
            Method setTileInfo = te.getClass().getMethod("setTileInfo", int.class);
            setTileInfo.invoke(te, frequency);
            done.add("frequency " + frequency);
        } else if (pasteAccess) {
            if (((ISecureTile) te).getAccess() != access) {
                ((ISecureTile) te).setAccess(access);
            }
            done.add("access");
        }
        if (done.isEmpty()) {
            return "Nothing in the copied " + name + " settings fits";
        }
        return "Pasted " + join(done) + (skipped.isEmpty() ? "" : " (skipped " + join(skipped) + ")") + " onto";
    }

    String describe() {
        List<String> parts = new ArrayList<String>();
        if (sides != null) parts.add("sides");
        if (redstoneDisable != null) parts.add("redstone " + (redstoneDisable ? "ignored" : redstoneSetting ? "high" : "low"));
        if (access != null) parts.add(access.name().toLowerCase());
        if (frequency != null) parts.add("frequency " + frequency);
        return parts.isEmpty() ? "nothing" : join(parts);
    }

    // ---- helpers ----

    private static boolean isTesseract(TileEntity te) {
        return te != null && te.getClass().getName().equals(TESSERACT);
    }

    private static byte[] sideCache(TileEntity te) {
        if (!(te instanceof IReconfigurableSides)) {
            return null;
        }
        try {
            return (byte[]) field(te, "sideCache").get(te);
        } catch (Exception e) {
            return null;
        }
    }

    private static int facing(TileEntity te) {
        return te instanceof IReconfigurableFacing ? ((IReconfigurableFacing) te).getFacing() : -1;
    }

    private static Field field(Object o, String name) throws NoSuchFieldException {
        for (Class<?> c = o.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                // try the superclass
            }
        }
        throw new NoSuchFieldException(name);
    }

    /**
     * Positions relative to the front: 0 bottom, 1 top, 2 front, 3 back, 4 left, 5 right. Only
     * horizontal facings rotate; otherwise sides are used as they are (absolute).
     */
    static int relativeToAbsolute(int rel, int facing) {
        if (facing < 2 || facing > 5 || rel < 2) {
            return rel;
        }
        ForgeDirection front = ForgeDirection.getOrientation(facing);
        ForgeDirection left = front.getRotation(ForgeDirection.UP);
        switch (rel) {
            case 2: return front.ordinal();
            case 3: return front.getOpposite().ordinal();
            case 4: return left.ordinal();
            default: return left.getOpposite().ordinal();
        }
    }

    static int absoluteToRelative(int abs, int facing) {
        for (int rel = 0; rel < 6; rel++) {
            if (relativeToAbsolute(rel, facing) == abs) {
                return rel;
            }
        }
        return abs;
    }

    private static String join(List<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) sb.append(i == parts.size() - 1 ? " and " : ", ");
            sb.append(parts.get(i));
        }
        return sb.toString();
    }
}
