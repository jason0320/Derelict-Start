package data.derelictstart.customStart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import lunalib.lunaExtensions.getCustomEntitiesWithType
import org.magiclib.kotlin.getNearbyFleets
import org.magiclib.kotlin.getStationFleet

class ds_nexusLocationIntel: BaseIntelPlugin() {
    val sprite = Global.getSettings().getSpriteName("icons", "derelictflag")
    val check = IntervalUtil(1f,1f)

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addImage(sprite, width,5f)
        info.addPara("You are privy to the location of any Explorarium Mothership in the sector, owing to your background.", 5f)
        info.addPara("The following systems contain a Explorarium Mothership;", 10f)

        val nexii = Global.getSector().getCustomEntitiesWithType("derelict_mothership")
        nexii.forEach {
            if (it.starSystem.isInConstellation) {
                if (it.starSystem.constellation.name.startsWith("The")){
                    info.addPara("${it.starSystem.name}, in ${it.starSystem.constellation.name}", 10f).setHighlight(it.starSystem.name)

                } else {
                    info.addPara("${it.starSystem.name}, in the ${it.starSystem.constellation.name}", 10f).setHighlight(it.starSystem.name)

                }
            }
            else {
                info.addPara("${it.starSystem.name}", 10f).setHighlight(it.starSystem.name)
            }
        }

        info.addPara("You may repair and restock at any Explorarium Mothership by docking and opening a comm link.", 5f)
    }

    override fun getIcon(): String {
        return sprite
    }

    override fun advanceImpl(amount: Float) {

            if (!Global.getSector().intelManager.hasIntelOfClass(ds_nexusHostilityIntel::class.java) && Global.getSector().playerFleet.fleetPoints >= 200) {
                Global.getSector().intelManager.addIntel(ds_nexusHostilityIntel(), false)
            }
            else if (Global.getSector().intelManager.hasIntelOfClass(ds_nexusHostilityIntel::class.java) && !Global.getSector().hasTransientScript(ds_nexusHostilityIntel::class.java)){
                Global.getSector().addTransientScript(ds_nexusHostilityIntel())
            }

    }

    override fun isHidden(): Boolean {
        return false
    }
    override fun autoAddCampaignMessage(): Boolean {
        return true
    }

    override fun getCommMessageSound(): String {
        return getSoundMajorPosting()
    }
    override fun getListInfoParam(): Any {
        return IntelInfoPlugin.ListInfoMode.MESSAGES
    }


    override fun isImportant(): Boolean {
        return true
    }

    override fun shouldRemoveIntel(): Boolean {
        return false
    }

    override fun getSortTier(): IntelInfoPlugin.IntelSortTier {
        return IntelInfoPlugin.IntelSortTier.TIER_1
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags = super.getIntelTags(map)
        tags.add(Tags.INTEL_IMPORTANT)
        return tags
    }


    override fun getName(): String {
        return "Explorarium Network"
    }

}