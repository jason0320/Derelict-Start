package data.derelictstart.plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.Factions
import exerelin.campaign.DiplomacyManager
import kaysaar.aotd_question_of_loyalty.data.scripts.commision.AoTDCommissionDataManager
import lunalib.lunaSettings.LunaSettings


class DS_modPlugin: BaseModPlugin() {

    override fun onGameLoad(newGame: Boolean) { // the mod plugin is TA but most things are labelled SB because i refactored wayyy too fucking late kill me
        val plugins: GenericPluginManagerAPI = Global.getSector().genericPlugins
        val sector = Global.getSector()
        val intmgr = Global.getSector().intelManager
        // if (!plugins.hasPlugin(ds_campaignPlugin::class.java)) plugins.addPlugin(ds_campaignPlugin())
        Global.getSector().registerPlugin(ds_campaignPlugin())

        if ( Global.getSector().memoryWithoutUpdate.getBoolean("\$ds_nexusStart")){
            val variants = Global.getSettings().allVariantIds
            Global.getSector().getFaction(Factions.DERELICT).knownShips.forEach {

                    if (Global.getSettings().getHullSpec(it).manufacturer != "Explorarium" && it != "rat_genesis") { // vague attempt to force remnants to re-learn hulls on save load if they don't have a default role
                        var role = "combatSmall" // we had to blacklist genesis because it uses a boss script that makes the game shit itself, i think?
                        when (Global.getSettings().getHullSpec(it).hullSize) {
                            ShipAPI.HullSize.CAPITAL_SHIP -> role = "combatCapital"
                            ShipAPI.HullSize.CRUISER -> role = "combatLarge"
                            ShipAPI.HullSize.DESTROYER -> role = "combatMedium"
                            else -> role = "combatSmall"
                        }
                        for (variant in variants) {
                            //if (Global.getSettings().getVariant(variant).hullSpec.hullId == it && Global.getSettings().getVariant(variant).isGoalVariant) {
                            if (Global.getSettings().getVariant(variant).hullSpec.hullId == it) {
                                Global.getSettings().addDefaultEntryForRole(role, variant, 0f) // set 0 weight so it doesn't bleed over into other fleets (if we learned the eternity and set it to >0 weight, it would spawn in enigma fleets. this is bad!)
                                Global.getSettings().addEntryForRole(Factions.DERELICT, role, variant, (0.5f)) // 1 weight is actually pretty high
                            }
                        }
                }
            }

            if (newGame) {

                val player = Global.getSector().getFaction(Factions.PLAYER)
                val remmy = Global.getSector().getFaction(Factions.DERELICT)

                var peacefulMode = LunaSettings.getBoolean("zz derelictstart", "peacefulMode")!!
                for (faction in Global.getSector().getAllFactions()) {
                    val factionId = faction.getId()
                    if (factionId == Factions.PLAYER) continue
                    if (factionId == Factions.DERELICT) continue
                    if (factionId == "nex_derelict") continue
                    if (factionId == Factions.REMNANTS) continue
                    if (factionId == Factions.OMEGA) continue
                    if (factionId == Factions.HEGEMONY) continue
                    if (factionId == Factions.TRITACHYON) continue
                    if (factionId == "sotf_dustkeepers") continue
                    if (factionId == "sotf_dustkeepers_proxies") continue
                    if (factionId == "sotf_sierra_faction") continue
                    if (factionId == "sotf_dreaminggestalt") continue

                    if (peacefulMode) {
                        remmy.setRelationship(factionId, 0f)
                        remmy.setRelationship(Factions.HEGEMONY, 0f)
                        remmy.setRelationship(Factions.TRITACHYON, 0f)
                        remmy.setRelationship(Factions.PIRATES, DiplomacyManager.STARTING_RELATIONSHIP_HOSTILE)
                        player.setRelationship(Factions.PIRATES, DiplomacyManager.STARTING_RELATIONSHIP_HOSTILE)
                        remmy.setRelationship(Factions.LUDDIC_PATH, DiplomacyManager.STARTING_RELATIONSHIP_HOSTILE)
                        player.setRelationship(Factions.LUDDIC_PATH, DiplomacyManager.STARTING_RELATIONSHIP_HOSTILE)
                    }
                    else
                    {
                        player.setRelationship(factionId, DiplomacyManager.STARTING_RELATIONSHIP_HOSTILE)
                        remmy.setRelationship(factionId, DiplomacyManager.STARTING_RELATIONSHIP_HOSTILE)
                    }
                }

                val derelictFaction: FactionAPI? = Global.getSector().getFaction("derelict")
                val nexderelictFaction: FactionAPI? = Global.getSector().getFaction("nex_derelict")
                if (derelictFaction != null && nexderelictFaction != null) {
                    val playerFaction: FactionAPI = Global.getSector().playerFaction
                    playerFaction.setRelationship(nexderelictFaction.id, 100f)
                    nexderelictFaction.setRelationship(playerFaction.id, 100f)
                    derelictFaction.setRelationship(nexderelictFaction.id, 100f)
                    nexderelictFaction.setRelationship(derelictFaction.id, 100f)
                }
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.REMNANTS, 0f)
                Global.getSector().getFaction(Factions.PLAYER).setRelationship(Factions.OMEGA, 0f)
                Global.getSector().getFaction(Factions.DERELICT).setRelationship(Factions.REMNANTS, 0f)
                Global.getSector().getFaction(Factions.DERELICT).setRelationship(Factions.OMEGA, 0f)

                if (Global.getSettings().modManager.isModEnabled("secretsofthefrontier")) {
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship("sotf_dustkeepers", 0f)
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship("sotf_dustkeepers_proxies", 0f)
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship("sotf_sierra_faction", 0f)
                    Global.getSector().getFaction(Factions.PLAYER).setRelationship("sotf_dreaminggestalt", 0f)
                    Global.getSector().getFaction(Factions.DERELICT).setRelationship("sotf_dustkeepers", 0f)
                    Global.getSector().getFaction(Factions.DERELICT).setRelationship("sotf_dustkeepers_proxies", 0f)
                    Global.getSector().getFaction(Factions.DERELICT).setRelationship("sotf_sierra_faction", 0f)
                    Global.getSector().getFaction(Factions.DERELICT).setRelationship("sotf_dreaminggestalt", 0f)
                }

                Global.getSector().playerFleet.fleetData.membersListCopy.forEach {
                    it.repairTracker.cr = 1f
                    it.status.hullFraction = 1f
                }
            }

            if (Global.getSettings().modManager.isModEnabled("IndEvo")) {
                Global.getSector().getFaction(Factions.PLAYER).setRelationship("IndEvo_derelict", 0f)
                Global.getSector().getFaction(Factions.DERELICT).setRelationship("IndEvo_derelict", 0f)
            }

            if (Global.getSettings().modManager.isModEnabled("aotd_qol")) {
                val market = Global.getSector().economy.getMarket("ds_nexusMarket")
                if (market != null) {
                    if (market.hasCondition(Conditions.DECIVILIZED_SUBPOP)) {
                        market.removeCondition(Conditions.DECIVILIZED_SUBPOP)
                    }
                    if (market.hasCondition(Conditions.DECIVILIZED)) {
                        market.removeCondition(Conditions.DECIVILIZED)
                    }
                    if (market.hasCondition(Conditions.RECENT_UNREST)){
                        market.removeCondition(Conditions.RECENT_UNREST)
                    }
                    for (i in 2..10) {
                        val condId = "population_$i"
                        if (market.hasCondition(condId)) {
                            market.removeCondition(condId)
                            market.addCondition(Conditions.POPULATION_1)
                        }
                    }
                    if (market.size != 1) {
                        market.size = 1
                    }
                    if (market.getCommodityData(Commodities.SUPPLIES).available < 1500) {
                        market.getCommodityData(Commodities.SUPPLIES).addTradeMod(Factions.DERELICT, 1500f, 30f)
                    }
                    if (market.getCommodityData(Commodities.FOOD).available < 3000) {
                        market.getCommodityData(Commodities.FOOD).addTradeMod(Factions.DERELICT, 3000f, 30f)
                    }
                }

                val manager = AoTDCommissionDataManager.getInstance()
                val rankIds = listOf(
                    "mercenary",
                    "auxiliary",
                    "commander",
                    "admiral",
                    "planetary_governor",
                    "system_governor",
                    "grand_moff"
                )

                for (id in rankIds) {
                    val old = manager.getRank(id)
                    if (old != null) {
                        val tags = old.tags.toMutableSet()
                        if (!tags.contains("nonrestrictive_colonization")) {
                            tags.add("nonrestrictive_colonization")
                            old.tags = tags
                        }
                        old.tags = tags
                        manager.addRank(old)
                    }
                }

            }

            if (Global.getSettings().modManager.isModEnabled("dex")) {
                val remmy = Global.getSector().getFaction(Factions.DERELICT)
                val loader = Global.getSettings().allShipHullSpecs  // map of hullId → ShipHullSpecAPI
                for (hull in loader) {
                    val tags = hull.tags
                    if (tags.contains("derelict_2") && !remmy.knownShips.contains(hull.baseHullId)) {
                        remmy.knownShips.add(hull.hullId)
                    }
                }
            }

            val remmy = Global.getSector().getFaction(Factions.DERELICT)
            remmy.isShowInIntelTab = true
        }
    }

}