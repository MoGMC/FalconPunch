package kjhf.falconpunch;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class PlayerListener implements Listener {

		private FalconPunch plugin;

		private Random random;

		public HashMap<UUID, Long> cooldowns;

		public PlayerListener(FalconPunch plugin) {

			this.plugin = plugin;

			random = new Random();

			cooldowns = new HashMap<UUID, Long>();

		}

		@EventHandler
		public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

			final Player player = event.getPlayer();

			if (!player.hasPermission("falconpunch.punch")) {
					return;

			}

			if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
					return;

			}

			if (plugin.shouldNotPunch(player.getUniqueId())) {
					return;

			}

			if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
					return;

			}

			if (player.isSneaking()) {
					return;

			}

			final Entity targetEntity = event.getRightClicked();

			if (targetEntity instanceof Player) {

					if (!this.plugin.AllowPVP) {

						return; // player doesn't allow player harm

					}

					final Player targetplayer = (targetEntity instanceof Player) ? (Player) targetEntity : null;

					if ((targetplayer != null) && targetplayer.hasPermission("falconpunch.immune") && !this.plugin.NoImmunity) {
						player.sendMessage(ChatColor.GOLD + "[FalconPunch] " + ChatColor.RED + "That person cannot be Falcon Punched. They have immune permission.");
						return;

					}

			} else if (this.plugin.OnlyPVP) {

					return; // server only allows player harm, not animal harm

			}

			// TODO: clean up pig logic

			// checks to see if the player is trying to get in a vehicle
			if ((targetEntity instanceof Vehicle)) {

					if (targetEntity instanceof Pig) {

						// is a pig

						if (((Pig) targetEntity).hasSaddle()) {

								if (targetEntity.isEmpty()) {

									return;

								}

								// checks to make sure person isn't punching their own pig as they enter it
								Entity vehicle = player.getVehicle();

								if (vehicle != null && vehicle.equals(targetEntity)) {
									return;

								}

						}

					} else if (targetEntity.isEmpty()) {

						return;

					}

			}

			if (targetEntity instanceof Wolf) {

					final Wolf wolf = (Wolf) targetEntity;

					if (wolf.isTamed()) {
						if ((wolf.getOwner() instanceof Player) && (player == (Player) wolf.getOwner())) {
								// don't let people punch their own dogs
								return;

						}

					}

			}

			if (!cooldowns.containsKey(player.getUniqueId())) {
					cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

			} else if (System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < plugin.cooldown) {
					player.sendMessage(ChatColor.DARK_AQUA + "You can only use falcon punch once every " + (plugin.cooldown / 1000) + " seconds!");
					return;

			}

			cooldowns.put(player.getUniqueId(), System.currentTimeMillis());

			int i = random.nextInt(99) + 1;

			if (i <= this.plugin.FailChance) {
					// The punch failed. Let's decide what we're going to do.

					if ((this.plugin.FailNothingChance + this.plugin.FailFireChance + this.plugin.FailLightningChance) <= 0) {
						this.plugin.getLogger().warning("Logic error. Please check fail probability in config for negative chances. Defaulting to no side-effect.");
						player.sendMessage(ChatColor.DARK_AQUA + "FALCON... Fail?!");
						return;
					}

					random = new Random();
					i = random.nextInt(this.plugin.FailNothingChance + this.plugin.FailFireChance + this.plugin.FailLightningChance) + 1;
					if ((0 < i) && (i <= this.plugin.FailNothingChance)) {
						// Show the Fail nothing message.
						player.sendMessage(ChatColor.DARK_AQUA + "FALCON... Fail?!");
						return;
					} else if ((this.plugin.FailNothingChance < i) && (i <= (this.plugin.FailNothingChance + this.plugin.FailFireChance))) {
						// Show the Fail fire message.
						player.setFireTicks(200);
						player.sendMessage(ChatColor.DARK_AQUA + "FALCON... Fail? [Burn Hit! Oh Noes!]");
						return;

					} else if (((this.plugin.FailNothingChance + this.plugin.FailFireChance) < i) && (i <= (this.plugin.FailNothingChance + this.plugin.FailFireChance + this.plugin.FailLightningChance))) {
						// Show the Fail lightning message.
						player.getWorld().strikeLightningEffect(player.getLocation());
						if (!player.getGameMode().equals(GameMode.CREATIVE)) {
								player.setHealth(0);
						}
						player.sendMessage(ChatColor.DARK_AQUA + "FALCON... Fail? [YOU HAVE BEEN SMITTEN!]");
						return;
					} else {
						// Logic error, show the Fail nothing message.
						this.plugin.getLogger().warning("Logic error. Please check fail probability config. Defaulting to no side-effect.");
						this.plugin.getLogger().warning("Generated num: " + i + ". FailNothingChance: " + this.plugin.FailNothingChance + ". FailFireChance: " + this.plugin.FailFireChance + ". FailLightningChance: " + this.plugin.FailLightningChance);
						player.sendMessage(ChatColor.DARK_AQUA + "FALCON... Fail?!");
						return;
					}

			}

			double crit = 2.0;

			if (!this.plugin.UseContinuousSystem) {
					if (this.plugin.CriticalsChance > 0) {
						random = new Random();
						i = random.nextInt(99) + 1;
						if (this.plugin.CriticalsChance >= i) {
								crit = 4;
						}
					}

			} else {
					random = new Random();
					i = (random.nextInt(59) + 1);
					crit = (double) i / 10; // crit is between 0.1 and 6.0

			}

			boolean burncrit = false;

			if (this.plugin.BurnChance > 0) {
					random = new Random();
					i = random.nextInt(99) + 1;
					if (i <= this.plugin.BurnChance) {
						burncrit = true;
						targetEntity.setFireTicks(200);
					}
			}

			final Vector direction = player.getLocation().getDirection();
			Vector additionalverticle = null;
			if ((direction.getY() >= -0.5) && (direction.getY() < 0.6)) {
					additionalverticle = new Vector(0, 0.5, 0);
			} else {
					additionalverticle = new Vector(0, 0, 0);
			}
			Vector velocity = new Vector(0, 0, 0);
			if (player.getVelocity() != null) {
					velocity = player.getVelocity().add(direction).add(additionalverticle).multiply(5).multiply(crit);
			} else {
					velocity = velocity.add(direction).add(additionalverticle).multiply(5).multiply(crit);
			}

			double x = targetEntity.getLocation().getX();
			double y = targetEntity.getLocation().getY();
			double z = targetEntity.getLocation().getZ();

			World world = targetEntity.getWorld();

			world.spawnParticle(Particle.EXPLOSION_HUGE, x, y, z, 3, 0, 2, 0);

			world.createExplosion(x, y, z, 0.0F, false, false);

			targetEntity.setVelocity(velocity);

			final StringBuilder message = new StringBuilder();
			message.append(ChatColor.DARK_AQUA + "FALCON... PAUNCH! ");
			if (!this.plugin.UseContinuousSystem) {
					if (burncrit) {
						if (crit == 4) {
								message.append("[" + ChatColor.RED + "Burn " + ChatColor.DARK_AQUA + "+" + ChatColor.RED + " Critical Hit! " + ChatColor.DARK_AQUA + "]");
						} else {
								message.append("[" + ChatColor.RED + "Burn Hit!" + ChatColor.DARK_AQUA + "]");
						}
					} else {
						if (crit == 4) {
								message.append("[" + ChatColor.RED + "Critical Hit!" + ChatColor.DARK_AQUA + "]");
						}
					}
			} else {
					message.append("[");

					if (crit > 5.75) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.GREEN + "||||||" + ChatColor.WHITE + "||");
					} else if (crit > 5.5) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.GREEN + "||||||" + ChatColor.WHITE + "|" + ChatColor.BLACK + "|");
					} else if (crit > 5.25) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.GREEN + "||||||" + ChatColor.BLACK + "||");
					} else if (crit > 5.0) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.GREEN + "|||||" + ChatColor.BLACK + "|||");
					} else if (crit > 4.75) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.GREEN + "||||" + ChatColor.BLACK + "||||");
					} else if (crit > 4.5) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.GREEN + "|||" + ChatColor.BLACK + "|||||");
					} else if (crit > 4.25) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.GREEN + "||" + ChatColor.BLACK + "||||||");
					} else if (crit > 4.0) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.GREEN + "|" + ChatColor.BLACK + "|||||||");
					} else if (crit > 3.75) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||||" + ChatColor.BLACK + "||||||||");
					} else if (crit > 3.5) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "|||||" + ChatColor.BLACK + "|||||||||");
					} else if (crit > 3.25) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||||" + ChatColor.BLACK + "||||||||||");
					} else if (crit > 3.0) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "|||" + ChatColor.BLACK + "|||||||||||");
					} else if (crit > 2.75) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "||" + ChatColor.BLACK + "||||||||||||");
					} else if (crit > 2.5) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.YELLOW + "|" + ChatColor.BLACK + "|||||||||||||");
					} else if (crit > 2.25) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||||" + ChatColor.BLACK + "||||||||||||||");
					} else if (crit > 2.0) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "|||||" + ChatColor.BLACK + "|||||||||||||||");
					} else if (crit > 1.75) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||||" + ChatColor.BLACK + "||||||||||||||||");
					} else if (crit > 1.5) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "|||" + ChatColor.BLACK + "|||||||||||||||||");
					} else if (crit > 1.25) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "||" + ChatColor.BLACK + "||||||||||||||||||");
					} else if (crit > 1.0) {
						message.append(ChatColor.RED + "||||||" + ChatColor.GOLD + "|" + ChatColor.BLACK + "|||||||||||||||||||");
					} else if (crit > 0.8) {
						message.append(ChatColor.RED + "||||||" + ChatColor.BLACK + "||||||||||||||||||||");
					} else if (crit > 0.6) {
						message.append(ChatColor.RED + "|||||" + ChatColor.BLACK + "|||||||||||||||||||||");
					} else if (crit > 0.4) {
						message.append(ChatColor.RED + "||||" + ChatColor.BLACK + "||||||||||||||||||||||");
					} else if (crit > 0.3) {
						message.append(ChatColor.RED + "|||" + ChatColor.BLACK + "|||||||||||||||||||||||");
					} else if (crit > 0.2) {
						message.append(ChatColor.RED + "||" + ChatColor.BLACK + "||||||||||||||||||||||||");
					} else {
						message.append(ChatColor.RED + "|" + ChatColor.BLACK + "|||||||||||||||||||||||||");
					}
					message.append(ChatColor.DARK_AQUA + "]");

					if (burncrit) {
						message.append(" [" + ChatColor.RED + "Burn!" + ChatColor.DARK_AQUA + "] ");
					}
			}
			player.sendMessage(message.toString());
		}

		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent e) {

			cooldowns.remove(e.getPlayer().getUniqueId());
			plugin.resetPlayer(e.getPlayer().getUniqueId());

		}

}