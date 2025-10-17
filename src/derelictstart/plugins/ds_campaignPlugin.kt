package data.derelictstart.plugins

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.CampaignPlugin.PickPriority

class ds_campaignPlugin: BaseCampaignPlugin() {

    override fun pickAICoreOfficerPlugin(commodityId: String): PluginPick<AICoreOfficerPlugin>? {
        return when (commodityId) {
            "ds_playercore" -> PluginPick<AICoreOfficerPlugin>(ds_coreThingyPlugin(), PickPriority.MOD_SET)
            else -> null
        }
    }




}