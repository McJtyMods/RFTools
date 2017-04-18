package mcjty.rftools.xnet;

import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.helper.AbstractConnectorSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class StorageConnectorSettings extends AbstractConnectorSettings {

    public static final String TAG_MODE = "mode";

    public enum Mode {
        DUAL,
        INS,
        EXT,
        STORAGE
    }

    private Mode mode = Mode.DUAL;

    public StorageConnectorSettings(boolean advanced, @Nonnull EnumFacing side) {
        super(advanced, side);
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        mode = Mode.values()[tag.getByte("mode")];
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setByte("mode", (byte) mode.ordinal());
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        switch (mode) {
            case DUAL:
                return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 57, 13, 10);
            case INS:
                return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 0, 48, 13, 10);
            case EXT:
                return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 48, 13, 10);
            case STORAGE:
                return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 67, 13, 10);
        }
        return null;
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        return true;
    }

    @Override
    public void createGui(IEditorGui gui) {
        sideGui(gui);
        gui.nl()
                .choices(TAG_MODE, "Mode for the storage scanner", mode, Mode.values());
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        mode = Mode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
    }
}
