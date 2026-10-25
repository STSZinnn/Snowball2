package com.example.punchsnowball;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

public final class PunchSnowballPlugin extends JavaPlugin implements Listener {
    private NamespacedKey specialKey;
    private static final int COOLDOWN_TICKS = 140; // 7 seconds
    private static final int SLOWNESS_TICKS = 40; // 2 seconds
    private static final double KNOCKBACK_HORIZONTAL = 1.9;
    private static final double KNOCKBACK_VERTICAL = 0.38;

    @Override
    public void onEnable() {
        specialKey = new NamespacedKey(this, "punch_snowball");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private ItemStack makeSnowball() {
        ItemStack item = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Punch Snowball");
        meta.setLore(List.of(ChatColor.GRAY + "Infinite throws", ChatColor.GRAY + "7-second cooldown",
                ChatColor.LIGHT_PURPLE + "Strong knockback + Slowness II (2s)"));
        meta.getPersistentDataContainer().set(specialKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isSpecial(ItemStack item) {
        return item != null && item.getType() == Material.SNOWBALL && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer()
                .has(specialKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onThrow(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof Snowball snowball)) return;
        if (!isSpecial(event.getItemStack())) return;

        Player player = event.getPlayer();
        if (player.hasCooldown(Material.SNOWBALL)) {
            event.setCancelled(true);
            return;
        }

        snowball.getPersistentDataContainer().set(specialKey, PersistentDataType.BYTE, (byte) 1);
        event.setShouldConsume(false);
        player.setCooldown(Material.SNOWBALL, COOLDOWN_TICKS);
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Snowball)) return;
        if (!projectile.getPersistentDataContainer().has(specialKey, PersistentDataType.BYTE)) return;
        if (!(event.getHitEntity() instanceof Player target)) return;
        if (projectile.getShooter() instanceof Player shooter && shooter.equals(target)) return;

        Vector direction = projectile.getVelocity().clone().setY(0);
        if (direction.lengthSquared() < 0.0001) {
            if (projectile.getShooter() instanceof Player shooter) {
                direction = target.getLocation().toVector()
                        .subtract(shooter.getLocation().toVector()).setY(0);
            }
        }
        if (direction.lengthSquared() < 0.0001) direction = new Vector(0, 0, 1);

        // Velocity-based knockback, targeted at roughly 5 blocks on level ground.
        // Actual distance varies with friction, obstacles, and server settings.
        direction.normalize().multiply(KNOCKBACK_HORIZONTAL).setY(KNOCKBACK_VERTICAL);
        target.setVelocity(direction);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                SLOWNESS_TICKS, 1, false, true, true));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("givesnowball")) return false;
        if (args.length > 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /givesnowball [player]");
            return true;
        }
        Player target;
        if (args.length == 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(ChatColor.RED + "Console usage: /givesnowball <player>");
            return true;
        }
        target.getInventory().addItem(makeSnowball()).values().forEach(
                leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        sender.sendMessage(ChatColor.GREEN + "Gave Punch Snowball to " + target.getName());
        return true;
    }
}
