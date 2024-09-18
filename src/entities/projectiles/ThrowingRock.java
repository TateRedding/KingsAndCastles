package entities.projectiles;

import entities.Entity;
import objects.Player;

public class ThrowingRock extends Projectile {
    public ThrowingRock(Player player, int id, float x, float y, int damage, Entity target, float rotation) {
        super(player, id, x, y, rotation, damage, target, THROWING_ROCK);
    }
}
