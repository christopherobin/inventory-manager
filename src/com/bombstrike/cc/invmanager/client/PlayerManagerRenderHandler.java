package com.bombstrike.cc.invmanager.client;

import com.bombstrike.cc.invmanager.InventoryManager;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class PlayerManagerRenderHandler implements ISimpleBlockRenderingHandler {
	private ModelPlayerManager model = new ModelPlayerManager();
	private TileEntityPlayerManagerRenderer pmrenderer;
	
	public PlayerManagerRenderHandler(TileEntityPlayerManagerRenderer renderer) {
		pmrenderer = renderer;
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {
		pmrenderer.render(0.5, 0.5, 0.5, false, 0, 0xF);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory() {
		return true;
	}

	@Override
	public int getRenderId() {
		return InventoryManager.renderId;
	}

}
