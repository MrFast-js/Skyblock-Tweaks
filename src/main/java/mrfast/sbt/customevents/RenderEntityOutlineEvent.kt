package mrfast.sbt.customevents

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItemFrame
import net.minecraftforge.fml.common.eventhandler.Event
import java.awt.Color
import java.util.function.Function


/**
 * Adapted from SkyblockAddons under MIT license
 * @link https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author BiscuitDevelopment
 */
open class RenderEntityOutlineEvent(
    /**
     * The entities we can outline. Note that this set and [.entitiesToOutline] are disjoint at all times.
     */
    var entitiesToChooseFrom: HashSet<Entity>? = null
) : Event() {
    /**
     * The entities to outline. This is progressively cumulated from [.entitiesToChooseFrom]
     */
    var entitiesToOutline: HashMap<Entity, Int> = HashMap()

    /**
     * Constructs the event, given the type and optional entities to outline.
     *
     * This will modify {@param potentialEntities} internally, so make a copy before passing it if necessary.
     *
     * @param potentialEntities the entities to outline
     */
    init {
        entitiesToChooseFrom?.let {
            entitiesToOutline = HashMap(it.size)
        }
    }

    /**
     * Conditionally queue entities around which to render entities.
     * Selects from the pool of [.entitiesToChooseFrom] to speed up the predicate testing on subsequent calls.
     * Is more efficient (theoretically) than calling [.queueEntityToOutline] for each entity because lists are handled internally.
     *
     * This function loops through all entities and so is not very efficient.
     * It's advisable to encapsulate calls to this function with global checks (those not dependent on an individual entity) for efficiency purposes.
     *
     * @param outlineColor a function to test
     */
    fun queueEntitiesToOutline(outlineColor: Function<Entity, Int?>?) {
        outlineColor ?: return
        entitiesToChooseFrom ?: computeAndCacheEntitiesToChooseFrom()

        val iterator = entitiesToChooseFrom!!.iterator()
        while (iterator.hasNext()) {
            val entity = iterator.next()
            outlineColor.apply(entity)?.let { color ->
                entitiesToOutline[entity] = color
                iterator.remove()
            }
        }
    }

    /**
     * Adds a single entity to the list of the entities to outline.
     *
     * @param entity the entity to add
     * @param outlineColor the color with which to outline
     */
    fun queueEntityToOutline(entity: Entity?, outlineColor: Color) {
        entity ?: return
        entitiesToChooseFrom ?: computeAndCacheEntitiesToChooseFrom()

        if (entitiesToChooseFrom!!.contains(entity)) {
            entitiesToOutline[entity] = outlineColor.rgb
            entitiesToChooseFrom!!.remove(entity)
        }
    }

    /**
     * Used for on-the-fly generation of entities. Driven by event handlers in a decentralized fashion.
     */
    private fun computeAndCacheEntitiesToChooseFrom() {
        val entities = Minecraft.getMinecraft().theWorld.loadedEntityList
        entitiesToChooseFrom = HashSet(entities.size)
        entities.filterTo(entitiesToChooseFrom!!) { entity ->
            entity != null && !(entity is EntityArmorStand && entity.isInvisible) && entity !is EntityItemFrame
        }
        entitiesToOutline = HashMap(entitiesToChooseFrom!!.size)
    }

    class Xray(entitiesToChooseFrom: HashSet<Entity>? = null) : RenderEntityOutlineEvent(entitiesToChooseFrom)

    class Normal(entitiesToChooseFrom: HashSet<Entity>? = null) : RenderEntityOutlineEvent(entitiesToChooseFrom)
}