package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiShaper extends GenericGuiContainer<ShaperTileEntity> {
    public static final int SHAPER_WIDTH = 256;
    public static final int SHAPER_HEIGHT = 238;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/shaper.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiShaper(ShaperTileEntity shaperTileEntity, ShaperContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, shaperTileEntity, container, RFTools.GUI_MANUAL_MAIN, "shaper");

        xSize = SHAPER_WIDTH;
        ySize = SHAPER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
