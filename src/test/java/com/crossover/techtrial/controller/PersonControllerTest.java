/**
 *
 */
package com.crossover.techtrial.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.crossover.techtrial.model.Person;
import com.crossover.techtrial.repositories.PersonRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author kshah
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PersonControllerTest {

    MockMvc mockMvc;

    @Mock
    private PersonController personController;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    PersonRepository personRepository;

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(personController).build();
    }

    @Test
    public void testPanelShouldBeRegistered() throws Exception {
        HttpEntity<Object> person = getHttpEntity(
                "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");
        ResponseEntity<Person> response = template.postForEntity(
                "/api/person", person, Person.class);

        personRepository.deleteById(response.getBody().getId());
        Assert.assertEquals("test 1", response.getBody().getName());
        Assert.assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testReturningPersons() throws Exception {
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

        ResponseEntity<List> response3 = template.getForEntity(
                "/api/person", List.class);


        personRepository.deleteById(response1.getBody().getId());
        personRepository.deleteById(response2.getBody().getId());
        Assert.assertEquals(2, response3.getBody().size());
    }

    @Test
    public void testReturningPersonById() throws Exception {
        HttpEntity<Object> person = getHttpEntity(
                "{\"name\": \"test 1\", \"email\": \"test10000000000001@gmail.com\","
                        + " \"registrationNumber\": \"41DCT\",\"registrationDate\":\"2018-08-08T12:12:12\" }");
        ResponseEntity<Person> response = template.postForEntity(
                "/api/person", person, Person.class);

        ResponseEntity<Person> responseGetById = template.getForEntity(
                "/api/person/" + response.getBody().getId(), Person.class);

        personRepository.deleteById(response.getBody().getId());

        Assert.assertEquals(response.getBody().getName(), responseGetById.getBody().getName());
    }

    private HttpEntity<Object> getHttpEntity(Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<Object>(body, headers);
    }

}
