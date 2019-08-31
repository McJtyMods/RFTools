package mcjty.rftools.blocks.shield;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.Property;

import java.util.Collection;
import java.util.Optional;

public class CamoProperty extends Property<CamoBlockId> {

    private final String name;

    public CamoProperty(String name) {
        super(name, CamoBlockId.class);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<CamoBlockId> getAllowedValues() {
        return null;
    }

    @Override
    public Optional<CamoBlockId> parseValue(String s) {
        CompoundNBT tag = null;
        try {
            tag = JsonToNBT.getTagFromJson(s);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException(e);
        }
        BlockState state = NBTUtil.readBlockState(tag);
        return Optional.of(new CamoBlockId(state));
    }

    @Override
    public String getName(CamoBlockId camoBlockId) {
        return camoBlockId.toString();
    }
}
