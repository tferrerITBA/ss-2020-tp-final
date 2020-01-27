package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PredictiveCollisionAvoidanceManager {
	
    private final Grid grid;
    private final Point goal;
    private final double timeStep;
	private double accumulatedTime = 0.0;
    
    public PredictiveCollisionAvoidanceManager(final Grid grid) {
    	this.grid = grid;
    	this.goal = Configuration.DEATH_STAR_POSITION.getSumVector(new Point(0, Configuration.DEATH_STAR_RADIUS + Configuration.REBEL_SHIP_RADIUS, 0));
    	this.timeStep = Configuration.TIME_STEP;
    }
    
    public void execute() {
		double accumulatedPrintingTime = 0.0;
		double printingTimeLimit = 0.05; //s
    	
//    	List<Particle> prevParticles = initPrevParticles(grid.getParticles());
//    	List<Particle> predictedParticles = new ArrayList<>(prevParticles.size());
//    	prevParticles.forEach(p -> predictedParticles.add(p.clone()));
//    	
		while(Double.compare(accumulatedTime, Configuration.getTimeLimit()) <= 0) {
			if (accumulatedPrintingTime >= printingTimeLimit) {
				Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());
				accumulatedPrintingTime = 0;
			}
			accumulatedTime += timeStep;
			accumulatedPrintingTime += timeStep;
			
			deleteOutOfBoundsProjectiles();
			
			for(int i = 0; i < grid.getTurrets().size(); i++) {
				Turret turret = grid.getTurrets().get(i);
				turret.fire(timeStep, grid.getRebelShip(), grid.getDeathStar(), grid.getParticles(), grid.getProjectiles());
			}
			for(int i = 0; i < grid.getDrones().size(); i++) {
				Drone drone = grid.getDrones().get(i);
				drone.fire(timeStep, grid);
			}
			
			//grid.setParticles(updateParticles(prevParticles, predictedParticles));
			moveProjectiles();
			moveRebelShip();
			moveDrones();
		}
	}

	private void moveRebelShip() {
    	Particle rebelShip = grid.getRebelShip();
    	Point goalForce = getGoalForce(rebelShip, goal, Configuration.DESIRED_VEL);
    	Point wallForce = getWallForce(rebelShip);
    	Point evasiveForce = getAverageEvasiveForce(rebelShip, goal, Configuration.DESIRED_VEL);
    	Point totalForce = goalForce.getSumVector(wallForce).getSumVector(evasiveForce);
    	
		rebelShip.setVelocity(rebelShip.getVelocity().getSumVector(totalForce.getScalarMultiplication(timeStep)));
		rebelShip.setPosition(rebelShip.getPosition().getSumVector(rebelShip.getVelocity().getScalarMultiplication(timeStep)));
    }
	
	private void moveDrones() {
    	Particle rebelShip = grid.getRebelShip();
    	for(Drone drone : grid.getDrones()) {
    		Point goalForce = getGoalForce(drone, rebelShip.getPosition(), Configuration.DRONE_DESIRED_VEL);
        	Point evasiveForce = getAverageEvasiveForce(drone, rebelShip.getPosition(), Configuration.DRONE_DESIRED_VEL);
        	Point totalForce = goalForce.getSumVector(evasiveForce);
        	
        	drone.setVelocity(drone.getVelocity().getSumVector(totalForce.getScalarMultiplication(timeStep)));
    		drone.setPosition(drone.getPosition().getSumVector(drone.getVelocity().getScalarMultiplication(timeStep)));
    	}
    }
    
    private void moveProjectiles() {
    	List<Projectile> projectiles = grid.getProjectiles();
    	for(Projectile p : projectiles) {
    		p.setPosition(p.getPosition().getSumVector(p.getVelocity().getScalarMultiplication(timeStep)));
    	}
    }
    
    private void deleteOutOfBoundsProjectiles() {
    	List<Projectile> projectiles = grid.getProjectiles();
    	List<Projectile> toDelete = new ArrayList<>();
    	for(Projectile p : projectiles) {
    		Point pos = p.getPosition();
    		if(pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0 || pos.getX() > Configuration.WIDTH
    				|| pos.getY() > Configuration.HEIGHT || pos.getZ() > Configuration.DEPTH) {
    			toDelete.add(p);
    			p.getShooter().getProjectiles().remove(p);
    		}
    	}
    	grid.getParticles().removeAll(toDelete);
    	
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
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), particle.getPosition().getY());
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	normalUnitVector = new Point(0, -1, 0);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), Configuration.HEIGHT - particle.getPosition().getY());
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	/* Width walls */
    	normalUnitVector = new Point(0, 0, 1);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), particle.getPosition().getZ());
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	normalUnitVector = new Point(0, 0, -1);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), Configuration.DEPTH - particle.getPosition().getZ());
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	/* Depth walls */
    	normalUnitVector = new Point(1, 0, 0);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), particle.getPosition().getX());
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	normalUnitVector = new Point(-1, 0, 0);
    	wallForceMagnitude = getWallForceMagnitude(particle.getRadius(), Configuration.WIDTH - particle.getPosition().getX());
    	wallForce = wallForce.getSumVector(normalUnitVector.getScalarMultiplication(wallForceMagnitude));
    	
    	/* Death Star */
    	
    	return wallForce;
    }
    
    private double getWallForceMagnitude(double radius, double distance) {
    	if(Double.compare(distance - radius, Configuration.WALL_SAFE_DISTANCE) >= 0)
    		return 0;
		return (Configuration.WALL_SAFE_DISTANCE + radius - distance) / Math.pow(distance - radius, Configuration.K_CONSTANT);
	}

	private Point getAverageEvasiveForce(Particle particle, Point goal, double desiredSpeed) {
    	Point accumulatedEvasiveForce = new Point(0, 0, 0);
    	int processedCollisions = 0;
    	Point desiredVelocity = particle.getVelocity().getSumVector(getGoalForce(particle, goal, desiredSpeed)
    			.getSumVector(getWallForce(particle)).getScalarMultiplication(timeStep));

    	List<Collision> collisions = predictCollisions(particle, goal, desiredSpeed);
		for(Collision collision : collisions) {
			/* Collisions may now not occur due to evasive action in others */
			Collision reprocessedCollision = predictCollision(particle, collision.getParticle(), desiredVelocity);
			if(reprocessedCollision != null) {
				Point evasiveForce = getEvasiveForce(particle, collision.getParticle(), 
						reprocessedCollision.getTime(), desiredVelocity);
				desiredVelocity = desiredVelocity.getSumVector(evasiveForce).getScalarMultiplication(timeStep);
				accumulatedEvasiveForce = accumulatedEvasiveForce.getSumVector(evasiveForce);
				processedCollisions++;
			}
		}
		
		if(processedCollisions == 0) {
			return accumulatedEvasiveForce;
		}
		return accumulatedEvasiveForce.getScalarDivision(processedCollisions);
	}
    
    private List<Collision> predictCollisions(Particle particle, Point goal, double desiredSpeed) {
    	List<Collision> collisions = new ArrayList<>();
    	Point desiredVelocity = particle.getVelocity().getSumVector(getGoalForce(particle, goal, desiredSpeed).getSumVector(getWallForce(particle)).getScalarMultiplication(timeStep));
    	
    	for(Projectile projectile : grid.getProjectiles()) {
    		if(!particle.getProjectiles().contains(projectile)) {
    			Collision collision = predictCollision(particle, projectile, desiredVelocity);
        		if(collision != null) {
        			collisions.add(collision);
        		}
    		}
    	}
    	for(Drone drone : grid.getDrones()) {
    		if(!particle.equals(drone)) {
    			Collision collision = predictCollision(particle, drone, desiredVelocity);
        		if(collision != null) {
        			collisions.add(collision);
        		}
    		}
    	}
    	if(!particle.equals(grid.getRebelShip())) {
    		/* Must be a drone */
    		Collision collision = predictCollision(particle, grid.getRebelShip(), desiredVelocity);
    		if(collision != null) {
    			collisions.add(collision);
    		}
    	}
    	
    	Collections.sort(collisions);
    	if(collisions.size() > Configuration.PROJECTILE_AWARENESS_COUNT) {
    		return collisions.subList(0, Configuration.PROJECTILE_AWARENESS_COUNT);
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
				return new Collision(other, Math.min(t1, t2));
			}
		}
		return null;
	}
	
	private double getPersonalSpace(Particle particle, Particle other) {
    	if(other.getClass() == Projectile.class) {
    		return Configuration.ENTITY_TO_PROJECTILE_PERSONAL_SPACE;
    	}
    	
    	/* Other is an entity */
    	if(particle.getClass() == Drone.class) {
    		/* Particle is drone */
			if(other.getClass() == Drone.class) {
				return Configuration.DRONE_TO_DRONE_PERSONAL_SPACE;
			} else {
				return Configuration.DRONE_TO_REBEL_SHIP_PERSONAL_SPACE;
			}
		} else {
			/* Particle is rebel ship => Other is drone */
			return Configuration.REBEL_SHIP_TO_DRONE_PERSONAL_SPACE;
		}
	}

	private Point getEvasiveForce(Particle particle, Particle other, double collisionTime, Point desiredVelocity) {
    	Point c_i = particle.getPosition().getSumVector(desiredVelocity.getScalarMultiplication(collisionTime));
    	Point c_j = other.getPosition().getSumVector(other.getVelocity().getScalarMultiplication(collisionTime));
    	Point forceDirection = c_i.getDiffVector(c_j).normalize();
    	
    	double D = c_i.getDiffVector(particle.getPosition()).getNorm() + c_i.getDiffVector(c_j).getNorm() 
    			- getPersonalSpace(particle, other) - other.getRadius();
    	double d_min = 0.5;
    	double d_mid = 1;
        double d_max = 2;
        double forceMagnitude = 0;
        double multiplier = 5;
        if(D < d_min) {
        	forceMagnitude = 1/D * multiplier;
        } else if(D < d_mid) {
        	forceMagnitude = 1/d_min * multiplier;
        } else if(D < d_max) {
        	forceMagnitude = (D - d_max) / (d_min * (d_mid - d_max)) * multiplier;
        }
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
    
//    private List<Particle> updateParticles(final List<Particle> prevParticles, final List<Particle> predictedParticles) {
//    	List<Particle> currentParticles = grid.getParticles();
//    	predictParticles(predictedParticles, currentParticles, prevParticles);
//    	List<Particle> updatedParticles = new ArrayList<>(currentParticles.size());
//    	
//		for(int i = 0; i < currentParticles.size(); i++) {
//			Particle currParticle = currentParticles.get(i);
//			Particle prevParticle = prevParticles.get(i);
//			Particle predParticle = predictedParticles.get(i);
//			Particle updatedParticle = currParticle.clone();
//			
//			Point2D.Double currAcceleration = getAcceleration(currParticle, currentParticles);
//			Point2D.Double prevAcceleration = getAcceleration(prevParticle, prevParticles);
//			Point2D.Double predAcceleration = getAcceleration(predParticle, predictedParticles);
//			
//			double correctedVelocityX = currParticle.getVelocity().getX()
//					+ (1 / 3.0) * predAcceleration.getX() * timeStep
//					+ (5 / 6.0) * currAcceleration.getX() * timeStep
//					- (1 / 6.0) * prevAcceleration.getX() * timeStep;
//			
//			double correctedVelocityY = currParticle.getVelocity().getY()
//					+ (1 / 3.0) * predAcceleration.getY() * timeStep
//					+ (5 / 6.0) * currAcceleration.getY() * timeStep
//					- (1 / 6.0) * prevAcceleration.getY() * timeStep;
//			
//			prevParticle.setPosition(currParticle.getPosition().getX(), currParticle.getPosition().getY());
//			prevParticle.setVelocity(currParticle.getVelocity().getX(), currParticle.getVelocity().getY());
//			updatedParticle.setPosition(predParticle.getPosition().getX(), predParticle.getPosition().getY());
//			updatedParticle.setVelocity(correctedVelocityX, correctedVelocityY);
//			updatedParticles.add(updatedParticle);
//		}
//		
//		return updatedParticles;
//	}
//    
//	private void predictParticles(final List<Particle> predictedParticles,
//			final List<Particle> currentParticles, final List<Particle> prevParticles) {
//		for(int i = 0; i < currentParticles.size(); i++) {
//			Particle currParticle = currentParticles.get(i);
//			Particle prevParticle = prevParticles.get(i);
//			
//			Point2D.Double currAcceleration = getAcceleration(currParticle, currentParticles);
//			Point2D.Double prevAcceleration = getAcceleration(prevParticle, prevParticles);
//			
//			double newPositionX = currParticle.getPosition().getX() + currParticle.getVelocity().getX() * timeStep
//					+ (2 / 3.0) * currAcceleration.getX() * Math.pow(timeStep, 2)
//					- (1 / 6.0) * prevAcceleration.getX() * Math.pow(timeStep, 2);
//			
//			double predictedVelocityX = currParticle.getVelocity().getX() 
//					+ (3 / 2.0) * currAcceleration.getX() * timeStep
//					- (1 / 2.0) * prevAcceleration.getX() * timeStep;
//			
//			double newPositionY = currParticle.getPosition().getY() + currParticle.getVelocity().getY() * timeStep
//					+ (2 / 3.0) * currAcceleration.getY() * Math.pow(timeStep, 2)
//					- (1 / 6.0) * prevAcceleration.getY() * Math.pow(timeStep, 2);
//			
//			double predictedVelocityY = currParticle.getVelocity().getY() 
//					+ (3 / 2.0) * currAcceleration.getY() * timeStep
//					- (1 / 2.0) * prevAcceleration.getY() * timeStep;
//			
//			Particle predictedParticle = predictedParticles.get(i);
//			predictedParticle.setPosition(newPositionX, newPositionY);
//			predictedParticle.setVelocity(predictedVelocityX, predictedVelocityY);
//			predictedParticles.add(predictedParticle);
//		}
//	}
//
//	private Point2D.Double getAcceleration(final Particle particle, final List<Particle> particles) {
//    	Point2D.Double granularForce = getGranularForce(particle);
//    	Point2D.Double socialForce = getSocialForce(particle);
//    	Point2D.Double drivingForce = getDrivingForce(particle);
//    	
//        return new Point2D.Double(
//        		(granularForce.getX() + socialForce.getX() + drivingForce.getX()) / particle.getMass(),
//        		(granularForce.getY() + socialForce.getY() + drivingForce.getY()) / particle.getMass());
//    }
//	
//	private Point2D.Double getDrivingForce(final Particle particle) {
//		double desiredDirectionUnitVectorX = (grid.getExternalRadius() - particle.getPosition().getY());
//		double desiredDirectionUnitVectorY = - (grid.getExternalRadius() - particle.getPosition().getX());
//		double norm = Math.sqrt(Math.pow(desiredDirectionUnitVectorX, 2) + Math.pow(desiredDirectionUnitVectorY, 2));
//		desiredDirectionUnitVectorX /= norm;
//		desiredDirectionUnitVectorY /= norm;
//		
//		double drivingForceX = particle.getMass() 
//				* (Configuration.DESIRED_VEL * desiredDirectionUnitVectorX - particle.getVelocity().getX()) / Configuration.TAU;
//		double drivingForceY = particle.getMass() 
//				* (Configuration.DESIRED_VEL * desiredDirectionUnitVectorY - particle.getVelocity().getY()) / Configuration.TAU;
//		
//		return new Point2D.Double(drivingForceX, drivingForceY);
//	}
//
//
//	private Point2D.Double getWallCollisionForce(final Particle particle) {
//		double normalForce = 0;
//		double tangentForce = 0;
//		double resultantForceX = 0;
//		double resultantForceY = 0;
//		
//		double tangentUnitVectorX = (grid.getExternalRadius() - particle.getPosition().getY());
//		double tangentUnitVectorY = - (grid.getExternalRadius() - particle.getPosition().getX());
//		double norm = Math.sqrt(Math.pow(tangentUnitVectorX, 2) + Math.pow(tangentUnitVectorY, 2));
//		tangentUnitVectorX /= norm;
//		tangentUnitVectorY /= norm;
//		Point2D.Double normalUnitVector = new Point2D.Double(tangentUnitVectorY, - tangentUnitVectorX);
//    	Point2D.Double tangentUnitVector = new Point2D.Double(tangentUnitVectorX, tangentUnitVectorY);
//		
//		double distanceToCenter = Point2D.distance(particle.getPosition().getX(), particle.getPosition().getY(),
//				grid.getExternalRadius(), grid.getExternalRadius());
//		double tangentVelocity = particle.getVelocity().getX() * tangentUnitVectorX + particle.getVelocity().getY() * tangentUnitVectorY;
//		
//		if(distanceToCenter < particle.getRadius() + grid.getInternalRadius()) {
//			// Inner wall collision
//			double innerOverlap = (particle.getRadius() + grid.getInternalRadius()) - distanceToCenter;
//			normalForce += innerOverlap * Configuration.K_NORM;
//			tangentForce -= innerOverlap * Configuration.K_TANG * tangentVelocity;
//			
//		}
//		if(distanceToCenter + particle.getRadius() > grid.getExternalRadius()) {
//			// Outer wall collision
//			double outerOverlap = distanceToCenter + particle.getRadius() - grid.getExternalRadius();
//			normalForce -= outerOverlap * Configuration.K_NORM;
//			tangentForce -= outerOverlap * Configuration.K_TANG * tangentVelocity;
//		}
//		
//		resultantForceX += normalForce * normalUnitVector.getX() + tangentForce * tangentUnitVector.getX();
//		resultantForceY += normalForce * normalUnitVector.getY() + tangentForce * tangentUnitVector.getY();
//		
//		return new Point2D.Double(resultantForceX, resultantForceY);
//	}
//	
//	// Euler Algorithm evaluated in (- timeStep)
//    private List<Particle> initPrevParticles(List<Particle> currentParticles) {
//    	List<Particle> previousParticles = new ArrayList<>();
//		for(Particle p : currentParticles) {
//			Particle prevParticle = p.clone();
//			
//			Point2D.Double acceleration = getAcceleration(p, currentParticles);
//			
//			double prevPositionX = p.getPosition().getX() - timeStep * p.getVelocity().getX()
//					+ Math.pow(timeStep, 2) * acceleration.getX() / 2;
//			double prevPositionY = p.getPosition().getY() - timeStep * p.getVelocity().getY()
//					+ Math.pow(timeStep, 2) * acceleration.getY() / 2;
//			
//			double prevVelocityX = p.getVelocity().getX() - timeStep * acceleration.getX();
//			double prevVelocityY = p.getVelocity().getY() - timeStep * acceleration.getY();
//			
//			prevParticle.setPosition(prevPositionX, prevPositionY);
//			prevParticle.setVelocity(prevVelocityX, prevVelocityY);
//			previousParticles.add(prevParticle);
//		}
//		
//		return previousParticles;
//	}
    
}
