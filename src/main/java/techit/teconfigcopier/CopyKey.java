package techit.teconfigcopier;

import java.util.EnumSet;

import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.TickType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;

/**
 * The copy key (C by default, changeable in Options > Controls): with paper in hand, copies the
 * machine you're looking at. Forge 1.6.4 only reports left-clicks on blocks to the server, so a
 * client-side mod can't use a left-click for this.
 */
public class CopyKey extends KeyBindingRegistry.KeyHandler {
    private static final int KEY_C = 46; // org.lwjgl.input.Keyboard.KEY_C

    /** Called through reflection from TEConfigCopier, only on the client. */
    public static void register() {
        KeyBindingRegistry.registerKeyBinding(new CopyKey());
    }

    private CopyKey() {
        super(new KeyBinding[] {new KeyBinding("Copy machine settings (with paper)", KEY_C)}, new boolean[] {false});
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        // Despite its name, FML 1.6.4 passes isRepeat = true when the key has just gone down (its
        // state changed). Act once: at the start of the tick in which it was pressed.
        boolean justPressed = isRepeat;
        if (tickEnd || !justPressed) {
            return;
        }
        Minecraft mc = Minecraft.func_71410_x(); // getMinecraft
        if (mc.field_71462_r != null || mc.field_71439_g == null || mc.field_71441_e == null) { // currentScreen, thePlayer, theWorld
            return;
        }
        MovingObjectPosition hit = mc.field_71476_x; // objectMouseOver
        if (hit == null || hit.field_72313_a != EnumMovingObjectType.TILE) { // typeOfHit
            return;
        }
        ClickHandler.copyAt(mc.field_71439_g, mc.field_71441_e, hit.field_72311_b, hit.field_72312_c, hit.field_72309_d); // blockX, blockY, blockZ
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }

    @Override
    public String getLabel() {
        return "TE Config Copier copy key";
    }
}
