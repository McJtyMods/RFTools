package mcjty.rftools.blocks.screens.data;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleDataInteger;

public class ModuleDataInteger implements IModuleDataInteger {
    public static final String ID = RFTools.MODID + ":integer";

    public final int i;

    @Override
    public String getId() {
        return ID;
    }

    public ModuleDataInteger(int i) {
        this.i = i;
    }

    public ModuleDataInteger(ByteBuf buf) {
        i = buf.readInt();
    }

    @Override
    public int get() {
        return i;
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        buf.writeInt(i);
    }
}
