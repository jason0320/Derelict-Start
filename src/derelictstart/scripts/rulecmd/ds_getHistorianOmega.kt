package data.derelictstart.scripts.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ds_getHistorianOmega: BaseCommandPlugin() {
    override fun execute(ruleId: String?, dialog: InteractionDialogAPI?, params: MutableList<Misc.Token>?, memoryMap: MutableMap<String, MemoryAPI>?): Boolean {
        val memory = getEntityMemory(memoryMap)
        val type = params!![0].getInt(memoryMap)
        val color = Color(100, 250, 210, 250)
        when (type){
            1 -> {
                if (memory.getString("\$genericHail_openComms") == null){
                    return false
                }
                if (memory.getString("\$genericHail_openComms") == "Nex_HistorianOmegaHail"){
                    return true
                }
            }
            2 -> {
                dialog!!.optionPanel.addOption("Transmit your Remnant IFF code", "ds_yeetOmega", color, "")
            }
            3 -> {
                dialog!!.visualPanel.fadeVisualOut()
                memory.unset("\$cfai_makeHostile")
                memory.unset("\$cfai_makeHostile_hist")
                memory.unset("\$cfai_makeAggressive")
                memory.unset("\$cfai_makeAggressive_hist")
                memory.unset("\$cfai_makeAggressiveLastsOneBattle")
                memory.unset("\$cfai_longPursuit")
                memory.unset("\$cfai_ignoreOtherFleets")
                memory.unset("\$cfai_doNotIgnorePlayer")
                memory.unset("\$ai_pursuitTarget")
                memory.set("\$cfai_makeNonHostile","true")
                memory.set("\$cfai_makeNonHostile_hist","true")

                val plugin = dialog.plugin as? FleetInteractionDialogPluginImpl
                val context = plugin?.context as? FleetEncounterContext
                context?.applyAfterBattleEffectsIfThereWasABattle()
                context?.battle?.let { battle ->
                    battle.leave(Global.getSector().playerFleet, false)
                    battle.leave(dialog!!.interactionTarget as CampaignFleetAPI?, false)
                    battle.finish(BattleAPI.BattleSide.NO_JOIN)
                }

                Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND_TECHNOLOGY, 1f,1f)
                return true
            }
        }


        return false
    }
}