package handlers;

import static entities.projectiles.Projectile.PROJECTILE_SIZE;
import static entities.units.Unit.getProjectileType;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

import entities.projectiles.Projectile;
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

    public void newProjectile(Unit u, Unit target) {
        if (u.getTargetEntity() == null) return;

        int projectileType = getProjectileType(u.getSubType());
        Rectangle unitHitbox = u.getHitbox();
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

        int damage = u.getDamage();

        for (Projectile p : projectiles)
            if (!p.isActive())
                if (p.getSubType() == projectileType) {
                    p.reuse(xPos, yPos, rotation, damage, u.getTargetEntity());
                    return;
                }

        projectiles.add(new Projectile(null, id++, xPos, yPos, rotation, damage, u.getTargetEntity(), projectileType));

    }

    public Play getPlay() {
        return play;
    }

    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }

}