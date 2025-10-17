package data.derelictstart.customStart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI

class ds_chauffeurAI(var fleet: CampaignFleetAPI, var market: MarketAPI): BaseAssignmentAI(){
    override fun giveInitialAssignments() {
        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, Global.getSector().playerFleet, 120f)
    }

    override fun pickNext() {
        if (fleet.containingLocation != null && fleet.containingLocation.getEntityById(market.primaryEntity.id) != null){
            fleet.removeFirstAssignment()
            fleet.addAssignment(FleetAssignment.ATTACK_LOCATION, market.primaryEntity, 30f)
        } else if (Global.getSector().playerFleet != null && fleet.currentAssignment?.assignment != FleetAssignment.ORBIT_PASSIVE){
            fleet.removeFirstAssignment()
            fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, Global.getSector().playerFleet, 30f)
        }
    }
}