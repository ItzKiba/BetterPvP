package me.mykindos.betterpvp.clans.champions.skills.types;

import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.skills.config.SkillConfigFactory;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillWeapons;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.cooldowns.Cooldown;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import me.mykindos.betterpvp.core.utilities.UtilPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public abstract class PrepareArrowSkill extends PrepareSkill implements CooldownSkill {

    protected final List<Arrow> arrows = new ArrayList<>();

    public PrepareArrowSkill(Clans clans, ChampionsManager championsManager, SkillConfigFactory configFactory) {
        super(clans, championsManager, configFactory);
    }

    @EventHandler
    public void onHit(CustomDamageEvent e) {
        if (!(e.getProjectile() instanceof Arrow arrow)) return;
        if (!(e.getDamagee() instanceof Player damagee)) return;
        if (!(e.getDamager() instanceof Player damager)) return;
        if (!arrows.contains(arrow)) return;

        int level = getLevel(damager);
        if (level > 0) {

            onHit(damager, damagee, level);
            arrows.remove(arrow);
            e.setReason(getName());

        }

    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!active.contains(player.getUniqueId())) return;
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        int level = getLevel(player);
        if (level > 0) {
            arrows.add(arrow);
            active.remove(player.getUniqueId());

            championsManager.getCooldowns().removeCooldown(player, getName(), true);
            championsManager.getCooldowns().add(player, getName(), getCooldown(level), showCooldownFinished(), false);
        }
    }

    @UpdateEvent
    public void updateParticle() {
        Iterator<Arrow> it = arrows.iterator();
        while (it.hasNext()) {
            Arrow next = it.next();
            if (next == null) {
                it.remove();
            } else if (next.isDead()) {
                it.remove();
            } else {
                Location loc = next.getLocation().add(new Vector(0, 0.25, 0));
                displayTrail(loc);

            }
        }
    }

    @UpdateEvent(delay = 250)
    public void onCheckCancel() {
        Iterator<UUID> it = active.iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                it.remove();
                continue;
            }

            int level = getLevel(player);
            if (level <= 0) {
                it.remove();
                continue;
            }

            if (!UtilPlayer.isHoldingItem(player, SkillWeapons.BOWS)) {
                Cooldown cooldown = championsManager.getCooldowns().getAbilityRecharge(player, getName());
                if (cooldown != null) {
                    if (cooldown.isCancellable()) {
                        championsManager.getCooldowns().removeCooldown(player, getName(), true);
                        UtilMessage.message(player, getClassType().getName(),  "%s was cancelled.",
                                ChatColor.GREEN + getName() + " " + level + ChatColor.GRAY);
                        it.remove();
                    }
                }

            }

        }

    }

    @UpdateEvent(delay = 1000)
    public void update() {
        arrows.removeIf(Entity::isDead);

    }

    @Override
    public boolean isCancellable() {
        return true;
    }

    public abstract void onHit(Player damager, LivingEntity target, int level);

    public abstract void displayTrail(Location location);

}