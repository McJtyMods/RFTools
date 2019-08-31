package mcjty.rftools.blocks.logic.invchecker;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;

public class GuiInvChecker extends GenericGuiContainer<InvCheckerTileEntity, GenericContainer> {

    public static final String OREDICT_USE = "Use";
    public static final String OREDICT_IGNORE = "Ignore";
    public static final String META_MATCH = "Match";
    public static final String META_IGNORE = "Ignore";

    public GuiInvChecker(InvCheckerTileEntity invCheckerTileEntity, GenericContainer container, PlayerInventory inventory) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, invCheckerTileEntity, container, inventory, GuiProxy.GUI_MANUAL_MAIN, "invchecker");
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/invchecker.gui"));
        super.init();

        initializeFields();
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
}
