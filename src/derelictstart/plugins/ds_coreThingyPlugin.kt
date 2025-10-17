package data.derelictstart.plugins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.BaseAICoreOfficerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color
import java.util.*

class ds_coreThingyPlugin: BaseAICoreOfficerPluginImpl(), AICoreOfficerPlugin {

    override fun createPerson(aiCoreId: String?, factionId: String?, random: Random?): PersonAPI? {
        return when (aiCoreId){
            "ds_core_delicious" -> makeTheGuy(factionId)
            "ds_playercore" -> Global.getSector().playerPerson
            else -> null
        }
    }
    fun makeTheGuy(factionId: String?): PersonAPI{

        val person = Misc.getAICoreOfficerPlugin(Commodities.GAMMA_CORE).createPerson("ds_core_delicious", factionId, Misc.random)
        person.apply {
            stats.level = 1
            memoryWithoutUpdate.set("\$ds_delicious", true)
            memoryWithoutUpdate.set("\$chatterChar", "freeborn")
            stats.setSkillLevel(Skills.COMBAT_ENDURANCE, 2f)


        }
     //   OfficerManagerEvent.pickSkill(person, Global.getSettings().skillIds, OfficerManagerEvent.SkillPickPreference.ANY, 1, Misc.random)
        return person

    }

    override fun createPersonalitySection(person: PersonAPI, tooltip: TooltipMakerAPI?) {
        val opad = 10f
        val text: Color = person.faction.baseUIColor
        val bg: Color = person.faction.darkUIColor
        tooltip!!.addPara("A somewhat primitive artificial intelligence built into the ship.", 5f).setAlignment(Alignment.MID)
        tooltip.addPara("While not quite as competent in combat as an AI core to start, they require no specialized maintenance and have a great learning capacity.", 5f)
        tooltip.addPara("May level up after combat and gain a random skill, up to level 6.", 5f).setHighlight("level 6.")



        tooltip.addSectionHeading("Personality: " + Misc.getPersonalityName(person), text, bg, Alignment.MID, 20F)
            when (person.personalityAPI.id) {
                Personalities.RECKLESS -> tooltip.addPara("In combat, this AI is single-minded and determined. " + "In a human captain, their traits might be considered reckless. In a machine, they're terrifying.", opad)

                Personalities.AGGRESSIVE -> tooltip.addPara("In combat, this AI will prefer to engage at a range that allows the use of " + "all of their ship's weapons and will employ any fighters under their command aggressively.", opad)

                Personalities.STEADY -> tooltip.addPara("In combat, this AI will favor a balanced approach with " + "tactics matching the current situation.", opad)

                Personalities.CAUTIOUS -> tooltip.addPara("In combat, this AI will prefer to stay out of enemy range, " + "only occasionally moving in if out-ranged by the enemy.", opad)

                Personalities.TIMID -> tooltip.addPara("In combat, this AI will attempt to avoid direct engagements if at all " + "possible, even if commanding a combat vessel.", opad)
            }

    }

}
