package com.bombstrike.cc.invmanager.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager.TYPE;

public class BlockPlayerManagerComputer extends BlockPlayerManager {
	public BlockPlayerManagerComputer(int blockId) {
		super(blockId);
		setBlockName("playerManagerPeripheral");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityPlayerManager(TYPE.COMPUTER);
	}
}
