/**
 * 
 */
package com.crossover.techtrial.service;

import com.crossover.techtrial.dto.TopDriverDTO;
import com.crossover.techtrial.model.Ride;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RideService for rides.
 * @author crossover
 *
 */
public interface RideService {
  
  public Ride save(Ride ride);
  
  public Ride findById(Long rideId);

  public List<TopDriverDTO> findTopRides(Long count, LocalDateTime startTime, LocalDateTime endTime);

}
