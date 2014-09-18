package com.mcjty.rftools.blocks.crafter;

import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.ChoiceEvent;
import com.mcjty.gui.events.SelectionEvent;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.PositionalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiCrafter extends GuiContainer {
    public static final int CRAFTER_WIDTH = 256;
    public static final int CRAFTER_HEIGHT = 224;

    private Window window;
    private EnergyBar energyBar;
    private WidgetList recipeList;
    private ChoiceLabel keepItem;
    private ChoiceLabel internalRecipe;
    private Button applyButton;

    private final CrafterBlockTileEntity crafterBlockTileEntity;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/crafter.png");

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = crafterBlockTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(12, 141, 8, 76)).setShowText(false);
        energyBar.setValue(crafterBlockTileEntity.getCurrentRF());

        keepItem = new ChoiceLabel(mc, this).
                addChoices("All", "Keep").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateRecipe();
                    }
                }).
                setLayoutHint(new PositionalLayout.PositionalHint(150, 7, 38, 14));
        internalRecipe = new ChoiceLabel(mc, this).
                addChoices("Ext", "Int").
                addChoiceEvent(new ChoiceEvent() {
                    @Override
                    public void choiceChanged(Widget parent, String newChoice) {
                        updateRecipe();
                    }
                }).
                setLayoutHint(new PositionalLayout.PositionalHint(150, 24, 38, 14));

        recipeList = new WidgetList(mc, this).
                setRowheight(16).
                addSelectionEvent(new SelectionEvent() {
                    @Override
                    public void select(Widget parent, int index) {
                        selectRecipe();
                    }
                }).
                setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 125, 80));
        populateList();

        Slider listSlider = new Slider(mc, this).setVertical().setScrollable(recipeList).setLayoutHint(new PositionalLayout.PositionalHint(137, 7, 11, 80));
        applyButton = new Button(mc, this).
                setText("Apply").
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        applyRecipe();
                    }
                }).
                setLayoutHint(new PositionalLayout.PositionalHint(212, 65, 34, 16));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).addChild(keepItem).addChild(internalRecipe).
                addChild(recipeList).addChild(listSlider).addChild(applyButton);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        selectRecipe();
        sendChangeToServer(-1, null, null, false, false);

        window = new Window(this, toplevel);
    }

    private void populateList() {
        recipeList.removeChildren();
        for (int i = 0 ; i < 8 ; i++) {
            CraftingRecipe recipe = crafterBlockTileEntity.getRecipe(i);
            ItemStack stack = recipe.getResult();
            addRecipeLine(stack);
        }
    }

    private void addRecipeLine(Object craftingResult) {
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChild(new BlockRender(mc, this).setRenderItem(craftingResult)).
                addChild(new Label(mc, this).setText(BlockInfo.getReadableName(craftingResult, 0)));
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
            return;
        }
        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
        InventoryCrafting inv = craftingRecipe.getInventory();
        for (int i = 0 ; i < 9 ; i++) {
            inventorySlots.getSlot(i).putStack(inv.getStackInSlot(i));
        }
        inventorySlots.getSlot(9).putStack(craftingRecipe.getResult());
        keepItem.setChoice(craftingRecipe.isKeepOne() ? "Keep" : "All");
        internalRecipe.setChoice(craftingRecipe.isCraftInternal() ? "Int" : "Ext");
    }

    private void testRecipe() {
        int selected = recipeList.getSelected();
        if (selected == -1) {
            return;
        }

        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
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

        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
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
        CraftingRecipe craftingRecipe = crafterBlockTileEntity.getRecipe(selected);
        boolean keepOne = "Keep".equals(keepItem.getCurrentChoice());
        boolean craftInternal = "Int".equals(internalRecipe.getCurrentChoice());
        craftingRecipe.setKeepOne(keepOne);
        craftingRecipe.setCraftInternal(craftInternal);
        sendChangeToServer(selected, craftingRecipe.getInventory(), craftingRecipe.getResult(), keepOne, craftInternal);
    }

    private boolean itemStacksEqual(ItemStack matches, ItemStack oldStack) {
        if (matches == null) {
            return oldStack == null;
        } else if (oldStack == null) {
            return false;
        } else {
            return matches.isItemEqual(oldStack);
        }
    }

    private void sendChangeToServer(int index, InventoryCrafting inv, ItemStack result, boolean keepOne, boolean craftInternal) {
        PacketHandler.INSTANCE.sendToServer(new PacketCrafter(crafterBlockTileEntity.xCoord, crafterBlockTileEntity.yCoord, crafterBlockTileEntity.zCoord, index, inv,
                result, keepOne, craftInternal));
    }

    public GuiCrafter(CrafterBlockTileEntity crafterBlockTileEntity, CrafterContainer container) {
        super(container);
        this.crafterBlockTileEntity = crafterBlockTileEntity;
        crafterBlockTileEntity.setOldRF(-1);
        crafterBlockTileEntity.setCurrentRF(crafterBlockTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = CRAFTER_WIDTH;
        ySize = CRAFTER_HEIGHT;
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
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        int currentRF = crafterBlockTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }
}
