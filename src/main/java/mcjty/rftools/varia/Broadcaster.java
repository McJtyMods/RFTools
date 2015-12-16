package mcjty.rftools.varia;

import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class Broadcaster {
    private static Map<String,Long> messages = new HashMap<String, Long>();

    public static void broadcast(World worldObj, int x, int y, int z, String message, float radius) {
        long time = System.currentTimeMillis();
        if (messages.containsKey(message)) {
            long t = messages.get(message);
            if ((time - t) > 2000) {
                messages.remove(message);
            } else {
                return;
            }
        }
        messages.put(message, time);
        for (Object p : worldObj.playerEntities) {
            EntityPlayer player = (EntityPlayer) p;
            double sqdist = player.getDistanceSq(x + .5, y + .5, z + .5);
            if (sqdist < radius) {
                Logging.warn(player, message);
            }
        }
    }
}
