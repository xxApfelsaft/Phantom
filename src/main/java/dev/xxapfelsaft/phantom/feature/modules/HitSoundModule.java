package dev.xxapfelsaft.phantom.feature.modules;

import com.dwarslooper.cactus.client.feature.module.Module;
import com.dwarslooper.cactus.client.feature.module.Module.Options;
import com.dwarslooper.cactus.client.systems.config.settings.impl.EnumSetting;
import com.dwarslooper.cactus.client.systems.config.settings.impl.EnumSetting.INamespaceOverrides;
import dev.xxapfelsaft.phantom.PhantomAddon;
import dev.xxapfelsaft.phantom.PhantomAttackHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

import java.util.function.Consumer;

public class HitSoundModule extends Module {

    public enum SoundMode implements INamespaceOverrides {
        catMeow(SoundEvents.CAT_AMBIENT_BABY.value()),
        catHiss(SoundEvents.CAT_HISS_BABY.value()),
        catPurr(SoundEvents.CAT_PURR_BABY.value()),
        catPurreow(SoundEvents.CAT_PURREOW_BABY.value()),
        catBeg(SoundEvents.CAT_BEG_FOR_FOOD_BABY.value()),
        catHurt(SoundEvents.CAT_HURT_BABY.value()),
        catDeath(SoundEvents.CAT_DEATH_BABY.value()),
        dolphinAttack(SoundEvents.DOLPHIN_ATTACK),
        wardenRoar(SoundEvents.WARDEN_ROAR),
        wardenSonicBoom(SoundEvents.WARDEN_SONIC_BOOM),
        blazeShoot(SoundEvents.BLAZE_SHOOT),
        creeperPrimed(SoundEvents.CREEPER_PRIMED),
        dragonHurt(SoundEvents.ENDER_DRAGON_HURT),
        dragonFireball(SoundEvents.DRAGON_FIREBALL_EXPLODE),
        evokerAttack(SoundEvents.EVOKER_PREPARE_ATTACK),
        ghastScream(SoundEvents.GHAST_SCREAM),
        witherShoot(SoundEvents.WITHER_SHOOT),
        explosion(SoundEvents.GENERIC_EXPLODE.value()),
        thunder(SoundEvents.LIGHTNING_BOLT_THUNDER),
        anvilLand(SoundEvents.ANVIL_LAND),
        fireworkLaunch(SoundEvents.FIREWORK_ROCKET_LAUNCH),
        beeSting(SoundEvents.BEE_STING),
        dragonGrowl(SoundEvents.ENDER_DRAGON_GROWL),
        goatPrepareRam(SoundEvents.GOAT_PREPARE_RAM),
        wolfArmorCrack(SoundEvents.WOLF_ARMOR_CRACK);

        private final SoundEvent sound;

        SoundMode(SoundEvent sound) {
            this.sound = sound;
        }

        public SoundEvent getSound() {
            return sound;
        }

        @Override
        public String getNamespace() {
            return "modules.hit_sound.settings.sound";
        }
    }

    private final Consumer<Entity> hitListener = this::onHit;
    private final EnumSetting<SoundMode> soundMode;

    public HitSoundModule() {
        super("hit_sound", PhantomAddon.CATEGORY, new Options());

        soundMode = new EnumSetting<>("Sound", SoundMode.catMeow);
        mainGroup.add(soundMode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        PhantomAttackHandler.register(hitListener);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        PhantomAttackHandler.unregister(hitListener);
    }

    private void onHit(Entity target) {
        if (!active()) return;

        SoundEvent sound = soundMode.get().getSound();
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(sound, 1.0f)
        );
    }
}
