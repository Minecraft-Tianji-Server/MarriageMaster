/*
 *   Copyright (C) 2022 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.MarriageMaster.Bukkit.Database;

import at.pcgamingfreaks.Bukkit.MinecraftMaterial;
import at.pcgamingfreaks.Bukkit.Util.Utils;
import at.pcgamingfreaks.Config.Configuration;
import at.pcgamingfreaks.Config.ILanguageConfiguration;
import at.pcgamingfreaks.Config.YamlFileUpdateMethod;
import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import at.pcgamingfreaks.MarriageMaster.Bukkit.Range;
import at.pcgamingfreaks.MarriageMaster.Bukkit.SpecialInfoWorker.UpgradedInfo;
import at.pcgamingfreaks.MarriageMaster.Database.DatabaseConfiguration;
import at.pcgamingfreaks.MarriageMaster.MagicValues;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.StringUtils;
import at.pcgamingfreaks.Version;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Config extends Configuration implements DatabaseConfiguration, ILanguageConfiguration
{
	public Config(final @NotNull MarriageMaster plugin)
	{
		super(plugin, new Version(MagicValues.CONFIG_VERSION));
	}

	@Override
	protected @Nullable YamlFileUpdateMethod getYamlUpdateMode()
	{
		return YamlFileUpdateMethod.UPGRADE;
	}

	@Override
	protected void doUpgrade(final @NotNull at.pcgamingfreaks.Config.YamlFileManager oldConfig)
	{
		if(oldConfig.version().olderThan(new Version(MagicValues.CONFIG_PRE_V2_VERSIONS)))
		{
			getLogger().warning(ConsoleColor.RED + "Your config file is from v1.x and is not compatible with versions newer than 2.5!" + ConsoleColor.RESET);
			new UpgradedInfo(MarriageMaster.getInstance());
		}
		else
		{
			Map<String, String> reMappings = new HashMap<>();
			if(oldConfig.version().olderThan(new Version(98))) reMappings.put("Misc.AutoUpdate.Enable", "Misc.AutoUpdate");
			if(oldConfig.version().olderThan(new Version(101))) reMappings.put("Database.Cache.UnCache.Strategy", "Database.Cache.UnCache.Strategie");
			Collection<String> keysToKeep = oldConfig.getYamlE().getKeysFiltered("Database\\.SQL\\.(Tables\\.Fields\\..+|MaxLifetime|IdleTimeout)");
			keysToKeep.add("Misc.ServerName");
			super.doUpgrade(oldConfig, reMappings, keysToKeep);
		}
	}

	@SuppressWarnings("SameParameterValue")
	private Set<GameMode> getGameModes(final @NotNull String key, final @Nullable GameMode fallback)
	{
		List<GameMode> modes = getConfigE().getStringList(key, new ArrayList<>(0)).stream()
				.map(mode -> Utils.getEnum(mode, (GameMode) null, GameMode.class))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		if(modes.isEmpty())
		{
			if(fallback != null) modes.add(fallback);
			else return EnumSet.noneOf(GameMode.class);
		}
		return EnumSet.copyOf(modes);
	}

	//region Getter
	//region Global settings
	public boolean areMultiplePartnersAllowed()
	{
		return getConfigE().getBoolean("Marriage.AllowMultiplePartners", false);
	}

	public boolean isSelfMarriageAllowed()
	{
		return !getConfigE().getBoolean("Marriage.RequirePriest", true);
	}

	public boolean isSelfDivorceAllowed()
	{
		String val = getConfigE().getString("Marriage.DivorceRequiresPriest", "auto").trim().toLowerCase(Locale.ENGLISH);
		switch(val)
		{
			case "on":
			case "yes":
			case "true": return false;
			case "off":
			case "no":
			case "false": return true;
			default: return isSelfMarriageAllowed();
		}
	}

	public boolean isSurnamesEnabled()
	{
		return getConfigE().getBoolean("Marriage.Surnames.Enable", false);
	}

	public boolean isSurnamesForced()
	{
		return getConfigE().getBoolean("Marriage.Surnames.Force", false);
	}

	public double getRange(Range option)
	{
		return getConfigE().getDouble("Range." + option.name(), 25.0);
	}

	public double getRangeSquared(Range option)
	{
		double range = getRange(option);
		return (range > 0) ? range * range : range;
	}

	public boolean isMarryAnnouncementEnabled()
	{
		return getConfigE().getBoolean("Marriage.AnnounceOnMarriage", true);
	}

	public boolean isSetPriestCommandEnabled()
	{
		return !getConfigE().getBoolean("Marriage.DisableSetPriestCommand", false);
	}
	//endregion

	//region Confirmation settings
	public boolean isMarryConfirmationEnabled()
	{
		return getConfigE().getBoolean("Marriage.Confirmation.Enable", true);
	}

	public boolean isMarryConfirmationAutoDialogEnabled()
	{
		return getConfigE().getBoolean("Marriage.Confirmation.AutoDialog", true);
	}

	public boolean isConfirmationBothPlayersOnDivorceEnabled()
	{
		return getConfigE().getBoolean("Marriage.Confirmation.BothPlayersOnDivorce", true);
	}

	public boolean isConfirmationOtherPlayerOnSelfDivorceEnabled()
	{
		return getConfigE().getBoolean("Marriage.Confirmation.OtherPlayerOnSelfDivorce", false);
	}
	//endregion

	//region Surname settings
	public boolean getSurnamesAllowColors()
	{
		return getConfigE().getBoolean("Marriage.Surnames.AllowColors", false);
	}

	public String getSurnamesAllowedCharacters()
	{
		return getConfigE().getString("Marriage.Surnames.AllowedCharacters", "A-Za-z");
	}

	public int getSurnamesMinLength()
	{
		return Math.max(getConfigE().getInt("Marriage.Surnames.MinLength", 3), 3);
	}

	public int getSurnamesMaxLength()
	{
		return Math.max(getConfigE().getInt("Marriage.Surnames.MaxLength", 16), getSurnamesMinLength());
	}
	//endregion

	//region List command settings
	public boolean useListFooter()
	{
		return getConfigE().getBoolean("List.UseFooter", true);
	}

	public int getListEntriesPerPage()
	{
		return getConfigE().getInt("List.EntriesPerPage", 8);
	}
	//endregion

	//region Home/TP command settings
	public int getTPDelayTime()
	{
		return (getConfigE().getBoolean("Teleport.Delay", false) ? getConfigE().getInt("Teleport.DelayTime", 3) : 0);
	}

	public Set<String> getTPBlackListedWorlds()
	{
		Set<String> blackListedWorlds = new HashSet<>();
		for(String world : getConfigE().getStringList("Teleport.BlacklistedWorlds", new LinkedList<>()))
		{
			blackListedWorlds.add(world.toLowerCase(Locale.ENGLISH));
		}
		return blackListedWorlds;
	}

	public boolean getSafetyCheck()
	{
		return getConfigE().getBoolean("Teleport.CheckSafety", true);
	}

	public boolean getRequireConfirmation()
	{
		return getConfigE().getBoolean("Teleport.RequireConfirmation", true);
	}
	//endregion

	//region Kiss command settings
	public boolean isKissEnabled()
	{
		return getConfigE().getBoolean("Kiss.Enable", true);
	}

	public boolean isKissInteractEnabled()
	{
		return getConfigE().getBoolean("Kiss.EnableInteract", true);
	}

	public int getKissWaitTime()
	{
		return (int) (getConfigE().getFloat("Kiss.WaitTime", 10.0f) * 1000);
	}

	public int getKissHearthCount()
	{
		return getConfigE().getInt("Kiss.HearthCount", 50);
	}
	//endregion

	//region Hug command settings
	public boolean isHugEnabled()
	{
		return getConfigE().getBoolean("Hug.Enable", true);
	}

	public boolean isHugInteractEnabled()
	{
		return getConfigE().getBoolean("Hug.EnableInteract", true);
	}

	public int getHugWaitTime()
	{
		return (int) (getConfigE().getFloat("Hug.WaitTime", 10.0f) * 1000);
	}
	//endregion

	//region Gift command settings
	public boolean isGiftEnabled()
	{
		return getConfigE().getBoolean("Gift.Enable", true);
	}

	public Set<GameMode> getGiftAllowedGameModes()
	{
		return getGameModes("Gift.AllowedGameModesAllowed", GameMode.SURVIVAL);
	}

	public Set<GameMode> getGiftAllowedReceiveGameModes()
	{
		return getGameModes("Gift.AllowedGameModesReceive", GameMode.SURVIVAL);
	}

	public boolean isGiftRequireConfirmationEnabled()
	{
		return getConfigE().getBoolean("Gift.RequireConfirmation", false);
	}

	public Set<String> getGiftBlackListedWorlds()
	{
		Set<String> blackListedWorlds = new HashSet<>();
		for(String world : getConfigE().getStringList("Gift.BlacklistedWorlds", new LinkedList<>()))
		{
			blackListedWorlds.add(world.toLowerCase(Locale.ENGLISH));
		}
		return blackListedWorlds;
	}

	public boolean isGiftItemFilterEnabled()
	{
		return getConfigE().getBoolean("Gift.ItemFilter.Enabled", false);
	}

	public Collection<MinecraftMaterial> getItemFilterMaterials()
	{
		List<String> stringMaterialList = getConfigE().getStringList("Gift.ItemFilter.Materials", new LinkedList<>());
		Collection<MinecraftMaterial> blacklist = new ArrayList<>(stringMaterialList.size());
		for(String item : stringMaterialList)
		{
			MinecraftMaterial mat = MinecraftMaterial.fromInput(item);
			if(mat != null) blacklist.add(mat);
		}
		return blacklist;
	}

	public Set<String> getGiftItemFilterNames()
	{
		Set<String> names = new HashSet<>();
		getConfigE().getStringList("Gift.ItemFilter.Names", new ArrayList<>(0)).forEach(name -> names.add(MessageColor.translateAlternateColorAndFormatCodes(name)));
		return names;
	}

	public Set<String> getGiftItemFilterLore()
	{
		Set<String> loreSet = new HashSet<>();
		getConfigE().getStringList("Gift.ItemFilter.Lore", new ArrayList<>(0)).forEach(lore -> loreSet.add(MessageColor.translateAlternateColorAndFormatCodes(lore)));
		return loreSet;
	}

	public boolean isGiftItemFilterModeWhitelist()
	{
		return StringUtils.arrayContains(new String[]{ "whitelist", "allowlist" }, getConfigE().getString("Gift.ItemFilter.Mode", "blacklist").toLowerCase(Locale.ENGLISH));
	}
	//endregion

	// PvP command settings
	public boolean getPvPAllowBlocking()
	{
		return getConfigE().getBoolean("PvP.AllowBlocking", true);
	}

	//region BonusXP
	public boolean isBonusXPEnabled()
	{
		return getConfigE().getBoolean("BonusXp.Enable", false);
	}

	public double getBonusXpMultiplier()
	{
		return getConfigE().getDouble("BonusXp.Multiplier", 2);
	}

	public boolean isBonusXPSplitOnPickupEnabled()
	{
		return getConfigE().getBoolean("BonusXp.SplitXpOnPickup", true);
	}

	public boolean isBonusXPSplitOnPickupWithAllEnabled()
	{
		return getConfigE().getBoolean("BonusXp.SplitWithAllPartnersInRange", true) && isBonusXPSplitOnPickupEnabled();
	}

	public boolean isSkillApiBonusXPEnabled()
	{
		return getConfigE().getBoolean("BonusXp.SkillAPI.Enable", false);
	}

	public List<String> getSkillApiBonusXpBlockedSources()
	{
		return getConfigE().getStringList("BonusXp.SkillAPI.ExcludeSources", new ArrayList<>(0));
	}

	public double getSkillApiBonusXpMultiplier()
	{
		return getConfigE().getDouble("BonusXp.SkillAPI.Multiplier", 2);
	}

	public boolean isSkillApiBonusXPSplitEnabled()
	{
		return getConfigE().getBoolean("BonusXp.SkillAPI.SplitXp", true);
	}

	public boolean isSkillApiBonusXPSplitWithAllEnabled()
	{
		return getConfigE().getBoolean("BonusXp.SkillAPI.SplitWithAllPartnersInRange", true) && isSkillApiBonusXPSplitEnabled();
	}

	public boolean isMcMMOBonusXPEnabled()
	{
		return getConfigE().getBoolean("BonusXp.McMMO.Enable", false);
	}

	public Set<String> getMcMMOBonusXpBlockedSources()
	{
		Set<String> blockedSources = new HashSet<>();
		getConfigE().getStringList("BonusXp.McMMO.ExcludeSources", new LinkedList<>()).forEach(source -> blockedSources.add(source.toUpperCase(Locale.ENGLISH)));
		return blockedSources;
	}

	public Set<String> getMcMMOBonusXpBlockedSkills()
	{
		Set<String> blockedSkills = new HashSet<>();
		getConfigE().getStringList("BonusXp.McMMO.ExcludeSkills", new LinkedList<>()).forEach(source -> blockedSkills.add(source.toUpperCase(Locale.ENGLISH)));
		return blockedSkills;
	}

	public float getMcMMOBonusXpMultiplier()
	{
		return getConfigE().getFloat("BonusXp.McMMO.Multiplier", 2);
	}

	public boolean isMcMMOBonusXPSplitEnabled()
	{
		return getConfigE().getBoolean("BonusXp.McMMO.SplitXp", true);
	}

	public boolean isMcMMOBonusXPSplitWithAllEnabled()
	{
		return getConfigE().getBoolean("BonusXp.McMMO.SplitWithAllPartnersInRange", true) && isMcMMOBonusXPSplitEnabled();
	}
	//endregion

	//region HP Regain
	public boolean isHPRegainEnabled()
	{
		return getConfigE().getBoolean("HealthRegain.Enable", false);
	}

	public double getHPRegainMultiplier()
	{
		return getConfigE().getDouble("HealthRegain.Multiplier", 2);
	}
	//endregion

	//region Database getter
	@Override
	public @NotNull String getDatabaseType()
	{
		return getConfigE().getString("Database.Type", "SQLite").toLowerCase(Locale.ENGLISH);
	}

	@Override
	public void setDatabaseType(final @NotNull String newType)
	{
		set("Database.Type", newType);
		trySave();
	}

	@Override
	public boolean useOnlineUUIDs()
	{
		String type = getConfigE().getString("Database.UUID_Type", "auto").toLowerCase(Locale.ENGLISH);
		if(type.equals("auto"))
		{
			if(isBungeeEnabled())
			{
				getLogger().warning("When using BungeeCord please make sure to set the UUID_Type config option explicitly!");
			}
			return Bukkit.getServer().getOnlineMode();
		}
		return type.equals("online");
	}

	public void setUseUUIDSeparators(boolean useUUIDSeparators)
	{
		set("Database.UseUUIDSeparators", useUUIDSeparators);
		trySave();
	}

	public void setUUIDType(String type)
	{
		set("Database.UUID_Type", type);
		trySave();
	}
	//endregion

	//region Join Leave Info
	public boolean isJoinLeaveInfoEnabled()
	{
		return getConfigE().getBoolean("InfoOnPartnerJoinLeave.Enable", true);
	}

	public long getJoinInfoDelay()
	{
		return getConfigE().getLong("InfoOnPartnerJoinLeave.JoinDelay", 0) * 20L;
	}
	//endregion

	//region Chat settings
	public boolean isChatEnabled()
	{
		return getConfigE().getBoolean("Chat.Enabled", true);
	}

	public boolean isChatSurveillanceEnabled()
	{
		return getConfigE().getBoolean("Chat.AllowSurveillance", false);
	}
	//endregion

	//region Prefix/Suffix
	public boolean isPrefixEnabled()
	{
		return getConfigE().getBoolean("Prefix.Enable", false) && !getPrefix().isEmpty();
	}

	public boolean isSuffixEnabled()
	{
		return getConfigE().getBoolean("Suffix.Enable", false) && !getSuffix().isEmpty();
	}

	public @NotNull String getPrefix()
	{
		return MessageColor.translateAlternateColorAndFormatCodes(getConfigE().getString("Prefix.String", "").replace("<heart>", MessageColor.RED + MagicValues.SYMBOL_HEART + MessageColor.WHITE));
	}

	public boolean isPrefixOnLineBeginning()
	{
		return getConfigE().getBoolean("Prefix.OnLineBeginning", true);
	}

	public @NotNull String getSuffix()
	{
		return MessageColor.translateAlternateColorAndFormatCodes(getConfigE().getString("Suffix.String", "").replace("<heart>", MessageColor.RED + MagicValues.SYMBOL_HEART + MessageColor.WHITE));
	}
	//endregion

	//region Backpack share
	public boolean isBackpackShareEnabled()
	{
		return getConfigE().getBoolean("BackpackShare.Enable", true);
	}
	//endregion

	//region Economy
	public boolean isEconomyEnabled()
	{
		return getConfigE().getBoolean("Economy.Enable", false);
	}

	public double getEconomyValue(String valueName)
	{
		return getConfigE().getDouble("Economy." + valueName, 0);
	}
	//endregion

	//region Command Executor
	public boolean isCommandExecutorEnabled()
	{
		return getConfigE().getBoolean("CommandExecutor.Enable", false);
	}

	public Collection<String> getCommandExecutorOnMarry()
	{
		return getConfigE().getStringList("CommandExecutor.OnMarry", new LinkedList<>());
	}

	public Collection<String> getCommandExecutorOnMarryWithPriest()
	{
		return getConfigE().getStringList("CommandExecutor.OnMarryWithPriest", new LinkedList<>());
	}

	public Collection<String> getCommandExecutorOnDivorce()
	{
		return getConfigE().getStringList("CommandExecutor.OnDivorce", new LinkedList<>());
	}

	public Collection<String> getCommandExecutorOnDivorceWithPriest()
	{
		return getConfigE().getStringList("CommandExecutor.OnDivorceWithPriest", new LinkedList<>());
	}
	//endregion

	public String getDefaultCommand()
	{
		return getConfigE().getString("DefaultCommand", "help").toLowerCase(Locale.ENGLISH);
	}

	public boolean isAllowPlayersToChangeMarriageColor()
	{
		return getConfigE().getBoolean("AllowPlayersToChangeMarriageColor", true);
	}

	//region Misc getter
	public boolean useUpdater()
	{
		return getConfigE().getBoolean("Misc.AutoUpdate.Enable", getConfigE().getBoolean("Misc.AutoUpdate", true));
	}

	public String getUpdateChannel()
	{
		String channel = getConfigE().getString("Misc.AutoUpdate.Channel", "Release");
		if("Release".equals(channel) || "Master".equals(channel) || "Dev".equals(channel))
		{
			return channel;
		}
		else getLogger().info("Unknown update Channel: " + channel);
		return null;
	}

	public boolean isBungeeEnabled()
	{
		boolean useBungee = getConfigE().getBoolean("Misc.UseBungeeCord", false);
		boolean spigotUsesBungee = Utils.detectBungeeCord();
		boolean shareableDB = getDatabaseType().equals("mysql") || getDatabaseType().equals("global");
		if(useBungee && !spigotUsesBungee)
		{
			getLogger().warning("You have BungeeCord enabled for the plugin, but it looks like you have not enabled it in your spigot.yml! You probably should check your configuration.");
		}
		else if(!useBungee && spigotUsesBungee && shareableDB)
		{
			getLogger().warning("Your server is running behind a BungeeCord server. If you are using the plugin on more than one server with a shared database, please make sure to also enable the 'UseBungeeCord' config option.");
		}
		else if(useBungee && !shareableDB)
		{
			getLogger().info("You have enabled BungeeCord mode for the plugin, but are not using a shared MySQL database.");
			return false; // No need to enable BungeeCord mode if the database does not support it
		}
		return useBungee;
	}

	public String getServerName()
	{
		return getConfigE().getString("Misc.ServerName", null);
	}

	public void setServerName(String serverName)
	{
		try
		{
			getConfigE().set("Misc.ServerName", serverName);
			save();
		}
		catch(Exception e)
		{
			getLogger().log(Level.WARNING, "Failed to set server name in config!", e);
		}
	}
	//endregion
	//endregion

	private void trySave()
	{
		try
		{
			save();
		}
		catch(FileNotFoundException e)
		{
			getLogger().log(Level.SEVERE, "Failed to save config!", e);
		}
	}
}