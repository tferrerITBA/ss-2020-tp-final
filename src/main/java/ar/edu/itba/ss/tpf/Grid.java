package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.List;

public class Grid {
	
	private RebelShip rebelShip;
	private DeathStar deathStar;
	private List<Turret> turrets;
	private List<Drone> drones;
	private List<Projectile> projectiles;
	private final double width;
	private final double depth;
	private final double height;
	
	public Grid(final List<Particle> particles) {
		this.rebelShip = (RebelShip)particles.get(Configuration.REBEL_SHIP_INDEX);
		this.deathStar = (DeathStar)particles.get(Configuration.DEATH_STAR_INDEX);
		this.turrets = Configuration.getTurrets();
		this.drones = Configuration.getDrones();
		this.projectiles = new ArrayList<>();
		this.width = Configuration.WIDTH;
		this.depth = Configuration.DEPTH;
		this.height = Configuration.HEIGHT;
	}
	
	public boolean rebelShipHasCollided() {
		return hasCollided(rebelShip);
	}
	
	public void deleteOutOfBoundsProjectiles() {
    	List<Projectile> toDelete = new ArrayList<>();
    	for(Projectile p : projectiles) {
    		Point pos = p.getPosition();
    		if(pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0 || pos.getX() > Configuration.WIDTH
    				|| pos.getY() > Configuration.HEIGHT || pos.getZ() > Configuration.DEPTH
    				|| p.inContact(deathStar)) {
    			toDelete.add(p);
    			if(p.getShooter() != null) {
    				p.getShooter().getProjectiles().remove(p);
    			}
    		}
    	}
    	projectiles.removeAll(toDelete);
	}
	
	public void checkDroneCollisions() {
		List<Drone> toDelete = new ArrayList<>();
		for(Drone drone : drones) {
			if(hasCollided(drone)) {
				toDelete.add(drone);
			}
		}
		if(!toDelete.isEmpty()) {
			for(Drone drone : toDelete) {
				drone.getProjectiles().forEach(p -> p.setShooter(null));
				drones.remove(drone);
			}
		}
	}
	
	private boolean hasCollided(Particle particle) {
		for(Projectile projectile : projectiles) {
			if(particle instanceof RebelShip || (particle instanceof Drone && !((Drone)particle).getProjectiles().contains(projectile))) {
				if(particle.inContact(projectile)) {
					return true;
				}
			}
		}
		for(Turret turret : turrets) {
			if(particle.inContact(turret)) {
				return true;
			}
		}
		for(Drone drone : drones) {
			if(!particle.equals(drone) && particle.inContact(drone)) {
				return true;
			}
		}
		
		return particle.inContact(deathStar);
	}
	
	public RebelShip getRebelShip() {
		return rebelShip;
	}

	public DeathStar getDeathStar() {
		return deathStar;
	}

	public List<Turret> getTurrets() {
		return turrets;
	}
	
	public List<Drone> getDrones() {
		return drones;
	}

	public List<Projectile> getProjectiles() {
		return projectiles;
	}

	public double getWidth() {
		return width;
	}

	public double getDepth() {
		return depth;
	}

	public double getHeight() {
		return height;
	}

	public void setRebelShip(RebelShip rebelShip) {
		this.rebelShip = rebelShip;
	}

	public void setDrones(List<Drone> drones) {
		this.drones = drones;
	}
	
	public void setProjectiles(List<Projectile> projectiles) {
		this.projectiles = projectiles;
	}
}
