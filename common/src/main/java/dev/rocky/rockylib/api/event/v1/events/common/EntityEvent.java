package dev.rocky.rockylib.api.event.v1.events.common;

import dev.rocky.rockylib.api.event.v1.Event;
import dev.rocky.rockylib.api.event.v1.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class EntityEvent {
    /**
     * An event that is called after an entity is damaged. This is fired from {@link LivingEntity#hurt} after damage
     * is applied, or after that damage was blocked by a shield.
     *
     * <p>The base damage taken is the damage initially applied to the entity. Damage taken is the amount of damage the
     * entity actually took, after effects such as shields and extra freezing damage are applied. Damage taken does NOT
     * include damage reduction from armor and enchantments.
     *
     * <p>This event is not fired if the entity was killed by the damage.
     */
    public static final Event<AfterDamage> AFTER_DAMAGE = EventFactory.createArrayBacked(
            AfterDamage.class, callbacks -> (entity, source, baseDamageTaken, damageTaken, blocked) -> {
                for (AfterDamage callback : callbacks) {
                    callback.afterDamage(entity, source, baseDamageTaken, damageTaken, blocked);
                }
            }
    );

    @FunctionalInterface
    public interface AfterDamage {
        /**
         * Called after a living entity took damage, unless they were killed. The base damage taken is given as damage
         * taken before armor or enchantments are applied, but after other effects like shields are applied.
         *
         * @param entity          the entity that was damaged
         * @param source          the source of the damage
         * @param baseDamageTaken the amount of damage initially dealt
         * @param damageTaken     the amount of damage actually taken by the entity, before armor and enchantment effects
         * @param blocked         whether the damage was blocked by a shield
         */
        void afterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken,
                boolean blocked);
    }
}
