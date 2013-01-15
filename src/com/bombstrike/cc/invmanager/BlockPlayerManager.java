package com.bombstrike.cc.invmanager;

import java.util.Random;

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

public class BlockPlayerManager extends BlockContainer {
	Class<?> computerEntityInterface = null;
	
	public BlockPlayerManager(int blockId) {
		super(blockId, Material.iron);
		setBlockName("Player Manager Peripheral");
		setHardness(3.0F);
		setStepSound(soundMetalFootstep);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F/8.0F, 1.0F);
		setCreativeTab(CreativeTabs.tabRedstone);
		setRequiresSelfNotify();

		try {
			computerEntityInterface = Class.forName("dan200.computer.shared.IComputerEntity");
		} catch (Exception e) {
			return;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
    {
		// for now return false
        return false;
    }
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving) {
		super.onBlockPlacedBy(world, x, y, z, entityliving);
		
		if (world.isRemote) return;

		recomputeConnections(world, x, y, z);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityPlayerManager();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public int getRenderType() {
		return InventoryManager.renderId;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID) {
		super.onNeighborBlockChange(world, x, y, z, blockID);
		
		if (world.isRemote) return;

		recomputeConnections(world, x, y, z);
	}
	
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
		// TODO: switch that thing to use a nice particle effect
		/*if ((world.getBlockMetadata(x, y, z) & 0x8) > 0) {
			int position = random.nextInt(4);
		    double origX = x + 0.125D;
		    double origY = y + 0.125D;
		    double origZ = z + 0.125D;
		    
		    // change corner randomly
		    switch (position) {
		    case 0:
		    	origZ += 0.75D;
		    	break;
		    case 1:
		    	origX += 0.75D;
		    	origZ += 0.75D;
		    	break;
		    case 2:
		    	origX += 0.75D;
		    	break;
		    }
		
		    world.spawnParticle("reddust", origX, origY, origZ, -1.0D, 1.0D, 0.0D);
		}*/
    }
	
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
    {
		if (world.isRemote) return;
		
    	if (entity instanceof EntityPlayer) {
			TileEntityPlayerManager tileEntity = (TileEntityPlayerManager)world.getBlockTileEntity(x, y, z);
			if (!tileEntity.isPlayerOn()) {
				tileEntity.setPlayer((EntityPlayer)entity);
				world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) | 0x8); // set fourth bit to 1 when plate is active
				// check again in a few ticks if the player is still there
				world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate());
			}
		}
    }
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random random)
    {
		// try and detect when the player leave the plate
		if (world.isRemote) return;

    	TileEntityPlayerManager tileEntity = (TileEntityPlayerManager)world.getBlockTileEntity(x, y, z);
    	if (tileEntity.isPlayerOn()) {
    		ChunkCoordinates pos = tileEntity.getPlayer().getPlayerCoordinates();
    		if (pos.posX != x || pos.posY != y || pos.posZ != z) {
    			tileEntity.setPlayer(null);
    			world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) & ~0x8); // remove 4th bit
    		} else {
    			// check again in N ticks
    			world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate());
    		}
    	}
    }
	
	@Override
	public int tickRate()
    {
        return 10;
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
