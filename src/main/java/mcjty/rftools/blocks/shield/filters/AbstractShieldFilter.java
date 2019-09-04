package mcjty.rftools.blocks.shield.filters;

import mcjty.lib.varia.Logging;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public abstract class AbstractShieldFilter implements ShieldFilter {
    private int action = ACTION_PASS;

    @Override
    public int getAction() {
        return action;
    }

    @Override
    public void setAction(int action) {
        this.action = action;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        CompoundNBT tagCompound = new CompoundNBT();
        writeToNBT(tagCompound);
        buf.writeCompoundTag(tagCompound);
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        action = tagCompound.getInt("action");
    }

    @Override
    public void writeToNBT(CompoundNBT tagCompound) {
        tagCompound.putString("type", getFilterName());
        tagCompound.putInt("action", action);
    }

    public static ShieldFilter createFilter(PacketBuffer buf) {
        CompoundNBT compound = buf.readCompoundTag();
        return createFilter(compound);
    }

    public static ShieldFilter createFilter(CompoundNBT compound) {
        String type = compound.getString("type");
        ShieldFilter filter = createFilter(type);
        filter.readFromNBT(compound);
        return filter;
    }

    public static ShieldFilter createFilter(String type) {
        ShieldFilter filter;
        // @todo: improve this if in a nicer manner
        if ("animal".equals(type)) {
            filter = new AnimalFilter();
        } else if ("hostile".equals(type)) {
            filter = new HostileFilter();
        } else if ("player".equals(type)) {
            filter = new PlayerFilter();
        } else if ("item".equals(type)) {
            filter = new ItemFilter();
        } else if ("default".equals(type)) {
            filter = new DefaultFilter();
        } else {
            Logging.log("Unknown filter type = " + type);
            filter = new DefaultFilter();
        }
        return filter;
    }
}
