package data.derelictstart.skills

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.characters.*
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription
import com.fs.starfarer.api.impl.hullmods.Automated
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class ds_paradeigma: BaseSkillEffectDescription(), ShipSkillEffect {
    val remmy = Global.getSettings().getFactionSpec(Factions.DERELICT)
    val color = Color(100,250,210,250)
    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
      val para1 =  info!!.addPara("A neural link taps into the Explorarium mass mind.", 0f)
        para1.setHighlight("Explorarium")
        para1.setHighlightColor(remmy.brightUIColor)
      val para2 =  info.addPara("While you are in good relations with the Explorarium, confers the following effects:", 0f)
        para2.setHighlight("good", "Explorarium")
        para2.setHighlightColors(Misc.getHighlightColor(), remmy.brightUIColor)
        info.addPara("Allows the recovery and piloting of automated ships.", 10f).setHighlight("recovery", "piloting")
        info.addPara("If you have no human officers, automated ships do not suffer from the normal CR penalty.", 0f).setHighlight("no human officers," ,"do not")
        info.addPara("Additionally, AI cores integrated into ships will grant additional deployment points in combat.\nHigher level AI cores grant more.",0f).setHighlight("integrated")
        info.addPara("This bonus maxes out at +20% deployment points.", 0f).setHighlight("+20%")
        info.addPara("Also prevents all damage from solar corona and similar hazards", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
    }


    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        if (!Global.getSector().hasTransientScript(playerCoreScript::class.java)){
            Global.getSector().addTransientScript(playerCoreScript())
            Misc.getAllowedRecoveryTags().add(Tags.AUTOMATED_RECOVERABLE)

        }
        if (Global.getSector().getFaction(Factions.DERELICT).relToPlayer.rel < -0.2f){
            Automated.MAX_CR_PENALTY = 1f
            return
        }
        Misc.getAllowedRecoveryTags().add(Tags.AUTOMATED_RECOVERABLE)
       Automated.MAX_CR_PENALTY = 0f
        stats!!.dynamic.getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, 0f)
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {
        Automated.MAX_CR_PENALTY = 1f
    }


    class paradeigmaCharacter: CharacterStatsSkillEffect{
        override fun getEffectDescription(level: Float): String {
            return ""
        }

        override fun getEffectPerLevelDescription(): String {
            return ""
        }

        override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
            return LevelBasedEffect.ScopeDescription.FLEET
        }

        override fun apply(stats: MutableCharacterStatsAPI?, id: String?, level: Float) {
            if (stats!!.isPlayerStats){
                Misc.getAllowedRecoveryTags().add(Tags.AUTOMATED_RECOVERABLE)
            }
        }

        override fun unapply(stats: MutableCharacterStatsAPI?, id: String?) {
        }

    }
} class playerCoreScript: EveryFrameScript{
    private val aiBoats = HashMap<FleetMemberAPI, String>() // ok this part is just stolen from digital soul
    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        if (Global.getSector().playerFleet == null) return
        val cargo = Global.getSector().playerFleet.cargo ?: return
        val player = Global.getSector().playerPerson ?: return
        if ( !player.stats.hasSkill("ds_paradeigma") || Global.getSector().getFaction(Factions.DERELICT).relToPlayer.rel < -0.2f){
            removeCore(cargo)
            Global.getSector().removeTransientScriptsOfClass(this::class.java)
            return
        }
        if (Global.getSector().campaignUI.currentCoreTab == CoreUITabId.REFIT || Global.getSector().campaignUI.currentCoreTab == CoreUITabId.FLEET){
            addCore(cargo)
            aiBoats.forEach { member ->
                if (member.key.captain.isPlayer) Global.getSector().playerFleet.cargo.addCommodity(member.value, 1f)
            }
            aiBoats.clear()
            Global.getSector().playerFleet.membersWithFightersCopy.forEach { member ->
                if (!member.isFighterWing && member.captain != null && member.captain.isAICore && !member.captain.isPlayer) {
                    aiBoats[member] = member.captain.aiCoreId
                }
            }
        } else { removeCore(cargo)}
    }
    private fun addCore(cargo: CargoAPI) {
        if (cargo.getCommodityQuantity("ds_playercore") == 0f) {
            cargo.addCommodity("ds_playercore", 1f)
        }
    }

    private fun removeCore(cargo: CargoAPI) {
       cargo.stacksCopy.forEach { stack ->
            if (stack.isCommodityStack && stack.commodityId == "ds_playercore") {
                val amt = stack.size
                cargo.removeCommodity("ds_playercore", amt)
            }
        }
    }

}