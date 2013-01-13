package com.bombstrike.cc.invmanager;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import buildcraft.transport.TileGenericPipe;

import cpw.mods.fml.common.Loader;

import dan200.computer.api.ComputerCraftAPI;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityRainFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockPlayerManager extends BlockContainer {
	public BlockPlayerManager(int blockId) {
		super(blockId, Material.iron);
		setBlockName("Player Manager Peripheral");
		setHardness(3.0F);
		setStepSound(soundMetalFootstep);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F/8.0F, 1.0F);
		setLightValue(5.0F/15.0F);
		setCreativeTab(ComputerCraftAPI.getCreativeTab());
		setRequiresSelfNotify();
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

		// heavily inspired from BuildCraft
		double dx = x - entityliving.posX;
		double dz = z - entityliving.posZ;

		double angle = Math.atan2(dz, dx) / Math.PI * 180 + 180;
		ForgeDirection orientation;
		if (angle < 45 || angle > 315)
			orientation = ForgeDirection.EAST;
		else if (angle < 135)
			orientation = ForgeDirection.SOUTH;
		else if (angle < 225)
			orientation = ForgeDirection.WEST;
		else
			orientation = ForgeDirection.NORTH;

		// initial meta data is this, ordinal between 0 and 5
		world.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite().ordinal());
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

		if ((blockID == 0) || (Item.itemsList[blockID].getItemName().compareTo("tile.cccomputer") == 0)) {
			//if (blockID > 0) InventoryManager.logger.info("Neighbor block changed 3: {" + x + ";" + y + ";" + z + "}@" + Item.itemsList[blockID].getItemName() + "/" + world.isRemote);
			//else InventoryManager.logger.info("Neighbor block changed 3: {" + x + ";" + y + ";" + z + "}@0/" + world.isRemote);
			recomputeConnections(world, x, y, z);
		}
	}
	
	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
		if (((TileEntityPlayerManager)world.getBlockTileEntity(x, y, z)).isPlayerOn()) {
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
		}
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
		// search for nearby computers
		int connections = 0;
		int shift = 0;
		ForgeDirection directions[] = { ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.NORTH };
		for (ForgeDirection direction : directions) {
			TileEntity te = Utils.getTileNeighbor(world, x, y, z, direction);
			if (te != null) {
				// is that a computer?
				if (Item.itemsList[te.getBlockType().blockID].getItemName().compareTo("tile.cccomputer") == 0) {
					// create connection
					connections |= 0x1 << shift;
				}
				//InventoryManager.logger.info("Neighbor block: {" + te.xCoord + ";" + te.yCoord + ";" + te.zCoord + "}@" + Item.itemsList[te.getBlockType().blockID].getItemName());
			}
			shift++;
		}
		// set the render state on the tile entity
		if (world.getBlockTileEntity(x, y, z) instanceof TileEntityPlayerManager) {
			((TileEntityPlayerManager)world.getBlockTileEntity(x, y, z)).setConnections(connections);
		}
	}
}
