package exnihiloomnia.compatibility.waila;

import java.util.List;

import exnihiloomnia.ENOConfig;
import exnihiloomnia.blocks.barrels.BlockBarrel;
import exnihiloomnia.blocks.barrels.tileentity.TileEntityBarrel;
import exnihiloomnia.blocks.crucibles.BlockCrucible;
import exnihiloomnia.blocks.crucibles.tileentity.TileEntityCrucible;
import exnihiloomnia.blocks.leaves.BlockInfestedLeaves;
import exnihiloomnia.blocks.leaves.TileEntityInfestedLeaves;
import exnihiloomnia.blocks.sieves.BlockSieve;
import exnihiloomnia.blocks.sieves.tileentity.TileEntitySieve;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class WailaCompatibility implements IWailaDataProvider {

	public static void initialize() {
		FMLInterModComms.sendMessage("Waila", "register", "exnihiloomnia.compatibility.waila.WailaCompatibility.register");
	}

	public static void register(IWailaRegistrar registrar) {
		WailaCompatibility instance = new WailaCompatibility();
		registrar.registerBodyProvider(instance, BlockSieve.class);
		registrar.registerBodyProvider(instance, BlockBarrel.class);
		registrar.registerBodyProvider(instance, BlockCrucible.class);
		registrar.registerBodyProvider(instance, BlockInfestedLeaves.class);
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return null;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		if (accessor.getBlock() instanceof BlockSieve) {
			TileEntitySieve sieve = (TileEntitySieve) accessor.getTileEntity();
			addSieveBody(sieve, currenttip);
		}
		else if (accessor.getBlock() instanceof BlockBarrel) {
			TileEntityBarrel barrel = (TileEntityBarrel) accessor.getTileEntity();
			addBarrelBody(barrel, currenttip);
		}
		else if(accessor.getBlock() instanceof BlockCrucible) {
			TileEntityCrucible crucible = (TileEntityCrucible) accessor.getTileEntity();
			addCrucibleBody(crucible, currenttip);
		}
		else if (accessor.getBlock() instanceof BlockInfestedLeaves) {
			TileEntityInfestedLeaves leaves = (TileEntityInfestedLeaves) accessor.getTileEntity();
			addInfestedLeavesBody(leaves, currenttip);
		}

		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return null;
	}

	public void addSieveBody(TileEntitySieve sieve, List<String> tip) {
		if (!sieve.hasMesh() && !ENOConfig.classic_sieve) {
			tip.add("No Mesh");
		}
		else {
			if (!ENOConfig.classic_sieve)
				tip.add(sieve.getMesh().getDisplayName() + " " + (sieve.getMesh().getMaxDamage() - sieve.getMesh().getItemDamage()) + "/" + sieve.getMesh().getMaxDamage());

			if (!sieve.canWork()) {
				tip.add("Empty");
			}
			else {
				tip.add("Processing " + sieve.getContents().getDisplayName() + ": " + format(sieve.getProgress() * 100) + "%");
			}
		}
	}

	public void addBarrelBody(TileEntityBarrel barrel, List<String> tip) {
		String[] body = barrel.getState().getWailaBody(barrel);

		if (body != null) {
			for (String s : body) {
				tip.add(s);
			}
		}
	}

	public void addCrucibleBody(TileEntityCrucible crucible, List<String> tip)
	{
		FluidStack fluid = crucible.getFluid();

		if (fluid != null)
			tip.add(fluid.getLocalizedName() + " " + fluid.amount + "mB");
		else
			tip.add("No fluid");
		
		if (crucible.getLastItemAdded() != null)
			tip.add(crucible.getLastItemAdded().getDisplayName() + " " + crucible.getSolidContent() / 200 + "/1000");
		else 
			tip.add("No material");
		
		if (crucible.getMeltingSpeed() != 0)
			tip.add("Speed " + Math.round(crucible.getTrueSpeed()) + "mB/s");
		else
			tip.add("No heat source");
	}

	public void addInfestedLeavesBody(TileEntityInfestedLeaves leaves, List<String> tip) {
	    if (leaves.getProgress() == 1.0f)
	        tip.add("Infested");
        else
		    tip.add("Infesting " + Math.round(leaves.getProgress() * 100) + "%");
	}

	public String format(float input) {
		return String.format("%.0f", input);
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
		return null;
	}
}
