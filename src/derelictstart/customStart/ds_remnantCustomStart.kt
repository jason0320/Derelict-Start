package data.derelictstart.customStart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.rules.MemKeys
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.CharacterCreationData
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.intel.FactionCommissionIntel
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest
import com.fs.starfarer.api.impl.campaign.rulecmd.NGCAddStandardStartingScript
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import exerelin.campaign.ExerelinSetupData
import exerelin.campaign.PlayerFactionStore
import exerelin.campaign.customstart.CustomStart
import exerelin.utilities.StringHelper
import lunalib.lunaExtensions.getCustomEntitiesWithType
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getNearbyFleets
import org.magiclib.kotlin.getStationFleet
import second_in_command.SCUtils
import kotlin.math.roundToInt


class ds_remnantCustomStart: CustomStart() {
    // need to do the following:
    // have cargo picker to buy cores (should be basically unlimited and uses remnant bucks) - can just copy my old code here
    // custom production menu to insta produce ships/hulls AND figure out shared market storage for non-markets (wtf??)
    // setup misc nexus interactions (tutorial text, etc etc bla bla)
    override fun execute(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        val data = memoryMap!![MemKeys.LOCAL]!!["\$characterData"] as CharacterCreationData

        dialog!!.textPanel.addPara("Note: Currently slightly experimental, may have some odd behavior.")
        dialog.textPanel.addPara("Start under the Explorarium faction, being able to access any Mothership for normal fleet operations.")
        dialog.textPanel.addPara("As you are starting with a Explorarium commission, you will likely be hostile to most factions.")

        //data.startingCargo.addCommodity(Commodities.GAMMA_CORE, 3f)
        //data.startingCargo.addCommodity(Commodities.BETA_CORE, 1f)
        data.startingCargo.addCrew(Global.getSettings().getHullSpec("revenant").maxCrew.roundToInt() + Global.getSettings().getHullSpec("phantom").maxCrew.roundToInt())
        data.startingCargo.credits.add(100000f)

        //AddRemoveCommodity.addCommodityGainText(Commodities.GAMMA_CORE, 3, dialog.textPanel)
        //AddRemoveCommodity.addCommodityGainText(Commodities.BETA_CORE, 1, dialog.textPanel)
        data.addStartingFleetMember("rampart_Standard", FleetMemberType.SHIP)
        data.addStartingFleetMember("berserker_Assault", FleetMemberType.SHIP)
        data.addStartingFleetMember("defender_PD", FleetMemberType.SHIP)
        data.addStartingFleetMember("picket_Assault", FleetMemberType.SHIP)
        data.addStartingFleetMember("sentry_FS", FleetMemberType.SHIP)
        data.addStartingFleetMember("warden_Defense", FleetMemberType.SHIP)
        data.addStartingFleetMember("revenant_Elite", FleetMemberType.SHIP)
        data.addStartingFleetMember("phantom_Elite", FleetMemberType.SHIP)
        ExerelinSetupData.getInstance().freeStart = true
        ExerelinSetupData.getInstance().randomStartLocation = false
        PlayerFactionStore.setPlayerFactionIdNGC(Factions.DERELICT)


        val tempFleet = FleetFactoryV3.createEmptyFleet(PlayerFactionStore.getPlayerFactionIdNGC(), FleetTypes.PATROL_SMALL, null)
        tempFleet.fleetData.apply {
            addFleetMember("rampart_Standard")
            addFleetMember("berserker_Assault")
            addFleetMember("defender_PD")
            addFleetMember("picket_Assault")
            addFleetMember("sentry_FS")
            addFleetMember("warden_Defense")
            addFleetMember("revenant_Elite")
            addFleetMember("phantom_Elite")
        }

        data.addScript {
            if (Global.getSettings().modManager.isModEnabled("second_in_command")) {
                var officer = SCUtils.createRandomSCOfficer("sc_automated")
                officer.increaseLevel(1)

                SCUtils.getPlayerData().addOfficerToFleet(officer)
                SCUtils.getPlayerData().setOfficerInEmptySlotIfAvailable(officer)
            }

            val fleet: CampaignFleetAPI = Global.getSector().playerFleet
            Global.getSector().getFaction(Factions.DERELICT).relToPlayer.rel = 100f
            Global.getSector().memoryWithoutUpdate.set("\$ds_nexusStart", true)
            val cargo = Global.getSector().playerFleet.cargo

            data.person.stats.addPoints(1)
            Global.getSector().playerPerson.stats.setSkillLevel("ds_paradeigma", 1f)
            NGCAddStandardStartingScript.adjustStartingHulls(fleet)

            fleet.fleetData.ensureHasFlagship()
            for (member in fleet.fleetData.membersListCopy) {
                val max = member.repairTracker.maxCR
                member.repairTracker.cr = max
                cargo.addCommodity(Commodities.SUPPLIES, member.cargoCapacity*0.7f)
                cargo.addCommodity(Commodities.FUEL, member.fuelCapacity*0.9f)
                cargo.addCommodity(Commodities.HEAVY_MACHINERY, member.cargoCapacity*0.1f)
            }
            fleet.fleetData.setSyncNeeded()
            val stationsystem = Global.getSector().getStarSystem("corvus")
            val station: SectorEntityToken = stationsystem.addCustomEntity("ds_nexusStorage", "Mothership Global Storage", "station_side05", Factions.NEUTRAL)
           // val market: MarketAPI = Global.getFactory().createMarket("ds_nexusStorage", "Nexus Global Storage", 0)
            Misc.setAbandonedStationMarket("ds_nexusStorage", station)
            station.sensorProfile = 0f
            station.setInteractionImage("icons", "derelictflag")

            station.market.addIndustry(Industries.SPACEPORT)

            //station.market.getSubmarket(Submarkets.SUBMARKET_STORAGE).cargo.addMothballedShip(FleetMemberType.SHIP, "fulgent_Assault", "Engiels")
            //station.market.getSubmarket(Submarkets.SUBMARKET_STORAGE).cargo.addMothballedShip(FleetMemberType.SHIP, "glimmer_Support", "Gomiel")
            //station.market.getSubmarket(Submarkets.SUBMARKET_STORAGE).cargo.addMothballedShip(FleetMemberType.SHIP, "glimmer_Support", "Halitosis")

            station.setCircularOrbitPointingDown(stationsystem.center, 0f, 100000f, 9999f)


        }
        data.addScriptBeforeTimePass {
            val nexii = Global.getSector().getCustomEntitiesWithType("derelict_mothership")
            nexii.forEach {
                it.cargo.addAll(addNexusCargo(it))
            }
            val nexusstart = WeightedRandomPicker<SectorEntityToken>()
            nexusstart.addAll(nexii)
            val startloc = nexusstart.pick()

            Global.getSector().intelManager.addIntel(ds_nexusLocationIntel(), false)
            FactionCommissionIntel(Global.getSector().getFaction(Factions.DERELICT)).missionAccepted()
            Global.getSector().memoryWithoutUpdate.set("\$nex_startLocation", startloc.id)

        }


        dialog.visualPanel.showFleetInfo(StringHelper.getString("exerelin_ngc", "playerFleet", true), tempFleet, null, null)

        dialog.optionPanel.addOption(StringHelper.getString("done", true), "nex_NGCDone")
        dialog.optionPanel.addOption(StringHelper.getString("back", true), "nex_NGCStartBack")
        FireBest.fire(null, dialog, memoryMap, "ExerelinNGCStep4")

    }



} fun addNexusCargo(nexus: SectorEntityToken): CargoAPI { // add cargo to all nexus, undamaged ones get more stuff
    var mult = 1f
    val omegaWeaponList = Global.getSettings().allWeaponSpecs.filter { it.hasTag(Tags.OMEGA) }
    val cargo = Global.getFactory().createCargo(false)
    cargo.addCommodity(Commodities.GAMMA_CORE, MathUtils.getRandomNumberInRange(8f, 15f)*mult.roundToInt())
    cargo.addCommodity(Commodities.BETA_CORE, MathUtils.getRandomNumberInRange(5f, 9f)*mult.roundToInt())
    cargo.addCommodity(Commodities.ALPHA_CORE, MathUtils.getRandomNumberInRange(4f, 7f)*mult.roundToInt())
    cargo.addCommodity(Commodities.FUEL, MathUtils.getRandomNumberInRange(9000f, 15000f)*mult.roundToInt())
    cargo.addCommodity(Commodities.SUPPLIES, MathUtils.getRandomNumberInRange(3000f, 5000f)*mult.roundToInt())
    omegaWeaponList.forEach {
        if (it.size == WeaponAPI.WeaponSize.SMALL && Math.random() >= 0.83f){

                cargo.addWeapons(it.weaponId, MathUtils.getRandomNumberInRange(1, 3))

        } else if (it.size == WeaponAPI.WeaponSize.MEDIUM && Math.random() >= 0.86f){
            cargo.addWeapons(it.weaponId, MathUtils.getRandomNumberInRange(1, 2))

        } else if (it.size == WeaponAPI.WeaponSize.LARGE && Math.random() >= 0.90f){
            cargo.addWeapons(it.weaponId, 1)
        }
    }
    if (Math.random() > 0.97f) cargo.addCommodity(Commodities.OMEGA_CORE, 1f)





    return cargo


}
