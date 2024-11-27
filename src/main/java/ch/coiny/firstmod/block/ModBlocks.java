package ch.coiny.firstmod.block;

import ch.coiny.firstmod.FirstMod;
import ch.coiny.firstmod.block.custom.AlexandriteLampBlock;
import ch.coiny.firstmod.block.custom.MagicBLock;
import ch.coiny.firstmod.item.ModItems;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FirstMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static final RegistryObject<Block> ALEXANDRITE_BLOCK = registerBlock("alexandrite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));
    public static final RegistryObject<Block> RAW_ALEXANDRITE_BLOCK = registerBlock("raw_alexandrite_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ALEXANDRITE_ORE = registerBlock("alexandrite_ore",
            () -> new DropExperienceBlock(UniformInt.of(2,4), BlockBehaviour.Properties.of()
                    .strength(4f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ALEXANDRITE_DEEPSLATE_ORE = registerBlock("alexandrite_deepslate_ore",
            () -> new DropExperienceBlock(UniformInt.of(6,9), BlockBehaviour.Properties.of()
                    .strength(5f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    public static RegistryObject<StairBlock> ALEXANDRITE_STAIRS = registerBlock("alexandrite_stairs",
            () -> new StairBlock(ModBlocks.ALEXANDRITE_BLOCK.get().defaultBlockState(),
                    BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops()));
    public static RegistryObject<SlabBlock> ALEXANDRITE_SLAB = registerBlock("alexandrite_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops()));

    public static RegistryObject<PressurePlateBlock> ALEXANDRITE_PRESSURE_PLATE = registerBlock("alexandrite_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.IRON,
                    BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops()));
    public static RegistryObject<ButtonBlock> ALEXANDRITE_BUTTON = registerBlock("alexandrite_button",
            () -> new ButtonBlock(BlockSetType.IRON, 1,
                    BlockBehaviour.Properties.of().strength(3f)
                            .requiresCorrectToolForDrops().noCollission()));

    public static RegistryObject<FenceBlock> ALEXANDRITE_FENCE = registerBlock("alexandrite_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops()));
    public static RegistryObject<FenceGateBlock> ALEXANDRITE_FENCE_GATE = registerBlock("alexandrite_fence_gate",
            () -> new FenceGateBlock(WoodType.ACACIA, BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops()));
    public static RegistryObject<WallBlock> ALEXANDRITE_WALL = registerBlock("alexandrite_wall",
            () -> new WallBlock(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops()));

    public static RegistryObject<DoorBlock> ALEXANDRITE_DOOR = registerBlock("alexandrite_door",
            () -> new DoorBlock(BlockSetType.IRON,
                    BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().noOcclusion()));
    public static RegistryObject<TrapDoorBlock> ALEXANDRITE_TRAPDOOR = registerBlock("alexandrite_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.IRON,
                    BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistryObject<Block> MAGIC_BLOCK = registerBlock("magic_block",
            () -> new MagicBLock(BlockBehaviour.Properties.of().strength(2).noLootTable()));
    public static final  RegistryObject<Block> ALEXANDRITE_LAMP = registerBlock("alexandrite_lamp",
            () -> new AlexandriteLampBlock(BlockBehaviour.Properties.of().strength(3f)
                    .lightLevel(state -> state.getValue(AlexandriteLampBlock.CLICKED) ? 15 : 0)));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name,toReturn);

        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}
