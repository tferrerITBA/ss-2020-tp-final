package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PredictiveCollisionAvoidanceManager {
	
    private final Grid grid;
    private final Point goal;
    private final double timeStep;
	private double accumulatedTime = 0.0;
	private static final double EPSILON = 1;
    
    public PredictiveCollisionAvoidanceManager(final Grid grid) {
    	this.grid = grid;
    	this.goal = Configuration.DEATH_STAR_POSITION.getSumVector(new Point(0, Configuration.DEATH_STAR_RADIUS + Configuration.REBEL_SHIP_RADIUS/* + Configuration.DEATH_STAR_SAFE_DISTANCE*/, 0));
    	this.timeStep = Configuration.TIME_STEP;
    }
    
    public void execute() {
		double accumulatedPrintingTime = 0.0;
		double printingTimeLimit = 0.05; //s
    	
		List<Particle> currentParticles = new ArrayList<>();
		currentParticles.add(grid.getRebelShip());
		currentParticles.addAll(grid.getDrones());
    	List<Particle> prevParticles = initPrevParticles(currentParticles);
//    	List<Particle> predictedParticles = new ArrayList<>(prevParticles.size());
//    	prevParticles.forEach(p -> predictedParticles.add(p.clone()));
    	
		while(!simulationEnded()) {
			if (accumulatedPrintingTime >= printingTimeLimit) {
				Configuration.writeOvitoOutputFile(accumulatedTime, grid);
				accumulatedPrintingTime = 0;
			}
			accumulatedTime += timeStep;
			accumulatedPrintingTime += timeStep;
			
			for(Turret turret : grid.getTurrets()) {
				turret.fire(timeStep, grid);
			}
			for(Drone drone : grid.getDrones()) {
				drone.fire(timeStep, grid);
			}
			
			//moveProjectiles();
			
			List<Particle> updatedParticles = updateParticles(prevParticles/*, predictedParticles*/);
			updatePreviousParticles(prevParticles, currentParticles);
			setUpdatedParticlesInGrid(updatedParticles);
			
			deleteOutOfBoundsProjectiles();
			checkDroneCollisions();
			
			//moveRebelShip(); // TODO SACAR
			//moveDrones();
			
		}
	}

	private void setUpdatedParticlesInGrid(List<Particle> updatedParticles) {
		RebelShip rebelShip = updatedParticles.stream().filter(p -> p instanceof RebelShip)
				.map(p -> (RebelShip)p).findFirst().get();
		List<Drone> drones = updatedParticles.stream().filter(p -> p instanceof Drone)
				.map(p -> (Drone)p).collect(Collectors.toList());
		List<Projectile> projectiles = updatedParticles.stream().filter(p -> p instanceof Projectile)
				.map(p -> (Projectile)p).collect(Collectors.toList());
		
		grid.setRebelShip(rebelShip);
		grid.setDrones(drones);
		grid.setProjectiles(projectiles);
	}

	private boolean simulationEnded() {
		if(hasCollided(grid.getRebelShip())) {
			System.out.println("Ship has been destroyed.");
			return true;
		}
		
		boolean reachedGoal = goal.getDiffVector(grid.getRebelShip().getPosition()).getNorm() < EPSILON;
		if(reachedGoal) {
			System.out.println("Death star has been destroyed!");
			return true;
		}
		
		if(!Configuration.isGoalTimeLimit()) {
			return Double.compare(accumulatedTime, Configuration.getTimeLimit()) >= 0;
		}
		
		return false;
	}

	private boolean hasCollided(Particle particle) {
		for(Projectile projectile : grid.getProjectiles()) {
			if(particle instanceof RebelShip || (particle instanceof Drone && !((Drone)particle).getProjectiles().contains(projectile))) {
				if(particle.inContact(projectile)) {
					return true;
				}
			}
		}
		for(Turret turret : grid.getTurrets()) {
			if(particle.inContact(turret)) {
				return true;
			}
		}
		for(Drone drone : grid.getDrones()) {
			if(!particle.equals(drone) && particle.inContact(drone)) {
				return true;
			}
		}
		if(particle.inContact(grid.getDeathStar())) {
			return true;
		}
		
		return false;
	}
	
	private void checkDroneCollisions() {
		List<Drone> toDelete = new ArrayList<>();
		for(Drone drone : grid.getDrones()) {
			if(hasCollided(drone)) {
				toDelete.add(drone);
			}
		}
		if(!toDelete.isEmpty()) {
			for(Drone drone : toDelete) {
				drone.getProjectiles().forEach(p -> p.setShooter(null));
				grid.getDrones().remove(drone);
			}
		}
	}

//	private void moveRebelShip() {
//    	Particle rebelShip = grid.getRebelShip();
//    	Point goalForce = getGoalForce(rebelShip, goal, Configuration.DESIRED_VEL);
//    	Point wallForce = getWallForce(rebelShip);
//    	Point evasiveForce = getAverageEvasiveForce(rebelShip, goal, Configuration.DESIRED_VEL);
//    	Point totalForce = goalForce.getSumVector(wallForce).getSumVector(evasiveForce);
//    	
//    	Point newVelocity = rebelShip.getVelocity().getSumVector(totalForce.getScalarMultiplication(timeStep));
//    	if(newVelocity.getNorm() > Configuration.REBEL_SHIP_MAX_VEL) {
//    		newVelocity = newVelocity.normalize().getScalarMultiplication(Configuration.REBEL_SHIP_MAX_VEL);
//    	}
//		rebelShip.setVelocity(newVelocity);
//		rebelShip.setPosition(rebelShip.getPosition().getSumVector(rebelShip.getVelocity().getScalarMultiplication(timeStep)));
//		//if(rebelShip.getPosition().getDiffVector(goal).getNorm() < 1) System.out.println(rebelShip.getPosition().getDiffVector(goal).getNorm());
//	}
//	
//	private void moveDrones() {
//    	Particle rebelShip = grid.getRebelShip();
//    	for(Drone drone : grid.getDrones()) {
//    		Point goalForce = getGoalForce(drone, rebelShip.getPosition(), Configuration.DRONE_DESIRED_VEL);
//    		Point wallForce = getWallForce(drone);
//        	Point evasiveForce = getAverageEvasiveForce(drone, rebelShip.getPosition(), Configuration.DRONE_DESIRED_VEL);
//        	Point totalForce = goalForce.getSumVector(wallForce).getSumVector(evasiveForce);
//        	
//        	Point newVelocity = drone.getVelocity().getSumVector(totalForce.getScalarMultiplication(timeStep));
//        	if(newVelocity.getNorm() > Configuration.DRONE_MAX_VEL) {
//        		newVelocity = newVelocity.normalize().getScalarMultiplication(Configuration.DRONE_MAX_VEL);
//        	}
//        	drone.setVelocity(newVelocity);
//    		drone.setPosition(drone.getPosition().getSumVector(drone.getVelocity().getScalarMultiplication(timeStep)));
//    		
//    		//if(drone.getId() == 20 && accumulatedTime > 21.221 && accumulatedTime < 21.223) System.out.println(accumulatedTime + " " + goalForce.getNorm() + " "  + evasiveForce.getNorm() + " " + evasiveForce);
//    	}
//    }
//    
//    private void moveProjectiles() {
//    	List<Projectile> projectiles = grid.getProjectiles();
//    	for(Projectile p : projectiles) {
//    		p.setPosition(p.getPosition().getSumVector(p.getVelocity().getScalarMultiplication(timeStep)));
//    	}
//    }
    
    private void deleteOutOfBoundsProjectiles() {
    	List<Projectile> toDelete = new ArrayList<>();
    	for(Projectile p : grid.getProjectiles()) {
    		Point pos = p.getPosition();
    		if(pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0 || pos.getX() > Configuration.WIDTH
    				|| pos.getY() > Configuration.HEIGHT || pos.getZ() > Configuration.DEPTH
    				|| p.inContact(grid.getDeathStar())) {
    			toDelete.add(p);
    			if(p.getShooter() != null) {
    				p.getShooter().getProjectiles().remove(p);
    			}
    		}
    	}
    	grid.getProjectiles().removeAll(toDelete);
	}
    
    private Point getGoalForce(Particle particle, Point goal, double desiredSpeed) {
    	Point goalUnitVector = goal.getDirectionUnitVector(particle.getPosition());
//    	if(particle.equals(grid.getRebelShip())) {
//    		goalUnitVector = goalUnitVector.getScalarMultiplication(2);
//    	}
    	return goalUnitVector.getScalarMultiplication(desiredSpeed)
    			.getDiffVector(particle.getVelocity()).getScalarDivision(Configuration.TAU);
    }
    
    private Point getWallForce(Particle particle) {
    	Point wallForce = new Point();
    	Point normalUnitVector;
    	double wallForceMagnitude;
    	
    	/* Height walls */
    	normalUnitVector = new Point(0, 1, 0);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), particle.getPosition().getY(), Configuration.WALL_SAFE_DISTANCE);
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	normalUnitVector = new Point(0, -1, 0);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), Configuration.HEIGHT - particle.getPosition().getY(), Configuration.WALL_SAFE_DISTANCE);
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	/* Width walls */
    	normalUnitVector = new Point(0, 0, 1);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), particle.getPosition().getZ(), Configuration.WALL_SAFE_DISTANCE);
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	normalUnitVector = new Point(0, 0, -1);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), Configuration.DEPTH - particle.getPosition().getZ(), Configuration.WALL_SAFE_DISTANCE);
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	/* Depth walls */
    	normalUnitVector = new Point(1, 0, 0);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), particle.getPosition().getX(), Configuration.WALL_SAFE_DISTANCE);
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	normalUnitVector = new Point(-1, 0, 0);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), Configuration.WIDTH - particle.getPosition().getX(), Configuration.WALL_SAFE_DISTANCE);
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	/* Death Star */
    	Point diffVector = particle.getPosition().getDiffVector(grid.getDeathStar().getPosition());
    	normalUnitVector = diffVector.normalize();
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), diffVector.getNorm() - grid.getDeathStar().getRadius(), Configuration.DEATH_STAR_SAFE_DISTANCE);
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	return wallForce;
    }
    
    private double getWallForceMagnitude(double radius, double distance, double safeDistance) {
    	if(Double.compare(distance - radius, safeDistance) >= 0)
    		return 0;
		return (safeDistance + radius - distance) / Math.pow(distance - radius, Configuration.K_CONSTANT);
	}

	private Point getAverageEvasiveForce(Particle particle, Point goal, double desiredSpeed, final List<Particle> particles) {
    	Point accumulatedEvasiveForce = new Point(0, 0, 0);
    	int processedCollisions = 0;
    	Point desiredVelocity = particle.getVelocity().getSumVector(getGoalForce(particle, goal, desiredSpeed)
    			.getSumVector(getWallForce(particle)).getScalarMultiplication(timeStep));

    	List<Collision> collisions = predictCollisions(particle, goal, desiredSpeed, particles);
    	
		for(Collision collision : collisions) {
			/* Collisions may now not occur due to evasive action in others */
			Collision reprocessedCollision = predictCollision(particle, collision.getParticle(), desiredVelocity);
			if(reprocessedCollision != null) {
				Point evasiveForce = getEvasiveForce(particle, collision.getParticle(), 
						reprocessedCollision.getTime(), desiredVelocity);
				desiredVelocity = desiredVelocity.getSumVector(evasiveForce).getScalarMultiplication(timeStep);
				//if((particle.getId() == 18 || particle.getId() == 20) && evasiveForce.getNorm() > 2000) System.out.println("BIG EV " + particle.getId() + " " + reprocessedCollision.getParticle().getId() + " " + reprocessedCollision.getTime() + " " + evasiveForce.getNorm() + " " + evasiveForce);
				//if(Double.compare(accumulatedTime, 21.22189999996388) == 0 && particle.getId() == 20) System.out.println("SI");
				accumulatedEvasiveForce = accumulatedEvasiveForce.getSumVector(evasiveForce);
				processedCollisions++;
			}
		}
		
		if(processedCollisions == 0) {
			return accumulatedEvasiveForce;
		}
		return accumulatedEvasiveForce.getScalarDivision(processedCollisions);
	}
    
    private List<Collision> predictCollisions(Particle particle, Point goal, double desiredSpeed,
    		final List<Particle> particles) {
    	List<Collision> collisions = new ArrayList<>();
    	Point desiredVelocity = particle.getVelocity().getSumVector(getGoalForce(particle, goal, desiredSpeed)
    			.getSumVector(getWallForce(particle)).getScalarMultiplication(timeStep));
    	
    	for(Particle other : particles) {
    		if(!particle.equals(other)) {
    			if(other instanceof Projectile) {
    				if(particle instanceof RebelShip || 
    	    				(particle instanceof Drone && !((Drone)particle).getProjectiles().contains(other))) {
    	    			Collision collision = predictCollision(particle, other, desiredVelocity);
    	        		if(collision != null) {
    	        			collisions.add(collision);
    	        		}
    	    		}
    			} else {
    				Collision collision = predictCollision(particle, other, desiredVelocity);
            		if(collision != null) {
            			collisions.add(collision);
            		}
            		//if(particle.getId() == 18 && drone.getId() == 20 && accumulatedTime > 21.2 && accumulatedTime < 21.25) System.out.println(collision + " " + particle.getPosition().getDiffVector(drone.getPosition()).getNorm());
        		}
    		}
    	}
    	
		for(Turret turret : grid.getTurrets()) {
			Collision collision = predictCollision(particle, turret, desiredVelocity);
    		if(collision != null) {
    			collisions.add(collision);
    		}
		}
    	
    	Collections.sort(collisions);
    	if(collisions.size() > Configuration.COLLISION_AWARENESS_COUNT) {
    		return collisions.subList(0, Configuration.COLLISION_AWARENESS_COUNT);
    	}
    	return collisions;
    }

	private Collision predictCollision(Particle particle, Particle other, Point desiredVelocity) {
    	Point particlePos = particle.getPosition();
    	Point vel = desiredVelocity.getDiffVector(other.getVelocity());
		Point otherPos = other.getPosition();
		double personalSpace = getPersonalSpace(particle, other);
		
		double a = Math.pow(vel.getNorm(), 2);
		double b = 2 * vel.getDotProduct(particlePos.getDiffVector(otherPos));
		double c = Math.pow(particlePos.getDiffVector(otherPos).getNorm(), 2) 
				- Math.pow(personalSpace + other.getRadius(), 2);
		
		double det = b*b - 4*a*c;
		//System.out.println(particle.getId() + " " + other.getId() + " " + det);
		/* Collision may take place */
		if(Double.compare(det, 0) > 0) {
			double t1 = (-b + Math.sqrt(det)) / (2*a);
			double t2 = (-b - Math.sqrt(det)) / (2*a);
			//System.out.println(t1 + " " + t2);
			if((t1 < 0 && t2 > 0) || (t2 < 0 && t1 > 0)) {
				return new Collision(other, 0);
			} else if(Double.compare(t1, 0) >= 0 && Double.compare(t2, 0) >= 0) {
				double minT = Math.min(t1, t2);
				if(minT < Configuration.COLLISION_PREDICTION_TIME_LIMIT)
					return new Collision(other, minT);
				else
					return null;
			}
		}
		return null;
	}
	
	private double getPersonalSpace(Particle particle, Particle other) {
    	if(particle instanceof Drone) {
    		/* Particle is drone */
    		if(other instanceof Projectile) {
    			return Configuration.DRONE_TO_PROJECTILE_PERSONAL_SPACE;
    		} else if(other instanceof Drone) {
				return Configuration.DRONE_TO_DRONE_PERSONAL_SPACE;
			} else {
				/* Other is rebel ship */
				return Configuration.DRONE_TO_REBEL_SHIP_PERSONAL_SPACE;
			}
		} else {
			/* Particle is rebel ship */
			if(other instanceof Projectile) {
    			return Configuration.REBEL_SHIP_TO_PROJECTILE_PERSONAL_SPACE;
    		} else if(other instanceof Drone) {
				return Configuration.REBEL_SHIP_TO_DRONE_PERSONAL_SPACE;
			} else {
				/* Other is turret */
				return Configuration.REBEL_SHIP_TO_TURRET_PERSONAL_SPACE;
			}
		}
	}

	private Point getEvasiveForce(Particle particle, Particle other, double collisionTime, Point desiredVelocity) {
    	Point c_i = particle.getPosition().getSumVector(desiredVelocity.getScalarMultiplication(collisionTime));
    	Point c_j = other.getPosition().getSumVector(other.getVelocity().getScalarMultiplication(collisionTime));
    	Point forceDirection = c_i.getDiffVector(c_j).normalize();
    	
    	double D = c_i.getDiffVector(particle.getPosition()).getNorm() + c_i.getDiffVector(c_j).getNorm() 
    			- getPersonalSpace(particle, other)/*particle.getRadius()*/ - other.getRadius();//TODO PREGUNTAR
    	double d_min = 0.25;
    	double d_mid = 4;
        double d_max = 5;
        double forceMagnitude = 0;
        double multiplier = 5;
        if(D < d_min) {
        	forceMagnitude = 1/(D*D) * multiplier;
        } else if(D < d_mid) {
        	forceMagnitude = 1/(d_min*d_min) * multiplier;
        } else if(D < d_max) {
        	forceMagnitude = (D - d_max) / (d_min * (d_mid - d_max)) * multiplier;
        }
        //if(accumulatedTime > 21.221 && accumulatedTime < 21.223 && particle.getId() == 20) System.out.println("EF " + particle.getId() + " " + other.getId() + " " + particle.getPosition() + " " + other.getPosition() + " DV " + desiredVelocity + " TIME " + collisionTime + " FM " + forceMagnitude + " D " + D + " " + c_i.getDiffVector(particle.getPosition()).getNorm() + " " + c_i.getDiffVector(c_j).getNorm() + " " + getPersonalSpace(particle, other) + " " + other.getRadius());
        return forceDirection.getScalarMultiplication(forceMagnitude);
    }
    
	
	
    private class Collision implements Comparable<Collision> {
    	private Particle particle;
    	private double time;
    	
    	public Collision(Particle particle, double time) {
    		this.particle = particle;
    		this.time = time;
    	}
    	
    	public Particle getParticle() {
			return particle;
		}

		public double getTime() {
			return time;
		}

		@Override
		public int compareTo(Collision o) {
			return Double.compare(time, o.time);
		}
		
		@Override
		public String toString() {
			return particle + " " + time;
		}
    }
    
    private List<Particle> updateParticles(final List<Particle> prevParticles/*, final List<Particle> predictedParticles*/) {
    	List<Particle> currentParticles = new ArrayList<>();
		currentParticles.add(grid.getRebelShip());
		currentParticles.addAll(grid.getDrones());
		currentParticles.addAll(grid.getProjectiles());
		
		//List<Particle> predictedParticles = new ArrayList<>();
		//currentParticles.forEach(p -> predictedParticles.add(p.clone()));
		
		List<Particle> predictedParticles = predictParticles(/*predictedParticles, */currentParticles, prevParticles);
    	
    	List<Particle> updatedParticles = new ArrayList<>(currentParticles.size());
    	
		for(Particle currentParticle : currentParticles) {
			/* New projectiles do not have a previous reference */
			Optional<Particle> optionalPrevParticle = prevParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst();
			Particle prevParticle = null;
			Point prevAcceleration = null;
			if(optionalPrevParticle.isPresent()) {
				prevParticle = optionalPrevParticle.get();
				prevAcceleration = getAcceleration(prevParticle, prevParticles);
			} else {
				prevAcceleration = new Point();
			}
			
			Particle predParticle = predictedParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst().get();
			Particle updatedParticle = currentParticle.clone();
			
			Point currAcceleration = getAcceleration(currentParticle, currentParticles);
			Point predAcceleration = getAcceleration(predParticle, predictedParticles);
			
			Point correctedVelocity = currentParticle.getVelocity()
					.getSumVector(predAcceleration.getScalarMultiplication((1 / 3.0) * timeStep))
					.getSumVector(currAcceleration.getScalarMultiplication((5 / 6.0) * timeStep))
					.getDiffVector(prevAcceleration.getScalarMultiplication((1 / 6.0) * timeStep));
			
			if(currentParticle instanceof RebelShip && correctedVelocity.getNorm() > Configuration.REBEL_SHIP_MAX_VEL) {
				correctedVelocity = correctedVelocity.normalize().getScalarMultiplication(Configuration.REBEL_SHIP_MAX_VEL);
			} else if(currentParticle instanceof Drone && correctedVelocity.getNorm() > Configuration.DRONE_MAX_VEL) {
				correctedVelocity = correctedVelocity.normalize().getScalarMultiplication(Configuration.DRONE_MAX_VEL);
			}
			//if(accumulatedTime < 0.001 && currentParticle.getId() == 0) System.out.println("PREV ACC: " + prevAcceleration + " " + prevAcceleration.getNorm() + ", CURR ACC: " + currAcceleration + " " + currAcceleration.getNorm() + ", PRED ACC: " + predAcceleration + " " + predAcceleration.getNorm() + ", PREV VEL: " + prevParticle.getVelocity() + " " + prevParticle.getVelocity().getNorm() + ", CURR VEL: " + currentParticle.getVelocity() + " " + currentParticle.getVelocity().getNorm() + ", PRED VEL: " + predParticle.getVelocity() + " " + predParticle.getVelocity().getNorm() + ", CORR VEL " + correctedVelocity + " " + correctedVelocity.getNorm());
			//prevParticle.setPosition(currentParticle.getPosition());
			//prevParticle.setVelocity(currentParticle.getVelocity());
			updatedParticle.setPosition(predParticle.getPosition());
			updatedParticle.setVelocity(correctedVelocity);
			updatedParticles.add(updatedParticle);
		}
		
		updateProjectileReferences(updatedParticles);
		
		return updatedParticles;
	}
    
    /* Drones & Projectiles still have outdated references between them */
	private void updateProjectileReferences(List<Particle> particles) {
		int referenceId;
		for(Particle particle : particles) {
			if(particle instanceof Drone) {
				Drone drone = (Drone) particle;
				List<Projectile> updatedProjectiles = new ArrayList<>();
				
				for(Projectile projectile : drone.getProjectiles()) {
					referenceId = projectile.getId();
					final int refId = referenceId;
					/* Search for the updated object within the updated particles */
					updatedProjectiles.add((Projectile) particles.stream().filter(p -> p.getId() == refId).findFirst().get());
				}
				/* Update the entire projectiles list */
				drone.setProjectiles(updatedProjectiles);
			} else if(particle instanceof Projectile) {
				Projectile projectile = (Projectile) particle;
				//System.out.println(particle + " " + projectile.getShooter() + " " + ((Particle) projectile.getShooter()));
				/* Search for the updated object within the updated particles (if shooter is a Turret it remains static) */
				if(!(projectile.getShooter() instanceof Turret)) {
					referenceId = ((Particle) projectile.getShooter()).getId();
					final int refId = referenceId;
					Particle shooter = particles.stream().filter(p -> p.getId() == refId).findFirst().get();
					projectile.setShooter((Shooter) shooter);
				}
			}
		}
	}

	private void updatePreviousParticles(List<Particle> prevParticles, List<Particle> currentParticles) {
		for(Particle currentParticle : currentParticles) {
			/* New projectiles do not have a previous reference */
			Optional<Particle> optionalPrevParticle = prevParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst();
			if(optionalPrevParticle.isPresent()) {
				//prevParticle = optionalPrevParticle.get();
				//prevParticle.setPosition(currentParticle.getPosition());
				//prevParticle.setVelocity(currentParticle.getVelocity());
				
				prevParticles.remove(optionalPrevParticle.get());
			}
			prevParticles.add(currentParticle);
		}
	}

	private List<Particle> predictParticles(/*final List<Particle> predictedParticles,*/
			final List<Particle> currentParticles, final List<Particle> prevParticles) {
		List<Particle> predictedParticles = new ArrayList<>();
		
		for(Particle currentParticle : currentParticles) {
			Point currAcceleration = null;
			Point prevAcceleration = null;
			if(currentParticle instanceof Projectile) {
				/* Projectiles have no acceleration */
				currAcceleration = new Point();
				prevAcceleration = new Point();
			} else {
				Particle prevParticle = prevParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst().get();
				currAcceleration = getAcceleration(currentParticle, currentParticles);
				prevAcceleration = getAcceleration(prevParticle, prevParticles);
			}
			
			Point newPosition = currentParticle.getPosition()
					.getSumVector(currentParticle.getVelocity().getScalarMultiplication(timeStep))
					.getSumVector(currAcceleration.getScalarMultiplication((2 / 3.0) * Math.pow(timeStep, 2)))
					.getDiffVector(prevAcceleration.getScalarMultiplication((1 / 6.0) * Math.pow(timeStep, 2)));
			
			Point predictedVelocity = currentParticle.getVelocity()
					.getSumVector(currAcceleration.getScalarMultiplication((3 / 2.0) * timeStep))
					.getDiffVector(prevAcceleration.getScalarMultiplication((1 / 2.0) * timeStep));
			
			if(currentParticle instanceof RebelShip && predictedVelocity.getNorm() > Configuration.REBEL_SHIP_MAX_VEL) {
				predictedVelocity = predictedVelocity.normalize().getScalarMultiplication(Configuration.REBEL_SHIP_MAX_VEL);
			} else if(currentParticle instanceof Drone && predictedVelocity.getNorm() > Configuration.DRONE_MAX_VEL) {
				predictedVelocity = predictedVelocity.normalize().getScalarMultiplication(Configuration.DRONE_MAX_VEL);
			}
			
			//if(currentParticle.getId() == 0) System.out.println(currAcceleration.getNorm() + " " + prevAcceleration.getNorm() + " " + currentParticle.getVelocity().getNorm() + " " + predictedVelocity.getNorm());
			Particle predictedParticle = currentParticle.clone();//predictedParticles.stream().filter(p -> p.getId() == currentParticle.getId()).findFirst().get();
			predictedParticle.setPosition(newPosition);
			predictedParticle.setVelocity(predictedVelocity);
			predictedParticles.add(predictedParticle);
		}
		
		updateProjectileReferences(predictedParticles);
		
		return predictedParticles;
	}

	private Point getAcceleration(final Particle particle, final List<Particle> particles) {
		if(particle instanceof Projectile) {
			return new Point();
		}
		
		Point goal = new Point();
		double desiredSpeed = 0;
		if(particle instanceof RebelShip) {
			goal = this.goal;
			desiredSpeed = Configuration.DESIRED_VEL;
		} else if(particle instanceof Drone) {
			goal = particles.stream().filter(p -> p instanceof RebelShip).findFirst().get().getPosition();
			desiredSpeed = Configuration.DRONE_DESIRED_VEL;
		}
		
		Point goalForce = getGoalForce(particle, goal, desiredSpeed);
    	Point wallForce = getWallForce(particle);
    	Point evasiveForce = getAverageEvasiveForce(particle, goal, desiredSpeed, particles);
    	Point totalForce = goalForce.getSumVector(wallForce).getSumVector(evasiveForce);
    	//if(particle.getId() == 0) System.out.println(wallForce.getNorm());
        return totalForce.getScalarDivision(particle.getMass());
    }
	
	// Euler Algorithm evaluated in (-dt)
    private List<Particle> initPrevParticles(List<Particle> currentParticles) {
    	List<Particle> previousParticles = new ArrayList<>(currentParticles.size());
    	
    	for(Particle particle : currentParticles) {
			previousParticles.add(initPrevParticle(particle, currentParticles));
		}
    	
    	// TODO ARREGLAR REFERENCIAS EN PREVIOUS PARTICLES (asumiendo que habria proyectiles al comienzo)
		
		return previousParticles;
	}
    
    private Particle initPrevParticle(Particle particle, List<Particle> currentParticles) {
    	Particle prevParticle = particle.clone();
    	
    	Point acceleration = getAcceleration(particle, currentParticles);
		
    	Point prevPosition = particle.getPosition().getDiffVector(particle.getVelocity().getScalarMultiplication(timeStep))
    			.getSumVector(acceleration.getScalarMultiplication(Math.pow(timeStep, 2) / 2.0)); // TODO INICIALIZAR EN CERO AMBOS?
		Point prevVelocity = new Point();//particle.getVelocity().getDiffVector(acceleration.getScalarMultiplication(timeStep));
		
		prevParticle.setPosition(prevPosition);
		prevParticle.setVelocity(prevVelocity);
		
		return prevParticle;
    }
    
}
