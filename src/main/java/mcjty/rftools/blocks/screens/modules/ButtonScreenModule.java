package mcjty.rftools.blocks.screens.modules;

import mcjty.rftools.RFTools;
import net.minecraft.nbt.NBTTagCompound;

public class ButtonScreenModule implements ScreenModule {
    private String line = "";

    public static final int RFPERTICK = 0;

    @Override
    public Object[] getData(long millis) {
        return null;
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
        }
    }

    @Override
    public void activate(int x, int y) {
        int xoffset;
        if (!line.isEmpty()) {
            xoffset = 7 + 80;
        } else {
            xoffset = 7 + 5;
        }
        if (x >= xoffset) {
            RFTools.log("Button: " + line);
        }
    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
