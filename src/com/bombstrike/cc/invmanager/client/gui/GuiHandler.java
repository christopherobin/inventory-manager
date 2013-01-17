package com.bombstrike.cc.invmanager.client.gui;

import com.bombstrike.cc.invmanager.inventory.ContainerPlayerManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityPlayerManager) {
			return new ContainerPlayerManager(player.inventory, (TileEntityPlayerManager)te);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityPlayerManager) {
			return new GuiPlayerManager(player.inventory, (TileEntityPlayerManager)te);
		}
		return null;
	}

}
