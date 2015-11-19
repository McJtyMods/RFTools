package mcjty.rftools.commands;

import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.DimensionStorage;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.rftools.dimension.description.DimensionDescriptor;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Map;

public class CmdListDimensions extends AbstractRfToolsCommand {
    @Override
    public String getHelp() {
        return "";
    }

    @Override
    public String getCommand() {
        return "list";
    }

    @Override
    public int getPermissionLevel() {
        return 0;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public void execute(ICommandSender sender, String[] args) {
        WorldServer[] worlds = DimensionManager.getWorlds();
        for (WorldServer world : worlds) {
            int id = world.provider.dimensionId;
            String dimName = world.provider.getDimensionName();
            sender.addChatMessage(new ChatComponentText("    Loaded: id:" + id + ", " + dimName));
        }

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(sender.getEntityWorld());
        DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(sender.getEntityWorld());
        for (Map.Entry<Integer,DimensionDescriptor> me : dimensionManager.getDimensions().entrySet()) {
            int id = me.getKey();
            DimensionInformation dimensionInformation = dimensionManager.getDimensionInformation(id);
            String dimName = dimensionInformation.getName();
            int energy = dimensionStorage.getEnergyLevel(id);
            String ownerName = dimensionInformation.getOwnerName();
            if (ownerName != null && !ownerName.isEmpty()) {
                sender.addChatMessage(new ChatComponentText("    RfTools: id:" + id + ", " + dimName + " (power " + energy + ") (owner " + ownerName + ")"));
            } else {
                sender.addChatMessage(new ChatComponentText("    RfTools: id:" + id + ", " + dimName + " (power " + energy + ")"));
            }
        }
    }
}
