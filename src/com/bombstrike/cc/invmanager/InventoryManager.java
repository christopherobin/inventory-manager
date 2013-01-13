package com.bombstrike.cc.invmanager;

import java.util.logging.Logger;

import com.bombstrike.cc.invmanager.client.PacketHandler;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import dan200.computer.api.ComputerCraftAPI;

@Mod(modid="invmanager-peripheral", name=InventoryManager.MODNAME, version="1.0", dependencies="required-after:ComputerCraft@[1.481,);required-after:Forge@[6.3.0.0,)")
@NetworkMod(clientSideRequired=false, serverSideRequired=true, channels={InventoryManager.CHANNEL}, packetHandler = PacketHandler.class)
public class InventoryManager
{
	// constants
	public static final String MODNAME = "InventoryManager Peripherals";
	public static final String CHANNEL = "ccinvmanager";
	
	// static stuff
	@SidedProxy(clientSide="com.bombstrike.cc.invmanager.client.ClientProxy", serverSide="com.bombstrike.cc.invmanager.CommonProxy")
	public static CommonProxy proxy;
	@Instance("InventoryManager")
	public static InventoryManager instance;
	public static Logger logger;
	public int blockPMid;
	public static BlockPlayerManager blockPM;
	public static ItemPlayerManager itemPM;
	public static int renderId;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent preinit)
	{
		logger = preinit.getModLog();
		logger.info("Starting " + MODNAME);

		Configuration cfg = new Configuration(preinit.getSuggestedConfigurationFile());
		try {
			cfg.load();
			// retrieve our block info
			Property blockPMprop = cfg.getBlock("playerManager", 1250);
			blockPMprop.comment = "The block ID for the player manager peripheral";
			blockPMid = blockPMprop.getInt(1250);
		} catch (Exception e) {
			logger.severe(MODNAME + " encountered an exception while trying to access it's configuration file:\n" + e);
		} finally {
			cfg.save();
		}
	}
	
	@Init
	public void init(FMLInitializationEvent init)
	{
		// create blocks
		blockPM = new BlockPlayerManager(blockPMid);
		// have fun with the registry
		GameRegistry.registerBlock(blockPM, ItemPlayerManager.class, blockPM.getBlockName());
		GameRegistry.registerTileEntity(TileEntityPlayerManager.class, "tile." + blockPM.getBlockName());
		LanguageRegistry.addName(blockPM, "Player Manager Peripheral");

		proxy.registerRenderInformation();
		proxy.registerTileEntityRenderers();
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		// add recipe
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent postInit)
	{
		GameRegistry.addRecipe(new ItemStack(blockPM, 1), "ppp", "rcr", "sss", 'p', Block.thinGlass, 'r', Item.redstone, 'c', Block.chest, 's', Block.stone);
		// register recipes?
		blockPM.setCreativeTab(ComputerCraftAPI.getCreativeTab());
	}
}

