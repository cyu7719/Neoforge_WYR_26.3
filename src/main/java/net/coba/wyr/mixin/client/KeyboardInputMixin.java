package net.coba.wyr.mixin.client;

import net.coba.wyr.ClientEffectCache;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void wyr_scrambleKeys(CallbackInfo ci) {
        int scrambleState = ClientEffectCache.getScrambleState();
        if (scrambleState == 0) return;

        KeyboardInput self = (KeyboardInput)(Object)this;
        if (self.keyPresses == null) return;

        boolean originalUp = self.keyPresses.forward();
        boolean originalDown = self.keyPresses.backward();
        boolean originalLeft = self.keyPresses.left();
        boolean originalRight = self.keyPresses.right();

        boolean up = originalUp;
        boolean down = originalDown;
        boolean left = originalLeft;
        boolean right = originalRight;

        switch (scrambleState) {
            case 1: // Swap forward/back, left/right
                up = originalDown;
                down = originalUp;
                left = originalRight;
                right = originalLeft;
                break;
            case 2: // Rotate left (W->A, A->S, S->D, D->W)
                up = originalRight;
                left = originalUp;
                down = originalLeft;
                right = originalDown;
                break;
            case 3: // Rotate right (W->D, D->S, S->A, A->W)
                up = originalLeft;
                right = originalUp;
                down = originalRight;
                left = originalDown;
                break;
            case 4: // Swap diagonals (W<->A, S<->D)
                up = originalLeft;
                left = originalUp;
                down = originalRight;
                right = originalDown;
                break;
        }

        self.keyPresses = new net.minecraft.world.entity.player.Input(
                up,
                down,
                left,
                right,
                self.keyPresses.jump(),
                self.keyPresses.shift(),
                self.keyPresses.sprint()
        );
    }
}