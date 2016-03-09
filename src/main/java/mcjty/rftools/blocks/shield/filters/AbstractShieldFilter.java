package mcjty.rftools.blocks.shield.filters;

import io.netty.buffer.ByteBuf;
import mcjty.lib.varia.Logging;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

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
    public void toBytes(ByteBuf buf) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        writeToNBT(tagCompound);
        ByteBufUtils.writeTag(buf, tagCompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        action = tagCompound.getInteger("action");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setString("type", getFilterName());
        tagCompound.setInteger("action", action);
    }

    public static ShieldFilter createFilter(ByteBuf buf) {
        NBTTagCompound compound = ByteBufUtils.readTag(buf);
        return createFilter(compound);
    }

    public static ShieldFilter createFilter(NBTTagCompound compound) {
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
