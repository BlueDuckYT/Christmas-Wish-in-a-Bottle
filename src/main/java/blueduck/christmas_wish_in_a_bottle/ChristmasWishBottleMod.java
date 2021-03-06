package blueduck.christmas_wish_in_a_bottle;

import blueduck.christmas_wish_in_a_bottle.registry.Registry;
import blueduck.jollyboxes.registry.JollyBoxesBlocks;
import blueduck.jollyboxes.registry.JollyBoxesSounds;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

import static blueduck.jollyboxes.JollyBoxesMod.CONFIG;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("christmas_wish_in_a_bottle")
public class ChristmasWishBottleMod
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public ChristmasWishBottleMod() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
//      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
//      Register the processIMC method for modloading
//      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);


        MinecraftForge.EVENT_BUS.addListener(this::onPlayerTick);

        Registry.init();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.getPersistentData().getInt("day_presents") > 0 && event.player.getEntityWorld().isNightTime()) {
            event.player.getPersistentData().putInt("night_presents", event.player.getPersistentData().getInt("day_presents") + event.player.getPersistentData().getInt("night_presents"));
            event.player.getPersistentData().putInt("day_presents", 0);

        }
        if (event.player.getPersistentData().getInt("night_presents") > 0 && event.player.getEntityWorld().isDaytime()) {
            BlockPos pos = event.player.getPosition();
            Boolean ring = false;
            for (int j = 0; j < event.player.getPersistentData().getInt("night_presents"); j++) {
                if (!event.player.getEntityWorld().isRemote()) {
                    for (int i = 0; i < event.player.getEntityWorld().getRandom().nextInt(5 + CONFIG.MINIMUM_PRESENTS.get()) + CONFIG.MINIMUM_PRESENTS.get(); i++) {
                        BlockPos pos2 = new BlockPos((pos.getX() + (event.player.getEntityWorld().getRandom().nextDouble() * 32) - 16), pos.getY() + 100, (double) (pos.getZ() + (event.player.getEntityWorld().getRandom().nextDouble() * 32) - 16));
                        if (getGroundPos(pos2, event.player.getEntityWorld()) != null) {
                            event.player.getEntityWorld().setBlockState(getGroundPos(pos2, event.player.getEntityWorld()), JollyBoxesBlocks.SMALL_JOLLY_BOX.get().getDefaultState());
                        }
                    }
                    ring = true;
                }
                if (ring) event.player.playSound(JollyBoxesSounds.SLEIGH_BELLS.get(), SoundCategory.AMBIENT, 1F, 1F);
            }
            event.player.getPersistentData().putInt("night_presents", 0);
        }
    }

    public BlockPos getGroundPos(BlockPos pos, World world) {
        for (int i = pos.getY(); i > 0; i--) {
            if (isValidPos(pos.down(pos.getY() - i), world) && (world.getBlockState(pos).equals(Blocks.AIR.getDefaultState()) || world.getBlockState(pos).equals(Blocks.SNOW.getDefaultState()))) {
                return pos.down(pos.getY() - i);
            }
        }
        return null;
    }

    public boolean isValidPos(BlockPos pos, World world) {
        return ((world.getBlockState(pos).equals(Blocks.AIR.getDefaultState()) || world.getBlockState(pos).equals(Blocks.SNOW.getDefaultState())) && world.getBlockState(pos.down()).isSolid());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
