package mcjty.rftools.blocks.infuser;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiMachineInfuser extends GenericGuiContainer<MachineInfuserTileEntity> {
    public static final int INFUSER_WIDTH = 180;
    public static final int INFUSER_HEIGHT = 152;

    private EnergyBar energyBar;
//    private ImageLabel arrow;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/machineinfuser.png");

    public GuiMachineInfuser(MachineInfuserTileEntity machineInfuserTileEntity, MachineInfuserContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, machineInfuserTileEntity, container, RFTools.GUI_MANUAL_DIMENSION, "infuser");
        machineInfuserTileEntity.setCurrentRF(machineInfuserTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = INFUSER_WIDTH;
        ySize = INFUSER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

//        arrow = new ImageLabel(mc, this).setImage(iconGuiElements, 192, 0);
//        arrow.setLayoutHint(new PositionalLayout.PositionalHint(90, 26, 16, 16));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar); //.addChild(arrow);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
//        tileEntity.requestResearchingFromServer();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
//        int researching = tileEntity.getResearching();
//        if (researching > 0) {
//            arrow.setImage(iconGuiElements, 144, 0);
//        } else {
//            arrow.setImage(iconGuiElements, 192, 0);
//        }

        drawWindow();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
//        tileEntity.requestResearchingFromServer();
    }
}
