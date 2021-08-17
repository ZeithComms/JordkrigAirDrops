package org.zeith.comms.c20jordkrigad.cap;

import net.minecraft.block.BlockChest;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.LockCode;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.zeith.comms.c20jordkrigad.JordkrigAirDrop;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class BaseJAD
		implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
{
	public BaseJAD()
	{
	}

	public int cooldownTillUnlock, cooldownTillSpawn;
	public BlockPos spawnPos;

	public void update(WorldServer world)
	{
		if(spawnPos != null && cooldownTillUnlock > 0 && --cooldownTillUnlock <= 0)
		{
			Chunk c = world.getChunk(spawnPos);
			TileEntity tile = c.getTileEntity(spawnPos, Chunk.EnumCreateEntityType.IMMEDIATE);
			if(tile instanceof TileEntityLockableLoot)
			{
				TileEntityLockableLoot te = (TileEntityLockableLoot) tile;
				te.setLootTable(JordkrigAirDrop.lootTable, world.rand.nextLong());
				te.setLockCode(LockCode.EMPTY_CODE);
				cooldownTillUnlock = 0;
			}
		}

		if(cooldownTillUnlock > 0 && spawnPos != null && world.getTotalWorldTime() % 20L == 0L)
		{
			if(world.isBlockLoaded(spawnPos))
			{
				TileEntity tile = world.getTileEntity(spawnPos);
				if(tile instanceof TileEntityLockableLoot)
				{
					TileEntityLockableLoot te = (TileEntityLockableLoot) tile;
					te.setLockCode(new LockCode(UUID.randomUUID().toString()));
				}
			}
		}

		if(cooldownTillSpawn >= 0 && --cooldownTillSpawn <= 0)
		{
			Random rng = world.rand;
			int x = nextInRange(rng, JordkrigAirDrop.x1, JordkrigAirDrop.x2);
			int z = nextInRange(rng, JordkrigAirDrop.z1, JordkrigAirDrop.z2);
			int y = world.getChunk(new BlockPos(x, 0, z)).getHeightValue(x & 15, z & 15);

			spawnPos = new BlockPos(x, y, z);

			world.setBlockState(spawnPos, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, randomElement(rng, EnumFacing.HORIZONTALS)));
			TileEntity tile = world.getTileEntity(spawnPos);
			TileEntityLockableLoot loot = tile instanceof TileEntityLockableLoot ? (TileEntityLockableLoot) tile : null;
			if(loot == null) world.setTileEntity(spawnPos, loot = new TileEntityChest());
			loot.setLootTable(JordkrigAirDrop.lootTable, world.rand.nextLong());
			loot.setLockCode(new LockCode(UUID.randomUUID().toString()));

			world.getMinecraftServer().getPlayerList().sendMessage(new TextComponentTranslation("chat.jordkrigad.chest_spawn", x, y, z));

			cooldownTillSpawn += JordkrigAirDrop.spawnIntervalTicks;
			cooldownTillUnlock += JordkrigAirDrop.unlockTimer;
		}
	}

	public static int nextInRange(Random rng, int min, int max)
	{
		return min + rng.nextInt(max - min + 1);
	}

	public static <T> T randomElement(Random rng, T[] array)
	{
		return array == null || array.length == 0 ? null : array[rng.nextInt(array.length)];
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityJAD.JAD;
	}

	@Nullable
	@Override
	public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
	{
		return capability == CapabilityJAD.JAD ? CapabilityJAD.JAD.cast(this) : null;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();

		nbt.setInteger("UnlockCooldown", cooldownTillUnlock);
		nbt.setInteger("SpawnCooldown", cooldownTillSpawn);

		if(spawnPos != null) nbt.setLong("SpawnedPosition", spawnPos.toLong());

		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		cooldownTillUnlock = nbt.getInteger("UnlockCooldown");
		cooldownTillSpawn = nbt.getInteger("SpawnCooldown");

		if(nbt.hasKey("SpawnedPosition", Constants.NBT.TAG_LONG))
			spawnPos = BlockPos.fromLong(nbt.getLong("SpawnedPosition"));
		else spawnPos = null;
	}
}