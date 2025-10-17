package data.derelictstart.customStart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc

class ds_remnantStartCombatPlugin: BaseEveryFrameCombatPlugin() {
    val max = 20f


    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (!Global.getSector().memoryWithoutUpdate.getBoolean("\$ds_nexusStart")) return
        val playerfm = Global.getCombatEngine().getFleetManager(0)
        val pofficer = Global.getSector().playerFleet?.fleetData?.officersCopy ?: return
        if (pofficer.isNotEmpty()) return

            var bonus = 0f
            Global.getSector().playerFleet.fleetData.membersListCopy.forEach {
                if (!it.captain.isDefault && it.captain.isAICore && Misc.isUnremovable(it.captain)) {
                    bonus = (bonus + (it.captain.stats.level / 3f)).coerceAtMost(max)
                }
            }
            playerfm.modifyPercentMax("playercores", bonus)


        super.advance(amount, events)
    }
}