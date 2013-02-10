/**
 * 
 */
package com.bombstrike.cc.invmanager.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.tileentity.BaseManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityInventoryManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author crobin
 * 
 */
public class BlockInventoryManager extends BlockBaseManager {
	/**
	 * @param par1
	 * @param par2Material
	 */
	public BlockInventoryManager(int blockId) {
		super(blockId, Material.iron);
		setBlockName("inventoryManager");
		setHardness(3.0F);
		setStepSound(soundMetalFootstep);
		setCreativeTab(CreativeTabs.tabRedstone);
		setBlockBounds(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F, 15.0F / 16.0F,
				15.0F / 16.0F, 15.0F / 16.0F);
		setRequiresSelfNotify();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityInventoryManager();
	}

	@Override
	public String getTextureFile() {
		return InventoryManager.GFXDIR.concat("blocks.png");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockTexture(IBlockAccess blockAccess, int x,
			int y, int z, int side) {
		TileEntity entity = blockAccess.getBlockTileEntity(x, y, z);
		if (entity instanceof BaseManager) {
			// check if the inventory manager is connected to a computer
			BaseManager manager = (BaseManager)entity;
			if (manager.getComputerConnections() != 0) {
				// add the green dots on any side that has a working chest
				int chestConnections = manager.getChestConnections();
				if (chestConnections > 0) {
					if ((chestConnections & (0x1 << side)) > 0) {
						return 2;
					}
				}
				return 1;
			}
		}
		
		return 0;
	}
}
