package mcjty.rftools.blocks.screens.modules;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ComputerScreenModule implements ScreenModule {
    public static final int RFPERTICK = 4;
    private String tag = "";

    private final List<ColoredText> textList = new ArrayList<ColoredText>();

    @Override
    public Object[] getData(long millis) {
        return textList.toArray(new Object[textList.size()]);
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            tag = tagCompound.getString("moduleTag");
        }
    }

    public String getTag() {
        return tag;
    }

    public void addText(String text, int color) {
        textList.add(new ColoredText(text, color));
    }

    public void clearText() {
        textList.clear();
    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    public static class ColoredText {
        private final String text;
        private final int color;

        public ColoredText(String text, int color) {
            this.text = text;
            this.color = color;
        }

        public String getText() {
            return text;
        }

        public int getColor() {
            return color;
        }
    }
}
