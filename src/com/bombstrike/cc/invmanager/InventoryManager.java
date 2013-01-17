package com.bombstrike.cc.invmanager;

import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.bombstrike.cc.invmanager.block.BlockPlayerManager;
import com.bombstrike.cc.invmanager.client.PacketHandler;
import com.bombstrike.cc.invmanager.client.gui.GuiHandler;
import com.bombstrike.cc.invmanager.item.ItemCatalyst;
import com.bombstrike.cc.invmanager.tileentity.TileEntityPlayerManager;

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
	
	// static stuff
	@SidedProxy(clientSide="com.bombstrike.cc.invmanager.client.ClientProxy", serverSide="com.bombstrike.cc.invmanager.CommonProxy")
	public static CommonProxy proxy;
	@Instance("invmanager-peripheral")
	public static InventoryManager instance;
	public static Logger logger;
	//public static int blockPlayerManagerId;
	public static BlockPlayerManager blockPlayerManager;
	public static ItemCatalyst itemCatalyst;
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
			Property blockPlayerManagerProperty = cfg.getBlock("playerManager", 1250);
			blockPlayerManagerProperty.comment = "The block ID for the player manager peripheral";
			blockPlayerManager = new BlockPlayerManager(blockPlayerManagerProperty.getInt(1250));
			
			Property itemCatalystProperty = cfg.getItem("itemCatalyst", 5500);
			itemCatalystProperty.comment = "The catalyst item for the plate";
			itemCatalyst = new ItemCatalyst(itemCatalystProperty.getInt(5500));
			
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
		//blockPlayerManager = new BlockPlayerManager(blockPlayerManagerId);
		// have fun with the registry
		GameRegistry.registerBlock(blockPlayerManager, ItemBlock.class, blockPlayerManager.getBlockName());
		GameRegistry.registerTileEntity(TileEntityPlayerManager.class, blockPlayerManager.getBlockName());
		LanguageRegistry.addName(blockPlayerManager, "Player Manager Peripheral");
		
		GameRegistry.registerItem(itemCatalyst, itemCatalyst.getItemName());
		LanguageRegistry.addName(itemCatalyst, "Inventory Catalyst");

		proxy.registerRenderInformation();
		proxy.registerTileEntityRenderers();
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		// add recipe
	}
	
	@PostInit
	public void postInit(FMLPostInitializationEvent postInit)
	{
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
		GameRegistry.addRecipe(new ItemStack(blockPlayerManager, 1), "ttt", "rer", "ppp", 't', Block.thinGlass, 'r', Item.redstone, 'e', Item.enderPearl, 'p', Block.stoneSingleSlab);
		GameRegistry.addRecipe(new ItemStack(itemCatalyst, 1), "lrl", "rpr", "lrl", 'l', Block.oreLapis, 'r', Item.redstone, 'g', Item.enderPearl);
	}
}

