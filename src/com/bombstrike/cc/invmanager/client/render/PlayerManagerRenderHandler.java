package com.bombstrike.cc.invmanager.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.client.render.TileEntityPlayerManagerRenderer.STATUS;

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
		pmrenderer.render(-0.5F, 0, -0.5F, STATUS.ONLINE, 0x0);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		return true;
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
