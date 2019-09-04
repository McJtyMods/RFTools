package mcjty.rftools.blocks.screens;


import com.google.common.collect.ImmutableSet;
import mcjty.rftools.api.screens.FormatStyle;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Allow only changes to the NBT that could have been legitimately made via the GUI.
 */
public class NbtSanitizerModuleGuiBuilder implements IModuleGuiBuilder {
    private final Map<String, Set<String>> enumKeys = new HashMap<>();
    private final Set<String> stringKeys = new HashSet<>();
    private final Map<String, Integer> boundedIntegerKeys = new HashMap<>();
    private final Set<String> integerKeys = new HashSet<>();
    private boolean hasModeKeys = false;
    private final Set<String> booleanKeys = new HashSet<>();
    private final Set<String> itemKeys = new HashSet<>();
    private final World world;
    @Nullable private final CompoundNBT oldCompound;

    public NbtSanitizerModuleGuiBuilder(World world, @Nullable CompoundNBT oldCompound) {
        this.world = world;
        this.oldCompound = oldCompound;
    }

    public CompoundNBT sanitizeNbt(CompoundNBT fromClient) {
        CompoundNBT newCompound = oldCompound != null ? oldCompound.copy() : new CompoundNBT();

        for(Map.Entry<String, Set<String>> entry : enumKeys.entrySet()) {
            String key = entry.getKey();
            if(fromClient.contains(key, Constants.NBT.TAG_STRING)) {
                String value = fromClient.getString(key);
                if(entry.getValue().contains(value)) {
                    newCompound.putString(key, value);
                }
            }
        }

        for(String key : stringKeys) {
            if(fromClient.contains(key, Constants.NBT.TAG_STRING)) {
                newCompound.putString(key, fromClient.getString(key));
            }
        }

        for(Map.Entry<String, Integer> entry : boundedIntegerKeys.entrySet()) {
            String key = entry.getKey();
            if(fromClient.contains(key, Constants.NBT.TAG_INT)) {
                int value = fromClient.getInt(key);
                if(value >= 0 && value < entry.getValue()) {
                    newCompound.putInt(key, value);
                }
            }
        }

        for(String key : integerKeys) {
            if(fromClient.contains(key, Constants.NBT.TAG_INT)) {
                newCompound.putInt(key, fromClient.getInt(key));
            }
        }

        if(hasModeKeys && fromClient.contains("showdiff", Constants.NBT.TAG_BYTE) && fromClient.contains("showpct", Constants.NBT.TAG_BYTE) && fromClient.contains("hidetext", Constants.NBT.TAG_BYTE)) {
            boolean showdiff = fromClient.getBoolean("showdiff");
            boolean showpct = fromClient.getBoolean("showpct");
            boolean hidetext = fromClient.getBoolean("hidetext");
            if(!((showdiff && showpct) || (showdiff && hidetext) || (showpct && hidetext))) {
                newCompound.putBoolean("showdiff", showdiff);
                newCompound.putBoolean("showpct", showpct);
                newCompound.putBoolean("hidetext", hidetext);
            }
        }

        for(String key : booleanKeys) {
            if(fromClient.contains(key, Constants.NBT.TAG_BYTE)) {
                newCompound.putBoolean(key, fromClient.getBoolean(key));
            }
        }

        for(String key : itemKeys) {
            if(fromClient.contains(key, Constants.NBT.TAG_COMPOUND)) {
                CompoundNBT tag = new CompoundNBT();
                ItemStack.read(fromClient.getCompound(key)).write(tag);
                newCompound.put(key, tag);
            } else {
                newCompound.remove(key);
            }
        }

        return newCompound;
    }

    @Override
    public CompoundNBT getCurrentData() {
        return oldCompound.copy();
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public IModuleGuiBuilder choices(String tagname, String tooltip, String... choices) {
        enumKeys.put(tagname, ImmutableSet.copyOf(choices));
        return this;
    }

    private static final Set<String> FORMAT_STRINGS = ImmutableSet.copyOf(Arrays.stream(FormatStyle.values()).map(FormatStyle::getName).toArray(String[]::new));

    @Override
    public IModuleGuiBuilder format(String tagname) {
        enumKeys.put(tagname, FORMAT_STRINGS);
        return this;
    }

    @Override
    public IModuleGuiBuilder text(String tagname, String... tooltip) {
        stringKeys.add(tagname);
        return this;
    }

    @Override
    public IModuleGuiBuilder choices(String tagname, Choice... choices) {
        boundedIntegerKeys.put(tagname, choices.length);
        return this;
    }

    @Override
    public IModuleGuiBuilder integer(String tagname, String... tooltip) {
        integerKeys.add(tagname);
        return this;
    }

    @Override
    public IModuleGuiBuilder color(String tagname, String... tooltip) {
        integerKeys.add(tagname);
        return this;
    }

    @Override
    public IModuleGuiBuilder mode(String componentName) {
        hasModeKeys = true;
        return this;
    }

    @Override
    public IModuleGuiBuilder toggle(String tagname, String label, String... tooltip) {
        booleanKeys.add(tagname);
        return this;
    }

    @Override
    public IModuleGuiBuilder toggleNegative(String tagname, String label, String... tooltip) {
        booleanKeys.add(tagname);
        return this;
    }

    @Override
    public IModuleGuiBuilder ghostStack(String tagname) {
        itemKeys.add(tagname);
        return this;
    }

    @Override
    public IModuleGuiBuilder label(String text) {
        // read-only in GUI
        return this;
    }

    @Override
    public IModuleGuiBuilder leftLabel(String text) {
        // read-only in GUI
        return this;
    }

    @Override
    public IModuleGuiBuilder block(String tagname) {
        // read-only in GUI
        return this;
    }

    @Override
    public IModuleGuiBuilder nl() {
        // read-only in GUI
        return this;
    }

}
