package techit.teconfigcopier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

/**
 * Paste: sneak + use (right-click) a machine with paper. The click is cancelled on the client, so
 * it never reaches the server. Copying is done with the copy key (see CopyKey).
 */
public class ClickHandler {
    private static Config clipboard;
    private long lastClick;
    private int lastX, lastY, lastZ;

    @ForgeSubscribe
    public void onInteract(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        World world = player.field_70170_p; // worldObj
        if (world == null || !world.field_72995_K) { // isRemote: only the player's own client acts
            return;
        }
        if (event.action != Action.RIGHT_CLICK_BLOCK || !player.func_70093_af() || !holdingPaper(player)) { // isSneaking
            return;
        }
        TileEntity te = world.func_72796_p(event.x, event.y, event.z); // getBlockTileEntity
        if (te == null || !Config.isSupported(te)) {
            return;
        }
        event.setCanceled(true);

        // A held use key fires repeatedly; paste once per click on a block.
        long now = System.currentTimeMillis();
        boolean repeat = now - lastClick < 400 && event.x == lastX && event.y == lastY && event.z == lastZ;
        lastClick = now;
        lastX = event.x;
        lastY = event.y;
        lastZ = event.z;
        if (repeat) {
            return;
        }

        String name = blockName(world, event.x, event.y, event.z);
        try {
            if (clipboard == null) {
                say(player, "Nothing copied yet. Hold paper, look at a machine and press the copy key (C) first.");
            } else {
                say(player, clipboard.paste(te, player.func_70005_c_()) + " " + name + "."); // getCommandSenderName: the username
            }
        } catch (Throwable t) {
            say(player, "That didn't work (" + t.getClass().getSimpleName() + ").");
            t.printStackTrace();
        }
    }

    /** Copies the machine at x, y, z, if the player holds paper. Called by the copy key. */
    static void copyAt(EntityPlayer player, World world, int x, int y, int z) {
        if (!holdingPaper(player)) {
            return;
        }
        TileEntity te = world.func_72796_p(x, y, z); // getBlockTileEntity
        if (te == null || !Config.isSupported(te)) {
            say(player, "That isn't a machine with settings to copy.");
            return;
        }
        String name = blockName(world, x, y, z);
        try {
            Config copied = Config.copy(te, name, player.func_70005_c_()); // getCommandSenderName: the username
            if (copied == null) {
                say(player, "You can't access this " + name + ", so it can't be copied.");
                return;
            }
            clipboard = copied;
            say(player, "Copied " + name + ": " + clipboard.describe());
        } catch (Throwable t) {
            say(player, "That didn't work (" + t.getClass().getSimpleName() + ").");
            t.printStackTrace();
        }
    }

    private static boolean holdingPaper(EntityPlayer player) {
        ItemStack held = player.func_71045_bC(); // getCurrentEquippedItem
        return held != null && held.func_77973_b() != null && "item.paper".equals(held.func_77973_b().func_77658_a()); // getItem, getUnlocalizedName
    }

    private static String blockName(World world, int x, int y, int z) {
        try {
            int id = world.func_72798_a(x, y, z); // getBlockId
            net.minecraft.block.Block block = net.minecraft.block.Block.field_71973_m[id]; // blocksList
            int meta = block.func_71899_b(world.func_72805_g(x, y, z)); // damageDropped(getBlockMetadata)
            return new ItemStack(id, 1, meta).func_82833_r(); // getDisplayName
        } catch (Throwable t) {
            return "machine";
        }
    }

    private static void say(EntityPlayer player, String text) {
        player.func_70006_a(ChatMessageComponent.func_111066_d("§7[Config Copier]§r " + text)); // sendChatToPlayer(createFromText)
    }
}
