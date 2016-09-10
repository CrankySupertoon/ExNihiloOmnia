package exnihiloomnia;

import java.io.File;
import java.util.List;

import exnihiloomnia.compatibility.ENOCompatibility;
import exnihiloomnia.compatibility.ENOOres;
import exnihiloomnia.fluids.ENOFluids;
import exnihiloomnia.registries.ENORegistries;
import exnihiloomnia.util.enums.EnumOre;
import net.minecraft.init.Items;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import exnihiloomnia.blocks.ENOBlocks;
import exnihiloomnia.blocks.barrels.states.BarrelStates;
import exnihiloomnia.client.textures.ENTextures;
import exnihiloomnia.crafting.ENOCrafting;
import exnihiloomnia.crafting.recipes.MobDrops;
import exnihiloomnia.entities.ENOEntities;
import exnihiloomnia.items.ENOBucketHandler;
import exnihiloomnia.items.ENOFuelHandler;
import exnihiloomnia.items.ENOItems;
import exnihiloomnia.items.materials.ENOToolMaterials;
import exnihiloomnia.proxy.Proxy;
import exnihiloomnia.world.ENOWorld;

@Mod(name = ENO.NAME, modid = ENO.MODID, version = ENO.VERSION)//, dependencies = "required-after:VeinMiner")
public class ENO
{
	@Instance(ENO.MODID)
	public static ENO instance;
	
	public static final String NAME = "Ex Nihilo Omnia";
	public static final String MODID = "exnihiloomnia";
	public static final String VERSION = "1.0.2";

	@SidedProxy(serverSide = "exnihiloomnia.proxy.ServerProxy", clientSide = "exnihiloomnia.proxy.ClientProxy")
	public static Proxy proxy;

	public static Logger log = LogManager.getLogger(ENO.NAME);
	public static String path;
	public static Configuration config;

	public static List<EnumOre> oreList;

	@EventHandler
	public void preInitialize(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);

		path = event.getModConfigurationDirectory().getAbsolutePath() + File.separator + "ExNihiloOmnia" + File.separator;
		config = new Configuration(new File(path + "ExNihiloOmnia.cfg"));

		config.load();
		ENOConfig.configure(config);
        oreList = ENOOres.getActiveOres();

		ENOFluids.register();
		ENOToolMaterials.configure();
		ENOBlocks.init();
		ENOItems.init();

        ENOBucketHandler.registerBuckets();
		ENOCrafting.configure(config);
		BarrelStates.configure(config);
		ENOWorld.configure(config);

		ENOEntities.configure();
		ENOCompatibility.configure(config);

		proxy.registerModels();
		proxy.registerRenderers();
		
		if(config.hasChanged())
			config.save();

	}

	@EventHandler
	public void doInitialize(FMLInitializationEvent event)
	{
        ENOCrafting.registerRecipes();
        ENORegistries.configure(config);

        ENOOres.addCrafting();
		ENOOres.addSmelting();
        proxy.registerColors();

		ENOWorld.registerWorldProviders();

		GameRegistry.registerFuelHandler(new ENOFuelHandler());
        MinecraftForge.EVENT_BUS.register(new ENOBucketHandler());
        ENOCompatibility.initialize();
	}

	@EventHandler
	public void postInitialize(FMLPostInitializationEvent event)
	{
		ENOOres.addOreDict();
    }

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchEvent(TextureStitchEvent.Pre e)
	{
		ENTextures.registerCustomTextures(e.getMap());
		ENTextures.setMeshTextures();
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load e)
	{
		if (!e.getWorld().isRemote && e.getWorld() instanceof WorldServer)
		{
			ENOWorld.load(e.getWorld());
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent e)
	{
		if (e.side == Side.SERVER && e.phase == TickEvent.Phase.START)
		{
			ENOWorld.tick(e.world);
		}
	}

	@SubscribeEvent
	public void onEntityDrop(LivingDropsEvent event) 
	{
		MobDrops.onMobDeath(event);
	}

	@SubscribeEvent
	public void onFish(LootTableLoadEvent event) {
		if (event.getName().equals(LootTableList.GAMEPLAY_FISHING_TREASURE)) {
			LootPool main = event.getTable().getPool("main");
			main.addEntry(new LootEntryItem(Items.REEDS, 1, 2, new LootFunction[0], new LootCondition[0], MODID + ":sugarcane"));
		}
	}
}