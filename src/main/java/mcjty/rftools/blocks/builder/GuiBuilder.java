package mcjty.rftools.blocks.builder;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.GuiShapeCard;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.typed.TypedMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import static mcjty.rftools.blocks.builder.BuilderTileEntity.*;

public class GuiBuilder extends GenericGuiContainer<BuilderTileEntity> {

    private EnergyBar energyBar;
    private Button currentLevel;
    private ImageChoiceLabel anchor[] = new ImageChoiceLabel[4];

    public GuiBuilder(BuilderTileEntity builderTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, builderTileEntity, container, RFTools.GUI_MANUAL_SHAPE, "builder");
        setCurrentRF(builderTileEntity.getEnergyStored());
    }

    @Override
    public void initGui() {
        window = new Window(this, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/builder.gui"));
        super.initGui();

        initializeFields();
        setupEvents();

        tileEntity.requestRfFromServer(RFTools.MODID);
        tileEntity.requestCurrentLevel();
    }

    private void setupEvents() {
        window.addChannelEvent("cardgui", (source, params) -> openCardGui());
        window.addChannelEvent("anchor", (source, params) -> selectAnchor(source.getName()));
    }

    private void initializeFields() {
        energyBar = window.findChild("energybar");
        currentLevel = window.findChild("level");
        anchor[0] = window.findChild("anchor0");
        anchor[1] = window.findChild("anchor1");
        anchor[2] = window.findChild("anchor2");
        anchor[3] = window.findChild("anchor3");

        energyBar.setMaxValue(tileEntity.getMaxEnergyStored());
        energyBar.setValue(getCurrentRF());
        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
        ((ChoiceLabel) window.findChild("mode")).setChoice(MODES[tileEntity.getMode()]);
        ChoiceLabel rotateButton = window.findChild("rotate");
        rotateButton.setChoice(String.valueOf(tileEntity.getRotate() * 90));
        if (!isShapeCard()) {
            anchor[tileEntity.getAnchor()].setCurrentChoice(1);
        }
        ((ImageChoiceLabel)window.findChild("silent")).setCurrentChoice(tileEntity.isSilent() ? 1 : 0);
        ((ImageChoiceLabel)window.findChild("support")).setCurrentChoice(tileEntity.hasSupportMode() ? 1 : 0);
        ((ImageChoiceLabel)window.findChild("entities")).setCurrentChoice(tileEntity.hasEntityMode() ? 1 : 0);
        ((ImageChoiceLabel)window.findChild("loop")).setCurrentChoice(tileEntity.hasLoopMode() ? 1 : 0);
        ((ImageChoiceLabel)window.findChild("wait")).setCurrentChoice(tileEntity.isWaitMode() ? 1 : 0);
        ((ImageChoiceLabel)window.findChild("hilight")).setCurrentChoice(tileEntity.isHilightMode() ? 1 : 0);
    }

    private void openCardGui() {
        ItemStack cardStack = inventorySlots.getSlot(SLOT_TAB).getStack();
        if (!cardStack.isEmpty()) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            GuiShapeCard.fromTEPos = tileEntity.getPos();
            GuiShapeCard.fromTEStackSlot = SLOT_TAB;
            GuiShapeCard.returnGui = this;
            player.openGui(RFTools.instance, RFTools.GUI_SHAPECARD_COMPOSER, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

    private void selectAnchor(String name) {
        int index = name.charAt(name.length()-1)-48;
        updateAnchorSettings(index);
        sendServerCommand(RFToolsMessages.INSTANCE, CMD_SETANCHOR, TypedMap.builder().put(PARAM_ANCHOR_INDEX, index).build());
    }

    private void updateAnchorSettings(int index) {
        for (int i = 0 ; i < anchor.length ; i++) {
            if (isShapeCard()) {
                anchor[i].setCurrentChoice(0);
            } else {
                anchor[i].setCurrentChoice(i == index ? 1 : 0);
            }
        }
    }

    private boolean isShapeCard() {
        ItemStack card = tileEntity.getStackInSlot(SLOT_TAB);
        return !card.isEmpty() && card.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int cury = getCurrentLevelClientSide();
        currentLevel.setText("Y: " + (cury == -1 ? "stop" : cury));

        ItemStack card = tileEntity.getStackInSlot(SLOT_TAB);
        if (card.isEmpty()) {
            window.setFlag("!validcard");
        } else if (card.getItem() == BuilderSetup.shapeCardItem) {
            window.setFlag("!validcard");
        } else {
            window.setFlag("validcard");
        }
        updateAnchorSettings(tileEntity.getAnchor());

        drawWindow();

        energyBar.setValue(getCurrentRF());

        tileEntity.requestRfFromServer(RFTools.MODID);
        tileEntity.requestCurrentLevel();
    }
}
