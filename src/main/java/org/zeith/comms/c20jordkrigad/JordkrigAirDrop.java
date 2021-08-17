package org.zeith.comms.c20jordkrigad;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.comms.c20jordkrigad.cap.CapabilityJAD;

@Mod(
		modid = JordkrigAirDrop.MOD_ID,
		name = JordkrigAirDrop.MOD_NAME,
		version = "@VERSION@",
		certificateFingerprint = "@FINGERPRINT@",
		updateJSON = "http://dccg.herokuapp.com/api/fmluc/@CF_ID@"
)
public class JordkrigAirDrop
{
	public static final String MOD_ID = "jordkrigad";
	public static final String MOD_NAME = "Jordkrig Air Drops";

	public static final Logger LOG = LogManager.getLogger();

	@Mod.Instance
	public static JordkrigAirDrop INSTANCE;

	public static long spawnIntervalTicks, unlockTimer;
	public static int dimension, x1, x2, z1, z2;
	public static ResourceLocation lootTable;

	@Mod.EventHandler
	public void certificateViolation(FMLFingerprintViolationEvent e)
	{
		LOG.warn("*****************************");
		LOG.warn("WARNING: Somebody has been tampering with " + JordkrigAirDrop.MOD_NAME + " jar!");
		LOG.warn("It is highly recommended that you redownload mod from https://www.curseforge.com/projects/@CF_ID@ !");
		LOG.warn("*****************************");
	}

	private Configuration cfg;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		cfg = new Configuration(e.getSuggestedConfigurationFile());
		reloadCfg();
		CapabilityJAD.register();
	}

	@SubscribeEvent
	public void reloadCfgs(ConfigChangedEvent e)
	{
		if(e.getModID().equalsIgnoreCase(MOD_ID))
			reloadCfg();
	}

	public Configuration getCfg()
	{
		return cfg;
	}

	public static void reloadCfg()
	{
		Configuration $ = INSTANCE.getCfg();

		String timeExmplanation = "\nThe time is calculated by appending numbers and time qualifiers (t - ticks, s - seconds, m - minutes, h - hours, d - days, w - weeks, M - months, y - years);" +
				"\nSome examples:" +
				"\n\t1h - 1 hour" +
				"\n\t2h30m - 2 hours plus 30 minutes" +
				"\n\t1h30m50s - 1 hour plus 30 minutes plus 50 seconds (90 minutes 50 seconds total)\n";

		try
		{
			spawnIntervalTicks = formatTimeToTicks($.getString("Interval", "Airdrop", "1h", "How frequently will the chest spawn?" + timeExmplanation));
			unlockTimer = Math.min(formatTimeToTicks($.getString("Unlock", "Airdrop", "30m", "How much time would pass before a chest gets unlocked?" + timeExmplanation)), spawnIntervalTicks);
		} catch(Throwable err)
		{
			err.printStackTrace();
		}

		dimension = $.getInt("Dimension", "Airdrop", 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "The dimension where the airdrop is going to spawn.");

		int $x1 = $.getInt("x1", "Airdrop", -32, Integer.MIN_VALUE, Integer.MAX_VALUE, "The first x coordinate for defining a boundary of the airdrop spawn field.");
		int $x2 = $.getInt("x2", "Airdrop", 32, Integer.MIN_VALUE, Integer.MAX_VALUE, "The second x coordinate for defining a boundary of the airdrop spawn field.");

		x1 = Math.min($x1, $x2);
		x2 = Math.max($x1, $x2);

		int $z1 = $.getInt("z1", "Airdrop", -32, Integer.MIN_VALUE, Integer.MAX_VALUE, "The first z coordinate for defining a boundary of the airdrop spawn field.");
		int $z2 = $.getInt("z2", "Airdrop", 32, Integer.MIN_VALUE, Integer.MAX_VALUE, "The second z coordinate for defining a boundary of the airdrop spawn field.");

		z1 = Math.min($z1, $z2);
		z2 = Math.max($z1, $z2);

		lootTable = new ResourceLocation($.getString("Loot Table", "Airdrop", LootTableList.CHESTS_SPAWN_BONUS_CHEST.toString(), "The loot table that will be populated inside the chest once it is going to be spawned."));

		if($.hasChanged()) $.save();
	}

	public static long formatTimeToTicks(String time)
	{
		long ticksElapsed = 0L;

		int last = 0;
		char[] arr = time.toCharArray();

		for(int i = 0; i < arr.length; ++i)
		{
			if(arr[i] >= '0' && arr[i] <= '9' || arr[i] == '-')
				continue;

			if(arr[i] == 't')
			{
				try
				{
					ticksElapsed += Integer.parseInt(new String(arr, last, i - last));
				} catch(Throwable ignored)
				{
				}
				last = i + 1;
			} else if(arr[i] == 's')
			{
				try
				{
					ticksElapsed += Integer.parseInt(new String(arr, last, i - last)) * 20L;
				} catch(Throwable ignored)
				{
				}
				last = i + 1;
			} else if(arr[i] == 'm')
			{
				try
				{
					ticksElapsed += Integer.parseInt(new String(arr, last, i - last)) * 20L * 60L;
				} catch(Throwable ignored)
				{
				}
				last = i + 1;
			} else if(arr[i] == 'h')
			{
				try
				{
					ticksElapsed += Integer.parseInt(new String(arr, last, i - last)) * 20L * 60L * 60L;
				} catch(Throwable ignored)
				{
				}
				last = i + 1;
			} else if(arr[i] == 'd')
			{
				try
				{
					ticksElapsed += Integer.parseInt(new String(arr, last, i - last)) * 20L * 60L * 60L * 24L;
				} catch(Throwable ignored)
				{
				}
				last = i + 1;
			} else if(arr[i] == 'w')
			{
				try
				{
					ticksElapsed += Integer.parseInt(new String(arr, last, i - last)) * 20L * 60L * 60L * 24L * 7L;
				} catch(Throwable ignored)
				{
				}
				last = i + 1;
			} else if(arr[i] == 'M')
			{
				try
				{
					ticksElapsed += Integer.parseInt(new String(arr, last, i - last)) * 20L * 60L * 60L * 24L * 30L;
				} catch(Throwable ignored)
				{
				}
				last = i + 1;
			} else if(arr[i] == 'y')
			{
				try
				{
					ticksElapsed += Integer.parseInt(new String(arr, last, i - last)) * 20L * 60L * 60L * 24L * 30L * 365L;
				} catch(Throwable ignored)
				{
				}
				last = i + 1;
			} else
				throw new RuntimeException("Undefined time unit: " + arr[i]);
		}

		return ticksElapsed;
	}
}