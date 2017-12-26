package mcjty.rftools.blocks.blockprotector;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiBlockProtector extends GenericGuiContainer<BlockProtectorTileEntity> {
    public static final int PROTECTOR_WIDTH = 180;
    public static final int PROTECTOR_HEIGHT = 152;

    private EnergyBar energyBar;
    private ImageChoiceLabel redstoneMode;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/blockprotector.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiBlockProtector(BlockProtectorTileEntity blockProtectorTileEntity, BlockProtectorContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, blockProtectorTileEntity, container, RFTools.GUI_MANUAL_MAIN, "protect");
        GenericEnergyStorageTileEntity.setCurrentRF(blockProtectorTileEntity.getEnergyStored());

        xSize = PROTECTOR_WIDTH;
        ySize = PROTECTOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored();
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        initRedstoneMode();

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).
                addChild(redstoneMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this);
        redstoneMode.addChoiceEvent((parent, newChoice) -> {
            changeRedstoneMode();
        });
        redstoneMode.addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0);
        redstoneMode.addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0);
        redstoneMode.addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(150, 46, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRSMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRSMode(RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()]);
        sendServerCommand(RFToolsMessages.INSTANCE, BlockProtectorTileEntity.CMD_RSMODE, new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()].getDescription()));
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();

        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}
