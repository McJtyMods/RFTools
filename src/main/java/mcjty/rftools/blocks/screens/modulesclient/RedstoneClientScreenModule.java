package mcjty.rftools.blocks.screens.modulesclient;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.rftools.api.screens.*;
import mcjty.rftools.api.screens.data.IModuleDataInteger;
import mcjty.rftools.blocks.screens.modulesclient.helper.ScreenTextHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneClientScreenModule implements IClientScreenModule<IModuleDataInteger> {

    private String line = "";
    private String yestext = "on";
    private String notext = "off";
    private int color = 0xffffff;
    private int yescolor = 0xffffff;
    private int nocolor = 0xffffff;
    private int dim = 0;
    private boolean analog = false;

    private ITextRenderHelper labelCache = new ScreenTextHelper();

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataInteger screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();

        int xoffset;
        if (!line.isEmpty()) {
            labelCache.setup(line, 160, renderInfo);
            labelCache.renderText(0, currenty, color, renderInfo);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        String text;
        int col;
        if (screenData != null) {
            int power = screenData.get();
            boolean rs = power > 0;
            if(analog) {
                text = Integer.toString(power);
            } else {
                text = rs ? yestext : notext;
            }
            col = rs ? yescolor : nocolor;
        } else {
            text = "<invalid>";
            col = 0xff0000;
        }
        renderHelper.renderText(xoffset, currenty, col, renderInfo, text);
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.hasKey("yestext")) {
                yestext = tagCompound.getString("yestext");
            }
            if (tagCompound.hasKey("notext")) {
                notext = tagCompound.getString("notext");
            }
            if (tagCompound.hasKey("color")) {
                color = tagCompound.getInteger("color");
            } else {
                color = 0xffffff;
            }
            if (tagCompound.hasKey("yescolor")) {
                yescolor = tagCompound.getInteger("yescolor");
            } else {
                yescolor = 0xffffff;
            }
            if (tagCompound.hasKey("nocolor")) {
                nocolor = tagCompound.getInteger("nocolor");
            } else {
                nocolor = 0xffffff;
            }
            if (tagCompound.hasKey("align")) {
                String alignment = tagCompound.getString("align");
                labelCache.align(TextAlign.get(alignment));
            } else {
                labelCache.align(TextAlign.ALIGN_LEFT);
            }
            analog = tagCompound.getBoolean("analog");
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}
