package data.derelictstart.customStart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.Industry
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.rewardlist
import data.derelictstart.scripts.rulecmd.ds_nexusStartRulecmd.Companion.targetmarket
import org.lazywizard.lazylib.MathUtils



class ds_nexusRaidIntel (var bombmarket: MarketAPI, var reward: ArrayList<Int>): BaseIntelPlugin(), ColonyPlayerHostileActListener {
    val pcargo = Global.getSector().playerFleet.cargo
    val title = "Coordinated Raid"

    val memory = Global.getSector().memoryWithoutUpdate
    val key = "\$ds_nexusParty"
    val sprite = Global.getSettings().getFactionSpec(Factions.DERELICT).crest
    // stage 0 -> go to planet
    // stage 1 -> shit bombed go back


    override fun createIntelInfo(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?) {
        if (memory.getInt(key) == 1){
            info!!.addTitle(title)
            info.addPara("Return to any Mothership to receive the rewards.", 5f)

        } else {
            info!!.addTitle(title)
            info.addPara("Bombard ${bombmarket.name} in the ${bombmarket.starSystem.name}.", 5f)

        }

    }

    override fun autoAddCampaignMessage(): Boolean {
        return true
    }

    override fun getCommMessageSound(): String {
        return getSoundMajorPosting()
    }

    override fun getSmallDescriptionTitle(): String {
        return title
    }
    override fun getIntelTags(map: SectorMapAPI?): Set<String> {
        val tags = super.getIntelTags(map)

            tags.add(Tags.INTEL_ACCEPTED)
            tags.add(Tags.INTEL_MISSIONS)

        return tags
    }

    override fun getIcon(): String {
        return sprite
    }

    override fun getFactionForUIColors(): FactionAPI {
        return Global.getSector().getFaction(Factions.DERELICT)
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        if (memory.getInt(key) == 1){

        }
    }

    override fun reportRaidForValuablesFinishedBeforeCargoShown(dialog: InteractionDialogAPI?, market: MarketAPI?, actionData: MarketCMD.TempData?, cargo: CargoAPI?) {


    }

    override fun reportRaidToDisruptFinished(dialog: InteractionDialogAPI?, market: MarketAPI?, actionData: MarketCMD.TempData?, industry: Industry?) {

    }

    override fun reportTacticalBombardmentFinished(dialog: InteractionDialogAPI?, market: MarketAPI?, actionData: MarketCMD.TempData?) {
        if (market!! == bombmarket){
            memory.set(key, 1)
            Global.getSector().intelManager.addIntelToTextPanel(this, dialog!!.textPanel)

        }
    }

    override fun reportSaturationBombardmentFinished(dialog: InteractionDialogAPI?, market: MarketAPI?, actionData: MarketCMD.TempData?) {
        if (market!! == bombmarket){
            memory.set(key, 1)
            Global.getSector().intelManager.addIntelToTextPanel(this, dialog!!.textPanel)
        }
    }

    override fun isDone(): Boolean {
        return (memory.getInt(key) == 2)
    }



    override fun reportRemovedIntel() {
        Global.getSector().listenerManager.removeListenerOfClass(ds_nexusRaidIntel::class.java)
        targetmarket = null
         rewardlist.clear()
         memory.set("\$ds_nexusPartyTimeout", true, 180f)
        memory.set(key, 2)

        memory.unset(key)
    }

    fun getRewardList(): ArrayList<Int> {
        return reward
    }


}