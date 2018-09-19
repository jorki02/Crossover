/**
 *
 */
package com.crossover.techtrial.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.crossover.techtrial.dto.TopDriverDTO;
import com.crossover.techtrial.exceptions.RideCreationExeption;
import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.repositories.PersonRepository;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.crossover.techtrial.model.Ride;
import com.crossover.techtrial.repositories.RideRepository;

/**
 * @author crossover
 */
@Service
public class RideServiceImpl implements RideService {

    //@Resource(name="Repo")
    @Autowired
    RideRepository rideRepository;

    @Autowired
    PersonRepository personRepository;

    public Ride save(Ride ride) {
        if(!personRepository.findById(ride.getDriver().getId()).isPresent()) {
            throw new RideCreationExeption("Driver doesn't exist");
        }
        if(!personRepository.findById(ride.getRider().getId()).isPresent()) {
            throw new RideCreationExeption("Rider doesn't exist");
        }

        LocalDateTime startDateTime = LocalDateTime.parse(ride.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.parse(ride.getEndTime());

        if(startDateTime.isAfter(endDateTime) ){
            throw new RideCreationExeption("Start date is older than End date");
        }

        return rideRepository.save(ride);
    }

    public Ride findById(Long rideId) {
        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isPresent()) {
            return optionalRide.get();
        } else return null;
    }

    public List<TopDriverDTO> findTopRides(Long count, LocalDateTime startTime, LocalDateTime endTime){
        Iterable<Ride> allRides = rideRepository.findAll();
        List<Ride> ridesWithinDuration = new ArrayList<>();
        allRides.forEach(x->{if(LocalDateTime.parse(x.getStartTime()).isAfter(startTime) &&
                LocalDateTime.parse(x.getEndTime()).isBefore(endTime) ) {ridesWithinDuration.add(x);}});

        Map<Long, Pair<Integer, TopDriverDTO>> personRidesInfoMap = new HashMap<>();
        for(Ride ride : ridesWithinDuration){
            if(!personRidesInfoMap.containsKey(ride.getDriver().getId())){
                personRidesInfoMap.put(ride.getDriver().getId(), new Pair<>(1, new TopDriverDTO(ride.getDriver().getName(),
                        ride.getDriver().getEmail(),
                        (long)(LocalDateTime.parse(ride.getEndTime()).getSecond() - LocalDateTime.parse(ride.getStartTime()).getSecond()),
                        (long)(LocalDateTime.parse(ride.getEndTime()).getSecond() - LocalDateTime.parse(ride.getStartTime()).getSecond()),
                        (double)ride.getDistance())));
            } else {
                Pair<Integer, TopDriverDTO> topDriverDTOPair = personRidesInfoMap.get(ride.getDriver().getId());
                TopDriverDTO topDriverDTO = topDriverDTOPair.getValue();
                if((long)(LocalDateTime.parse(ride.getEndTime()).getSecond() - LocalDateTime.parse(ride.getStartTime()).getSecond()) >
                        topDriverDTO.getMaxRideDurationInSecods()){
                    topDriverDTO.setMaxRideDurationInSecods(
                            (long)(LocalDateTime.parse(ride.getEndTime()).getSecond() - LocalDateTime.parse(ride.getStartTime()).getSecond()));
                }
                topDriverDTO.setTotalRideDurationInSeconds(topDriverDTO.getTotalRideDurationInSeconds()
                        + (long)(LocalDateTime.parse(ride.getEndTime()).getSecond() - LocalDateTime.parse(ride.getStartTime()).getSecond()));

                topDriverDTO.setAverageDistance(
                        (topDriverDTO.getAverageDistance()*topDriverDTOPair.getKey() + ride.getDistance())/(topDriverDTOPair.getKey() + 1));
                personRidesInfoMap.put(ride.getDriver().getId(), new Pair<>(topDriverDTOPair.getKey() + 1, topDriverDTO));
            }
        }

        List<TopDriverDTO> topDriverDTOList = new ArrayList<>();
        personRidesInfoMap.entrySet().stream().forEach(x->{topDriverDTOList.add(x.getValue().getValue());});
        Collections.sort(topDriverDTOList, new Comparator<TopDriverDTO>() {
            @Override
            public int compare(TopDriverDTO o1, TopDriverDTO o2) {
                return o2.getTotalRideDurationInSeconds().compareTo(o1.getTotalRideDurationInSeconds());
            }
        });

        if(topDriverDTOList.size() < count.intValue()){
            return topDriverDTOList;
        } else {
            return topDriverDTOList.subList(0, count.intValue());
        }
    }

}
