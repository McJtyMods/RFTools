package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.Panel;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiPearlInjector  extends GenericGuiContainer<PearlInjectorTileEntity> {
    public static final int PEARLINJECTOR_WIDTH = 180;
    public static final int PEARLINJECTOR_HEIGHT = 152;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/pearlinjector.png");

    public GuiPearlInjector(PearlInjectorTileEntity pearlInjectorTileEntity, PearlInjectorContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, pearlInjectorTileEntity, container, RFTools.GUI_MANUAL_MAIN, "powinjector");

        xSize = PEARLINJECTOR_WIDTH;
        ySize = PEARLINJECTOR_HEIGHT;
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
