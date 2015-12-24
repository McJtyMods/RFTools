package mcjty.rftools.blocks.storage;

import mcjty.lib.network.clientinfo.PacketGetInfoFromServer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import mcjty.rftools.network.RFToolsMessages;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

import static mcjty.rftools.blocks.storage.ModularTypeModule.*;

public class ModularStorageBlock extends GenericRFToolsBlock {

    public static final PropertyEnum<ModularTypeModule> TYPEMODULE = PropertyEnum.create("type", ModularTypeModule.class);

    public ModularStorageBlock() {
        super(Material.iron, ModularStorageTileEntity.class, ModularStorageContainer.class, GuiModularStorage.class, "modular_storage", true);
    }

    @Override
    public void initModel() {
        // @@@ temporary
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(RFTools.MODID + ":" + getUnlocalizedName().substring(5), "inventory"));
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MODULAR_STORAGE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "This modular storage system can store a lot");
            list.add(EnumChatFormatting.WHITE + "of items and allows easy searching and filtering.");
            list.add(EnumChatFormatting.WHITE + "You must first insert a storage module item before");
            list.add(EnumChatFormatting.WHITE + "you can use it");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    private static long lastTime = 0;

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        ModularStorageTileEntity te = (ModularStorageTileEntity) world.getTileEntity(pos);
        ItemStack stack = te.getInventoryHelper().getStackInSlot(ModularStorageContainer.SLOT_TYPE_MODULE);
        if (stack == null) {
            return state.withProperty(TYPEMODULE, TYPE_NONE);
        } else if (stack.getItem() == ModularStorageSetup.genericTypeItem) {
            return state.withProperty(TYPEMODULE, TYPE_GENERIC);
        } else if (stack.getItem() == ModularStorageSetup.oreDictTypeItem) {
            return state.withProperty(TYPEMODULE, TYPE_ORE);
        }
        return state.withProperty(TYPEMODULE, TYPE_NONE);
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, TYPEMODULE);
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return layer == EnumWorldBlockLayer.SOLID || layer == EnumWorldBlockLayer.CUTOUT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof ModularStorageTileEntity) {
            ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
            int maxSize = modularStorageTileEntity.getMaxSize();
            if (maxSize == 0) {
                currenttip.add(EnumChatFormatting.YELLOW + "No storage module!");
            } else {
                if (System.currentTimeMillis() - lastTime > 500) {
                    lastTime = System.currentTimeMillis();
                    RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new StorageInfoPacketServer(modularStorageTileEntity.getWorld().provider.getDimensionId(),
                            modularStorageTileEntity.getPos())));
                }
                int stacks = StorageInfoPacketClient.cntReceived;
                if (stacks == -1) {
                    currenttip.add(EnumChatFormatting.YELLOW + "Maximum size: " + maxSize);
                } else {
                    currenttip.add(EnumChatFormatting.GREEN + "" + stacks + " out of " + maxSize);
                }
            }
        }
        return currenttip;
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        // Make sure the client has sufficient information to show the data.
        ((ModularStorageTileEntity) tileEntity).markDirtyClient();
        return super.createServerContainer(entityPlayer, tileEntity);
    }
}
