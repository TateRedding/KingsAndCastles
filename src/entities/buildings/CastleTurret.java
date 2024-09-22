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
    }

    public void releaseUnit() {
        Point spawn = buildingHandler.getSpawnTile(this);
        if (spawn != null) {
            occupyingUnit.reactivate(toPixelX(spawn.x), toPixelY(spawn.y));
            occupyingUnit = null;
        }
    }

    public void findAndAttackTarget() {
        Play play = buildingHandler.getPlay();
        for (Unit u : play.getUnitHandler().getUnits()) {
            if (u.getPlayer().getPlayerID() == player.getPlayerID() || !u.isActive()) continue;
            if (isTargetInRange(u, (int) (occupyingUnit.getActionRange() * TURRET_ATTACK_RANGE_MODIFIER)) && isLineOfSightOpen(u)) {
                if (attackTick >= TURRET_ATTACK_TICK_MAX) {
                    play.getProjectileHandler().newProjectile(this, u, (int) (occupyingUnit.getDamage() * TURRET_DAMAGE_MODIFIER));
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

    public Unit getOccupyingUnit() {
        return occupyingUnit;
    }

    public void setOccupyingUnit(Unit occupyingUnit) {
        this.occupyingUnit = occupyingUnit;
    }
}
