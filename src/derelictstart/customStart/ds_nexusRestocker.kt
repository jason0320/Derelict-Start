package data.derelictstart.customStart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener
import com.fs.starfarer.api.impl.campaign.ids.Entities
import lunalib.lunaExtensions.getCustomEntitiesWithType

class ds_nexusRestocker : EconomyTickListener { // restocks nexii monthly
    override fun reportEconomyTick(iterIndex: Int) {
    }

    override fun reportEconomyMonthEnd() {
        val nexii = Global.getSector().getCustomEntitiesWithType(Entities.DERELICT_MOTHERSHIP)
        nexii.forEach {
            if (it.id!="derelict_mothership")
            {
                nexii.minus(it)
            }
        }
        nexii.forEach {
            if (it.cargo != null) {
                it.cargo.clear()
            }
            it.cargo.addAll(addNexusCargo(it))
        }
    }
}