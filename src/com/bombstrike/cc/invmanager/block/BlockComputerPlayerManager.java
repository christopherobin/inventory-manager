package com.bombstrike.cc.invmanager.block;

import java.util.Random;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager.TYPE;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import dan200.computer.api.ComputerCraftAPI;

public class BlockComputerPlayerManager extends BlockBasicPlayerManager {
	Class<?> computerEntityInterface = null;
	
	public BlockComputerPlayerManager(int blockId) {
		super(blockId);
		setBlockName("playerManagerPeripheral");
		
		try {
			computerEntityInterface = Class.forName("dan200.computer.shared.IComputerEntity");
		} catch (Exception e) {
			return;
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityPlayerManager(TYPE.COMPUTER);
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving) {
		super.onBlockPlacedBy(world, x, y, z, entityliving);
		
		if (world.isRemote) return;

		recomputeConnections(world, x, y, z);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID) {
		super.onNeighborBlockChange(world, x, y, z, blockID);
		
		if (world.isRemote) return;

		recomputeConnections(world, x, y, z);
	}
	
	private void recomputeConnections(World world, int x, int y, int z) {
		if (computerEntityInterface == null) return;

		// search for nearby computers
		int connections = 0;
		int shift = 0;
		ForgeDirection directions[] = { ForgeDirection.WEST, ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH };
		
		for (ForgeDirection direction : directions) {
			TileEntity te = Utils.getTileNeighbor(world, x, y, z, direction);
			if (te != null) {
				if (computerEntityInterface.isAssignableFrom(te.getClass())) {
					// create connection
					connections |= 0x1 << shift;
				}
			}
			shift++;
		}
		// set the render state on the tile entity
		if (world.getBlockTileEntity(x, y, z) instanceof TileEntityPlayerManager) {
			((TileEntityPlayerManager)world.getBlockTileEntity(x, y, z)).setConnections(connections);
		}
	}
}
