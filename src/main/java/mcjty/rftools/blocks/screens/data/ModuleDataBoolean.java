package mcjty.rftools.blocks.screens.data;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.data.IModuleDataBoolean;

public class ModuleDataBoolean implements IModuleDataBoolean {

    public static final String ID = RFTools.MODID + ":bool";

    private final boolean b;

    @Override
    public String getId() {
        return ID;
    }

    public ModuleDataBoolean(boolean b) {
        this.b = b;
    }

    public ModuleDataBoolean(ByteBuf buf) {
        b = buf.readBoolean();
    }

    @Override
    public boolean get() {
        return b;
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        buf.writeBoolean(b);
    }
}
