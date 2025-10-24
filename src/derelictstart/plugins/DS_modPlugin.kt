package data.derelictstart.plugins

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.GenericPluginManagerAPI
import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import exerelin.campaign.DiplomacyManager



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
                            if (Global.getSettings().getVariant(variant).hullSpec.hullId == it && Global.getSettings().getVariant(variant).isGoalVariant) {
                                Global.getSettings().addDefaultEntryForRole(role, variant, 0f) // set 0 weight so it doesn't bleed over into other fleets (if we learned the eternity and set it to >0 weight, it would spawn in enigma fleets. this is bad!)
                                Global.getSettings().addEntryForRole(Factions.DERELICT, role, variant, (0.5f)) // 1 weight is actually pretty high
                            }
                        }
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
        }
    }

    override fun onNewGameAfterEconomyLoad() {
        val player = Global.getSector().getFaction(Factions.PLAYER)
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

            player.setRelationship(factionId, DiplomacyManager.STARTING_RELATIONSHIP_HOSTILE)
        }
    }

}