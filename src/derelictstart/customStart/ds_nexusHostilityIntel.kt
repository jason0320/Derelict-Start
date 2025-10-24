package data.derelictstart.customStart

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.elapsedDaysSinceGameStart
import org.magiclib.util.MagicCampaign
import kotlin.math.roundToInt

class ds_nexusHostilityIntel: BaseIntelPlugin(), EconomyTickListener {
    val factionlist = ArrayList<String>()
    var stamp: Long? = null
    var days = 90f
    val title = "Hostile Operations"
    val sprite = Global.getSettings().getSpriteName("intel", "hostile_activity")
    init {
        for (faction in Global.getSector().getAllFactions()) {
            val factionId = faction.getId()
            if (factionId == Factions.PLAYER) continue
            if (factionId == Factions.DERELICT) continue
            if (factionId == "nex_derelict") continue
            if (factionId == Factions.REMNANTS) continue
            if (factionId == Factions.OMEGA) continue
            if (factionId == Factions.TRITACHYON) continue
            if (factionId == "sotf_dustkeepers") continue
            if (factionId == "sotf_dustkeepers_proxies") continue
            if (factionId == "sotf_sierra_faction") continue
            if (factionId == "sotf_dreaminggestalt") continue

            factionlist.add(factionId)
        }
    }
    // the gist...
    // use reporteconomytick, check if player is near or in core worlds (check hyperspace loc if in hyper, otherwise check system dist to center)
    // plus check fleet points, probably like > 200?
    // if success, add this intel which tracks time from last spawn and player location
    // if time from last spawn is greater than some value within like 90 to 180 days and player is in core worlds, spawns several random faction hunter killer fleets to chase them down
    // where from? umm... have to figure that out. try locating a random market in all systems near the player, maybe? then spawn that fac? if it's not TT
    // maybe use a whitelist
    // once enough time passes from this intel first being added, triggers very large attack fleet and then stops entirely if defeated


    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String>? {
        return super.getIntelTags(map)
    }

    override fun autoAddCampaignMessage(): Boolean {
        return true
    }

    override fun getIcon(): String {
        return sprite
    }

    override fun getName(): String {
        return title
    }

    override fun getSortString(): String {
        return title
    }

    override fun getSortTier(): IntelInfoPlugin.IntelSortTier {
        return IntelInfoPlugin.IntelSortTier.TIER_2
    }

    override fun isHidden(): Boolean {
        return false
    }

    override fun isImportant(): Boolean {
        return true
    }
    override fun getCommMessageSound(): String {
        return getSoundMajorPosting()
    }

    override fun shouldRemoveIntel(): Boolean {
        return false
    }

    override fun createIntelInfo(info: TooltipMakerAPI?, mode: ListInfoMode?) {
        info!!.addTitle(title)
        info.addPara("Your fleet is perceived as a growing threat.", 5f)
        info.addPara("Others may be dispatched to hunt you down.", 5f)
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addPara("Your fleet has grown large enough to be considered an anomaly by numerous military operatives working within the Core Worlds.", 5f)
        info.addPara("Occasionally, you may be tailed by hunter-killer fleets if within the core worlds, or bounty hunters may follow you to the fringes.", 5f)
        info.addPara("They will not take you lightly, and do their best to outmatch your fleet.", 5f)

    }

    override fun getListInfoParam(): Any {
        return ListInfoMode.MESSAGES
    }

    override fun advanceImpl(amount: Float) {
        if (!Global.getSector().listenerManager.hasListenerOfClass(ds_nexusHostilityIntel::class.java)) {
            Global.getSector().listenerManager.addListener(ds_nexusHostilityIntel(), true)
        }
    }

    override fun reportEconomyTick(iterIndex: Int) {
        if (Global.getSector().playerFleet == null) return
        val pfleet = Global.getSector().playerFleet
       // if (Global.getSector().clock.elapsedDaysSinceGameStart() > 180f && Global.getSector().playerFleet.fleetPoints > 200f && hide){
      //      hide = false
      //      Global.getSector().campaignUI.addMessage(this)
      //  }

            val clock = Global.getSector().clock
            if (stamp == null) {
                stamp = clock.timestamp
                return
            }
            if (clock.getElapsedDaysSince(stamp!!) > days) {
                days = MathUtils.getRandomNumberInRange(120f, 240f)
                stamp = null

                val loc = getPlayerLocation()
                if (Misc.getDistance(loc, Vector2f(0f, 0f)) > 12000f && Misc.getDistance(loc, Vector2f(0f,0f)) < 35000f) {
                    val fleet = MagicCampaign.createFleetBuilder()
                    .setFleetFaction(Factions.INDEPENDENT)
                            .setReinforcementFaction(Factions.MERCENARY)
                            .setFleetName("Mercenary Hunters")
                            .setFleetType(FleetTypes.MERC_BOUNTY_HUNTER)
                            .setTransponderOn(true)
                            .setAssignmentTarget(pfleet)
                            .setAssignment(FleetAssignment.INTERCEPT)
                            .setMinFP((pfleet.fleetPoints*1.1f).coerceAtMost(600f).roundToInt())
                            .setQualityOverride(2f)
                            .setSpawnLocation(Misc.findNearestJumpPointTo(pfleet, true))
                            .create()
                    fleet.memoryWithoutUpdate.apply {
                        set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, true)
                        set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true)
                        set(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS, true)
                        set(MemFlags.MEMORY_KEY_PURSUE_PLAYER, true)
                        set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true)
                        set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true)
                        set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true)
                        set(MemFlags.FLEET_DO_NOT_IGNORE_PLAYER, true)
                    }
                    fleet.addScript(fleetDespawner(fleet, clock.timestamp))

                } else {
                    val elegiblemarkets = Misc.getNearbyMarkets(getPlayerLocation(), 25f).filter { factionlist.contains(it.factionId) && it.size > 3 && it.hasSpaceport() && !it.isPlayerOwned && it.primaryEntity != null }
                    if (elegiblemarkets.isEmpty()) return
                    val marketpicker = WeightedRandomPicker<MarketAPI>()
                    marketpicker.addAll(elegiblemarkets)
                    for (i in 0 until 3){
                        val spawnloc = marketpicker.pick()
                        val fleet = MagicCampaign.createFleetBuilder()
                                .setFleetName("${spawnloc.faction.displayName} Hunters")
                                .setFleetFaction(spawnloc.factionId)
                                .setMinFP((pfleet.fleetPoints/1.5f).coerceAtMost(250f).roundToInt())
                                .setQualityOverride(2f)
                                .setSpawnLocation(spawnloc.primaryEntity)
                                .setAssignment(FleetAssignment.INTERCEPT)
                                .setAssignmentTarget(pfleet)
                                .setQualityOverride(2f)
                                .setFleetType(FleetTypes.TASK_FORCE)
                                .create()
                        fleet.memoryWithoutUpdate.apply {
                            set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, true)
                            set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true)
                            set(MemFlags.MEMORY_KEY_IGNORE_PLAYER_COMMS, true)
                            set(MemFlags.MEMORY_KEY_PURSUE_PLAYER, true)
                            set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true)
                        }
                        fleet.addScript(fleetDespawner(fleet, clock.timestamp))
                    }
                }
            }

    }

    override fun reportEconomyMonthEnd() {

    }
}
fun getPlayerLocation(): Vector2f {
    val pfleet = Global.getSector().playerFleet
    if (pfleet.isInHyperspace || pfleet.starSystem == null){
        return pfleet.location
    }
    return pfleet.starSystem.hyperspaceAnchor.location
}
class fleetDespawner(var fleet: CampaignFleetAPI, var time: Long): EveryFrameScript{
    var done = false
    var check = IntervalUtil(1f,1f)
    override fun advance(amount: Float) {
        check.advance(amount)
        if (check.intervalElapsed()) {
            if (Global.getSector().clock.getElapsedDaysSince(time) > 120f && !fleet.isVisibleToPlayerFleet) {
                fleet.despawn()
                done = true
            }
        }
    }

    override fun isDone(): Boolean {
        return done
    }

    override fun runWhilePaused(): Boolean {
        return false
    }
}