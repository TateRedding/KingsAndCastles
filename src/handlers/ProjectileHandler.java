package handlers;

import static entities.Projectile.PROJECTILE_SIZE;
import static entities.units.Unit.*;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import entities.CombatEntity;
import entities.Entity;
import entities.Projectile;
import entities.buildings.CastleTurret;
import entities.units.Unit;
import gamestates.Play;

public class ProjectileHandler implements Serializable {

    private Play play;
    private ArrayList<Projectile> projectiles = new ArrayList<>();

    private int id = 0;

    public ProjectileHandler(Play play) {
        this.play = play;
    }

    public void update() {
        for (Projectile p : projectiles)
            if (p.isActive())
                p.update();
    }

    public void render(Graphics g, int mapXOffset, int mapYOffset) {
        for (Projectile p : projectiles)
            if (p.isActive())
                p.render(g, mapXOffset, mapYOffset);
    }

    public void newProjectile(Entity attacker, CombatEntity target, int damage) {
        if (!(attacker instanceof Unit || attacker instanceof CastleTurret)) return;
        if (attacker instanceof Unit attackingUnit && (attackingUnit.getTargetEntity() == null || getAttackStyle(attackingUnit.getSubType()) != RANGED))
            return;

        int projectileType = (attacker instanceof CastleTurret turret ? getProjectileType(turret.getOccupyingUnit().getSubType()) : getProjectileType(attacker.getSubType()));
        Rectangle unitHitbox = attacker.getHitbox();
        Rectangle targetHitbox = target.getHitbox();
        int unitMidX = unitHitbox.x + unitHitbox.width / 2;
        int unitMidY = unitHitbox.y + unitHitbox.height / 2;

        int xDist = unitMidX - (targetHitbox.x + targetHitbox.width / 2);
        int yDist = unitMidY - (targetHitbox.y + targetHitbox.height / 2);

        float arcValue = (float) Math.atan(yDist / (float) xDist);
        float rotation = (float) Math.toDegrees(arcValue);

        if (xDist < 0)
            rotation += 180;

        float xPos = (float) unitMidX - (float) PROJECTILE_SIZE / 2;
        float yPos = (float) unitMidY - (float) PROJECTILE_SIZE / 2;

        for (Projectile p : projectiles)
            if (!p.isActive())
                if (p.getSubType() == projectileType) {
                    p.reuse(xPos, yPos, rotation, damage, target);
                    return;
                }

        projectiles.add(new Projectile(null, id++, xPos, yPos, rotation, damage, target, projectileType));

    }

    public Play getPlay() {
        return play;
    }

    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

}