package mcjty.base;

import mcjty.gui.GuiStyle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public interface ModBase {
    /**
     * This is called whenever the user change the gui style.
     * @param player
     * @param style
     */
    void setGuiStyle(EntityPlayerMP player, GuiStyle style);

    /**
     * Get the current gui style.
     * @param player
     * @return
     */
    GuiStyle getGuiStyle(EntityPlayer player);

    /**
     * Open the manual at a specific page.
     * @param player
     * @param bookindex
     * @param page
     */
    void openManual(EntityPlayer player, int bookindex, String page);
}
