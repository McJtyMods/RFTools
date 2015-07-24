package mcjty.base;

import net.minecraft.entity.player.EntityPlayerMP;

public interface ModBase {
    /**
     * This is called whenever the user change the gui style.
     * @param playerEntity
     * @param style
     */
    void setGuiStyle(EntityPlayerMP playerEntity, String style);
}
