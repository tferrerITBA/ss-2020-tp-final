package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Grid {
	
	private List<Particle> particles;
	private Particle rebelShip;
	private Particle deathStar;
	private List<Turret> turrets;
	private List<Projectile> projectiles;
	private final double width;
	private final double depth;
	private final double height;
	
	public Grid(final List<Particle> particles) {
		this.particles = particles;
		this.rebelShip = particles.get(0);
		this.deathStar = particles.get(1);
		this.turrets = Configuration.getTurrets();
		this.projectiles = new ArrayList<>();
		this.width = Configuration.WIDTH;
		this.depth = Configuration.DEPTH;
		this.height = Configuration.HEIGHT;
	}
	
	public List<Particle> getParticles() {
		return particles;
	}
	
	public void setParticles(List<Particle> newParticles) {
		Objects.requireNonNull(newParticles);
		this.particles = newParticles;
	}
	
	public Particle getRebelShip() {
		return rebelShip;
	}

	public Particle getDeathStar() {
		return deathStar;
	}

	public List<Turret> getTurrets() {
		return turrets;
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
	
}
