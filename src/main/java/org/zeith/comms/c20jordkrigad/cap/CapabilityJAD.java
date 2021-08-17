package org.zeith.comms.c20jordkrigad.cap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.zeith.comms.c20jordkrigad.JordkrigAirDrop;

@Mod.EventBusSubscriber
public class CapabilityJAD
{
	@CapabilityInject(BaseJAD.class)
	public static Capability<BaseJAD> JAD = null;

	public static void register()
	{
		CapabilityManager.INSTANCE.register(BaseJAD.class, new Capability.IStorage<BaseJAD>()
				{
					@Override
					public NBTBase writeNBT(Capability<BaseJAD> capability, BaseJAD instance, EnumFacing side)
					{
						return instance.serializeNBT();
					}

					@Override
					public void readNBT(Capability<BaseJAD> capability, BaseJAD instance, EnumFacing side, NBTBase nbt)
					{
						instance.deserializeNBT((NBTTagCompound) nbt);
					}
				},
				BaseJAD::new);
	}

	@SubscribeEvent
	public static void addCapability(AttachCapabilitiesEvent<World> evt)
	{
		evt.addCapability(new ResourceLocation(JordkrigAirDrop.MOD_ID, "data"), new BaseJAD());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void breakBlock(BlockEvent.BreakEvent e)
	{
		World wld = e.getWorld();
		BaseJAD jad = wld.getCapability(JAD, null);
		if(jad != null && jad.spawnPos != null && jad.spawnPos.equals(e.getPos()) && jad.cooldownTillUnlock > 0)
			e.setCanceled(true);
	}

	@SubscribeEvent
	public static void serverTick(TickEvent.WorldTickEvent e)
	{
		if(e.phase == TickEvent.Phase.START && e.side == Side.SERVER && e.world.provider.getDimension() == JordkrigAirDrop.dimension && e.world instanceof WorldServer)
		{
			BaseJAD jad = e.world.getCapability(JAD, null);
			if(jad != null) jad.update((WorldServer) e.world);
		}
	}
}