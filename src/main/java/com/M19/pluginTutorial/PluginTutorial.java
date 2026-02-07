package com.M19.pluginTutorial;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.List;


public final class PluginTutorial extends JavaPlugin implements Listener {
    // Custom launcher item names and projectile behavior tuning.
    private static final String EGG_LAUNCHER_NAME = "Egg Launcher";
    private static final String SNOWBALL_LAUNCHER_NAME = "SnowBall Launcher";
    private static final double IRON_DAMAGE = 4.0;
    private static final double DIAMOND_DAMAGE = 6.0;
    private static final int FLAME_COUNT = 12;
    private static final double FLAME_OFFSET = 0.15;
    private static final double FLAME_SPEED = 0.02;

    @Override
    public void onEnable() {
        // Register this class as an event listener when the plugin starts.
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    // Track only projectiles fired from our custom items so we can apply extra effects.
    public List<Egg> specialEggs = new ArrayList<>();
    public List<Snowball> specialSnowballs = new ArrayList<>();




    // Prebuilt launcher items handed to players on join.
    public ItemStack IronHoe = new ItemStack(Material.IRON_HOE);
    public ItemStack DiamondHoe = new ItemStack(Material.DIAMOND_HOE);


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        // Only react to main-hand right-clicks with a named item.
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = e.getAction();
        // Ignore left-clicks and physical actions.
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Avoid NPEs on empty hands or items without metadata.
        if (!e.hasItem()) {
            return;
        }

        ItemMeta meta = e.getItem().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        Player player = e.getPlayer();
        // Use the display name to identify our custom launcher items.
        String displayName = meta.getDisplayName();

        if (displayName.equals(EGG_LAUNCHER_NAME)) {
            Egg EggProjectile = player.launchProjectile(Egg.class);
            specialEggs.add(EggProjectile);
            // Visual feedback for a "special" projectile.
            startFlameTrail(EggProjectile);
        } else if (displayName.equals(SNOWBALL_LAUNCHER_NAME)) {
            Snowball SnowBallProjectile = player.launchProjectile(Snowball.class);
            specialSnowballs.add(SnowBallProjectile);
            // Visual feedback for a "special" projectile.
            startFlameTrail(SnowBallProjectile);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        // Only apply custom damage to projectiles launched by our items.
        if (e.getEntity() instanceof Egg) {
            Egg egg = (Egg) e.getEntity();
            if (specialEggs.remove(egg)) {
                applyHoeDamage(egg, DIAMOND_DAMAGE, e);
            }
            // Return so an egg is not also processed by later snowball logic.
            return;
        }

        if (e.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) e.getEntity();
            if (specialSnowballs.remove(snowball)) {
                applyHoeDamage(snowball, IRON_DAMAGE, e);
            }
        }
    }

    private void startFlameTrail(Projectile projectile) {
        // Spawn a short-lived flame particle trail while the projectile is in flight.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (projectile.isDead() || !projectile.isValid()) {
                    // Cleanup: remove stale entries so lists do not grow forever.
                    if (projectile instanceof Egg) {
                        specialEggs.remove(projectile);
                    } else if (projectile instanceof Snowball) {
                        specialSnowballs.remove(projectile);
                    }
                    cancel();
                    return;
                }

                projectile.getWorld().spawnParticle(
                        Particle.FLAME,
                        projectile.getLocation(),
                        FLAME_COUNT,
                        FLAME_OFFSET,
                        FLAME_OFFSET,
                        FLAME_OFFSET,
                        FLAME_SPEED
                );
            }
        // Run every tick for a smooth trail (period = 1 tick).
        }.runTaskTimer(this, 0L, 1L);
    }

    private void applyHoeDamage(Projectile projectile, double damage, ProjectileHitEvent e) {
        // Apply damage only if we actually hit a living entity.
        if (!(e.getHitEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity target = (LivingEntity) e.getHitEntity();
        ProjectileSource shooter = projectile.getShooter();
        // Attribute damage to the shooter if possible (for combat log, aggro, etc.).
        if (shooter instanceof Entity) {
            target.damage(damage, (Entity) shooter);
        } else {
            target.damage(damage);
        }
    }

    @EventHandler
    public void onPlayerJoin (PlayerJoinEvent e){
        // Give each joining player the two launcher items with custom names.
        ItemMeta IroHoe = IronHoe.getItemMeta();
        ItemMeta DiaHoe = DiamondHoe.getItemMeta();


        IroHoe.setDisplayName(SNOWBALL_LAUNCHER_NAME);
        DiaHoe.setDisplayName(EGG_LAUNCHER_NAME);

        IronHoe.setItemMeta(IroHoe);
        DiamondHoe.setItemMeta(DiaHoe);

        e.getPlayer().getInventory().addItem(IronHoe);
        e.getPlayer().getInventory().addItem(DiamondHoe);
    }
}
