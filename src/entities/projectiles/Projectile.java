package entities.projectiles;

import entities.Entity;
import objects.Player;
import utils.ImageLoader;

import java.awt.*;

public class Projectile extends Entity {

    private static final float PROJECTILE_SPEED = 10.0f;
    public static final int PROJECTILE_SIZE = 16;

    // Projectile SubTypes
    public static final int THROWING_ROCK = 0;
    public static final int ARROW = 1;
    public static final int BOLT = 2;

    protected float rotation;
    protected int damage;
    protected boolean active = true;

    protected Entity target;

    public Projectile(Player player, int id, float x, float y, float rotation, int damage, Entity target, int projectileType) {
        super(player, PROJECTILE, projectileType, x, y, id);
        this.rotation = rotation;
        this.target = target;
        this.hitbox = new Rectangle((int) x, (int) y, PROJECTILE_SIZE, PROJECTILE_SIZE);
    }

    public void reuse(float x, float y, float rotation, int damage, Entity target) {
        this.x = x;
        this.y = y;
        updateHitbox();
        this.rotation = rotation;
        this.damage = damage;
        this.target = target;
        active = true;
    }

    public void update() {
        move();
        if (target.getHitbox().intersects(hitbox)) {
            active = false;
            target.hurt(damage);
        }

    }

    public void render(Graphics g, int mapXOffset, int mapYOffset) {
        Graphics2D g2d = (Graphics2D) g;

        int xMid = hitbox.x + hitbox.width / 2;
        int yMid = hitbox.y + hitbox.height / 2;
        g2d.rotate(Math.toRadians(rotation), xMid, yMid);

        g2d.drawImage(ImageLoader.projectiles[subType], hitbox.x - mapXOffset, hitbox.y - mapYOffset, null);
        g2d.rotate(-Math.toRadians(rotation), xMid, yMid);
    }

    public void move() {
        int midX = hitbox.x + hitbox.width / 2;
        int midY = hitbox.y + hitbox.height / 2;

        Rectangle targetHitbox = target.getHitbox();
        int targetMidX = targetHitbox.x + targetHitbox.width / 2;
        int targetMidY = targetHitbox.y + targetHitbox.height / 2;
        int xDist = midX - targetMidX;
        int yDist = midY - targetMidY;
        int totDist = Math.abs(xDist) + Math.abs(yDist);
        float xPer = (float) Math.abs(xDist) / totDist;
        float xSpeed = xPer * PROJECTILE_SPEED;
        float ySpeed = PROJECTILE_SPEED - xSpeed;

        if (midX > targetMidX)
            xSpeed *= -1;
        if (midY > targetMidY)
            ySpeed *= -1;

        x += xSpeed;
        y += ySpeed;
        updateHitbox();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
