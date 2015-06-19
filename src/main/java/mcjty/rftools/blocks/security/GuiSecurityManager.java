package mcjty.rftools.blocks.security;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiSecurityManager extends GenericGuiContainer<SecurityManagerTileEntity> {
    public static final int SECURITYMANAGER_WIDTH = 244;
    public static final int SECURITYMANAGER_HEIGHT = 206;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/securitymanager.png");

    public GuiSecurityManager(SecurityManagerTileEntity securityManagerTileEntity, SecurityManagerContainer container) {
        super(securityManagerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "security");

        xSize = SECURITYMANAGER_WIDTH;
        ySize = SECURITYMANAGER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
