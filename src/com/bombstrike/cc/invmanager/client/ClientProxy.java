package com.bombstrike.cc.invmanager.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

import com.bombstrike.cc.invmanager.CommonProxy;
import com.bombstrike.cc.invmanager.InventoryManager;
import com.bombstrike.cc.invmanager.TileEntityPlayerManager;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerTileEntityRenderers() {
		super.registerTileEntityRenderers();

		// setup render id
		InventoryManager.renderId = RenderingRegistry.getNextAvailableRenderId();
		// create our entity renderer
		TileEntityPlayerManagerRenderer entityRenderer = new TileEntityPlayerManagerRenderer();
		// then setup the handlers
		RenderingRegistry.registerBlockHandler(new PlayerManagerRenderHandler(entityRenderer));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPlayerManager.class, entityRenderer);
	}
	
	@Override
	public void registerRenderInformation() {
		super.registerRenderInformation();
		MinecraftForgeClient.preloadTexture("/bombstrike/playermanager/blocks.png");
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}
}
