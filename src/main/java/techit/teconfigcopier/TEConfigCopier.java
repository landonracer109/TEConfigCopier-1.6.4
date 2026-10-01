package techit.teconfigcopier;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraftforge.common.MinecraftForge;

/**
 * TE Config Copier: copy a Thermal Expansion machine's settings (sides, redstone mode, access, and
 * a Tesseract's frequency and modes) and paste them onto another. Hold paper: the copy key (C)
 * copies the machine you're looking at, sneak + use (right-click) pastes.
 *
 * Client side only. Pasting does exactly what clicking the buttons in the machine's GUI does, so it
 * works on any server and is subject to the same rules there.
 */
@Mod(modid = TEConfigCopier.MODID, name = "TE Config Copier", version = TEConfigCopier.VERSION, dependencies = "after:ThermalExpansion")
@NetworkMod(clientSideRequired = false, serverSideRequired = false)
public class TEConfigCopier {
    public static final String MODID = "teconfigcopier";
    public static final String VERSION = "1.0.0";

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new ClickHandler());
            try {
                // Through reflection so a server never loads CopyKey's client-only classes.
                Class.forName("techit.teconfigcopier.CopyKey").getMethod("register").invoke(null);
            } catch (Exception e) {
                throw new RuntimeException("TE Config Copier: couldn't register the copy key", e);
            }
        }
    }
}
