package mcjty.rftools.blocks.security;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.rftools.blocks.security.SecuritySetup.TYPE_SECURITY_MANAGER;

public class SecurityManagerTileEntity extends GenericTileEntity {

    public static final String CMD_SETCHANNELNAME = "security.setChannelName";
    public static final Key<String> PARAM_NAME = new Key<>("name", Type.STRING);

    public static final String CMD_SETMODE = "security.setMode";
    public static final Key<Boolean> PARAM_WHITELIST = new Key<>("whitelist", Type.BOOLEAN);

    public static final String CMD_ADDPLAYER = "security.addPlayer";
    public static final String CMD_DELPLAYER = "security.delPlayer";
    public static final Key<String> PARAM_PLAYER = new Key<>("player", Type.STRING);

    public static final int SLOT_CARD = 0;
    public static final int SLOT_LINKER = 1;
    public static final int SLOT_BUFFER = 2;
    public static final int BUFFER_SIZE = (3*4);
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(BUFFER_SIZE + 2) {
        @Override
        protected void setup() {
            slot(SlotDefinition.specific(new ItemStack(SecuritySetup.securityCardItem)), CONTAINER_CONTAINER, SLOT_CARD, 10, 7);
            slot(SlotDefinition.specific(new ItemStack(SecuritySetup.securityCardItem)), CONTAINER_CONTAINER, SLOT_LINKER, 42, 7);
            box(SlotDefinition.specific(new ItemStack(SecuritySetup.securityCardItem)), CONTAINER_CONTAINER, SLOT_BUFFER, 10, 124, 3, 4);
            playerSlots(74, 124);
        }
    };

    public static final int SLOT_PLAYERINV = SLOT_CARD + BUFFER_SIZE + 2;

    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);

    public SecurityManagerTileEntity() {
        super(TYPE_SECURITY_MANAGER);
    }

    private void updateCard(ItemStack cardStack) {
        if (world.isRemote) {
            return;
        }
        if (cardStack.isEmpty()) {
            return;
        }
        CompoundNBT tagCompound = cardStack.getOrCreateTag();
        if (!tagCompound.contains("channel")) {
            SecurityChannels securityChannels = SecurityChannels.get();
            int id = securityChannels.newChannel();
            tagCompound.putInt("channel", id);
            securityChannels.save();
            markDirtyClient();
        }
    }

    private void updateLinkedCard() {
        if (world.isRemote) {
            return;
        }
        itemHandler.ifPresent(h -> {
            ItemStack masterCard = h.getStackInSlot(SLOT_CARD);
            if (masterCard.isEmpty()) {
                return;
            }
            ItemStack linkerCard = h.getStackInSlot(SLOT_LINKER);
            if (linkerCard.isEmpty()) {
                return;
            }

            CompoundNBT masterNBT = masterCard.getTag();
            if (masterNBT == null) {
                return;
            }
            CompoundNBT linkerNBT = linkerCard.getOrCreateTag();
            linkerNBT.putInt("channel", masterNBT.getInt("channel"));
            markDirtyClient();
        });
    }

    private void addPlayer(String player) {
        getCardInfo().ifPresent(tagCompound -> {
            if (tagCompound.contains("channel")) {
                SecurityChannels securityChannels = SecurityChannels.get();
                int id = tagCompound.getInt("channel");
                SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
                channel.addPlayer(player);
                securityChannels.save();
                markDirtyClient();
            }
        });
    }

    private void delPlayer(String player) {
        getCardInfo().ifPresent(tagCompound -> {
            if (tagCompound.contains("channel")) {
                SecurityChannels securityChannels = SecurityChannels.get();
                int id = tagCompound.getInt("channel");
                SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
                channel.delPlayer(player);
                securityChannels.save();
                markDirtyClient();
            }
        });
    }

    private void setWhiteListMode(boolean whitelist) {
        getCardInfo().ifPresent(tagCompound -> {
            if (tagCompound.contains("channel")) {
                SecurityChannels securityChannels = SecurityChannels.get();
                int id = tagCompound.getInt("channel");
                SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
                channel.setWhitelist(whitelist);
                securityChannels.save();
                markDirtyClient();
            }
        });
    }

    private void setChannelName(String name) {
        getCardInfo().ifPresent(tagCompound -> {
            if (tagCompound.contains("channel")) {
                SecurityChannels securityChannels = SecurityChannels.get();
                int id = tagCompound.getInt("channel");
                SecurityChannels.SecurityChannel channel = securityChannels.getOrCreateChannel(id);
                channel.setName(name);
                securityChannels.save();
                markDirtyClient();
            }
        });
    }

    private LazyOptional<CompoundNBT> getCardInfo() {
        return itemHandler.map(h -> h.getStackInSlot(SLOT_CARD)).filter(s -> !s.isEmpty()).map(s -> s.getOrCreateTag());
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETCHANNELNAME.equals(command)) {
            setChannelName(params.get(PARAM_NAME));
            return true;
        } else if (CMD_SETMODE.equals(command)) {
            setWhiteListMode(params.get(PARAM_WHITELIST));
            return true;
        } else if (CMD_ADDPLAYER.equals(command)) {
            addPlayer(params.get(PARAM_PLAYER));
            return true;
        } else if (CMD_DELPLAYER.equals(command)) {
            delPlayer(params.get(PARAM_PLAYER));
            return true;
        }
        return false;
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(SecurityManagerTileEntity.this, CONTAINER_FACTORY) {
            @Override
            protected void onUpdate(int index) {
                super.onUpdate(index);
                if (index == SLOT_CARD) {
                    updateCard(getStackInSlot(index));
                    updateLinkedCard();
                } else if (index == SLOT_LINKER) {
                    updateLinkedCard();
                }
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
//        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
//            return screenHandler.cast();
//        }
        return super.getCapability(cap, facing);
    }
}
