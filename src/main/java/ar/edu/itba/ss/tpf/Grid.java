package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.List;

public class Grid {
	
	//private List<Particle> particles;
	private RebelShip rebelShip;
	private DeathStar deathStar;
	private List<Turret> turrets;
	private List<Drone> drones;
	private List<Projectile> projectiles;
	private final double width;
	private final double depth;
	private final double height;
	
	public Grid(final List<Particle> particles) {
		//this.particles = particles;
		this.rebelShip = (RebelShip)particles.get(0);
		this.deathStar = (DeathStar)particles.get(1);
		this.turrets = Configuration.getTurrets();
		this.drones = Configuration.getDrones();
		this.projectiles = new ArrayList<>();
		this.width = Configuration.WIDTH;
		this.depth = Configuration.DEPTH;
		this.height = Configuration.HEIGHT;
	}
	
//	public List<Particle> getParticles() {
//		return particles;
//	}
	
//	public void setParticles(List<Particle> newParticles) {
//		Objects.requireNonNull(newParticles);
//		this.particles = newParticles;
//	}
	
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
