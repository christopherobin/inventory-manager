package com.bombstrike.cc.invmanager.block;

import java.util.Random;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.Utils;
import com.bombstrike.cc.invmanager.tileentity.BaseManager;
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

public class BlockPlayerManagerComputer extends BlockPlayerManager {
	public BlockPlayerManagerComputer(int blockId) {
		super(blockId);
		setBlockName("playerManagerPeripheral");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityPlayerManager(TYPE.COMPUTER);
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving) {
		super.onBlockPlacedBy(world, x, y, z, entityliving);
		
		if (world.isRemote) return;

		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity != null && entity instanceof BaseManager) {
			if (((BaseManager) entity).recomputeConnections()) {
				world.markBlockForUpdate(x, y, z);
			}
		}
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID) {
		super.onNeighborBlockChange(world, x, y, z, blockID);
		
		if (world.isRemote) return;

		TileEntity entity = world.getBlockTileEntity(x, y, z);
		if (entity != null && entity instanceof BaseManager) {
			if (((BaseManager) entity).recomputeConnections()) {
				world.markBlockForUpdate(x, y, z);
			}
		}
	}
}
