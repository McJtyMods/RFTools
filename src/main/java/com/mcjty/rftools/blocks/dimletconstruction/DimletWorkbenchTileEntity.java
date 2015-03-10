package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.*;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRequestIntegerFromServer;
import com.mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class DimletWorkbenchTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {
    public static final String CMD_STARTEXTRACT = "startExtract";
    public static final String CMD_GETEXTRACTING = "getExtracting";
    public static final String CLIENTCMD_GETEXTRACTING = "getExtracting";
    public static final String CMD_SETAUTOEXTRACT = "setAutoExtract";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimletWorkbenchContainer.factory, DimletWorkbenchContainer.SIZE_BUFFER + 9);

    private int extracting = 0;
    private DimletKey idToExtract = null;
    private int inhibitCrafting = 0;
    private boolean autoExtract = false;

    public int getExtracting() {
        return extracting;
    }

    public boolean isAutoExtract() {return autoExtract;
    }

    public DimletWorkbenchTileEntity() {
        super(DimletConstructionConfiguration.WORKBENCH_MAXENERGY, DimletConstructionConfiguration.WORKBENCH_RECEIVEPERTICK);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return DimletWorkbenchContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return index == DimletWorkbenchContainer.SLOT_INPUT;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return index == DimletWorkbenchContainer.SLOT_OUTPUT;
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getStacks().length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStacks()[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        ItemStack s = inventoryHelper.decrStackSize(index, amount);
        checkCrafting();
        return s;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (index < DimletWorkbenchContainer.SLOT_BASE || index > DimletWorkbenchContainer.SLOT_ESSENCE) {
            return;
        }

        checkCrafting();
    }

    private void checkCrafting() {
        if (inhibitCrafting == 0) {
            if (!checkDimletCrafting()) {
                if (inventoryHelper.getStacks()[DimletWorkbenchContainer.SLOT_OUTPUT] != null) {
                    inventoryHelper.setInventorySlotContents(0, DimletWorkbenchContainer.SLOT_OUTPUT, null);
                }
            }
        }
    }

    @Override
    public String getInventoryName() {
        return "Workbench Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    protected void checkStateServer() {
        if (extracting > 0) {
            extracting--;
            if (extracting == 0) {
                if (!doExtract()) {
                    // We failed due to not enough power. Try again later.
                    extracting = 10;
                }
            }
            markDirty();
        } else if (autoExtract) {
            startExtracting();
        }
    }

    private boolean checkDimletCrafting() {
        ItemStack stackBase = inventoryHelper.getStacks()[DimletWorkbenchContainer.SLOT_BASE];
        if (stackBase == null) {
            return false;
        }
        ItemStack stackController = inventoryHelper.getStacks()[DimletWorkbenchContainer.SLOT_CONTROLLER];
        if (stackController == null) {
            return false;
        }
        ItemStack stackTypeController = inventoryHelper.getStacks()[DimletWorkbenchContainer.SLOT_TYPE_CONTROLLER];
        if (stackTypeController == null) {
            return false;
        }
        ItemStack stackMemory = inventoryHelper.getStacks()[DimletWorkbenchContainer.SLOT_MEMORY];
        if (stackMemory == null) {
            return false;
        }
        ItemStack stackEnergy = inventoryHelper.getStacks()[DimletWorkbenchContainer.SLOT_ENERGY];
        if (stackEnergy == null) {
            return false;
        }
        ItemStack stackEssence = inventoryHelper.getStacks()[DimletWorkbenchContainer.SLOT_ESSENCE];
        if (stackEssence == null) {
            return false;
        }

        DimletType type = DimletType.values()[stackTypeController.getItemDamage()];
        switch (type) {
            case DIMLET_BIOME:
                return attemptBiomeDimletCrafting(stackController, stackMemory, stackEnergy, stackEssence);
            case DIMLET_MOBS:
                return attemptMobDimletCrafting(stackController, stackMemory, stackEnergy, stackEssence);
            case DIMLET_SPECIAL:
                return attemptSpecialDimletCrafting(stackController, stackMemory, stackEnergy, stackEssence);
            case DIMLET_MATERIAL:
                return attemptMaterialDimletCrafting(stackController, stackMemory, stackEnergy, stackEssence);
            case DIMLET_LIQUID:
                return attemptLiquidDimletCrafting(stackController, stackMemory, stackEnergy, stackEssence);
            case DIMLET_TIME:
                return attemptTimeDimletCrafting(stackController, stackMemory, stackEnergy, stackEssence);
            case DIMLET_SKY:
            case DIMLET_WEATHER:
            case DIMLET_STRUCTURE:
            case DIMLET_TERRAIN:
            case DIMLET_FEATURE:
            case DIMLET_EFFECT:
            case DIMLET_CONTROLLER:
            case DIMLET_DIGIT:
            case DIMLET_FOLIAGE:
                return false;
        }

        return true;
    }

    private boolean attemptTimeDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidTimeEssence(stackEssence, stackEssence.getTagCompound())) {
            return false;
        }
        DimletKey timeDimlet = findTimeDimlet(stackEssence);
        if (timeDimlet == null) {
            return false;
        }
        if (!matchDimletRecipe(timeDimlet, stackController, stackMemory, stackEnergy)) {
            return false;
        }
        inventoryHelper.setInventorySlotContents(1, DimletWorkbenchContainer.SLOT_OUTPUT, KnownDimletConfiguration.makeKnownDimlet(timeDimlet, worldObj));
        return true;
    }

    private boolean attemptLiquidDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidLiquidEssence(stackEssence, stackEssence.getTagCompound())) {
            return false;
        }
        DimletKey liquidDimlet = findLiquidDimlet(stackEssence.getTagCompound());
        if (liquidDimlet == null) {
            return false;
        }
        if (!matchDimletRecipe(liquidDimlet, stackController, stackMemory, stackEnergy)) {
            return false;
        }
        inventoryHelper.setInventorySlotContents(1, DimletWorkbenchContainer.SLOT_OUTPUT, KnownDimletConfiguration.makeKnownDimlet(liquidDimlet, worldObj));
        return true;
    }

    private boolean attemptMaterialDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidMaterialEssence(stackEssence, stackEssence.getTagCompound())) {
            return false;
        }
        DimletKey materialDimlet = findMaterialDimlet(stackEssence.getTagCompound());
        if (materialDimlet == null) {
            return false;
        }
        if (!matchDimletRecipe(materialDimlet, stackController, stackMemory, stackEnergy)) {
            return false;
        }
        inventoryHelper.setInventorySlotContents(1, DimletWorkbenchContainer.SLOT_OUTPUT, KnownDimletConfiguration.makeKnownDimlet(materialDimlet, worldObj));
        return true;
    }

    private boolean attemptSpecialDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidSpecialEssence(stackEssence)) {
            return false;
        }
        DimletKey specialDimlet = findSpecialDimlet(stackEssence);
        if (specialDimlet == null) {
            return false;
        }
        if (!matchDimletRecipe(specialDimlet, stackController, stackMemory, stackEnergy)) {
            return false;
        }
        inventoryHelper.setInventorySlotContents(1, DimletWorkbenchContainer.SLOT_OUTPUT, KnownDimletConfiguration.makeKnownDimlet(specialDimlet, worldObj));
        return true;
    }

    private boolean attemptMobDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidMobEssence(stackEssence, stackEssence.getTagCompound())) {
            return false;
        }
        String mob = stackEssence.getTagCompound().getString("mobName");
        if (!matchDimletRecipe(new DimletKey(DimletType.DIMLET_MOBS, mob), stackController, stackMemory, stackEnergy)) {
            return false;
        }
        DimletKey mobDimlet = new DimletKey(DimletType.DIMLET_MOBS, mob);
        inventoryHelper.setInventorySlotContents(1, DimletWorkbenchContainer.SLOT_OUTPUT, KnownDimletConfiguration.makeKnownDimlet(mobDimlet, worldObj));
        return true;
    }

    private boolean attemptBiomeDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidBiomeEssence(stackEssence, stackEssence.getTagCompound())) {
            return false;
        }
        DimletKey biomeDimlet = findBiomeDimlet(stackEssence.getTagCompound());
        if (biomeDimlet == null) {
            return false;
        }
        if (!matchDimletRecipe(biomeDimlet, stackController, stackMemory, stackEnergy)) {
            return false;
        }
        inventoryHelper.setInventorySlotContents(1, DimletWorkbenchContainer.SLOT_OUTPUT, KnownDimletConfiguration.makeKnownDimlet(biomeDimlet, worldObj));
        return true;
    }

    public void craftDimlet() {
        inhibitCrafting++;
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_BASE, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_CONTROLLER, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_TYPE_CONTROLLER, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_ENERGY, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_MEMORY, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_ESSENCE, 1);
        inhibitCrafting--;
        checkCrafting();
    }

    private boolean matchDimletRecipe(DimletKey key, ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy) {
        DimletEntry dimletEntry = KnownDimletConfiguration.getEntry(key);
        int rarity = dimletEntry.getRarity();
        if (stackController.getItemDamage() != rarity) {
            return false;
        }
        int level = calculateItemLevelFromRarity(rarity);
        if (stackMemory.getItemDamage() != level) {
            return false;
        }
        if (stackEnergy.getItemDamage() != level) {
            return false;
        }

        return true;
    }

    private DimletKey findTimeDimlet(ItemStack stackEssence) {
        float angle = stackEssence.getTagCompound().getFloat("angle");
        return TimeAbsorberTileEntity.findBestTimeDimlet(angle);
    }

    private DimletKey findSpecialDimlet(ItemStack stackEssence) {
        if (stackEssence.getItem() == ModItems.peaceEssenceItem) {
            return new DimletKey(DimletType.DIMLET_SPECIAL, "Peaceful");
        } else if (stackEssence.getItem() == ModItems.efficiencyEssenceItem) {
            return new DimletKey(DimletType.DIMLET_SPECIAL, "Efficiency");
        } else if (stackEssence.getItem() == ModItems.mediocreEfficiencyEssenceItem) {
            return new DimletKey(DimletType.DIMLET_SPECIAL, "Mediocre Efficiency");
        }
        return null;
    }

    private DimletKey findBiomeDimlet(NBTTagCompound essenceCompound) {
        int biomeID = essenceCompound.getInteger("biome");
        for (Map.Entry<DimletKey, BiomeGenBase> entry : DimletObjectMapping.idToBiome.entrySet()) {
            if (entry.getValue().biomeID == biomeID) {
                return entry.getKey();
            }
        }
        return null;
    }

    private DimletKey findMaterialDimlet(NBTTagCompound essenceCompound) {
        int blockID = essenceCompound.getInteger("block");
        for (Map.Entry<DimletKey, BlockMeta> entry : DimletObjectMapping.idToBlock.entrySet()) {
            if (entry.getValue() != null) {
                int id = Block.blockRegistry.getIDForObject(entry.getValue().getBlock());
                if (blockID == id) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private DimletKey findLiquidDimlet(NBTTagCompound essenceCompound) {
        int blockID = essenceCompound.getInteger("liquid");
        for (Map.Entry<DimletKey, Block> entry : DimletObjectMapping.idToFluid.entrySet()) {
            if (entry.getValue() != null) {
                int id = Block.blockRegistry.getIDForObject(entry.getValue());
                if (blockID == id) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private boolean isValidBiomeEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        Block essenceBlock = getBlock(stackEssence);

        if (essenceBlock != ModBlocks.biomeAbsorberBlock) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int absorbing = essenceCompound.getInteger("absorbing");
        int biome = essenceCompound.getInteger("biome");
        if (absorbing > 0 || biome == -1) {
            return false;
        }
        return true;
    }

    private boolean isValidTimeEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        Block essenceBlock = getBlock(stackEssence);

        if (essenceBlock != ModBlocks.timeAbsorberBlock) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int absorbing = essenceCompound.getInteger("absorbing");
        float angle = essenceCompound.getFloat("angle");
        if (absorbing > 0 || angle < -0.01f) {
            return false;
        }
        return true;
    }

    private boolean isValidLiquidEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        Block essenceBlock = getBlock(stackEssence);

        if (essenceBlock != ModBlocks.liquidAbsorberBlock) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int absorbing = essenceCompound.getInteger("absorbing");
        int blockID = essenceCompound.getInteger("liquid");
        if (absorbing > 0 || blockID == -1) {
            return false;
        }
        return true;
    }

    private boolean isValidMaterialEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        Block essenceBlock = getBlock(stackEssence);

        if (essenceBlock != ModBlocks.materialAbsorberBlock) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int absorbing = essenceCompound.getInteger("absorbing");
        int blockID = essenceCompound.getInteger("block");
        if (absorbing > 0 || blockID == -1) {
            return false;
        }
        return true;
    }

    private Block getBlock(ItemStack stackEssence) {
        if (stackEssence.getItem() instanceof ItemBlock) {
            return ((ItemBlock) stackEssence.getItem()).field_150939_a;
        } else {
            return null;
        }
    }

    private boolean isValidMobEssence(ItemStack stackEssence, NBTTagCompound essenceCompound) {
        if (stackEssence.getItem() != ModItems.syringeItem) {
            return false;
        }
        if (essenceCompound == null) {
            return false;
        }
        int level = essenceCompound.getInteger("level");
        String mob = essenceCompound.getString("mobName");
        if (level < DimletConstructionConfiguration.maxMobInjections || mob == null) {
            return false;
        }
        return true;
    }

    private boolean isValidSpecialEssence(ItemStack stackEssence) {
        if (stackEssence.getItem() == ModItems.peaceEssenceItem) {
            return true;
        }
        if (stackEssence.getItem() == ModItems.efficiencyEssenceItem) {
            return true;
        }
        if (stackEssence.getItem() == ModItems.mediocreEfficiencyEssenceItem) {
            return true;
        }

        return false;
    }

    private void startExtracting() {
        if (extracting > 0) {
            // Already extracting
            return;
        }
        ItemStack stack = inventoryHelper.getStacks()[DimletWorkbenchContainer.SLOT_INPUT];
        if (stack != null) {
            if (ModItems.knownDimlet.equals(stack.getItem())) {
                DimletKey key = KnownDimletConfiguration.getDimletKey(stack, worldObj);
                if (!KnownDimletConfiguration.craftableDimlets.contains(key)) {
                    extracting = 64;
                    idToExtract = key;
                    inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_INPUT, 1);
                    markDirty();
                }
            }
        }
    }

    private boolean doExtract() {
        int rf = DimletConstructionConfiguration.rfExtractOperation;
        if (getEnergyStored(ForgeDirection.DOWN) < rf) {
            // Not enough energy.
            return false;
        }
        extractEnergy(ForgeDirection.DOWN, rf, false);

        float factor = getInfusedFactor();

        DimletEntry entry = KnownDimletConfiguration.getEntry(idToExtract);

        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(ModItems.dimletBaseItem));
        }

        int rarity = entry.getRarity();

        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(ModItems.dimletTypeControllerItem, 1, entry.getKey().getType().ordinal()));
        }

        int level = calculateItemLevelFromRarity(rarity);
        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(ModItems.dimletMemoryUnitItem, 1, level));
        } else {
            factor += 0.1f;     // If this failed we increase our chances a bit
        }

        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(ModItems.dimletEnergyModuleItem, 1, level));
        } else {
            factor += 0.1f;     // If this failed we increase our chances a bit
        }

        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(ModItems.dimletControlCircuitItem, 1, rarity));
        }

        idToExtract = null;
        markDirty();

        return true;
    }

    private int calculateItemLevelFromRarity(int rarity) {
        int level;
        if (rarity <= 1) {
            level = 0;
        } else if (rarity <= 3) {
            level = 1;
        } else {
            level = 2;
        }
        return level;
    }

    private boolean extractSuccess(float factor) {
        return worldObj.rand.nextFloat() <= (0.61f + factor * 0.4f);
    }

    private void mergeItemOrThrowInWorld(ItemStack stack) {
        int notInserted = inventoryHelper.mergeItemStack(this, stack, DimletWorkbenchContainer.SLOT_BUFFER, DimletWorkbenchContainer.SLOT_BUFFER + DimletWorkbenchContainer.SIZE_BUFFER, null);
        if (notInserted > 0) {
            BlockTools.spawnItemStack(worldObj, xCoord, yCoord, zCoord, stack);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        extracting = tagCompound.getInteger("extracting");
        idToExtract = null;
        if (tagCompound.hasKey("extDkey")) {
            DimletType type = DimletType.getTypeByOpcode(tagCompound.getString("extKtype"));
            idToExtract = new DimletKey(type, tagCompound.getString("extDkey"));
        } else {
            if (tagCompound.hasKey("idToExtract")) {
                // Compatibility with old system.
                int id = tagCompound.getInteger("idToExtract");
                if (id != -1) {
                    DimletMapping mapping = DimletMapping.getDimletMapping(worldObj);
                    idToExtract = mapping.getKey(id);
                }
            }
        }
        autoExtract = tagCompound.getBoolean("autoExtract");
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setInteger("extracting", extracting);
        if (idToExtract != null) {
            tagCompound.setString("extKtype", idToExtract.getType().getOpcode());
            tagCompound.setString("extDkey", idToExtract.getName());
        }
        tagCompound.setBoolean("autoExtract", autoExtract);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_STARTEXTRACT.equals(command)) {
            startExtracting();
            return true;
        } else if (CMD_SETAUTOEXTRACT.equals(command)) {
            autoExtract = args.get("auto").getBoolean();
            markDirty();
            return true;
        }
        return false;
    }

    // Request the extracting amount from the server. This has to be called on the client side.
    public void requestExtractingFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETEXTRACTING,
                CLIENTCMD_GETEXTRACTING));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETEXTRACTING.equals(command)) {
            return extracting;
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETEXTRACTING.equals(command)) {
            extracting = result;
            return true;
        }
        return false;
    }

}
