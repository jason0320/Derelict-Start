package data.derelictstart.scripts.rulecmd

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.Script
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.BattleAPI.BattleSide
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.themes.*
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import data.derelictstart.customStart.ds_chauffeurAI
import data.derelictstart.customStart.ds_nexusCustomProduction
import data.derelictstart.customStart.ds_nexusRaidIntel
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.METALS_PER_NEXUS
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.RARE_METALS_PER_NEXUS
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.SUPPLIES_PER_NEXUS
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.factionspec
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.rewardlist
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.ship
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.targetmarket
import exerelin.campaign.DiplomacyManager
import lunalib.lunaExtensions.getCustomEntitiesWithType
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.input.Keyboard
import org.magiclib.util.MagicCampaign
import second_in_command.misc.previouslyLoadedSprite
import java.awt.Color
import kotlin.math.roundToInt

class ds_nexusStartRulecmd: BaseCommandPlugin() { // stuff to handle nexus interactions
    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {
        val type = params!![0].getInt(memoryMap)
        val remmy = Global.getSector().getFaction(Factions.DERELICT)
        when (type)  {

            0 -> {
                dialog!!.textPanel.clear()

                dialog.textPanel.addPara("The Explorarium Mothership welcomes you into its graces, allowing you to make use of its services.")
                dialog.textPanel.addPara("Open a comm link with the Mothership to begin.")
                dialog.textPanel.setFontSmallInsignia()
                dialog.textPanel.addPara("You may repair your ships at no cost at any Mothership, and make use of their services to maintain your fleet.")
                dialog.textPanel.addPara("Each Mothership has its own cargo for offer, and are prepared to produce Explorarium hulls and weapons instantaneously - provided you have the credits to authorize the production, that is.")
                dialog.textPanel.setFontInsignia()

            }

            1 -> {
                getCargoPicker(dialog)
            }
            2 -> {
            //   fighterlist.addAll(remmy.knownFighters.filter { Global.getSettings().getFighterWingSpec(it).hasTag(Tags.NO_SELL) })
            //    weaponlist.addAll(remmy.knownWeapons.filter { Global.getSettings().getWeaponSpec(it).hasTag(Tags.NO_SELL) })
            //    fighterlist.forEach { Global.getSettings().getFighterWingSpec(it).tags.remove(Tags.NO_SELL)
            //    Global.getSettings().getFighterWingSpec(it).tags.remove(Tags.RESTRICTED)}
           //     weaponlist.forEach { Global.getSettings().getWeaponSpec(it).tags.remove(Tags.NO_SELL)
           //     Global.getSettings().getWeaponSpec(it).tags.remove(Tags.RESTRICTED)}
                dialog!!.showCustomProductionPicker(ds_nexusCustomProduction(dialog))
            }
            3 -> {
                Global.getSector().addTransientScript(
                    nexusStorageScript(dialog!!.interactionTarget as SectorEntityToken, dialog)
                )
            } 4 -> {
                Global.getSector().playerFleet.fleetData.membersListCopy.forEach {
                    it.repairTracker.cr = it.repairTracker.maxCR
                    it.status.hullFraction = 1f
                }
            } 5 -> {
            dialog!!.optionPanel.clearOptions()
            dialog.xOffset = 0f
            if (height != null){
                dialog.textHeight = height!!
                dialog.textWidth = width!!
            } else {
                height = dialog.textHeight
                width = dialog.textWidth
            }
            dialog.textPanel.updateSize()
            dialog.textPanel.clear()
            dialog.textPanel.setFontVictor()
            dialog.textPanel.addPara("affirm // detected : Explorarium property transponder from [target_fleet] // waiting waiting waiting...")
            if (Global.getSector().playerFleet.fleetPoints > 300){ // you're a big guy.
                dialog.textPanel.addPara("receipt of FLEET no. 417 confirmed. \"Welcome back, esteemed user!\"")
            } else {
                dialog.textPanel.addPara("receipt of SCOUT no. 417 confirmed. \"Welcome back, esteemed user!\"")

            }
            Global.getSector().playerFleet.fleetData.membersListCopy.forEach {
                it.repairTracker.cr = it.repairTracker.maxCR
                it.status.hullFraction = 1f
            }
            coreguy = dialog.interactionTarget.activePerson
            factionspec = ""
            dialog.textPanel.addPara("CASE permit [offload implements] to [target_fleet] // verification required")
            dialog.textPanel.addPara("CASE produce [replicate] implements // verification required")
            dialog.textPanel.addPara("CASE refit [test/analyze/rearm] offered for [target_fleet] //")
            dialog.textPanel.addPara("CASE leave [cutCommLink] // \"We wish you a very nice day.\"")
            dialog.textPanel.addPara("CASE repair [restore] expeditiously available")
            dialog.textPanel.setFontInsignia()
            dialog.optionPanel.addOption("Evaluate available cargo", "ds_nexusCargoPicker")
            dialog.optionPanel.setTooltip("ds_nexusCargoPicker", "Open a dialog to purchase supplies, AI cores, and occasionally other rare items. Every Mothership has its own stock.")
            dialog.optionPanel.addOption("Request immediate production", "ds_nexusProductionPicker")
            dialog.optionPanel.setTooltip("ds_nexusProductionPicker", "Instantly produce Explorarium hulls and weapons to be delivered to your fleet.")
            dialog.optionPanel.addOption("Access the storage network", "ds_nexusStorage")
            dialog.optionPanel.setTooltip("ds_nexusStorage", "Allows you to refit your ships. Counts as a spaceport for hullmods that require a dock.")
            dialog.optionPanel.addOption("Initiate fleet repairs", "ds_nexusRepair")
            dialog.optionPanel.setTooltip("ds_nexusRepair", "A free automated repair procedure. Restores all ships to full CR and hull integrity at no cost.")
            dialog.optionPanel.addOption("Manage automated hulls", "ds_nexusDeconstructMain")
            dialog.optionPanel.setTooltip("ds_nexusDeconstruct", "Destroy an automated hull to add it to the Explorarium's known hulls.")
            dialog.optionPanel.addOption("Consider building a new Mothership", "ds_nexusConstructMenu")
            dialog.optionPanel.setTooltip("ds_nexusConstructMenu", "Construct a new Mothership")
            if (Global.getSector().intelManager.hasIntelOfClass(ds_nexusRaidIntel::class.java) && Global.getSector().memoryWithoutUpdate.getInt("\$ds_nexusParty")==1){
                dialog.optionPanel.addOption("Raid rewards", "ds_nexusPartyTimeReward")
            }
            else if (Global.getSector().memoryWithoutUpdate.getBoolean("\$ds_nexusPartyTimeout")){
                dialog.optionPanel.addOption("Raid cooldown", "ds_nexusPartyCoolDown")
                dialog.optionPanel.setEnabled("ds_nexusPartyCoolDown", false)
                val expire = Global.getSector().memoryWithoutUpdate.getExpire("\$ds_nexusPartyTimeout")
                if  (expire > 0f){
                    dialog.optionPanel.setTooltip("ds_nexusPartyCoolDown","You may throw another party in ${expire.roundToInt()} days.")
                }
            }
            else if (!Global.getSector().intelManager.hasIntelOfClass(ds_nexusRaidIntel::class.java) && !Global.getSector().memoryWithoutUpdate.getBoolean("\$ds_nexusPartyTimeout")){
                dialog.optionPanel.addOption("Raid requests", "ds_nexusPartyTimeShow")
            }

            dialog.optionPanel.addOption("Leave", "defaultLeave")
            dialog.optionPanel.setShortcut("ds_nexusRepair", Keyboard.KEY_A, false, false,false , false)
            //dialog.visualPanel.showPersonInfo(dialog.interactionTarget.activePerson)
            dialog.visualPanel.showImageVisual(dialog.interactionTarget.customInteractionDialogImageVisual)
        }
            6 -> {

                dialog!!.textPanel.clear()
               // dialog.optionPanel.clearOptions()
              //  dialog.optionPanel.addOption("Access the control node", "marketOpenCoreUI")
             //   dialog.optionPanel.addOption("Log off", "defaultLeave")
              //  dialog.optionPanel.removeOption("marketRepair")
                dialog.textPanel.addPara("You're currently accessing the Explorarium Mothership's global data storage network.")
                dialog.textPanel.addPara("Any items stored in here will be available to retrieve from any other Explorarium Mothership in the sector.")
                dialog.textPanel.addPara("Exit the storage network to return to using the Explorarium Mothership.")
            }
            7 -> {
                dialog!!.optionPanel.clearOptions()
                dialog.optionPanel.addOption("Manage commodity manifest", "marketOpenCargo")
                dialog.optionPanel.setShortcut("marketOpenCargo", Keyboard.KEY_I, false,false,false,false)
                dialog.optionPanel.addOption("Upload or download ships", "marketOpenFleet")
                dialog.optionPanel.setShortcut("marketOpenFleet", Keyboard.KEY_F, false,false,false,false)
                dialog.optionPanel.addOption("Use the Mothership to refit your ships", "marketOpenRefit")
                dialog.optionPanel.setShortcut("marketOpenRefit", Keyboard.KEY_R, false,false,false,false)
                dialog.optionPanel.addOption("Log off", "defaultLeave")
                dialog.optionPanel.setShortcut("defaultLeave", Keyboard.KEY_ESCAPE, false,false,false,true)
                dialog.makeOptionOpenCore("marketOpenRefit", CoreUITabId.REFIT, CampaignUIAPI.CoreUITradeMode.OPEN)
            }
            8 -> {
                dialog!!.xOffset = 0f
                if (height != null){
                    dialog.textHeight = height!!
                    dialog.textWidth = width!!
                } else {
                    height = dialog.textHeight
                    width = dialog.textWidth
                }
                dialog.textPanel.updateSize()


                showPickerDialog(dialog)
            }
            9 -> {
                var hascores = false
                // learn all weapons when like 50% of hulls are known
                // wings at 75%
                // if the faction has unique ai cores, populate nexii with them at like 90%
                dialog!!.textPanel.setFontSmallInsignia()
                dialog.optionPanel.clearOptions()
                dialog.textPanel.addPara("The ${ship!!.shipName} has been deconstructed. Moments later, a sonorous chime emits from the Mothership.")
//                remmy.addKnownShip(ship!!.hullSpec.baseHullId, false)
//                remmy.alwaysKnownShips.add(ship!!.hullSpec.baseHullId)
//                remmy.addUseWhenImportingShip(ship!!.hullSpec.baseHullId)
                remmy.addKnownShip(ship!!.hullSpec.hullId, false)
                remmy.alwaysKnownShips.add(ship!!.hullSpec.hullId)
                remmy.addUseWhenImportingShip(ship!!.hullSpec.hullId)
                if (ship!!.hullSpec.baseHullId != "rat_genesis") {
                    val varlist = Global.getSettings().allVariantIds
                    val shipvarlist = ArrayList<String>()
                    varlist.forEach {
                        //if (Global.getSettings().getVariant(it).hullSpec.baseHullId == ship!!.hullSpec.baseHullId && Global.getSettings().getVariant(it).isGoalVariant) {
                        if (Global.getSettings().getVariant(it).hullSpec.baseHullId == ship!!.hullSpec.baseHullId) {

                                shipvarlist.add(it)
                        }
                    }
                    var role = "combatSmall"
                    //when (Global.getSettings().getHullSpec(ship!!.hullSpec.baseHullId).hullSize) {
                    when (Global.getSettings().getHullSpec(ship!!.hullSpec.hullId).hullSize) {
                        ShipAPI.HullSize.CAPITAL_SHIP -> role = "combatCapital"
                        ShipAPI.HullSize.CRUISER -> role = "combatLarge"
                        ShipAPI.HullSize.DESTROYER -> role = "combatMedium"
                        else -> role = "combatSmall"
                    }
                    shipvarlist.forEach {
                        Global.getSettings().addDefaultEntryForRole(role, it, 0f) // set 0 weight so it doesn't bleed over into other fleets (if we learned the eternity and set it to >0 weight, it would spawn in enigma fleets. this is bad!)
                        Global.getSettings().addEntryForRole(Factions.DERELICT, role, it, (1f))
                    }
                }

                Global.getSector().playerFleet.removeFleetMemberWithDestructionFlash(ship)
                Global.getSoundPlayer().playUISound("ui_industry_install_any_item", 1f, 1f)
                //val spec = Global.getSettings().getHullSpec(ship!!.hullSpec.baseHullId)
                val spec = Global.getSettings().getHullSpec(ship!!.hullSpec.hullId)
                val banlist = ArrayList<String>()
                banlist.add("sotf_dustkeepers_burnouts")
                banlist.add("rat_abyssals")
                banlist.add("ai_all")
                Global.getSector().allFactions.forEach {
                    //if (!banlist.contains(it.id) && it.knowsShip(ship!!.hullSpec.baseHullId) && it.factionSpec.id != "derelict") {
                    if (!banlist.contains(it.id) && it.knowsShip(ship!!.hullSpec.hullId) && it.factionSpec.id != "derelict") {
                        factionspec = it.id
                        if (factionspec == "rat_abyssals_deep" || factionspec == "tahlan_legiodaemons" || factionspec == "vestige"){
                            hascores = true // need to hardcode this because there's no way to know
                        }
                    }
                }
                if (ship!!.hullSpec.baseHullId.startsWith("istl") || ship!!.hullSpec.baseHullId.startsWith("bbplus")){
                    factionspec = "blade_breakers" // for some reason we have to do this???????
                }
                dialog.textPanel.addPara("Explorarium fleets may now use the ${ship!!.hullSpec.hullNameWithDashClass} ${ship!!.hullSpec.hullSize.name}.").setColor(Misc.getHighlightColor())


                    doFactionCheck(factionspec, hascores, dialog)

                dialog.textPanel.setFontInsignia()
                dialog.optionPanel.addOption("Continue", "ds_nexusDeconstructMain")

            }
            10 -> {
                //Global.getSector().addTransientScript(nexusMidnightScript(dialog!!, coreguy!!))
            }
            11 -> {
                dialog!!.interactionTarget.activePerson = coreguy
            }
            12 -> {
                showRecountInfo(dialog!!)
            }
            13 -> {
                dialog!!.textPanel.clear()
                dialog.textPanel.addPara("The Explorarium are ever-hungry to expand their knowledge, especially towards those whose hulls resemble their own.")
                dialog.xOffset = 0f
                if (height != null){
                    dialog.textHeight = height!!
                    dialog.textWidth = width!!
                } else {
                    height = dialog.textHeight
                    width = dialog.textWidth
                }
                dialog.textPanel.updateSize()


            }
            14 -> { // call this to show all resource costs, then check resources and disable option to build nexus if either time is too early or
                val expire = Global.getSector().memoryWithoutUpdate.getExpire("\$ds_nexusBuildTimeout")
                if  (Global.getSector().memoryWithoutUpdate.getExpire("\$ds_nexusBuildTimeout") > 0f){
                    dialog!!.textPanel.addPara("It will take some time to prepare to construct another Mothership.")
                    dialog.textPanel.addPara("You may construct another in ${expire.roundToInt()} days.")
                }
                dialog!!.textPanel.addCostPanel("Construction Costs", Commodities.SUPPLIES, SUPPLIES_PER_NEXUS.roundToInt(), true, Commodities.METALS, METALS_PER_NEXUS.roundToInt(), true, Commodities.RARE_METALS, RARE_METALS_PER_NEXUS.roundToInt(), true)
                // dialog.textPanel.co
                var supplies = false
                var metals = false
                var raremetals = false
                val pcargo = Global.getSector().playerFleet.cargo
                if (pcargo.getCommodityQuantity(Commodities.SUPPLIES) >= SUPPLIES_PER_NEXUS) supplies = true
                if (pcargo.getCommodityQuantity(Commodities.METALS) >= METALS_PER_NEXUS) metals = true
                if (pcargo.getCommodityQuantity(Commodities.RARE_METALS) >= RARE_METALS_PER_NEXUS) raremetals = true

                if (Global.getSector().memoryWithoutUpdate.getBoolean("\$ds_nexusBuildTimeout") || !supplies || !metals || !raremetals){
                    dialog.optionPanel.setEnabled("ds_nexusConstruct", false)
                    dialog.optionPanel.setTooltip("ds_nexusConstruct", "Can't build this yet.")
                }
            }
            15 -> {
                ShowNexusBuildPicker(dialog!!)
            }
            16 ->{
                getShowRaidTarget(dialog!!, targetmarket, rewardlist)
            }
            17 -> {
                doSetup(dialog!!)
            }
            18 -> {
                getRaidReward(dialog!!)
            }
        }
        return true
    }
    companion object {
        val SUPPLIES_PER_NEXUS = 800f
        val METALS_PER_NEXUS = 1500f
        val RARE_METALS_PER_NEXUS = 200f
        val WEAPONS_THRESHOLD = 0.40f
        val FIGHTERS_THRESHOLD = 0.50f
        val AICORES_THRESHOLD = 0.75f
        var targetmarket: MarketAPI? = null
        var rewardlist = ArrayList<Int>()
        var coreguy: PersonAPI? = null
        var ship: FleetMemberAPI? = null
        var height: Float? = null
        var width: Float? = null
        var factionspec: String = ""
    }
}

class ds_nexusBuildScript(var source: CampaignFleetAPI, var loc: EntityLocation) : Script {
    override fun run() {
        val system = source.starSystem ?: return // if we aren't in a star system somehow when this runs, something really fucked up.
        val random = Misc.random
        Global.getSector().campaignUI.messageDisplay.addMessage("Mothership construction finished in ${system.name}")

//        val fleet = FleetFactoryV3.createEmptyFleet(Factions.DERELICT, FleetTypes.BATTLESTATION, null)
//
//        val member: FleetMemberAPI = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "station_derelict_survey_mothership_Standard")
//        fleet.fleetData.addFleetMember(member)
//
//
//        //fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
//        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE] = true
//        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_JUMP] = true
//        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE] = true
//        fleet.addTag(Tags.NEUTRINO_HIGH)
//
//        fleet.isStationMode = true
//
//        //RemnantThemeGenerator.addRemnantStationInteractionConfig(fleet)
//
//        system.addEntity(fleet)
//
//
//        //fleet.setTransponderOn(true);
//        fleet.clearAbilities()
//        fleet.addAbility(Abilities.TRANSPONDER)
//        fleet.getAbility(Abilities.TRANSPONDER).activate()
//        fleet.detectedRangeMod.modifyFlat("gen", 1000f)
//
//        fleet.ai = null
//
//        BaseThemeGenerator.setEntityLocation(fleet, loc, null)
//        BaseThemeGenerator.convertOrbitWithSpin(fleet, 5f)
//
//        val coreId = Commodities.ALPHA_CORE
//
//        val plugin = Misc.getAICoreOfficerPlugin(coreId)
//        val commander = plugin.createPerson(coreId, fleet.faction.id, random)
//
//        fleet.commander = commander
//        fleet.flagship.captain = commander
//
//
////        RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.flagship)
////        RemnantOfficerGeneratorPlugin.addCommanderSkills(commander, fleet, null, 3, random)
//
//        member.repairTracker.cr = member.repairTracker.maxCR
//        fleet.cargo.addAll(addNexusCargo(fleet))
//        val maxFleets = 8 + random.nextInt(5)
//        val activeFleets = RemnantStationFleetManager(
//            fleet, 1f, 0, maxFleets, 15f, 8, 24
//        )
//        system.addScript(activeFleets)
        val stable = source.getContainingLocation().addCustomEntity(null, null, "derelict_mothership", "derelict");
        val orbitRadius = com.fs.starfarer.api.util.Misc.getDistance(source, system.getCenter());
        val orbitDays = orbitRadius / (20f + random.nextFloat() * 5f);
        val angle = com.fs.starfarer.api.util.Misc.getAngleInDegrees(system.getCenter().getLocation(), source.getLocation());
        stable.setCircularOrbit(system.getCenter(), angle, orbitRadius, orbitDays);
        system.addTag(Tags.THEME_DERELICT_MOTHERSHIP) // necessary for other derelict-related stuff to work properly
//        source.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, fleet, 999f)
    }
}

private fun ShowNexusBuildPicker(dialog: InteractionDialogAPI){
    //val nexusList = MiscellaneousThemeGenerator.getRemnantStations(true, false)
    val nexusList = Global.getSector().getCustomEntitiesWithType(Entities.DERELICT_MOTHERSHIP)
    val bannedSystemsList = ArrayList<StarSystemAPI>()
    nexusList.forEach { if (it.starSystem != null)  bannedSystemsList.add(it.starSystem) }
    val validSystemList = Global.getSector().starSystems.filter { it.isEnteredByPlayer && it.isProcgen && !it.isDeepSpace && !it.hasTag(Tags.THEME_HIDDEN) && !bannedSystemsList.contains(it) }
    val centers = ArrayList<SectorEntityToken>()
    validSystemList.forEach { centers.add(it.center) }
    dialog.showCampaignEntityPicker("Title", "Selected", "Ok", Global.getSector().getFaction(Factions.DERELICT), centers, object : BaseCampaignEntityPickerListener(){
        override fun cancelledEntityPicking() {
            dialog.textPanel.addPara("cancelled")
        }



        override fun getFuelRangeMult(): Float {
            return 0f
        }

        override fun pickedEntity(entity: SectorEntityToken) {
            // upon pick, set memory and boot us back to the main dialog + spawn a fleet with assignment script to go to location
            // will orbit a token created in the system for x days if possible, upon order completion run a script that creates the fleet (check theme gen for hwo
            val cargo = Global.getSector().playerFleet.cargo
            cargo.removeCommodity(Commodities.SUPPLIES, SUPPLIES_PER_NEXUS)
            cargo.removeCommodity(Commodities.METALS, METALS_PER_NEXUS)
            cargo.removeCommodity(Commodities.RARE_METALS, RARE_METALS_PER_NEXUS)
            AddRemoveCommodity.addCommodityLossText(Commodities.SUPPLIES, SUPPLIES_PER_NEXUS.roundToInt(), dialog.textPanel)
            AddRemoveCommodity.addCommodityLossText(Commodities.METALS, METALS_PER_NEXUS.roundToInt(), dialog.textPanel)
            AddRemoveCommodity.addCommodityLossText(Commodities.RARE_METALS, RARE_METALS_PER_NEXUS.roundToInt(), dialog.textPanel)

            val system = entity!!.starSystem
            val loc = BaseThemeGenerator.pickCommonLocation(Misc.random, system, 200f, true, null)
            val token = system.createToken(MathUtils.getRandomPointOnCircumference(entity.location, 6000f))
            Global.getSector().memoryWithoutUpdate.set("\$ds_nexusBuildTimeout", true, 90f)
            dialog.optionPanel.setEnabled("ds_nexusConstruct", false)
            val constructorFleet = MagicCampaign.createFleetBuilder()
                .setFleetFaction(Factions.DERELICT)
                .setFleetName("Construction Fleet")
                .setFleetType(FleetTypes.SUPPLY_FLEET)
                .setAssignment(FleetAssignment.GO_TO_LOCATION)
                .setAssignmentTarget(token)
                .setSpawnLocation(dialog.interactionTarget)
                .setIsImportant(true)
                .setMinFP(200)
                .create()

            constructorFleet.removeFirstAssignment()
            constructorFleet.addAssignment(FleetAssignment.GO_TO_LOCATION, token, 999f)

            constructorFleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, token, 30f, "Constructing Mothership", ds_nexusBuildScript(constructorFleet, loc))
            dialog.textPanel.addPara("A fleet has been dispatched to ${system.name}.").setHighlight(system.name)
            dialog.textPanel.addPara("Once it arrives, it will take 30 days to construct the Mothership.").setHighlight("30 days")
        }

        override fun canConfirmSelection(entity: SectorEntityToken?): Boolean {
            return (entity != null)
        }

        override fun createInfoText(info: TooltipMakerAPI?, entity: SectorEntityToken?) {
            if (entity == null) return
            info!!.addPara("Selected system: ${entity.starSystem?.name}", 5f)
            if (entity.starSystem?.constellation != null) {
                val const = entity.constellation.name
                info.addPara(const, 5f)
            }
            val ly = Misc.getDistanceToPlayerLY(entity)
            info.addPara("$ly light years away from your location.", 5f).setHighlight("$ly")
        }

        override fun getMenuItemNameOverrideFor(entity: SectorEntityToken?): String {
            return entity?.starSystem?.name ?: return "Location"
        }
    } )

}

private fun getCargoPicker(dialog: InteractionDialogAPI?){
    val memory = Global.getSector().playerMemoryWithoutUpdate
   val creds = Global.getSector().playerFleet.cargo.credits
    val cargo = Global.getFactory().createCargo(false)
    val nexuscargo = dialog!!.interactionTarget.cargo
    cargo.addAll(nexuscargo)

    cargo.sort()


    dialog!!.showCargoPickerDialog("Mothership Supply", "Requisition", "Cancel", true, 310f, cargo, object : CargoPickerListener {
        override fun pickedCargo(cargo: CargoAPI?) {
            cargo!!.sort()
            val cost = getCost(cargo)
            if (cost > 0 && cost < creds.get()){
                creds.subtract(cost)
                dialog.textPanel.setFontSmallInsignia()
                for (stack in cargo.stacksCopy){
                    Global.getSector().playerFleet.cargo.addItems(stack.type, stack.data, stack.size)
                    AddRemoveCommodity.addStackGainText(stack, dialog.textPanel, false)
                    nexuscargo.removeItems(stack.type, stack.data, stack.size)
                }
                AddRemoveCommodity.addCreditsLossText(cost.roundToInt(), dialog.textPanel)
            }
        }

        override fun cancelledCargoSelection() {
        }


        override fun recreateTextPanel(panel: TooltipMakerAPI?, cargo: CargoAPI, pickedUp: CargoStackAPI?, pickedUpFromSource: Boolean, combined: CargoAPI) {
            val cost = getCost(combined)
            val pad = 3f
            val bigpad = 10f
            val highlight = Misc.getHighlightColor()
            val highlightspooky = Misc.getNegativeHighlightColor()
            val hegsprite = Global.getSettings().getFactionSpec(Factions.DERELICT).crest

            panel!!.addImage(hegsprite, 310f, pad)
            val para1 =  panel.addPara("You currently have ${creds.get().roundToInt()} credits.", bigpad)
            para1.setHighlight("${creds.get().roundToInt()}")
            para1.setHighlightColor(highlight)
            val para2 = panel.addPara("The cost of your selection is ${cost.roundToInt()} credits.", pad)
            para2.setHighlight("${cost.roundToInt()}")
            para2.setHighlightColor(highlightspooky)
            if (cost > creds.get()) {
                val para3 =    panel.addPara("You don't have enough credits to authorize this transaction.", pad)
                para3.setColor(highlightspooky)
            }
        }



    })

}
fun getCost(cargo: CargoAPI): Float {
    var cost = 0f
    for (item in cargo.stacksCopy){
        if (item.isSpecialStack){
            when (item.specialDataIfSpecial.id){ // the shop can sell weapons, commodities or specials so we need to handle all 3 separately
            }
        }
        if (item.isCommodityStack) {
            when (item.commodityId) {
                (Commodities.OMEGA_CORE) -> {
                    cost += (item.baseValuePerUnit * 3f * item.size).roundToInt()
                }
                (Commodities.ALPHA_CORE) -> {
                    cost += (item.baseValuePerUnit * 3f * item.size).roundToInt()
                }
                (Commodities.BETA_CORE) -> {
                    cost += (item.baseValuePerUnit * 3f * item.size).roundToInt()
                }
                (Commodities.GAMMA_CORE) -> {
                    cost += (item.baseValuePerUnit * 3f * item.size).roundToInt()
                }
                Commodities.FUEL -> {
                    cost += (item.baseValuePerUnit * 0.5f * item.size).roundToInt()
                }
                Commodities.SUPPLIES -> {
                    cost += (item.baseValuePerUnit * 0.5f * item.size).roundToInt()
                }
                else -> {
                    if (Global.getSettings().getCommoditySpec(item.commodityId).demandClass == "ai_cores"){
                        cost += (item.baseValuePerUnit * 3f * item.size).roundToInt()
                    }
                }

            }
        } else if (item.isWeaponStack){
            when (item.weaponSpecIfWeapon.size){
                WeaponAPI.WeaponSize.LARGE -> {
                    cost += (item.baseValuePerUnit * 1.5f * item.size).roundToInt()
                }
                WeaponAPI.WeaponSize.MEDIUM -> {
                    cost += (item.baseValuePerUnit * 1.3f * item.size).roundToInt()
                }
                else -> {
                    cost += (item.baseValuePerUnit * 1f * item.size).roundToInt()
                }
            }

        }
    }
    return cost

} fun showPickerDialog(dialog: InteractionDialogAPI?) {
    if (getValidShips().isEmpty()){
        dialog!!.textPanel.addPara("None of the automated ships in your fleet are unknown to the Explorarium.")
        dialog.textPanel.addPara("Consider returning once you have one.")
        return
    }
    dialog!!.showFleetMemberPickerDialog("Re-origination Protocol",
            "Proceed",
            "Cancel",
            4,
            4,
            160f,
            true,
            false,
            getValidShips(),
            object: FleetMemberPickerListener {
                override fun pickedFleetMembers(members: List<FleetMemberAPI?>?){

                    if (members!!.isEmpty()){
                        return
                    }
                    for (member in members){
                        dialog.optionPanel.clearOptions()
                        ship = member
                        Global.getSector().allFactions.forEach {
                            if (it.knowsShip(ship!!.hullSpec.baseHullId)) {
                                factionspec = it.id
                            }
                        }
                        val para1 = dialog.textPanel.addPara(member!!.shipName + ", a " + member.hullSpec.hullNameWithDashClass + " is selected.").setHighlight(member.shipName)
                        showFactionInfo(factionspec, dialog)
                        dialog.textPanel.addPara("Proceeding will destroy the ship and add it to the Explorarium's known ships.").setHighlight("destroy", "add")
                        dialog.textPanel.addPara("This action cannot be undone.").setColor(Color.RED)
                        dialog.textPanel.addPara("Would you like to proceed?")
                        dialog.visualPanel.showFleetMemberInfo(member)
                        dialog.optionPanel.addOption("Proceed with deconstruction", "ds_nexusDeconstructProceed")
                        dialog.optionPanel.addOption("Go back", "ds_nexusDeconstructMain")
                    }

                }
                override fun cancelledFleetMemberPicking(){
                    return
                }
            }

    )

}
fun getValidShips(): ArrayList<FleetMemberAPI> {
     val remmies = Global.getSector().getFaction(Factions.DERELICT)

     val validShips = ArrayList<FleetMemberAPI>()

        for (playerFM in Global.getSector().playerFleet.membersWithFightersCopy) {
         if (playerFM.variant.hasHullMod("automated") && playerFM.hullSpec.hasTag(Tags.AUTOMATED_RECOVERABLE) && !remmies.knowsShip(playerFM.hullSpec.baseHullId) &&!playerFM.isFighterWing) validShips.add(playerFM)
    }
    return validShips
}

class nexusStorageScript(
    private val nexus: SectorEntityToken,
    private val dialog: InteractionDialogAPI
) : EveryFrameScript {
    private var cancel = false
    private var ranStorage = false
    private val canceller = IntervalUtil(0.03f, 0.03f)
    private val storageEntity: SectorEntityToken? = Global.getSector().getEntityById("ds_nexusStorage")

    override fun isDone(): Boolean = false
    override fun runWhilePaused(): Boolean = true

    override fun advance(amount: Float) {
        if (storageEntity == null) {
            Global.getSector().removeTransientScriptsOfClass(nexusStorageScript::class.java)
            return
        }

        if (!cancel) {
            canceller.advance(amount)
            if (canceller.intervalElapsed()) {
                cancel = true

                val plugin = dialog.plugin as? FleetInteractionDialogPluginImpl
                val context = plugin?.context as? FleetEncounterContext
                if (context != null) {
                    context.applyAfterBattleEffectsIfThereWasABattle()
                    val b = context.battle
                    b.leave(Global.getSector().playerFleet, false)
                    b.finish(BattleSide.NO_JOIN, false)
                }
                dialog.dismiss()
                return
            }
        }

        // After the fleet dialog is dismissed, open a *normal* interaction dialog on storage
        if (cancel && !ranStorage && !Global.getSector().campaignUI.isShowingDialog) {
            // Use default dialog, not fleet plugin
            Global.getSector().campaignUI.showInteractionDialog(storageEntity)
            ranStorage = true
            return
        }

        // After storage dialog closes, open the nexus dialog (back to original)
        if (ranStorage && !Global.getSector().campaignUI.isShowingDialog &&
            Global.getCurrentState() == GameState.CAMPAIGN) {

            // We want to open nexus in its normal mode
            Global.getSector().campaignUI.showInteractionDialog(nexus)
            // If nexus is a fleet interaction, it will create the appropriate plugin
            Global.getSector().removeTransientScriptsOfClass(nexusStorageScript::class.java)
        }
    }
} fun doFactionCheck(spec: String, factionHasCores: Boolean, dialog: InteractionDialogAPI){
    if (spec.isEmpty()) return // break immediately if there's no faction that knows the hull
    val mem = Global.getSector().memoryWithoutUpdate
    val remmy = Global.getSector().getFaction(Factions.DERELICT)
    if (!Global.getSettings().getFactionSpec(spec).knownShips.contains(ship!!.hullSpec.baseHullId)) return // don't do any of this if we scrap a ship that isn't actually in the spec
    val facships = Global.getSettings().getFactionSpec(spec).knownShips.filter { Global.getSettings().getHullSpec(it).hasTag(Tags.AUTOMATED_RECOVERABLE) }
    val remmyships = remmy.knownShips.filter { Global.getSettings().getFactionSpec(spec).knownShips.contains(it) }
    val facweaps = Global.getSettings().getFactionSpec(spec).knownWeapons.filter { !Global.getSettings().getWeaponSpec(it).aiHints.contains(WeaponAPI.AIHints.SYSTEM) && Global.getSettings().getWeaponSpec(it).type != WeaponType.DECORATIVE }
    val facwings = Global.getSettings().getFactionSpec(spec).knownFighters.filter { Global.getSettings().getFighterWingSpec(it).hasTag(Tags.AUTOMATED_FIGHTER) }

    showFactionInfo(spec, dialog)



    if ((remmyships.size.toFloat() / facships.size.toFloat()) >= 0.5f && !mem.contains("\$ds_"+ spec + "weapons")) {
        facweaps.forEach {
            remmy.addKnownWeapon(it, false)
        }
        Global.getSoundPlayer().playUISound("ui_char_spent_story_point", 1f,1f)
        dialog.textPanel.addPara("The Explorarium now know 50% or more of this faction's automated hulls.").setHighlight("50%")
        dialog.textPanel.addPara("Iterative analysis of their systems now allows the Explorarium to use all of this faction's known weapons.").setHighlight("known weapons.")
        dialog.textPanel.addPara("These will also be available through production orders.")
        dialog.textPanel.setFontSmallInsignia()
        dialog.textPanel.addPara("Fighter blueprints will be unlocked at 75%.").setColor(Misc.getHighlightColor())
        mem.set("\$ds_" + spec + "weapons", true)
    }
    if ((remmyships.size.toFloat() / facships.size.toFloat()) >= 0.75f && !mem.contains("\$ds_"+ spec + "wings")) {
        facwings.forEach {
            remmy.addKnownFighter(it, false)
        }
        Global.getSoundPlayer().playUISound("ui_char_spent_story_point", 1f,1f)
        dialog.textPanel.addPara("The Explorarium now know 75% or more of this faction's automated hulls.").setHighlight("75%")
        dialog.textPanel.addPara("Comprehensive analysis of their systems now allows the Explorarium to use all of this faction's known fighters.").setHighlight("known fighters.")
        dialog.textPanel.addPara("These will also be available through production orders.")
        dialog.textPanel.setFontSmallInsignia()
        if (factionHasCores){
            dialog.textPanel.addPara("Explorarium Mothershipes will offer this faction's AI cores at 90%.").setColor(Misc.getHighlightColor())
        }
        mem.set("\$ds_" + spec + "wings", true)
    }
    if ((remmyships.size.toFloat() / facships.size.toFloat()) >= 0.90f && !mem.contains("\$ds_"+ spec + "cores") && factionHasCores) {
        Global.getSoundPlayer().playUISound("ui_char_spent_story_point", 1f,1f)
        dialog.textPanel.addPara("The Explorarium now know 90% or more of this faction's automated hulls.").setHighlight("90%")
        dialog.textPanel.addPara("Thorough reconstruction of their behaviors now allows any Mothership to offer some of the faction's AI cores.").setHighlight("AI cores.")
        dialog.textPanel.addPara("These are available through the supply cargo picker.")
        dialog.textPanel.setFontSmallInsignia()
        mem.set("\$ds_" + spec + "cores", true)
        val nexii = Global.getSector().getCustomEntitiesWithType(Entities.DERELICT_MOTHERSHIP)
        when (spec){
            "rat_abyssals_deep" -> {
                nexii.forEach {
                    it.cargo.addCommodity("rat_chronos_core", MathUtils.getRandomNumberInRange(5f, 7f))
                    it.cargo.addCommodity("rat_cosmos_core", MathUtils.getRandomNumberInRange(5f, 7f))
                    it.cargo.addCommodity("rat_seraph_core", MathUtils.getRandomNumberInRange(3f, 5f))
                }
            }
            "tahlan_legiodaemons" -> {

                nexii.forEach {
                        it.cargo.addCommodity("tahlan_daemoncore", MathUtils.getRandomNumberInRange(6f, 9f))
                        it.cargo.addCommodity("tahlan_archdaemoncore", MathUtils.getRandomNumberInRange(3f, 5f))
                    }
                }
            "vestige" ->{
                nexii.forEach {
                        it.cargo.addCommodity("vestige_core", MathUtils.getRandomNumberInRange(5f, 8f))
                        it.cargo.addCommodity("volantian_core", MathUtils.getRandomNumberInRange(5f, 8f))
                    }
            }
        }
    }
} fun showFactionInfo(spec: String, dialog: InteractionDialogAPI){
    if (spec.isEmpty()) return
    val remmy = Global.getSector().getFaction(Factions.DERELICT)
    val facships = Global.getSettings().getFactionSpec(spec).knownShips.filter { Global.getSettings().getHullSpec(it).hasTag(Tags.AUTOMATED_RECOVERABLE) }
    val remmyships = remmy.knownShips.filter { Global.getSettings().getFactionSpec(spec).knownShips.contains(it) }
    val facname = Global.getSettings().getFactionSpec(spec).displayName
    val namecolor = Global.getSettings().getFactionSpec(spec).brightUIColor
    val para =  dialog.textPanel.addPara("This ship's origin, the $facname, knows ${facships.size} automated ships.")
    para.setHighlight(facname, "${facships.size}")
    para.setHighlightColors(namecolor, Misc.getHighlightColor())
    dialog.textPanel.addPara("The Explorarium know ${remmyships.size} of them.").setHighlight("${remmyships.size}")
}
fun showRecountInfo(dialog: InteractionDialogAPI){
    dialog.textPanel.clear()
    dialog.xOffset = Global.getSettings().screenWidth/6f
    dialog.textWidth = Global.getSettings().screenWidth/3f
    dialog.textHeight = Global.getSettings().screenHeight/1.4f
    dialog.textPanel.updateSize()
    dialog.textPanel.addPara("The following is a list of all factions that use automated ships which can be recovered.")
    dialog.textPanel.addPara("Note that some automated ships may be absent from this list if they don't have an associated faction.")

    val remmy = Global.getSector().getFaction(Factions.DERELICT)
    val banlist = ArrayList<String>()
    banlist.add("sotf_dustkeepers_burnouts")
    banlist.add("rat_abyssals")
    banlist.add("ai_all")
    val factionlist = ArrayList<String>() // TODO: blacklist more hidden mod factions because the way this works is retarded
    // in order this gets all faction specs, then on each faction spec it gets all their known ships, and then on each of those ships...
    // it checks if the factionlist doesn't already contain the faction the ship corresponds to, whether it's in the banlist, then checks if it's actually automated and not remnant tagged
    // and if ALL that succeeds, we add the faction to the list to show.
    Global.getSector().allFactions.forEach { fac ->
        Global.getSettings().getFactionSpec(fac.id).knownShips.forEach {
            if (!factionlist.contains(fac.id) && !banlist.contains(fac.id) && Global.getSettings().getHullSpec(it).hasTag(Tags.AUTOMATED_RECOVERABLE) && Global.getSettings().getHullSpec(it).manufacturer!="Explorarium"){
                factionlist.add(fac.id)
            }
        }
    }
    factionlist.forEach { facid ->
        val facships = Global.getSettings().getFactionSpec(facid).knownShips.filter { Global.getSettings().getHullSpec(it).hasTag(Tags.AUTOMATED_RECOVERABLE) }
        val remmyships = remmy.knownShips.filter { facships.contains(it) }
        val cent = ((remmyships.size.toFloat() / facships.size.toFloat()) * 100f).roundToInt()
        val facname = Global.getSettings().getFactionSpec(facid).displayName
        val namecolor = Global.getSettings().getFactionSpec(facid).brightUIColor
        val facicon = Global.getSettings().getFactionSpec(facid).crest
        val tip = dialog.textPanel.beginTooltip()
        tip.addSectionHeading(facname, Global.getSettings().getFactionSpec(facid).brightUIColor, Global.getSettings().getFactionSpec(facid).darkUIColor, Alignment.MID, 10f)
        val img = tip.beginImageWithText(facicon, 64f, dialog.textWidth, false)
        img.addTitle(facname, namecolor).italicize(0.5f)
        img.addPara("Collection Progress: $cent%", 5f).setHighlight("$cent%")
        img.addPara("This faction knows ${facships.size} compatible automated ships.", 5f).setHighlight("${facships.size}")
        img.addPara("The Explorarium currently know ${remmyships.size} of them.", 5f).setHighlight("${remmyships.size}")
        tip.addImageWithText(5f)
        dialog.textPanel.addTooltip()
    }
}
fun getShowRaidTarget( dialog: InteractionDialogAPI,  target: MarketAPI?,  rewards: ArrayList<Int>){
    var isinit = false
    var stationname = ""
    var basename = ""
    var spec = targetmarket?.factionId
    var recipe = WeightedRandomPicker<String>()

    if (target == null){

        val factionlist = WeightedRandomPicker<String>()
        for (faction in Global.getSector().getAllFactions()) {
            val factionId = faction.getId()
            if (factionId != Factions.DERELICT && factionId != "nex_derelict" && factionId != Factions.REMNANTS && factionId != Factions.OMEGA && factionId != Factions.TRITACHYON && factionId != Factions.PLAYER){
                val markets = Misc.getFactionMarkets(factionId)
                val hasMilitaryMarket = markets.any { m ->
                    !m.isHidden && m.hasSpaceport() &&
                            (m.hasIndustry(Industries.MILITARYBASE) || m.hasIndustry(Industries.HIGHCOMMAND))
                }
                if (hasMilitaryMarket) {
                    factionlist.add(factionId)
                }
            }
        }
        spec = factionlist.pick()

        isinit = true
        val list = WeightedRandomPicker<MarketAPI>()
        Misc.getFactionMarkets(spec).forEach {
            if (!it.isHidden && it.hasSpaceport() && (it.hasIndustry(Industries.MILITARYBASE) || it.hasIndustry(Industries.HIGHCOMMAND))){
                list.add(it)
            }
        }
        targetmarket = list.pick()

    }
    if (targetmarket!!.hasIndustry(Industries.MILITARYBASE)){
        basename = "military base"
    } else basename = "high command"
    val facname = Global.getSettings().getFactionSpec(spec).displayName
    val faccolor = Global.getSettings().getFactionSpec(spec).baseUIColor
    if (rewards!!.isEmpty()){
        val defense = targetmarket!!.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).computeEffective(0f)
        rewardlist.add(0, (defense/100f).roundToInt())
        rewardlist.add(1, (defense/500f).roundToInt())
        rewardlist.add(2, (defense/1000f).roundToInt())
        rewardlist.add(3, (defense*1000f).roundToInt())
    }
    var numgamma = rewardlist[0]
    var numbeta = rewardlist[1]
    var numalpha = rewardlist[2]
    var numcredits = rewardlist[3]
    dialog.visualPanel.showMapMarker(targetmarket!!.primaryEntity, targetmarket!!.name, faccolor,  false, null, "Competitor Military Operations", null)
    dialog.textPanel.setFontVictor()
    dialog.textPanel.addPara("RECONSTRUCTING MESSAGE - // waiting ... //")
    dialog.textPanel.setFontInsignia()
    dialog.textPanel.addPara("Voluminous greetings, caretaker.")
    val para1 = dialog.textPanel.addPara("Regrettable activity within a $facname $basename leads us to purveying this request to be received within your flock.")
    para1.setHighlight(facname)
    para1.setHighlightColor(faccolor)
    dialog.textPanel.addPara("Please initiate protocol \"heartfelt fireworks celebration\" and visit upon ${targetmarket!!.name} to shower joy breadth skies.").setHighlight(targetmarket!!.name)
    dialog.textPanel.addPara("Butlers will be invited to assist in decoration and management of guests. Forth the apex of annihilation, promptly disperse and recollect within the Mothership.")
    dialog.textPanel.addPara("To be dispensed includes $numgamma chocolate mousse, $numbeta lemon zest, $numalpha blueberry gummies and $numcredits credits will be given ").setHighlight(numgamma.toString(), numbeta.toString(), numalpha.toString(), numcredits.toString())

    dialog.textPanel.setFontSmallInsignia()
    dialog.textPanel.addPara("Bombard the colony of ${targetmarket!!.name} to disrupt their military operations and return to a Mothership to receive your reward. Explorarium fleets will be dispatched to assist you. Expect resistance.").setHighlight(targetmarket!!.name)
    dialog.textPanel.addPara("Rewards are $numgamma gamma core, $numbeta beta core, $numalpha alpha core and $numcredits credits.").setHighlight(numgamma.toString(), numbeta.toString(), numalpha.toString(), numcredits.toString())
    dialog.textPanel.setFontInsignia()


}

fun doSetup(dialog: InteractionDialogAPI){
    Global.getSector().intelManager.addIntel(ds_nexusRaidIntel(targetmarket!!, rewardlist), false, dialog.textPanel)
    Global.getSector().listenerManager.addListener(ds_nexusRaidIntel(targetmarket!!, rewardlist))
    val targfaction = targetmarket!!.factionId
    for (i in 0 until 2) {
        val remmy = MagicCampaign.createFleetBuilder().setFleetName("Chauffeurs").setFleetFaction(Factions.DERELICT)
            .setAssignmentTarget(Global.getSector().playerFleet).setAssignment(FleetAssignment.ORBIT_PASSIVE)
            .setMinFP(Global.getSector().playerFleet.fleetPoints / 2).setIsImportant(true)
            .setSpawnLocation(Global.getSector().playerFleet.interactionTarget).setTransponderOn(false)
            .setQualityOverride(2f).create()
        remmy.memoryWithoutUpdate.apply {
            set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, true)
            set(MemFlags.MEMORY_KEY_ALLOW_PLAYER_BATTLE_JOIN_TOFF, true)
            set(MemFlags.DO_NOT_TRY_TO_AVOID_NEARBY_FLEETS, true)
            set(MemFlags.MEMORY_KEY_FLEET_DO_NOT_GET_SIDETRACKED, true)
            set(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, true)
        }
        remmy.removeFirstAssignment()
        remmy.addAssignment(FleetAssignment.ATTACK_LOCATION, targetmarket!!.primaryEntity, 30f)
        //remmy.addScript(ds_chauffeurAI(remmy, targetmarket!!))
    }
}

fun getRaidReward(dialog: InteractionDialogAPI){

    val intel = Global.getSector().intelManager.getFirstIntel(ds_nexusRaidIntel::class.java)

    if (intel is ds_nexusRaidIntel) {
        val rewards = intel.reward

        val numgamma = rewards[0]
        val numbeta = rewards[1]
        val numalpha = rewards[2]
        val numcredits = rewards[3]

        val playerFleet = Global.getSector().playerFleet
        val cargo = playerFleet.cargo

        cargo.addCommodity(Commodities.GAMMA_CORE, numgamma.toFloat())
        cargo.addCommodity(Commodities.BETA_CORE, numbeta.toFloat())
        cargo.addCommodity(Commodities.ALPHA_CORE, numalpha.toFloat())
        cargo.credits.add(numcredits.toFloat())

        dialog.textPanel.setFontVictor()
        dialog.textPanel.addPara("RECONSTRUCTING MESSAGE - // waiting ... //")
        dialog.textPanel.setFontInsignia()
        dialog.textPanel.addPara("Voluminous greetings, caretaker.")
        dialog.textPanel.addPara("$numgamma chocolate mousse, $numbeta lemon zest, $numalpha blueberry gummies and $numcredits credits are given")
            .setHighlight(numgamma.toString(), numbeta.toString(), numalpha.toString(), numcredits.toString())

        dialog.textPanel.setFontSmallInsignia()
        dialog.textPanel.addPara("$numgamma gamma core, $numbeta beta core, $numalpha alpha core and $numcredits credits are rewarded")
            .setHighlight(numgamma.toString(), numbeta.toString(), numalpha.toString(), numcredits.toString())
        dialog.textPanel.setFontInsignia()

        val sector = Global.getSector()
        val listenerMgr = sector.listenerManager
        val intelMgr = sector.intelManager

        listenerMgr.removeListenerOfClass(ds_nexusRaidIntel::class.java)

        val toRemove = mutableListOf<IntelInfoPlugin>()
        for (intel in intelMgr.intel) {
            if (intel is ds_nexusRaidIntel) {
                toRemove.add(intel)
            }
        }

        for (intel in toRemove) {
            intelMgr.removeIntel(intel)
        }

        targetmarket = null
        rewardlist.clear()
        Global.getSector().memoryWithoutUpdate.set("\$ds_nexusPartyTimeout", true, 180f)
        Global.getSector().memoryWithoutUpdate.set("\$ds_nexusParty", 2)

        Global.getSector().memoryWithoutUpdate.unset("\$ds_nexusParty")
    }

}