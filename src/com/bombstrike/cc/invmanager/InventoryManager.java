package com.bombstrike.cc.invmanager;

import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.bombstrike.cc.invmanager.block.BlockInventoryManager;
import com.bombstrike.cc.invmanager.block.BlockPlayerManager;
import com.bombstrike.cc.invmanager.block.BlockPlayerManagerComputer;
import com.bombstrike.cc.invmanager.client.PacketHandler;
import com.bombstrike.cc.invmanager.tileentity.BaseManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityInventoryManager;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

import cpw.mods.fml.common.Loader;
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

@Mod(modid="invmanager-peripheral", name=InventoryManager.MODNAME, version="1.0", dependencies="required-after:Forge@[6.3.0.0,)")
@NetworkMod(clientSideRequired=false, serverSideRequired=true, channels={InventoryManager.CHANNEL}, packetHandler = PacketHandler.class)
public class InventoryManager
{
	// constants
	public static final String MODNAME = "InventoryManager Peripherals";
	public static final String CHANNEL = "ccinvmanager";
	public static final String GFXDIR = "/com/bombstrike/cc/invmanager/gfx/";
	
	// static stuff
	@SidedProxy(clientSide="com.bombstrike.cc.invmanager.client.ClientProxy", serverSide="com.bombstrike.cc.invmanager.CommonProxy")
	public static CommonProxy proxy;
	@Instance("invmanager-peripheral")
	public static InventoryManager instance;
	public static Logger logger;
	//public static int blockPlayerManagerId;
	public static BlockPlayerManager blockBasicPlayerManager;
	public static BlockPlayerManagerComputer blockComputerPlayerManager;
	public static BlockInventoryManager blockInventoryManager;
	public static int renderId;
	
	/**
	 * Setup configuration and instanciate blocks
	 * @param preinit
	 */
	@PreInit
	public void preInit(FMLPreInitializationEvent preinit)
	{
		logger = preinit.getModLog();
		logger.info("Starting " + MODNAME);

		Configuration cfg = new Configuration(preinit.getSuggestedConfigurationFile());
		try {
			cfg.load();
			// retrieve our block info
			Property blockBasicPlayerManagerProperty = cfg.getBlock("basicPlayerManager", 1250);
			blockBasicPlayerManagerProperty.comment = "The block ID for the player manager plate";
			blockBasicPlayerManager = new BlockPlayerManager(blockBasicPlayerManagerProperty.getInt(1250));
			
			if (Loader.isModLoaded("ComputerCraft")) {
				Property blockComputerPlayerManagerProperty = cfg.getBlock("computerPlayerManager", 1251);
				blockComputerPlayerManagerProperty.comment = "The block ID for the computer player manager peripheral";
				blockComputerPlayerManager = new BlockPlayerManagerComputer(blockComputerPlayerManagerProperty.getInt(1251));
			}
			
			Property blockInventoryManagerProperty = cfg.getBlock("inventoryManager", 1252);
			blockInventoryManagerProperty.comment = "Block ID for the Inventory Manager Peripheral";
			blockInventoryManager = new BlockInventoryManager(blockInventoryManagerProperty.getInt(1252));
		} catch (Exception e) {
			logger.severe(MODNAME + " encountered an exception while trying to access it's configuration file:\n" + e);
		} finally {
			cfg.save();
		}
	}
	
	/**
	 * Register the blocks and items, setup proxies
	 * @param init
	 */
	@Init
	public void init(FMLInitializationEvent init)
	{
		// have fun with the registry
		GameRegistry.registerBlock(blockBasicPlayerManager, ItemBlock.class, blockBasicPlayerManager.getBlockName());
		GameRegistry.registerTileEntity(TileEntityPlayerManager.class, blockBasicPlayerManager.getBlockName());
		LanguageRegistry.addName(blockBasicPlayerManager, "Player Manager Plate");

		if (Loader.isModLoaded("ComputerCraft")) {
			GameRegistry.registerBlock(blockInventoryManager, ItemBlock.class, blockInventoryManager.getBlockName());
			GameRegistry.registerTileEntity(TileEntityInventoryManager.class, blockInventoryManager.getBlockName());
			LanguageRegistry.addName(blockInventoryManager, "Inventory Manager Peripheral");

			GameRegistry.registerBlock(blockComputerPlayerManager, ItemBlock.class, blockComputerPlayerManager.getBlockName());
			GameRegistry.registerTileEntity(TileEntityPlayerManager.class, blockComputerPlayerManager.getBlockName());
			LanguageRegistry.addName(blockComputerPlayerManager, "Player Manager Peripheral");
		}
		
		proxy.registerRenderInformation();
		proxy.registerTileEntityRenderers();
		proxy.prepareApis();
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		// add recipe
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent postInit)
	{
		GameRegistry.addRecipe(new ItemStack(blockBasicPlayerManager, 1), "ttt", "geg", "ggg", 't', Block.thinGlass, 'e', Item.enderPearl, 'g', Item.ingotGold);
		if (Loader.isModLoaded("ComputerCraft")) {
			GameRegistry.addRecipe(new ItemStack(blockComputerPlayerManager, 1), "ttt", "rer", "ppp", 't', Block.thinGlass, 'r', Item.redstone, 'e', Item.enderPearl, 'p', Block.stoneSingleSlab);
		}
	}
}

