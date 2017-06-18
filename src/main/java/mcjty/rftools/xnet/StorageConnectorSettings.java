package mcjty.rftools.xnet;

import mcjty.rftools.blocks.storagemonitor.InventoryAccessSettings;
import mcjty.xnet.api.gui.IEditorGui;
import mcjty.xnet.api.gui.IndicatorIcon;
import mcjty.xnet.api.helper.AbstractConnectorSettings;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class StorageConnectorSettings extends AbstractConnectorSettings {

    public static final String TAG_MODE = "mode";
    public static final String TAG_BIGUI = "bigui";
    public static final String TAG_BIAUTO = "biauto";
    public static final String TAG_BISCREEN = "biscreen";
    public static final String TAG_BOGUI = "bogui";
    public static final String TAG_BOAUTO = "boauto";
    public static final String TAG_BOSCREEN = "boscreen";
    public static final String TAG_FILTER = "flt";
    public static final String TAG_BLACKLIST = "blacklist";
    public static final String TAG_OREDICT = "od";
    public static final String TAG_NBT = "nbt";
    public static final String TAG_META = "meta";

    public enum Mode {
        INVENTORY,
        STORAGE
    }

    private Mode mode = Mode.INVENTORY;
    private InventoryAccessSettings accessSettings = new InventoryAccessSettings();

    public StorageConnectorSettings(boolean advanced, @Nonnull EnumFacing side) {
        super(advanced, side);
    }

    public Mode getMode() {
        return mode;
    }

    public InventoryAccessSettings getAccessSettings() {
        return accessSettings;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        mode = Mode.values()[tag.getByte("mode")];
        accessSettings.setBlockInputGui(tag.getBoolean("bigui"));
        accessSettings.setBlockInputAuto(tag.getBoolean("biauto"));
        accessSettings.setBlockInputScreen(tag.getBoolean("biscreen"));
        accessSettings.setBlockOutputGui(tag.getBoolean("bogui"));
        accessSettings.setBlockOutputAuto(tag.getBoolean("boauto"));
        accessSettings.setBlockOutputScreen(tag.getBoolean("boscreen"));
        for (int i = 0 ; i < InventoryAccessSettings.FILTER_SIZE ; i++) {
            if (tag.hasKey("filter" + i)) {
                NBTTagCompound itemTag = tag.getCompoundTag("filter" + i);
                accessSettings.getFilters().set(i, new ItemStack(itemTag));
            } else {
                accessSettings.getFilters().set(i, ItemStack.EMPTY);
            }
        }
        accessSettings.setOredictMode(tag.getBoolean("oredictMode"));
        accessSettings.setMetaMode(tag.getBoolean("metaMode"));
        accessSettings.setNbtMode(tag.getBoolean("nbtMode"));
        accessSettings.setBlacklist(tag.getBoolean("blacklist"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setByte("mode", (byte) mode.ordinal());
        tag.setBoolean("bigui", accessSettings.isBlockInputGui());
        tag.setBoolean("biauto", accessSettings.isBlockInputAuto());
        tag.setBoolean("biscreen", accessSettings.isBlockInputScreen());
        tag.setBoolean("bogui", accessSettings.isBlockOutputGui());
        tag.setBoolean("boauto", accessSettings.isBlockOutputAuto());
        tag.setBoolean("boscreen", accessSettings.isBlockOutputScreen());
        for (int i = 0 ; i < InventoryAccessSettings.FILTER_SIZE ; i++) {
            if (!accessSettings.getFilters().get(i).isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                accessSettings.getFilters().get(i).writeToNBT(itemTag);
                tag.setTag("filter" + i, itemTag);
            }
        }
        tag.setBoolean("oredictMode", accessSettings.isOredictMode());
        tag.setBoolean("metaMode", accessSettings.isMetaMode());
        tag.setBoolean("nbtMode", accessSettings.isNbtMode());
        tag.setBoolean("blacklist", accessSettings.isBlacklist());

    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        switch (mode) {
            case INVENTORY:
                boolean inputBlocked = accessSettings.inputBlocked();
                boolean outputBlocked = accessSettings.outputBlocked();
                if (inputBlocked && outputBlocked) {
                    return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 75, 13, 10);
                } else if (inputBlocked) {
                    return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 48, 13, 10);
                } else if (outputBlocked) {
                    return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 0, 48, 13, 10);
                } else {
                    return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 57, 13, 10);
                }
            case STORAGE:
                return new IndicatorIcon(StorageChannelSettings.iconGuiElements, 13, 66, 13, 10);
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
        if (tag.equals(TAG_FACING)) {
            return isAdvanced();
        }
        return true;
    }

    @Override
    public void createGui(IEditorGui gui) {
        sideGui(gui);
        gui.shift(30)
                .choices(TAG_MODE, "Set to 'inventory' or 'storage'", mode, Mode.values());
        if (mode == Mode.INVENTORY) {
            gui.nl()
                    .label("Input ")
                    .shift(12)
                    .label("UI")
                    .toggle(TAG_BIGUI, "Block input from scanner UI", accessSettings.isBlockInputGui())
                    .label("Auto")
                    .toggle(TAG_BIAUTO, "Block input from automation", accessSettings.isBlockInputAuto())
                    .label("Scr")
                    .toggle(TAG_BISCREEN, "Block input from screens", accessSettings.isBlockInputScreen());
            gui.nl()
                    .label("Output")
                    .shift(10)
                    .label("UI")
                    .toggle(TAG_BOGUI, "Block extraction from scanner UI", accessSettings.isBlockOutputGui())
                    .label("Auto")
                    .toggle(TAG_BOAUTO, "Block extraction from automation", accessSettings.isBlockOutputAuto())
                    .label("Scr")
                    .toggle(TAG_BOSCREEN, "Block extraction from screens", accessSettings.isBlockOutputScreen());
            gui.nl()
                    .toggleText(TAG_BLACKLIST, "Enable blacklist mode", "BL", accessSettings.isBlacklist()).shift(2)
                    .toggleText(TAG_OREDICT, "Ore dictionary matching", "Ore", accessSettings.isOredictMode()).shift(2)
                    .toggleText(TAG_META, "Metadata matching", "Meta", accessSettings.isMetaMode()).shift(2)
                    .toggleText(TAG_NBT, "NBT matching", "NBT", accessSettings.isNbtMode())
                    .nl();
            for (int i = 0 ; i < InventoryAccessSettings.FILTER_SIZE ; i++) {
                gui.ghostSlot(TAG_FILTER + i, accessSettings.getFilters().get(i));
            }
        }
    }

    private static boolean toBool(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            return false;
        }
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        mode = Mode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
        accessSettings.setBlockInputGui(toBool(data.get(TAG_BIGUI)));
        accessSettings.setBlockInputAuto(toBool(data.get(TAG_BIAUTO)));
        accessSettings.setBlockInputScreen(toBool(data.get(TAG_BISCREEN)));
        accessSettings.setBlockOutputGui(toBool(data.get(TAG_BOGUI)));
        accessSettings.setBlockOutputAuto(toBool(data.get(TAG_BOAUTO)));
        accessSettings.setBlockOutputScreen(toBool(data.get(TAG_BOSCREEN)));

        for (int i = 0 ; i < InventoryAccessSettings.FILTER_SIZE ; i++) {
            accessSettings.getFilters().set(i, (ItemStack) data.get(TAG_FILTER+i));
        }

        accessSettings.setOredictMode(toBool(data.get(TAG_OREDICT)));
        accessSettings.setMetaMode(toBool(data.get(TAG_META)));
        accessSettings.setNbtMode(toBool(data.get(TAG_NBT)));
        accessSettings.setBlacklist(toBool(data.get(TAG_BLACKLIST)));

    }
}
