package mcjty.rftools.blocks.screens.data;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.data.IModuleDataString;

public class ModuleDataString implements IModuleDataString {

    public static final String ID = RFTools.MODID + ":string";

    private final String s;

    @Override
    public String getId() {
        return ID;
    }

    public ModuleDataString(String s) {
        this.s = s;
    }

    public ModuleDataString(ByteBuf buf) {
        s = NetworkTools.readString(buf);
    }

    @Override
    public String get() {
        return s;
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        NetworkTools.writeString(buf, s);
    }
}
