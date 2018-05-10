package mcjty.rftools.blocks.crafter;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.RenderHelper;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.RFTools;
import mcjty.rftools.craftinggrid.CraftingRecipe;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

public class GuiCrafter extends GenericGuiContainer<CrafterBaseTE> {
    private EnergyBar energyBar;
    private WidgetList recipeList;
    private ChoiceLabel keepItem;
    private ChoiceLabel internalRecipe;
    private Button applyButton;

    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private static int lastSelected = -1;

    public GuiCrafter(CrafterBlockTileEntity1 te, CrafterContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, RFTools.GUI_MANUAL_MAIN, "crafter");
        GenericEnergyStorageTileEntity.setCurrentRF(te.getEnergyStored());
    }

    public GuiCrafter(CrafterBlockTileEntity2 te, CrafterContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, RFTools.GUI_MANUAL_MAIN, "crafter");
        GenericEnergyStorageTileEntity.setCurrentRF(te.getEnergyStored());
    }

    public GuiCrafter(CrafterBlockTileEntity3 te, CrafterContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, te, container, RFTools.GUI_MANUAL_MAIN, "crafter");
        GenericEnergyStorageTileEntity.setCurrentRF(te.getEnergyStored());
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/crafter.gui"));
        super.initGui();

        initializeFields();

        if (lastSelected != -1 && lastSelected < tileEntity.getSizeInventory()) {
            recipeList.setSelected(lastSelected);
        }
//        sendChangeToServer(-1, null, null, false, CraftingRecipe.CraftMode.EXT);

        window.event("apply", (source, params) -> applyRecipe());
        window.event("select", (source, params) -> selectRecipe());

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void initializeFields() {
        recipeList = window.findChild("recipes");
        energyBar = window.findChild("energybar");
        applyButton = window.findChild("apply");
        keepItem = window.findChild("keep");
        internalRecipe = window.findChild("internal");

        energyBar.setMaxValue(tileEntity.getMaxEnergyStored());
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        ((ImageChoiceLabel) window.findChild("redstone")).setCurrentChoice(tileEntity.getRSMode().ordinal());
        ((ImageChoiceLabel) window.findChild("speed")).setCurrentChoice(tileEntity.getSpeedMode());

        populateList();
    }


    private void populateList() {
        recipeList.removeChildren();
        for (int i = 0 ; i < tileEntity.getSupportedRecipes() ; i++) {
            CraftingRecipe recipe = tileEntity.getRecipe(i);
            addRecipeLine(recipe.getResult());
        }
    }

    private void addRecipeLine(ItemStack craftingResult) {
        String readableName = BlockTools.getReadableName(craftingResult);
        int color = StyleConfig.colorTextInListNormal;
        if (craftingResult.isEmpty()) {
            readableName = "<no recipe>";
            color = 0xFF505050;
        }
        Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout())
                .addChild(new BlockRender(mc, this)
                        .setRenderItem(craftingResult)
                        .setTooltips("Double click to edit this recipe"))
                .addChild(new Label(mc, this)
                        .setColor(color)
                        .setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT)
                        .setDynamic(true)
                        .setText(readableName)
                        .setTooltips("Double click to edit this recipe"));
        recipeList.addChild(panel);
    }

    private void selectRecipe() {
        int selected = recipeList.getSelected();
        lastSelected = selected;
        if (selected == -1) {
            for (int i = 0 ; i < 10 ; i++) {
                inventorySlots.getSlot(i).putStack(ItemStack.EMPTY);
            }
            keepItem.setChoice("All");
            internalRecipe.setChoice("Ext");
            return;
        }
        CraftingRecipe craftingRecipe = tileEntity.getRecipe(selected);
        InventoryCrafting inv = craftingRecipe.getInventory();
        for (int i = 0 ; i < 9 ; i++) {
            inventorySlots.getSlot(i).putStack(inv.getStackInSlot(i));
        }
        inventorySlots.getSlot(9).putStack(craftingRecipe.getResult());
        keepItem.setChoice(craftingRecipe.isKeepOne() ? "Keep" : "All");
        internalRecipe.setChoice(craftingRecipe.getCraftMode().getDescription());
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
        IRecipe recipe = CraftingRecipe.findRecipe(mc.world, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = ItemStack.EMPTY;
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

        if (selected >= tileEntity.getSupportedRecipes()) {
            recipeList.setSelected(-1);
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
        IRecipe recipe = CraftingRecipe.findRecipe(mc.world, inv);
        ItemStack newResult;
        if (recipe == null) {
            newResult = ItemStack.EMPTY;
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
        CraftingRecipe.CraftMode mode;
        if ("Int".equals(internalRecipe.getCurrentChoice())) {
            mode = CraftingRecipe.CraftMode.INT;
        } else if ("Ext".equals(internalRecipe.getCurrentChoice())) {
            mode = CraftingRecipe.CraftMode.EXT;
        } else {
            mode = CraftingRecipe.CraftMode.EXTC;
        }
        craftingRecipe.setKeepOne(keepOne);
        craftingRecipe.setCraftMode(mode);
        sendChangeToServer(selected, craftingRecipe.getInventory(), craftingRecipe.getResult(), keepOne, mode);
    }

    private boolean itemStacksEqual(ItemStack matches, ItemStack oldStack) {
        if (matches.isEmpty()) {
            return oldStack.isEmpty();
        } else {
            return !oldStack.isEmpty() && matches.isItemEqual(oldStack);
        }
    }

    private void sendChangeToServer(int index, InventoryCrafting inv, ItemStack result, boolean keepOne,
                                    CraftingRecipe.CraftMode mode) {

        RFToolsMessages.INSTANCE.sendToServer(new PacketCrafter(tileEntity.getPos(), index, inv,
                result, keepOne, mode));
    }

    /**
     * Draws the screen and all the components in it.
     */
    @Override
    public void drawScreen(int par1, int par2, float par3) {
        updateButtons();
        super.drawScreen(par1, par2, par3);
        testRecipe();
    }

    private void updateButtons() {
        boolean selected = recipeList.getSelected() != -1;
        keepItem.setEnabled(selected);
        internalRecipe.setEnabled(selected);
        applyButton.setEnabled(selected);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int x, int y) {
        drawWindow();
        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);

        // Draw the ghost slots here
        drawGhostSlots();
    }

    private void drawGhostSlots() {
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop, 0.0F);
        GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (short) 240 / 1.0F, 240.0f);

        ItemStackList ghostSlots = tileEntity.getGhostSlots();
        zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        for (int i = 0 ; i < ghostSlots.size() ; i++) {
            ItemStack stack = ghostSlots.get(i);
            if (!stack.isEmpty()) {
                int slotIdx;
                if (i < CrafterContainer.BUFFER_SIZE) {
                    slotIdx = i + CrafterContainer.SLOT_BUFFER;
                } else {
                    slotIdx = i + CrafterContainer.SLOT_BUFFEROUT - CrafterContainer.BUFFER_SIZE;
                }
                Slot slot = inventorySlots.getSlot(slotIdx);
                if (!slot.getHasStack()) {
                    itemRender.renderItemAndEffectIntoGUI(stack, slot.xPos, slot.yPos);

                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    GlStateManager.disableDepth();
                    this.mc.getTextureManager().bindTexture(iconGuiElements);
                    RenderHelper.drawTexturedModalRect(slot.xPos, slot.yPos, 14 * 16, 3 * 16, 16, 16);
                    GlStateManager.enableDepth();
                    GlStateManager.disableBlend();
                    GlStateManager.enableLighting();
                }
            }

        }
        itemRender.zLevel = 0.0F;
        zLevel = 0.0F;

        GlStateManager.popMatrix();
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
    }
}
