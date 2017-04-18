package mcjty.xnet.api.channels;

import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Channel specific settings
 */
public interface IChannelSettings {

    void readFromNBT(NBTTagCompound tag);

    void writeToNBT(NBTTagCompound tag);

    /**
     * Do a tick on this channel. This is called server-side
     */
    void tick(int channel, IControllerContext context);

    /**
     * Clean cache. This is called when the controller knows too much
     * has changed and you should not rely on cached data from consumers
     * and such.
     */
    void cleanCache();

    /**
     * If this channel controls clientinfo colors then this returns the latest colors status.
     */
    int getColors();

    /**
     * Return an optional indicator icon
     */
    @Nullable
    IndicatorIcon getIndicatorIcon();

    /**
     * Return a one-char indicator of the current status. If this is
     * present it is drawn on top of the existing icon.
     */
    @Nullable
    String getIndicator();

    /**
     * Return true if a tag is enabled given the current settings
     */
    boolean isEnabled(String tag);

    /**
     * Create the gui for this channel and fill with the current values or
     * defaults if it is not set yet. This is called client-side.
     */
    void createGui(IEditorGui gui);

    /**
     * If something changes on the gui then this will be called server
     * side with a map for all gui components
     */
    void update(Map<String, Object> data);
}
