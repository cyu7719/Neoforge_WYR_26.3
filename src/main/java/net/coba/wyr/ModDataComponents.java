package net.coba.wyr;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.coba.wyr.WYR;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, WYR.MODID);

    // --- Codec for saving to disk (This part is fine) ---
    private static final Codec<ItemStack> OVERSIZED_STACK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(stack -> BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem())),
            Codec.INT.fieldOf("count").forGetter(ItemStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
    ).apply(instance, ItemStack::new));

    private static final Codec<ItemContainerContents> BACKPACK_CONTENTS_CODEC =
            OVERSIZED_STACK_CODEC.listOf().xmap(ItemContainerContents::fromItems, contents -> {
                NonNullList<ItemStack> list = NonNullList.create();
                contents.copyInto(list);
                return list;
            });


    // --- Stream Codec for networking (THE FIX) ---

    // First, we define a robust codec for a NON-EMPTY item stack using Minecraft's safe composite builder.
    // This handles all the complex parts correctly.
    private static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> FULL_ITEM_STACK_STREAM_CODEC = StreamCodec.of(
            (buf, stack) -> {
                ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()));
                ByteBufCodecs.VAR_INT.encode(buf, stack.getCount());
                DataComponentPatch.STREAM_CODEC.encode(buf, stack.getComponentsPatch());
            },
            (buf) -> {
                var itemHolder = ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf);
                var count = ByteBufCodecs.VAR_INT.decode(buf);
                var components = DataComponentPatch.STREAM_CODEC.decode(buf);
                var stack = new ItemStack(itemHolder, count);
                stack.applyComponents(components);
                return stack;
            }
    );

    // Second, we wrap our robust codec with a simple check for empty stacks.
    // This is the final, stable version that the Mixin will use.
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OVERSIZED_STACK_STREAM_CODEC = StreamCodec.of(
            (buf, stack) -> {
                if (stack.isEmpty()) {
                    buf.writeBoolean(false); // Signal that the stack is empty
                } else {
                    buf.writeBoolean(true); // Signal that there is data to read
                    FULL_ITEM_STACK_STREAM_CODEC.encode(buf, stack);
                }
            },
            (buf) -> {
                if (buf.readBoolean()) { // If there is data...
                    return FULL_ITEM_STACK_STREAM_CODEC.decode(buf);
                } else { // Otherwise...
                    return ItemStack.EMPTY;
                }
            }
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, ItemContainerContents> BACKPACK_CONTENTS_STREAM_CODEC =
            OVERSIZED_STACK_STREAM_CODEC.apply(ByteBufCodecs.list()).map(ItemContainerContents::fromItems, contents -> {
                NonNullList<ItemStack> list = NonNullList.create();
                contents.copyInto(list);
                return list;
            });

    // --- Final Component Registration ---
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> BACKPACK_CONTENTS =
            DATA_COMPONENTS.register("backpack_contents", () -> DataComponentType.<ItemContainerContents>builder()
                    .persistent(BACKPACK_CONTENTS_CODEC)
                    .networkSynchronized(BACKPACK_CONTENTS_STREAM_CODEC)
                    .build()
            );
}