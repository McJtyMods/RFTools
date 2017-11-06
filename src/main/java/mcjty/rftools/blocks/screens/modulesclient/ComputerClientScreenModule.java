package mcjty.rftools.blocks.screens.modulesclient;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleRenderHelper;
import mcjty.rftools.api.screens.ModuleRenderInfo;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.screens.modules.ComputerScreenModule;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComputerClientScreenModule implements IClientScreenModule<ComputerScreenModule.ModuleComputerInfo> {

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty,
            ComputerScreenModule.ModuleComputerInfo screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        if (screenData != null) {
            int x = 7;
            for (ComputerScreenModule.ColoredText ct : screenData) {
                fontRenderer.drawString(ct.getText(), x, currenty, ct.getColor());
                x += fontRenderer.getStringWidth(ct.getText());
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .leftLabel("Contents of this module is").nl()
                .leftLabel("controlled with a computer.").nl()
                .leftLabel("Only works with OpenComputers.").nl() // "Only works with OC or CC."
                .label("Tag:").text("moduleTag", "Tag used by LUA to identify module").nl();
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, BlockPos pos) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }

}
