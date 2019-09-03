package mcjty.rftools.items.teleportprobe;

import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class AdvancedChargedPorterItem extends ChargedPorterItem {
    public static final int MAXTARGETS = 8;

    public AdvancedChargedPorterItem() {
        super("advanced_charged_porter", TeleportConfiguration.ADVANCED_CHARGEDPORTER_MAXENERGY.get());
    }

    @Override
    protected int getSpeedBonus() {
        return TeleportConfiguration.advancedSpeedBonus.get();
    }

    @Override
    protected void selectOnReceiver(PlayerEntity player, World world, CompoundNBT tagCompound, int id) {
        for (int i = 0 ; i < MAXTARGETS ; i++) {
            if (tagCompound.contains("target"+i) && tagCompound.getInt("target"+i) == id) {
                // Id is already there.
                Logging.message(player, TextFormatting.YELLOW + "Receiver " + id + " was already added to the charged porter.");
                return;
            }
        }

        for (int i = 0 ; i < MAXTARGETS ; i++) {
            if (!tagCompound.contains("target"+i)) {
                tagCompound.putInt("target"+i, id);
                if (world.isRemote) {
                    Logging.message(player, "Receiver " + id + " is added to the charged porter.");
                }
                if (!tagCompound.contains("target")) {
                    tagCompound.putInt("target", id);
                }
                return;
            }
        }

        if (world.isRemote) {
            Logging.message(player, TextFormatting.YELLOW + "Charged porter has no free targets!");
        }
    }

    @Override
    protected void selectReceiver(ItemStack stack, World world, PlayerEntity player) {
        if (world.isRemote) {
            // @todo 1.14
//            player.openGui(RFTools.instance, GuiProxy.GUI_ADVANCEDPORTER, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
        }
    }

    @Override
    protected void selectOnThinAir(PlayerEntity player, World world, CompoundNBT tagCompound, ItemStack stack) {
        selectReceiver(stack, world, player);
    }
}
