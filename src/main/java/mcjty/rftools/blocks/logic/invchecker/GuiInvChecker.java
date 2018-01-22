package mcjty.rftools.blocks.logic.invchecker;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class GuiInvChecker extends GenericGuiContainer<InvCheckerTileEntity> {
    public static final int INVCHECKER_WIDTH = 180;
    public static final int INVCHECKER_HEIGHT = 152;

    public static final String OREDICT_USE = "Use";
    public static final String OREDICT_IGNORE = "Ignore";
    public static final String META_MATCH = "Match";
    public static final String META_IGNORE = "Ignore";

    private TextField amountField;
    private TextField slotField;
    private ChoiceLabel oreDictLabel;
    private ChoiceLabel metaLabel;

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

        amountField = new TextField(mc, this).setTooltips("Set the amount of items in slot")
                .setLayoutHint(new PositionalLayout.PositionalHint(60, 19, 80, 14))
                .addTextEvent((parent, newText) -> setAmount());
        int amount = tileEntity.getAmount();
        amountField.setText(String.valueOf(amount));

        slotField = new TextField(mc, this).setTooltips("Set the slot index")
                .setLayoutHint(new PositionalLayout.PositionalHint(60, 3, 80, 14))
                .addTextEvent((parent, newText) -> setSlot());
        int current = tileEntity.getSlot();
        slotField.setText(String.valueOf(current));

        metaLabel = new ChoiceLabel(mc, this)
                .addChoices(META_IGNORE, META_MATCH)
                .addChoiceEvent((parent, newChoice) -> setMetaUsage())
                .setChoiceTooltip(META_IGNORE, "Ignore meta/damage on item")
                .setChoiceTooltip(META_MATCH, "Meta/damage on item must match");
        metaLabel.setLayoutHint(new PositionalLayout.PositionalHint(60, 35, 80, 14));
        metaLabel.setChoice(tileEntity.isUseMeta() ? META_MATCH : META_IGNORE);

        oreDictLabel = new ChoiceLabel(mc, this)
                .addChoices(OREDICT_IGNORE, OREDICT_USE)
                .addChoiceEvent((parent, newChoice) -> setOredictUsage())
                .setChoiceTooltip(OREDICT_IGNORE, "Ingore ore dictionary")
                .setChoiceTooltip(OREDICT_USE, "Use ore dictionary matching");
        oreDictLabel.setLayoutHint(new PositionalLayout.PositionalHint(60, 51, 80, 14));
        oreDictLabel.setChoice(tileEntity.isOreDict() ? OREDICT_USE : OREDICT_IGNORE);

        toplevel
                .addChild(new Label(mc, this).setText("Slot:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(new PositionalLayout.PositionalHint(10, 3, 50, 14)))
                .addChild(slotField)
                .addChild(new Label(mc, this).setText("Amount:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(new PositionalLayout.PositionalHint(10, 19, 50, 14)))
                .addChild(amountField)
                .addChild(new Label(mc, this).setText("Meta:")
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                        .setLayoutHint(new PositionalLayout.PositionalHint(10, 35, 50, 14)))
                .addChild(metaLabel)
                .addChild(new Label(mc, this).setText("Oredict:")
                .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT)
                .setLayoutHint(new PositionalLayout.PositionalHint(10, 51, 50, 14)))
                .addChild(oreDictLabel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));
        window = new Window(this, toplevel);
    }

    private void setMetaUsage() {
        boolean b = META_MATCH.equals(metaLabel.getCurrentChoice());
        tileEntity.setUseMeta(b);
        sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETMETA, new Argument("b", b));
    }

    private void setOredictUsage() {
        boolean b = OREDICT_USE.equals(oreDictLabel.getCurrentChoice());
        tileEntity.setOreDict(b);
        sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETOREDICT, new Argument("b", b));
    }

    private void setAmount() {
        String d = amountField.getText();
        int amount;
        try {
            amount = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            amount = 1;
        }
        tileEntity.setAmount(amount);
        sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETAMOUNT, new Argument("amount", amount));
    }

    private void setSlot() {
        String d = slotField.getText();
        int slot;
        try {
            slot = Integer.parseInt(d);
        } catch (NumberFormatException e) {
            slot = 0;
        }
        tileEntity.setSlot(slot);
        sendServerCommand(RFToolsMessages.INSTANCE, InvCheckerTileEntity.CMD_SETSLOT, new Argument("slot", slot));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
    }
}
