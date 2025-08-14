package com.ntloc.demo.customer;

import com.ntloc.demo.AbstractTestContainersTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest extends AbstractTestContainersTest {

    public static final String API_CUSTOMER_PATH = "/api/v1/customers";

    //
    @Autowired
    TestRestTemplate testRestTemplate;


    @Test
    void shouldCreateCustomer() {
        // dto
        CreateCustomerRequest request = new CreateCustomerRequest(
                "name",
                (UUID.randomUUID()) + "@gmail.com",
                "US"
        );
//
        ResponseEntity<Void> createCustomerResponse = testRestTemplate.postForEntity(
                API_CUSTOMER_PATH,
                new HttpEntity<>(request),
                Void.class
        );
        assertThat(createCustomerResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        //
        ResponseEntity<List<Customer>> allCustomerResponse = testRestTemplate.exchange(
                API_CUSTOMER_PATH,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(allCustomerResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertNotNull(allCustomerResponse.getBody());

        Customer customerCreated = allCustomerResponse.getBody().stream()
                .filter(c -> c.getEmail().equals(request.email()))
                .findFirst()
                .orElseThrow();

        assertThat(customerCreated.getName()).isEqualTo(request.name());
        assertThat(customerCreated.getEmail()).isEqualTo(request.email());
        assertThat(customerCreated.getAddress()).isEqualTo(request.address());


    }

    @Test
    void shouldUpdateCustomer() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "name",
                (UUID.randomUUID()) + "@gmail.com",
                "US"
        );

        ResponseEntity<Void> createCustomerResponse = testRestTemplate.exchange(
                API_CUSTOMER_PATH,
                HttpMethod.POST,
                new HttpEntity<>(request),
                Void.class
        );

        assertThat(createCustomerResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        ResponseEntity<List<Customer>> allCustomerResponse = testRestTemplate.exchange(
                API_CUSTOMER_PATH,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(allCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(allCustomerResponse.getBody());

        Long id = allCustomerResponse.getBody().stream()
                .filter(c -> c.getEmail().equals(request.email()))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        // when
        String newEmail = (UUID.randomUUID()) + "@gmail.com";

        testRestTemplate.exchange(
                API_CUSTOMER_PATH + "/" + id + "?email=" + newEmail,
                HttpMethod.PUT,
                null,
                Void.class
        ).getStatusCode().is2xxSuccessful();

        ResponseEntity<Customer> customerById = testRestTemplate.exchange(
                API_CUSTOMER_PATH + "/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(customerById.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertNotNull(customerById.getBody());

        Customer customerUpdated = customerById.getBody();

        assertThat(customerUpdated.getName()).isEqualTo(request.name());
        assertThat(customerUpdated.getEmail()).isEqualTo(newEmail);
        assertThat(customerUpdated.getAddress()).isEqualTo(request.address());

    }

    @Test
    void deleteCustomer() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "name",
                (UUID.randomUUID()) + "@gmail.com",
                "US"
        );

        ResponseEntity<Void> createCustomerResponse = testRestTemplate.exchange(
                API_CUSTOMER_PATH,
                HttpMethod.POST,
                new HttpEntity<>(request),
                Void.class
        );

        assertThat(createCustomerResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        ResponseEntity<List<Customer>> allCustomerResponse = testRestTemplate.exchange(
                API_CUSTOMER_PATH,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(allCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(allCustomerResponse.getBody());

        Long id = allCustomerResponse.getBody().stream()
                .filter(c -> c.getEmail().equals(request.email()))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        testRestTemplate.exchange(
                API_CUSTOMER_PATH+"/"+id,
                HttpMethod.DELETE,
                null,
                Void.class
        ).getStatusCode().is2xxSuccessful();

        //then

        ResponseEntity<Customer> customerById = testRestTemplate.exchange(
                API_CUSTOMER_PATH+"/"+id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(customerById.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}