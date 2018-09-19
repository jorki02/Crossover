package com.crossover.techtrial.controller;

import com.crossover.techtrial.dto.TopDriverDTO;
import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.model.Ride;
import com.crossover.techtrial.repositories.PersonRepository;
import com.crossover.techtrial.repositories.RideRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RideControllerTest {

    MockMvc mockMvc;

    @Mock
    private RideController rideController;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    RideRepository rideRepository;

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(rideController).build();
    }

    @Test
    public void testRideShouldBeRegistered() throws Exception {
        HttpEntity<Object> person1 = getHttpEntity(
                "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");
        HttpEntity<Object> person2 = getHttpEntity(
                "{\"name\": \"test 2\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");

        ResponseEntity<Person> response1 = template.postForEntity(
                "/api/person", person1, Person.class);
        ResponseEntity<Person> response2 = template.postForEntity(
                "/api/person", person2, Person.class);

        Ride ride = new Ride();
        ride.setDistance(1000L);
        ride.setEndTime("2018-08-08T12:12:12");
        ride.setStartTime("2018-08-08T12:10:12");
        ride.setDriver(response1.getBody());
        ride.setRider(response2.getBody());

        ResponseEntity<Ride> response = template.postForEntity(
                "/api/ride", ride, Ride.class);

        personRepository.deleteById(response1.getBody().getId());
        personRepository.deleteById(response2.getBody().getId());
        Assert.assertEquals(new Long(1000), response.getBody().getDistance());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testExceptionIfRiderDoesntExist() throws Exception {
        HttpEntity<Object> person1 = getHttpEntity(
                "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");

        ResponseEntity<Person> response1 = template.postForEntity(
                "/api/person", person1, Person.class);


        Person person = new Person();
        person.setId(-111L);
        Ride ride = new Ride();
        ride.setDistance(1000L);
        ride.setEndTime("2018-08-08T12:12:12");
        ride.setStartTime("2018-08-08T12:10:12");
        ride.setDriver(response1.getBody());
        ride.setRider(person);

        ResponseEntity<Ride> response = template.postForEntity(
                    "/api/ride", ride, Ride.class);

        personRepository.deleteById(response1.getBody().getId());

        Assert.assertEquals(400, response.getStatusCode().value());
    }

    @Test
    public void testExceptionIfStartDateOlderThanEndDate() {
        HttpEntity<Object> person1 = getHttpEntity(
                "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");
        HttpEntity<Object> person2 = getHttpEntity(
                "{\"name\": \"test 2\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");

        ResponseEntity<Person> response1 = template.postForEntity(
                "/api/person", person1, Person.class);
        ResponseEntity<Person> response2 = template.postForEntity(
                "/api/person", person2, Person.class);

        Ride ride = new Ride();
        ride.setDistance(1000L);
        ride.setStartTime ("2018-08-08T12:13:12");
        ride.setEndTime("2018-08-08T12:12:12");
        ride.setDriver(response1.getBody());
        ride.setRider(response2.getBody());

        ResponseEntity<Ride> response = template.postForEntity("/api/ride", ride, Ride.class);

        personRepository.deleteById(response1.getBody().getId());
        personRepository.deleteById(response2.getBody().getId());

        Assert.assertEquals(400, response.getStatusCode().value());

    }

    @Test
    public void testTopRiders() throws Exception {
        HttpEntity<Object> person1 = getHttpEntity(
                "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");
        HttpEntity<Object> person2 = getHttpEntity(
                "{\"name\": \"test 2\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");
        HttpEntity<Object> person3 = getHttpEntity(
                "{\"name\": \"test 3\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");

        ResponseEntity<Person> response1 = template.postForEntity(
                "/api/person", person1, Person.class);
        ResponseEntity<Person> response2 = template.postForEntity(
                "/api/person", person2, Person.class);
        ResponseEntity<Person> response3 = template.postForEntity(
                "/api/person", person3, Person.class);

        Ride ride1 = new Ride();
        ride1.setDistance(1000L);
        ride1.setStartTime("2018-08-08T12:10:12");
        ride1.setEndTime("2018-08-08T12:19:12");
        ride1.setDriver(response1.getBody());
        ride1.setRider(response2.getBody());

        Ride ride2 = new Ride();
        ride2.setDistance(400L);
        ride2.setStartTime("2018-08-08T12:10:12");
        ride2.setEndTime("2018-08-08T12:19:12");
        ride2.setDriver(response1.getBody());
        ride2.setRider(response2.getBody());

        Ride ride3 = new Ride();
        ride3.setDistance(800L);
        ride3.setStartTime("2018-08-08T12:14:12");
        ride3.setEndTime("2018-08-08T12:20:12");
        ride3.setDriver(response2.getBody());
        ride3.setRider(response3.getBody());

        Ride ride4 = new Ride();
        ride4.setDistance(1200L);
        ride4.setStartTime("2018-08-08T12:01:12");
        ride4.setEndTime("2018-08-08T12:04:12");
        ride4.setDriver(response3.getBody());
        ride4.setRider(response1.getBody());

        ResponseEntity<Ride> responseRide1 = template.postForEntity(
                "/api/ride", ride1, Ride.class);
        ResponseEntity<Ride> responseRide2 = template.postForEntity(
                "/api/ride", ride2, Ride.class);
        ResponseEntity<Ride> responseRide3 = template.postForEntity(
                "/api/ride", ride3, Ride.class);
        ResponseEntity<Ride> responseRide4 = template.postForEntity(
                "/api/ride", ride4, Ride.class);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("/api/top-rides")
                .queryParam("startTime", "2018-08-08T12:01:00")
                .queryParam("endTime", "2018-08-08T12:23:00")
                .queryParam("max", 2L);

        List topDriverDTOList = template.getForEntity(
                builder.toUriString(), List.class).getBody();

        builder = UriComponentsBuilder
                .fromUriString("/api/top-rides")
                .queryParam("startTime", "2018-08-08T12:14:00")
                .queryParam("endTime", "2018-08-08T12:21:00")
                .queryParam("max", 2L);

        List topDriverDTOListCheckDuration = template.getForEntity(
                builder.toUriString(), List.class).getBody();

        personRepository.deleteById(response1.getBody().getId());
        personRepository.deleteById(response2.getBody().getId());
        personRepository.deleteById(response3.getBody().getId());

        Assert.assertEquals("test 1", ((Map)topDriverDTOList.get(0)).get("name"));
        Assert.assertEquals("test 2", ((Map)topDriverDTOListCheckDuration.get(0)).get("name"));
    }

    @Test
    public void testReturningRide() throws Exception {
        HttpEntity<Object> person1 = getHttpEntity(
                "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");
        HttpEntity<Object> person2 = getHttpEntity(
                "{\"name\": \"test 2\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");

        ResponseEntity<Person> response1 = template.postForEntity(
                "/api/person", person1, Person.class);
        ResponseEntity<Person> response2 = template.postForEntity(
                "/api/person", person2, Person.class);

        Ride ride = new Ride();
        ride.setDistance(1000L);
        ride.setStartTime("2018-08-08T12:10:10");
        ride.setEndTime("2018-08-08T12:12:12");
        ride.setDriver(response1.getBody());
        ride.setRider(response2.getBody());

        ResponseEntity<Ride> response = template.postForEntity(
                "/api/ride", ride, Ride.class);

        ResponseEntity<Ride> responseGetById = template.getForEntity(
                "/api/ride/" + response.getBody().getId(), Ride.class);

        personRepository.deleteById(response1.getBody().getId());
        personRepository.deleteById(response2.getBody().getId());
        Assert.assertEquals(response.getBody().getDistance(), responseGetById.getBody().getDistance());
        Assert.assertEquals(200, responseGetById.getStatusCode().value());
    }


    private HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<Object>(body, headers);
    }
}
