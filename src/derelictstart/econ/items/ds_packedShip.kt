package data.derelictstart.econ.items

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color
import kotlin.math.roundToInt

class ds_packedShip: BaseSpecialItemPlugin() {
    private var memberID: String? = null
    private var member: FleetMemberAPI? = null


    override fun performRightClickAction() {
        if (member == null) return
        Global.getSector().playerFleet.fleetData.addFleetMember(member)
        Global.getSector().campaignUI.messageDisplay.addMessage("Retrieved the ${member!!.shipName}.")

    }

    override fun render(x: Float, y: Float, w: Float, h: Float, alphaMult: Float, glowMult: Float, renderer: SpecialItemPlugin.SpecialItemRendererAPI?) {
        super.render(x, y, w, h, alphaMult, glowMult, renderer)
    }

    override fun init(stack: CargoStackAPI?) {
        memberID = stack!!.specialDataIfSpecial.data
        Global.getSector().getEntityById("ds_nexusStorage").market.getSubmarket(Submarkets.SUBMARKET_STORAGE).cargo.mothballedShips.membersListCopy.forEach {
            if (it.id == memberID){
                member = it
            }
        }
    }


    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, transferHandler: CargoTransferHandlerAPI?, stackSource: Any?) {
        tooltip!!.addPara("i packed this ship into your butthole",5f)
        if (member == null) return
        tooltip.addTitle(member!!.shipName).setColor(Color.PINK)
        if (!expanded){

        } else {
           val img = tooltip.beginImageWithText(member!!.hullSpec.spriteName, 128f)
            img.addTitle(member!!.shipName).setColor(Color.PINK)
        }
    }

    override fun getName(): String {
        if (member != null) return "Packed ${member!!.hullSpec.nameWithDesignationWithDashClass}"
        return ""
    }


    override fun isTooltipExpandable(): Boolean {
        return true
    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun getPrice(market: MarketAPI?, submarket: SubmarketAPI?): Int {
        return member!!.baseValue.roundToInt()
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }




}