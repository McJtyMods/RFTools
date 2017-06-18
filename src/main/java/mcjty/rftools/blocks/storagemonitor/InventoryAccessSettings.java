package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.varia.ItemStackList;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class InventoryAccessSettings {
    private boolean blockInputGui = false;
    private boolean blockInputAuto = false;
    private boolean blockInputScreen = false;
    private boolean blockOutputGui = false;
    private boolean blockOutputAuto = false;
    private boolean blockOutputScreen = false;

    public static final int FILTER_SIZE = 18;
    private boolean oredictMode = false;
    private boolean metaMode = false;
    private boolean nbtMode = false;
    private boolean blacklist = false;
    private ItemStackList filters = ItemStackList.create(FILTER_SIZE);

    // Cached matcher for items
    private Predicate<ItemStack> matcher = null;

    public ItemStackList getFilters() {
        return filters;
    }

    public boolean isOredictMode() {
        return oredictMode;
    }

    public void setOredictMode(boolean oredictMode) {
        this.oredictMode = oredictMode;
    }

    public boolean isMetaMode() {
        return metaMode;
    }

    public void setMetaMode(boolean metaMode) {
        this.metaMode = metaMode;
    }

    public boolean isNbtMode() {
        return nbtMode;
    }

    public void setNbtMode(boolean nbtMode) {
        this.nbtMode = nbtMode;
    }

    public boolean isBlacklist() {
        return blacklist;
    }

    public void setBlacklist(boolean blacklist) {
        this.blacklist = blacklist;
    }

    public boolean isBlockInputGui() {
        return blockInputGui;
    }

    public void setBlockInputGui(boolean blockInputGui) {
        this.blockInputGui = blockInputGui;
    }

    public boolean isBlockInputAuto() {
        return blockInputAuto;
    }

    public void setBlockInputAuto(boolean blockInputAuto) {
        this.blockInputAuto = blockInputAuto;
    }

    public boolean isBlockInputScreen() {
        return blockInputScreen;
    }

    public void setBlockInputScreen(boolean blockInputScreen) {
        this.blockInputScreen = blockInputScreen;
    }

    public boolean isBlockOutputGui() {
        return blockOutputGui;
    }

    public void setBlockOutputGui(boolean blockOutputGui) {
        this.blockOutputGui = blockOutputGui;
    }

    public boolean isBlockOutputScreen() {
        return blockOutputScreen;
    }

    public void setBlockOutputScreen(boolean blockOutputScreen) {
        this.blockOutputScreen = blockOutputScreen;
    }

    public boolean isBlockOutputAuto() {
        return blockOutputAuto;
    }

    public void setBlockOutputAuto(boolean blockOutputAuto) {
        this.blockOutputAuto = blockOutputAuto;
    }

    public boolean inputBlocked() {
        return blockInputGui || blockInputScreen || blockInputAuto;
    }

    public boolean outputBlocked() {
        return blockOutputGui || blockOutputScreen || blockOutputAuto;
    }

    public Predicate<ItemStack> getMatcher() {
        if (matcher == null) {
            ItemStackList filterList = ItemStackList.create();
            for (ItemStack stack : filters) {
                if (!stack.isEmpty()) {
                    filterList.add(stack);
                }
            }
            if (filterList.isEmpty()) {
                matcher = itemStack -> true;
            } else {
                ItemFilterCache filterCache = new ItemFilterCache(metaMode, oredictMode, blacklist, nbtMode, filterList);
                matcher = filterCache::match;
            }
        }
        return matcher;
    }

}
