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
    //diamond hoe = egg launcher  iron hoe = snowball launcher
    private static final String EGG_LAUNCHER_NAME = "Egg Launcher";
    private static final String SNOWBALL_LAUNCHER_NAME = "SnowBall Launcher";
    private static final double IRON_DAMAGE = 4.0;
    private static final double DIAMOND_DAMAGE = 6.0;
    private static final int FLAME_COUNT = 12;
    private static final double FLAME_OFFSET = 0.15;
    private static final double FLAME_SPEED = 0.02;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public List<Egg> specialEggs = new ArrayList<>();
    public List<Snowball> specialSnowballs = new ArrayList<>();




    public ItemStack IronHoe = new ItemStack(Material.IRON_HOE);
    public ItemStack DiamondHoe = new ItemStack(Material.DIAMOND_HOE);


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!e.hasItem()) {
            return;
        }

        ItemMeta meta = e.getItem().getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        Player player = e.getPlayer();
        String displayName = meta.getDisplayName();

        if (displayName.equals(EGG_LAUNCHER_NAME)) {
            Egg myEgg = player.launchProjectile(Egg.class);
            specialEggs.add(myEgg);
            startFlameTrail(myEgg);
        } else if (displayName.equals(SNOWBALL_LAUNCHER_NAME)) {
            Snowball mySnowBall = player.launchProjectile(Snowball.class);
            specialSnowballs.add(mySnowBall);
            startFlameTrail(mySnowBall);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Egg) {
            Egg egg = (Egg) e.getEntity();
            if (specialEggs.remove(egg)) {
                applyHoeDamage(egg, DIAMOND_DAMAGE, e);
            }
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
        new BukkitRunnable() {
            @Override
            public void run() {
                if (projectile.isDead() || !projectile.isValid()) {
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
        }.runTaskTimer(this, 0L, 1L);
    }

    private void applyHoeDamage(Projectile projectile, double damage, ProjectileHitEvent e) {
        if (!(e.getHitEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity target = (LivingEntity) e.getHitEntity();
        ProjectileSource shooter = projectile.getShooter();
        if (shooter instanceof Entity) {
            target.damage(damage, (Entity) shooter);
        } else {
            target.damage(damage);
        }
    }

    @EventHandler
    public void onPlayerJoin (PlayerJoinEvent e){
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
