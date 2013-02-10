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

public class BlockPlayerManager extends BlockContainer {
	public BlockPlayerManager(int blockId) {
		super(blockId, Material.iron);
		setBlockName("basicPlayerManager");
		setHardness(3.0F);
		setStepSound(soundMetalFootstep);
		setBlockBounds(1.0F/16.0F, 0.0F, 1.0F/16.0F, 15.0F/16.0F, 1.0F/8.0F, 15.0F/16.0F);
		setCreativeTab(CreativeTabs.tabRedstone);
		setRequiresSelfNotify();
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityPlayerManager(TYPE.BASIC);
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
}
