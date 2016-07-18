package kjhf.falconpunch;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FalconPunch extends JavaPlugin {

		public boolean AllowPVP = true;
		public boolean OnlyPVP = false;
		public boolean NoImmunity = false;

		public boolean UseContinuousSystem = true;

		public int CriticalsChance = 1;
		public int BurnChance = 1;

		public int FailChance = 1;
		public int FailNothingChance = 1;
		public int FailFireChance = 1;
		public int FailLightningChance = 1;

		public int cooldown = 60000;

		Set<UUID> noPunch;

		@Override
		public void onDisable() {
			this.getLogger().info("Version " + this.getDescription().getVersion() + " disabled.");
		}

		@Override
		public void onEnable() {

			this.loadConfigs();

			this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

			noPunch = new HashSet<UUID>();

			this.getLogger().info("Version " + this.getDescription().getVersion() + " is enabled!");

		}

		@Override
		public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

			if (!(sender instanceof Player)) {
					return false;

			}

			UUID uuid = ((Player) sender).getUniqueId();

			if (noPunch.contains(uuid)) {
					noPunch.remove(uuid);
					sender.sendMessage(ChatColor.DARK_AQUA + "Toggled FalconPunch on!");

			} else {
					noPunch.add(uuid);
					sender.sendMessage(ChatColor.DARK_AQUA + "Toggled FalconPunch off!");
					
			}

			return true;

		}

		boolean shouldNotPunch(UUID uuid) {
			return noPunch.contains(uuid);

		}

		public void resetPlayer(UUID uuid) {
			noPunch.remove(uuid);

		}

		private void loadConfigs() {
			final File configFile = new File(this.getDataFolder(), "config.yml");
			if (!configFile.exists()) {
					this.saveDefaultConfig();
			}
			this.reloadConfig();
			final FileConfiguration config = this.getConfig();

			this.AllowPVP = config.getBoolean("Settings.AllowPVPFalconPunch", true);
			this.OnlyPVP = config.getBoolean("Settings.OnlyPVPFalconPunch", false);
			this.NoImmunity = config.getBoolean("Settings.NoImmunity", false);
			this.UseContinuousSystem = config.getBoolean("Criticals.UseContinuousSystem", true);
			this.CriticalsChance = config.getInt("Criticals.CriticalsChance", 1);
			this.BurnChance = config.getInt("Burns.BurnChance", 1);
			this.FailChance = config.getInt("Fails.FailChance", 1);
			this.FailNothingChance = config.getInt("Fails.AfterFailDoNothingChance", 34);
			this.FailFireChance = config.getInt("Fails.AfterFailDoFireChance", 33);
			this.FailLightningChance = config.getInt("Fails.AfterFailDoLightningChance", 33);

			this.cooldown = config.getInt("Settings.cooldown");

		}

}