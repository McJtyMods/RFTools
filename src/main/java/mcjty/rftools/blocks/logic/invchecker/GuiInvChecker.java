package mcjty.rftools.blocks.logic.invchecker;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;

public class GuiInvChecker extends GenericGuiContainer<InvCheckerTileEntity> {
    public static final int INVCHECKER_WIDTH = 180;
    public static final int INVCHECKER_HEIGHT = 152;

    public static final String OREDICT_USE = "Use";
    public static final String OREDICT_IGNORE = "Ignore";
    public static final String META_MATCH = "Match";
    public static final String META_IGNORE = "Ignore";

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/invchecker.png");

    public GuiInvChecker(InvCheckerTileEntity invCheckerTileEntity, InvCheckerContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, invCheckerTileEntity, container, RFTools.GUI_MANUAL_MAIN, "invchecker");
        xSize = INVCHECKER_WIDTH;
        ySize = INVCHECKER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout());

        TextField amountField = new TextField(mc, this)
                .setName("amount").setChannel("amount")
                .setTooltips("Set the amount of items in slot")
                .setLayoutHint(60, 19, 80, 14);

        TextField slotField = new TextField(mc, this).setTooltips("Set the slot index")
                .setName("slot").setChannel("slot")
                .setLayoutHint(60, 3, 80, 14);

        ChoiceLabel metaLabel = new ChoiceLabel(mc, this)
                .setName("meta").setChannel("meta")
                .addChoices(META_IGNORE, META_MATCH)
                .setChoiceTooltip(META_IGNORE, "Ignore meta/damage on item")
                .setChoiceTooltip(META_MATCH, "Meta/damage on item must match");
        metaLabel.setLayoutHint(60, 35, 80, 14);

        ChoiceLabel oreDictLabel = new ChoiceLabel(mc, this)
                .setName("ore").setChannel("ore")
                .addChoices(OREDICT_IGNORE, OREDICT_USE)
                .setChoiceTooltip(OREDICT_IGNORE, "Ingore ore dictionary")
                .setChoiceTooltip(OREDICT_USE, "Use ore dictionary matching");
        oreDictLabel.setLayoutHint(60, 51, 80, 14);

        toplevel
                .addChild(new Label(mc, this).setText("Slot:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 3, 50, 14))
                .addChild(slotField)
                .addChild(new Label(mc, this).setText("Amount:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 19, 50, 14))
                .addChild(amountField)
                .addChild(new Label(mc, this).setText("Meta:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setLayoutHint(10, 35, 50, 14))
                .addChild(metaLabel)
                .addChild(new Label(mc, this).setText("Oredict:")
                .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                .setLayoutHint(10, 51, 50, 14))
                .addChild(oreDictLabel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
    }

    private void initializeFields() {
        TextField amountField = window.findChild("amount");
        amountField.setText(String.valueOf(tileEntity.getAmount()));

        TextField slotField = window.findChild("slot");
        slotField.setText(String.valueOf(tileEntity.getSlot()));

        ChoiceLabel metaLabel = window.findChild("meta");
        metaLabel.setChoice(tileEntity.isUseMeta() ? META_MATCH : META_IGNORE);

        ChoiceLabel oreDictLabel = window.findChild("ore");
        oreDictLabel.setChoice(tileEntity.isOreDict() ? OREDICT_USE : OREDICT_IGNORE);
    }

    private void setupEvents() {
        window.addChannelEvent("amount", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETAMOUNT, params));
        window.addChannelEvent("slot", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETSLOT, params));
        window.addChannelEvent("meta", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETMETA, params));
        window.addChannelEvent("ore", (source, params) -> sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETOREDICT, params));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
