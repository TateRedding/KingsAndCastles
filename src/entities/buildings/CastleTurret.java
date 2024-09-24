package entities.buildings;

import entities.units.Unit;
import gamestates.Play;
import handlers.BuildingHandler;
import objects.Player;

import java.awt.*;

import static main.Game.toPixelX;
import static main.Game.toPixelY;

public class CastleTurret extends Building {

    public static final float TURRET_DAMAGE_MODIFIER = 1.35f;
    public static final float TURRET_ATTACK_RANGE_MODIFIER = 1.5f;
    public static final int TURRET_ATTACK_TICK_MAX = 50;

    private Unit occupyingUnit;
    private int attackTick = TURRET_ATTACK_TICK_MAX;

    public CastleTurret(Player player, int id, int x, int y, BuildingHandler buildingHandler) {
        super(player, id, x, y, CASTLE_TURRET, buildingHandler);
        setDamageAndRanges(null);
    }

    public void releaseUnit() {
        Point spawn = ((BuildingHandler) combatEntityHandler).getSpawnTile(this);
        if (spawn != null) {
            occupyingUnit.reactivate(toPixelX(spawn.x), toPixelY(spawn.y));
            occupyingUnit = null;
        }
    }

    public void findAndAttackTarget() {
        Play play = combatEntityHandler.getPlay();
        for (Unit target : play.getUnitHandler().getUnits()) {
            if (target.getPlayer().getPlayerID() == player.getPlayerID() || !target.isActive()) continue;
            if (isTargetInRange(target, actionRange) && isLineOfSightOpen(target)) {
                if (attackTick >= TURRET_ATTACK_TICK_MAX) {
                    play.getProjectileHandler().newProjectile(this, target, (int) (occupyingUnit.getDamage() * TURRET_DAMAGE_MODIFIER));
                    attackTick = 0;
                } else
                    attackTick++;
                return;
            }
        }
        // No target found
        if (attackTick < TURRET_ATTACK_TICK_MAX)
            attackTick++;
    }

    private void setDamageAndRanges(Unit occupyingUnit) {
        if (occupyingUnit == null)
            this.damage = this.actionRange = this.sightRange = 0;
        else {
            this.damage = (int) (occupyingUnit.getDamage() * TURRET_DAMAGE_MODIFIER);
            this.actionRange = this.sightRange = (int) (occupyingUnit.getActionRange() * TURRET_ATTACK_RANGE_MODIFIER);
        }
    }


    public Unit getOccupyingUnit() {
        return occupyingUnit;
    }

    public void setOccupyingUnit(Unit occupyingUnit) {
        this.occupyingUnit = occupyingUnit;
        setDamageAndRanges(occupyingUnit);
    }
}
