package com.bombstrike.cc.invmanager.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.bombstrike.cc.invmanager.tileentity.BaseManager;

public class BlockBaseManager extends BlockContainer {
	private Random random = new Random();

	public BlockBaseManager(int par1, Material par2Material) {
		super(par1, par2Material);
	}

	public BlockBaseManager(int par1, int par2, Material par3Material) {
		super(par1, par2, par3Material);
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return null;
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
	
	@Override
	public void breakBlock(World world, int x, int y, int z,
			int blockID, int metadata) {
		BaseManager manager = (BaseManager)world.getBlockTileEntity(x, y, z);
		if (manager != null) {
			IInventory buffer = manager.getBuffer();
			ItemStack stack = buffer.getStackInSlot(0); 
			//InventoryManager.logger.info("Stack in buffer slot 0: " + stack.getDisplayName());
			if (stack != null) {
				// drop items from the buffer into the world
				EntityItem entity = new EntityItem(world, x, y, z, stack.copy());
				float scale = 0.05F;
                entity.motionX = (double)((float)this.random.nextGaussian() * scale);
                entity.motionY = (double)((float)this.random.nextGaussian() * scale + 0.2F);
                entity.motionZ = (double)((float)this.random.nextGaussian() * scale);
				world.spawnEntityInWorld(entity);
			}
		}

		super.breakBlock(world, x, y, z, blockID, metadata);
	}
}
