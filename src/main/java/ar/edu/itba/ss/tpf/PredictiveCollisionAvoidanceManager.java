package ar.edu.itba.ss.tpf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PredictiveCollisionAvoidanceManager {
	
	private final Grid grid;
    private final Point rebelShipGoal;
    private final double timeStep;
    
    public PredictiveCollisionAvoidanceManager(final Grid grid) {
    	this.grid = grid;
    	this.rebelShipGoal = Configuration.DEATH_STAR_POSITION.getSumVector(
    			new Point(0, Configuration.DEATH_STAR_RADIUS + Configuration.REBEL_SHIP_RADIUS/* + Configuration.DEATH_STAR_SAFE_DISTANCE*/, 0));
    	this.timeStep = Configuration.TIME_STEP;
    }
    
    public Point getAcceleration(final Particle particle, final List<Particle> particles) {
		if(particle instanceof Projectile) {
			return new Point();
		}
		
		Point goal = new Point();
		double desiredSpeed = 0;
		if(particle instanceof RebelShip) {
			goal = this.rebelShipGoal;
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
//    	if(totalForce.getScalarDivision(particle.getMass()).getNorm() > 1E9)
//    		System.out.println("ACCCC " + particle.getId() + " " + accumulatedTime + " " + totalForce + " " 
//    				+ " " + evasiveForce);
        return totalForce.getScalarDivision(particle.getMass());
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
				desiredVelocity = desiredVelocity.getSumVector(evasiveForce.getScalarMultiplication(timeStep));
				//if((particle.getId() == 18 || particle.getId() == 20) && evasiveForce.getNorm() > 2000) System.out.println("BIG EV " + particle.getId() + " " + reprocessedCollision.getParticle().getId() + " " + reprocessedCollision.getTime() + " " + evasiveForce.getNorm() + " " + evasiveForce);
				//if(Double.compare(accumulatedTime, 21.22189999996388) == 0 && particle.getId() == 20) System.out.println("SI");
//				if(particle.getId() == 21 && accumulatedTime > 41.935 && accumulatedTime < 41.936) {
//					System.out.println("COLLISION: " + collision.getParticle().getId() + " " + evasiveForce);
//				}
				accumulatedEvasiveForce = accumulatedEvasiveForce.getSumVector(evasiveForce);
				processedCollisions++;
			}
		}
		
		if(processedCollisions == 0) {
			return accumulatedEvasiveForce;
		}
		return accumulatedEvasiveForce.getScalarDivision(processedCollisions);
	}
	
	private Point getEvasiveForce(Particle particle, Particle other, double collisionTime, Point desiredVelocity) {
    	Point c_i = particle.getPosition().getSumVector(desiredVelocity.getScalarMultiplication(collisionTime));
    	Point c_j = other.getPosition().getSumVector(other.getVelocity().getScalarMultiplication(collisionTime));
    	Point forceDirection = c_i.getDiffVector(c_j).normalize();
    	
    	double D = c_i.getDiffVector(particle.getPosition()).getNorm() + c_i.getDiffVector(c_j).getNorm() 
    			- /*getPersonalSpace(particle, other)*/particle.getRadius() - other.getRadius();//TODO PREGUNTAR
    	double d_min = getPersonalSpace(particle, other) - particle.getRadius();
    	double d_mid = d_min * 1.5;
        double d_max = d_min * 2;
        double forceMagnitude = 0;
        double multiplier = 3000;
        if(D < d_min) {
        	forceMagnitude = 1/(D*D) * multiplier;
        } else if(D < d_mid) {
        	forceMagnitude = 1/(d_min*d_min) * multiplier;
        } else if(D < d_max) {
        	forceMagnitude = (D - d_max) / (d_min*d_min * (d_mid - d_max)) * multiplier;
        }
//        if(collisionTime == 0 && (particle.getId() == 0 || other.getId() == 29))
//        	System.out.println("EVASIVE FORCE " + forceMagnitude + " " + particle.getId() + " " + other.getId() + ", DIST " + particle.getPosition().getDiffVector(
//        			other.getPosition()).getNorm() + ", D: " + D);
        
//        	System.out.println("D " + D + " PPOS " + particle.getPosition() + " OPOS " + other.getPosition() 
//			+ " OVEL " + other.getVelocity() + " DV " + desiredVelocity + " CT " + collisionTime 
//			+ " CI " + c_i + " CJ " + c_j);
//        if(particle.getId() == 29 && accumulatedTime > 21.030 && accumulatedTime < 21.031) {
//			System.out.println("D " + D + " PPOS " + particle.getPosition() + " OPOS " + other.getPosition() 
//			+ " OVEL " + other.getVelocity() + " DV " + desiredVelocity + " CT " + collisionTime 
//			+ " CI " + c_i + " CJ " + c_j);
//		}
        //if(accumulatedTime > 21.221 && accumulatedTime < 21.223 && particle.getId() == 20) System.out.println("EF " + particle.getId() + " " + other.getId() + " " + particle.getPosition() + " " + other.getPosition() + " DV " + desiredVelocity + " TIME " + collisionTime + " FM " + forceMagnitude + " D " + D + " " + c_i.getDiffVector(particle.getPosition()).getNorm() + " " + c_i.getDiffVector(c_j).getNorm() + " " + getPersonalSpace(particle, other) + " " + other.getRadius());
        return forceDirection.getScalarMultiplication(forceMagnitude);
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

	public Point getRebelShipGoal() {
		return rebelShipGoal;
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
    
}
