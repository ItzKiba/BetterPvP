package me.mykindos.betterpvp.core.combat.adapters;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import me.mykindos.betterpvp.core.Core;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.framework.adapter.PluginAdapter;
import me.mykindos.betterpvp.core.framework.events.ServerStartEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilFormat;
import me.mykindos.betterpvp.core.utilities.UtilServer;
import me.mykindos.betterpvp.core.utilities.events.FetchNearbyEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Iterator;

@PluginAdapter("MythicMobs")
@Singleton
@BPvPListener
public class MythicMobsDamageAdapter implements Listener {

    private final Core core;

    @Inject
    public MythicMobsDamageAdapter(Core core) {
        this.core = core;
    }

    @EventHandler
    public void onServerStart(ServerStartEvent event) { // Cleanse fresh loaded worlds of any mythic mobs
        UtilServer.runTaskLater(core, () -> {
            Iterator<ActiveMob> iterator = MythicBukkit.inst().getMobManager().getMobRegistry().values().iterator();
            while (iterator.hasNext()) {
                ActiveMob am = iterator.next();
                am.setDespawned();
                MythicBukkit.inst().getMobManager().unregisterActiveMob(am);
                if (am.getEntity() != null) {
                    am.getEntity().remove();
                }
                iterator.remove();

            }

        }, 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void processDamageModifiers(CustomDamageEvent event) {
        var mobManager = MythicBukkit.inst().getMobManager();
        ActiveMob damagee = mobManager.getActiveMob(event.getDamagee().getUniqueId()).orElse(null);
        if (damagee != null) {
            if (damagee.getType().getDamageModifiers().containsKey(event.getCause().name())) {
                event.setDamage(event.getDamage() * damagee.getType().getDamageModifiers().get(event.getCause().name()));

                if (event.getDamager() instanceof Player player) {
                    if (damagee.getType().usesThreatTable()) {
                        AbstractPlayer mmPlayer = BukkitAdapter.adapt(player);
                        damagee.getThreatTable().threatGain(mmPlayer, event.getDamage());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFetchEntity(FetchNearbyEntityEvent<?> event) {
        event.getEntities().removeIf(entity -> UtilFormat.stripColor(entity.getKey().getName()).isEmpty());
    }

}
