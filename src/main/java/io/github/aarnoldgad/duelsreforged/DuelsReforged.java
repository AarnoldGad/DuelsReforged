package io.github.aarnoldgad.duelsreforged;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("unused")
public class DuelsReforged extends JavaPlugin implements Listener
{
   public static final String NAME = "Duels Reforged";
   public static final String VERSION = "1.0_REV9_B";

   private List<Duel> duels = new ArrayList<>();

   @Override
   public void onEnable()
   {
      getServer().getScheduler().runTaskTimer(this, this::duelScheduler, 1, 20);
      getServer().getPluginManager().registerEvents(this, this);
      getCommand("duel").setTabCompleter(this);
      getLogger().info("Thanks for using Duels Reforged by The Aarnold Project");
   }

   @Override
   public void onDisable()
   {
      getLogger().info("Goodbye Duels Reforged");
   }

   @Override
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
   {
      boolean isPlayer = (sender instanceof Player);

      if (args.length > 0)
      {
         if (isPlayer)
         {
            Duel duel;
            String firstArgument = args[0].toLowerCase();
            switch (firstArgument)
            {
               case "challenge":
                  if (args.length > 1)
                     tryChallengePlayer((Player) sender, getServer().getPlayerExact(args[1]));
                  else
                     sender.sendMessage(ChatColor.RED + "Veuillez préciser le nom d'un joueur");
                  return true;
               case "accept":
                  duel = tryGetDuel((Player) sender);
                  if (duel != null && duel.getChallenged().equals(sender))
                     duel.engage();
                  return true;
               case "deny":
                  duel = tryGetDuel((Player) sender);
                  if (duel != null && !duel.isEngaged())
                     cancelDuelIfExists((Player) sender);
                  return true;
            }
         }
      }
      return false;
   }

   @Nullable
   @Override
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
   {
      if (label.equals("duel"))
      {
         if (args.length == 1)
         {
            List<String> completion = new ArrayList<>();
            List<String> commands = new ArrayList<>();
            commands.add("challenge");
            commands.add("accept");
            commands.add("deny");
            StringUtil.copyPartialMatches(args[0], commands, completion);
            return completion;
         }
      }

      return Collections.emptyList();
   }

   private void duelScheduler()
   {
      Iterator<Duel> it = duels.iterator();
      while (it.hasNext())
      {
         Duel duel = it.next();
         duel.elapseASecond();

         Player p1 = duel.getChallenger();
         Player p2 = duel.getChallenged();

         if (!p1.getWorld().equals(p2.getWorld()))
         {
            duel.getChallenger().sendMessage(ChatColor.RED + "Vous ou votre adversaire a fuit !");
            duel.getChallenged().sendMessage(ChatColor.RED + "Vous ou votre adversaire a fuit !");
            it.remove();
         }
         else if (duel.isEngaged())
         {
            switch (duel.getElapsedTime())
            {
               case 1:
                  p1.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.DARK_AQUA + "5");
                  p2.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.DARK_AQUA + "5");

                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_PURR, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_PURR, 1f, 1f);
                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  break;
               case 2:
                  p1.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.AQUA + "4");
                  p2.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.AQUA + "4");

                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  break;
               case 3:
                  p1.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.GREEN + "3");
                  p2.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.GREEN + "3");

                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  break;
               case 4:
                  p1.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.DARK_GREEN + "2");
                  p2.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.DARK_GREEN + "2");

                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_PURR, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_PURR, 1f, 1f);
                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  break;
               case 5:
                  p1.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.YELLOW + "1");
                  p2.sendMessage(ChatColor.GOLD + "Combat dans " + ChatColor.YELLOW + "1");

                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1f);
                  break;
               case 6:
                  p1.sendMessage(ChatColor.RED + "Combat !");
                  p2.sendMessage(ChatColor.RED + "Combat !");

                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_HISS, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_HISS, 1f, 1f);
                  break;
               case 600:
                  p1.sendMessage(ChatColor.GOLD + "Match nul après 10mn !");
                  p2.sendMessage(ChatColor.GOLD + "Match nul après 10mn !");

                  p1.getWorld().playSound(p1.getLocation(), Sound.ENTITY_CAT_DEATH, 1f, 1f);
                  p2.getWorld().playSound(p2.getLocation(), Sound.ENTITY_CAT_DEATH, 1f, 1f);
                  it.remove();
                  break;
               default:
                  break;
            }
         }
         else if (duel.getElapsedTime() == 30)
         {
            p1.sendMessage(ChatColor.RED + "Défi contre " + p2.getDisplayName() + ChatColor.RED + " expiré !");
            p2.sendMessage(ChatColor.RED + "Défi de " + p1.getDisplayName() + ChatColor.RED + " expiré !");
            it.remove();
         }
      }
   }

   private boolean isDueling(@NotNull Player player)
   {
      for (Duel duel : duels)
         if (duel.isImplied(player))
            return true;
      return false;
   }

   @Nullable
   private Duel tryGetDuel(@NotNull Player player)
   {
      for (Duel duel : duels)
         if (duel.isImplied(player))
            return duel;
      return null;
   }

   private boolean isEngagedDuelBetween(Player p1, Player p2)
   {
      for (Duel duel : duels)
         if (duel.isImplied(p1) && duel.isImplied(p2) && duel.isEngaged() && duel.getElapsedTime() > 5)
            return true;
      return false;
   }

   private void tryChallengePlayer(Player challenger, Player challenged)
   {
      if      (challenged == null)                                   challenger.sendMessage(ChatColor.RED + "Ce joueur n'est pas connecté !");
      else if (challenger.equals(challenged))                        challenger.sendMessage(ChatColor.GOLD + "Mais c'est vous !");
      else if (isDueling(challenged))                                challenger.sendMessage(challenged.getDisplayName() + ChatColor.RED + " est déjà en combat ou a reçu un défi !");
      else if (isDueling(challenger))                                challenger.sendMessage(ChatColor.RED + "Vous êtes déjà en combat ou avez reçu un défi !");
      else if (!challenger.getWorld().equals(challenged.getWorld())) challenger.sendMessage(ChatColor.RED + "Vous n'êtes pas dans le même monde !");
      else
      {
         challenger.sendMessage(ChatColor.GOLD + "Vous venez de défier " + challenged.getDisplayName());
         challenged.sendMessage(challenger.getDisplayName() + ChatColor.GOLD + " vous défi en duel ! Sneak + Clique Droit avec une arme sur lui pour accepter !");
         duels.add(new Duel(challenger, challenged));
      }
   }

   private void cancelDuelIfExists(Player player)
   {
      Duel duel = tryGetDuel(player);
      if (duel != null)
      {
         if (duel.isEngaged())
         {
            duel.getChallenger().sendMessage(ChatColor.RED + (duel.getChallenger().equals(player) ? "Vous fuyez !" : "Votre adversaire fuit !"));
            duel.getChallenged().sendMessage(ChatColor.RED + (duel.getChallenged().equals(player) ? "Vous fuyez !" : "Votre adversaire fuit !"));
         }
         else
         {
            duel.getChallenger().sendMessage(ChatColor.RED + "Combat refusé !");
            duel.getChallenged().sendMessage(ChatColor.RED + "Combat refusé !");
         }

         duels.remove(duel);
      }
   }

   @EventHandler
   private void cMartinKaDi(PlayerInteractEntityEvent event)
   {
      if (event.getRightClicked().getType() == EntityType.PLAYER
          && (event.getPlayer().getInventory().getItemInMainHand().getType().name().toLowerCase().contains("sword")
           || event.getPlayer().getInventory().getItemInMainHand().getType().name().toLowerCase().contains("axe"))
          && event.getPlayer().isSneaking() && event.getHand().equals(EquipmentSlot.HAND))
      {
         Duel duel = tryGetDuel(event.getPlayer());
         if (duel != null && duel.getChallenger().equals(event.getRightClicked()))
            duel.engage();
         else
            tryChallengePlayer(event.getPlayer(), (Player) event.getRightClicked());
      }
   }

   private void finishDuel(Player defeated, Player victorious)
   {
      defeated.setHealth(defeated.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
      victorious.setHealth(victorious.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

      ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
      SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
      if (meta != null)
      {
         meta.setDisplayName(defeated.getDisplayName());
         meta.setOwningPlayer(defeated);
         playerHead.setItemMeta(meta);
      }

      victorious.getWorld().dropItem(victorious.getLocation(), playerHead);

      defeated.getWorld().playSound(defeated.getLocation(), Sound.ENTITY_CAT_DEATH, 1f, 1f);
      getServer().broadcastMessage(victorious.getDisplayName() + ChatColor.GOLD + " a vaincu " + defeated.getDisplayName() + ChatColor.GOLD + " !");
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   private void onDamageTaken(EntityDamageEvent event)
   {
      if (event.getEntity().getType() == EntityType.PLAYER)
      {
         Player damagee = (Player) event.getEntity();
         Duel duel = tryGetDuel(damagee);

         if (duel != null && event.getFinalDamage() >= damagee.getHealth())
         {
            Player victorious = duel.getChallenger().equals(damagee) ? duel.getChallenged() : duel.getChallenger();
            finishDuel(damagee, victorious);
            duels.remove(duel);
         }
      }
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   private void onDamagedByEntity(EntityDamageByEntityEvent event)
   {
      if (event.getEntity().getType().equals(EntityType.PLAYER)
          && (event.getDamager().getType().equals(EntityType.PLAYER)
           || (event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)
            && ((Projectile) event.getDamager()).getShooter() instanceof Player)))
      {
         Player damagee = (Player) event.getEntity();
         Player damager = event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE) ?
                          (Player) ((Projectile)event.getDamager()).getShooter() : (Player) event.getDamager();

         if (isEngagedDuelBetween(damagee, damager))
         {
            if (event.getFinalDamage() >= damagee.getHealth())
            {
               Duel duel = tryGetDuel(damagee);
               finishDuel(damagee, damager);
               duels.remove(duel);
            }
            else
               return;
         }

         event.setDamage(0);
         event.setCancelled(true);
      }
   }

   @EventHandler
   private void onSplashPotion(PotionSplashEvent event)
   {
      if (event.getPotion().getShooter() instanceof Player)
         for (PotionEffect effect : event.getPotion().getEffects())
            if (effect.getType().equals(PotionEffectType.HARM) || effect.getType().equals(PotionEffectType.POISON))
            {
               for (LivingEntity entity : event.getAffectedEntities())
                  if (entity.getType() == EntityType.PLAYER && !isEngagedDuelBetween((Player) entity, (Player) event.getPotion().getShooter()))
                     event.setIntensity(entity, 0);
               break;
            }
   }

   @EventHandler
   public void onPlayerKicked(PlayerKickEvent event)
   {
      cancelDuelIfExists(event.getPlayer());
   }

   @EventHandler
   public void onPlayerFled(PlayerQuitEvent event)
   {
      cancelDuelIfExists(event.getPlayer());
   }

   @EventHandler
   public void onPlayerEscaping(PlayerPortalEvent event)
   {
      cancelDuelIfExists(event.getPlayer());
   }

   static class Duel
   {
      @NotNull private Player challenger;
      @NotNull private Player challenged;
      private boolean engaged;
      private int elapsedTime;

      Duel(@NotNull Player challenger, @NotNull Player challenged)
      {
         this.challenger = challenger;
         this.challenged = challenged;
         engaged = false;
         elapsedTime = 0;
      }

      @NotNull Player getChallenger()
      {
         return challenger;
      }

      @NotNull Player getChallenged()
      {
         return challenged;
      }

      boolean isImplied(Player player)
      {
         return player.equals(challenger) || player.equals(challenged);
      }

      void engage()
      {
         if (!engaged)
         {
            engaged = true;
            elapsedTime = 0;
         }
      }

      boolean isEngaged()
      {
         return engaged;
      }

      int getElapsedTime()
      {
         return elapsedTime;
      }

      void elapseASecond()
      {
         elapsedTime++;
      }
   }
}
