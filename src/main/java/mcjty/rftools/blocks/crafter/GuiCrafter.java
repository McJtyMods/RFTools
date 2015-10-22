package mcjty.rftools.blocks.crafter;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.events.ChoiceEvent;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.network.Argument;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.RedstoneMode;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiCrafter extends GenericGuiContainer<CrafterBaseTE> {
    public static final int CRAFTER_WIDTH = 256;
    public static final int CRAFTER_HEIGHT = 224;

    private EnergyBar energyBar;
    private WidgetList recipeList;
    private ChoiceLabel keepItem;
    private ChoiceLabel internalRecipe;
    private Button applyButton;
    private ImageChoiceLabel redstoneMode;
    private ImageChoiceLabel speedMode;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/crafter.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiCrafter(CrafterBaseTE crafterBlockTileEntity, CrafterContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, crafterBlockTileEntity, container, RFTools.GUI_MANUAL_MAIN, "crafter");
        crafterBlockTileEntity.setCurrentRF(crafterBlockTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = CRAFTER_WIDTH;
        ySize = CRAFTER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(12, 141, 10, 76)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        initKeepMode();
        initInternalRecipe();
        Slider listSlider = initRecipeList();

        applyButton = new Button(mc, this).
                setText("Apply").
                setTooltips("Press to apply the", "recipe to the crafter").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        applyRecipe();
                    }
                }).
                setEnabled(false).
                setLayoutHint(new PositionalLayout.PositionalHint(212, 65, 34, 16));

        initRedstoneMode();
        initSpeedMode();

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).addChild(keepItem).addChild(internalRecipe).
                addChild(recipeList).addChild(listSlider).addChild(applyButton).addChild(redstoneMode).addChild(speedMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        selectRecipe();
        sendChangeToServer(-1, null, null, false, false);

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
    }

    private Slider initRecipeList() {
        recipeList = new WidgetList(mc, this).
                addSelectionEvent(new DefaultSelectionEvent() {
                    @Override
                    public void select(Widget parent, int index) {
                        selectRecipe();
                    }
                }).
                setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 126, 80));
        populateList();

        return new Slider(mc, this).setVertical().setScrollable(recipeList).setLayoutHint(new PositionalLayout.PositionalHint(137, 7, 10, 80));
    }

    private void initInternalRecipe() {
        internalRecipe = new ChoiceLabel(mc, this).
                addChoices("Ext", "Int").
                setTooltips("'Int' will put result of", "crafting operation in", "inventory instead of", "output buffer").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateRecipe();
                    }
                }).
                setEnabled(false).
                setLayoutHint(new PositionalLayout.PositionalHint(148, 24, 41, 14));
    }

    private void initKeepMode() {
        keepItem = new ChoiceLabel(mc, this).
                addChoices("All", "Keep").
                setTooltips("'Keep' will keep one", "item in every inventory", "slot").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateRecipe();
                    }
                }).
                setEnabled(false).
                setLayoutHint(new PositionalLayout.PositionalHint(148, 7, 41, 14));
    }

    private void initSpeedMode() {
        speedMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeSpeedMode();
                    }
                }).
                addChoice("Slow", "Speed mode:\nSlow", iconGuiElements, 48, 0).
                addChoice("Fast", "Speed mode:\nFast", iconGuiElements, 64, 0);
        speedMode.setLayoutHint(new PositionalLayout.PositionalHint(49, 186, 16, 16));
        speedMode.setCurrentChoice(tileEntity.getSpeedMode());
    }

    private void initRedstoneMode() {
        redstoneMode = new ImageChoiceLabel(mc, this).
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        changeRedstoneMode();
                    }
                }).
                addChoice(RedstoneMode.REDSTONE_IGNORED.getDescription(), "Redstone mode:\nIgnored", iconGuiElements, 0, 0).
                addChoice(RedstoneMode.REDSTONE_OFFREQUIRED.getDescription(), "Redstone mode:\nOff to activate", iconGuiElements, 16, 0).
                addChoice(RedstoneMode.REDSTONE_ONREQUIRED.getDescription(), "Redstone mode:\nOn to activate", iconGuiElements, 32, 0);
        redstoneMode.setLayoutHint(new PositionalLayout.PositionalHint(31, 186, 16, 16));
        redstoneMode.setCurrentChoice(tileEntity.getRedstoneMode().ordinal());
    }

    private void changeRedstoneMode() {
        tileEntity.setRedstoneMode(RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()]);
        sendChangeToServer();
    }

    private void changeSpeedMode() {
        tileEntity.setSpeedMode(speedMode.getCurrentChoiceIndex());
        sendChangeToServer();
    }

    private void sendChangeToServer() {
        sendServerCommand(RFToolsMessages.INSTANCE, CrafterBaseTE.CMD_MODE,
                new Argument("rs", RedstoneMode.values()[redstoneMode.getCurrentChoiceIndex()].getDescription()),
                new Argument("speed", speedMode.getCurrentChoiceIndex()));
    }

    private void populateList() {
        recipeList.removeChildren();
        for (int i = 0 ; i < tileEntity.getSupportedRecipes() ; i++) {
            CraftingRecipe recipe = tileEntity.getRecipe(i);
            ItemStack stack = recipe.getResult();
            addRecipeLine(stack);
        }
    }

    private void addRecipeLine(Object craftingResult) {
        String readableName = BlockInfo.getReadableName(craftingResult, 0);
        int color = StyleConfig.colorTextInListNormal;
        if (craftingResult == null) {
            readableName = "<no recipe>";
            color = 0xFF505050;
        }
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChild(new BlockRender(mc, this).setRenderItem(craftingResult)).
                addChild(new Label(mc, this).setColor(color).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDynamic(true).setText(readableName));
        recipeList.addChild(panel);
    }

    private void selectRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            for (int i = 0 ; i < 10 ; i++) {
                inventorySlots.getSlot(i).putStack(null);
            }
            keepItem.setChoice("All");
            internalRecipe.setChoice("Ext");
            keepItem.setEnabled(false);
            internalRecipe.setEnabled(false);
            applyButton.setEnabled(false);
            return;
        }
        CraftingRecipe craftingRecipe = tileEntity.getRecipe(selected);
        InventoryCrafting inv = craftingRecipe.getInventory();
        for (int i = 0 ; i < 9 ; i++) {
            inventorySlots.getSlot(i).putStack(inv.getStackInSlot(i));
        }
        inventorySlots.getSlot(9).putStack(craftingRecipe.getResult());
        keepItem.setChoice(craftingRecipe.isKeepOne() ? "Keep" : "All");
        internalRecipe.setChoice(craftingRecipe.isCraftInternal() ? "Int" : "Ext");
        keepItem.setEnabled(true);
        internalRecipe.setEnabled(true);
        applyButton.setEnabled(true);
    }

    private void testRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        InventoryCrafting inv = new InventoryCrafting(new Container() {
            @Override
            public boolean canInteractWith(EntityPlayer var1) {
                return false;
            }
        }, 3, 3);

        for (int i = 0 ; i < 9 ; i++) {
            inv.setInventorySlotContents(i, inventorySlots.getSlot(i).getStack());
        }

        // Compare current contents to avoid unneeded slot update.
        IRecipe recipe = CraftingRecipe.findRecipe(mc.theWorld, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = null;
        } else {
            newResult = recipe.getCraftingResult(inv);
        }
        inventorySlots.getSlot(9).putStack(newResult);
    }

    private void applyRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        CraftingRecipe craftingRecipe = tileEntity.getRecipe(selected);
        InventoryCrafting inv = craftingRecipe.getInventory();

        for (int i = 0 ; i < 9 ; i++) {
            ItemStack oldStack = inv.getStackInSlot(i);
            ItemStack newStack = inventorySlots.getSlot(i).getStack();
            if (!itemStacksEqual(oldStack, newStack)) {
                inv.setInventorySlotContents(i, newStack);
            }
        }

        // Compare current contents to avoid unneeded slot update.
        IRecipe recipe = CraftingRecipe.findRecipe(mc.theWorld, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = null;
        } else {
            newResult = recipe.getCraftingResult(inv);
        }
        ItemStack oldResult = inventorySlots.getSlot(9).getStack();
        if (!itemStacksEqual(oldResult, newResult)) {
            inventorySlots.getSlot(9).putStack(newResult);
        }

        craftingRecipe.setResult(newResult);
        updateRecipe();
        populateList();
    }

    private void updateRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }
        CraftingRecipe craftingRecipe = tileEntity.getRecipe(selected);
        boolean keepOne = "Keep".equals(keepItem.getCurrentChoice());
        boolean craftInternal = "Int".equals(internalRecipe.getCurrentChoice());
        craftingRecipe.setKeepOne(keepOne);
        craftingRecipe.setCraftInternal(craftInternal);
        sendChangeToServer(selected, craftingRecipe.getInventory(), craftingRecipe.getResult(), keepOne, craftInternal);
    }

    private boolean itemStacksEqual(ItemStack matches, ItemStack oldStack) {
        if (matches == null) {
            return oldStack == null;
        } else {
            return oldStack != null && matches.isItemEqual(oldStack);
        }
    }

    private void sendChangeToServer(int index, InventoryCrafting inv, ItemStack result, boolean keepOne, boolean craftInternal) {
        RFToolsMessages.INSTANCE.sendToServer(new PacketCrafter(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, index, inv,
                result, keepOne, craftInternal));
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);
        testRecipe();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
        int currentRF = tileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFToolsMessages.INSTANCE);
    }
}
