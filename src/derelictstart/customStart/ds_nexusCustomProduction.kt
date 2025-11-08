package data.derelictstart.customStart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomProductionPickerDelegate
import com.fs.starfarer.api.campaign.FactionProductionAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.loading.VariantSource


class ds_nexusCustomProduction (var dialog: InteractionDialogAPI): CustomProductionPickerDelegate {
    override fun getAvailableShipHulls(): MutableSet<String> {
        return Global.getSettings().allShipHullSpecs.filter { (Global.getSector().getFaction(Factions.DERELICT).knowsShip(it.hullId)) && !it.hullId.contains("station", true) }.map { it.hullId }.toMutableSet()
    }

    override fun getAvailableWeapons(): MutableSet<String> {
        return Global.getSettings().allWeaponSpecs.filter { Global.getSector().getFaction(Factions.DERELICT).knowsWeapon(it.weaponId) }.map { it.weaponId }.toMutableSet()
    }

    override fun getAvailableFighters(): MutableSet<String> {
        return Global.getSettings().allFighterWingSpecs.filter { Global.getSector().getFaction(Factions.DERELICT).knowsFighter(it.id) }.map { it.id }.toMutableSet()

    }

    override fun getCostMult(): Float {
        return 1f
    }

    override fun getMaximumValue(): Float {
        return Global.getSector().playerFleet.cargo.credits.get()
    }

    override fun withQuantityLimits(): Boolean {
        return false
    }

    override fun notifyProductionSelected(production: FactionProductionAPI?) { // stolen ratman code because idk how this works. thanks ratman.
        val currentstuff = production!!.current
        val pfleet = Global.getSector().playerFleet
        val pcargo = pfleet.cargo // restore NO_SELL tags
        //  fighterlist.forEach { Global.getSettings().getFighterWingSpec(it).addTag(Tags.NO_SELL) }
        //  fighterlist.clear()
        //   weaponlist.forEach { Global.getSettings().getWeaponSpec(it).addTag(Tags.NO_SELL) }
        //   weaponlist.clear()

        for (guy in currentstuff){
            if (guy.type == FactionProductionAPI.ProductionItemType.SHIP){
                for (i in 0 until guy.quantity){
                    val member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, guy.specId + "_Hull")
                    member.repairTracker.cr = member.repairTracker.maxCR
                    if (!member.variant.hasHullMod(HullMods.AUTOMATED) && !member.variant.hasHullMod("DEX_semiautomated")) member.variant.addPermaMod(HullMods.AUTOMATED)
                    member.fixVariant()
                    member.shipName = Global.getSector().getFaction(Factions.DERELICT).pickRandomShipName()
                    Global.getSector().playerFleet.fleetData.addFleetMember(member)
                    AddRemoveCommodity.addFleetMemberGainText(member, dialog.textPanel)

                }
            }
            else if (guy.type == FactionProductionAPI.ProductionItemType.FIGHTER){
                pcargo.addFighters(guy.specId, guy.quantity)
                AddRemoveCommodity.addFighterGainText(guy.specId, guy.quantity, dialog.textPanel)
            } else if (guy.type == FactionProductionAPI.ProductionItemType.WEAPON){
                pcargo.addWeapons(guy.specId, guy.quantity)
                AddRemoveCommodity.addWeaponGainText(guy.specId, guy.quantity, dialog.textPanel)
            }
        }
        val bucksSpent = production.totalCurrentCost
        AddRemoveCommodity.addCreditsLossText(bucksSpent, dialog.textPanel)
        Global.getSector().playerFleet.cargo.credits.subtract(bucksSpent.toFloat())

    }

    override fun getWeaponColumnNameOverride(): String {
        return "Weapons But Cool"
    }

    override fun getNoMatchingBlueprintsLabelOverride(): String {
        return "This shows up if you have nothing available"
    }

    override fun getMaximumOrderValueLabelOverride(): String {
        return "Maximum Bandwidth"
    }

    override fun getCurrentOrderValueLabelOverride(): String {
        return "Requested Bandwidth"
    }

    override fun getCustomOrderLabelOverride(): String {
        return "CustomOrderLabel"
    }

    override fun getNoProductionOrdersLabelOverride(): String {
        return "Nothing selected."
    }

    override fun getItemGoesOverMaxValueStringOverride(): String {
        return "I'm sorry, player. I can't do that."
    }

    override fun isUseCreditSign(): Boolean {
        return true
    }

    override fun getCostOverride(item: Any?): Int {
        return -1
    }

    companion object{
        fun ShipVariantAPI.getRefitVariant(): ShipVariantAPI {
            var shipVariant = this
            if (shipVariant.isStockVariant || shipVariant.source != VariantSource.REFIT) {
                shipVariant = shipVariant.clone()
                shipVariant.originalVariant = null
                shipVariant.source = VariantSource.REFIT
            }
            return shipVariant
        }

        fun FleetMemberAPI.fixVariant() {
            val newVariant = this.variant.getRefitVariant()
            if (newVariant != this.variant) {
                this.setVariant(newVariant, false, false)
            }

            newVariant.fixModuleVariants()
        } fun ShipVariantAPI.fixModuleVariants() {
            this.stationModules.forEach { (slotId, _) ->
                val moduleVariant = this.getModuleVariant(slotId)
                val newModuleVariant = moduleVariant.getRefitVariant()
                if (newModuleVariant != moduleVariant) {
                    this.setModuleVariant(slotId, newModuleVariant)
                }

                newModuleVariant.fixModuleVariants()
            }
        }
    }
}

