package mcjty.rftools.items.teleportprobe;

import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AdvancedChargedPorterItem extends ChargedPorterItem {
    public static final int MAXTARGETS = 4;

    public AdvancedChargedPorterItem() {
        super();
        capacity = TeleportConfiguration.ADVANCED_CHARGEDPORTER_MAXENERGY;
    }

    @Override
    protected void setup() {
        setUnlocalizedName("advanced_charged_porter");
        setRegistryName("advanced_charged_porter");
        setCreativeTab(RFTools.tabRfTools);
        GameRegistry.register(this);
    }

    @Override
    protected int getSpeedBonus() {
        return TeleportConfiguration.advancedSpeedBonus;
    }

    @Override
    protected void selectOnReceiver(EntityPlayer player, World world, NBTTagCompound tagCompound, int id) {
        for (int i = 0 ; i < MAXTARGETS ; i++) {
            if (tagCompound.hasKey("target"+i) && tagCompound.getInteger("target"+i) == id) {
                // Id is already there.
                Logging.message(player, TextFormatting.YELLOW + "Receiver " + id + " was already added to the charged porter.");
                return;
            }
        }

        for (int i = 0 ; i < MAXTARGETS ; i++) {
            if (!tagCompound.hasKey("target"+i)) {
                tagCompound.setInteger("target"+i, id);
                if (world.isRemote) {
                    Logging.message(player, "Receiver " + id + " is added to the charged porter.");
                }
                if (!tagCompound.hasKey("target")) {
                    tagCompound.setInteger("target", id);
                }
                return;
            }
        }

        if (world.isRemote) {
            Logging.message(player, TextFormatting.YELLOW + "Charged porter has no free targets!");
        }
    }

    @Override
    protected void selectReceiver(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_ADVANCEDPORTER, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

    @Override
    protected void selectOnThinAir(EntityPlayer player, World world, NBTTagCompound tagCompound, ItemStack stack) {
        selectReceiver(stack, world, player);
    }
}
