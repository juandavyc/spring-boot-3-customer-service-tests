package com.ntloc.demo.customer;

import com.ntloc.demo.AbstractTestContainersTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest extends AbstractTestContainersTest {

    @Autowired
    CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        //given
        String email = "leon@gmail.com";
        Customer customer = Customer.create(
                "leon",
                email,
                "us"
        );
        //when
        customerRepository.save(customer);
    }

    @AfterEach
    void tearDown() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldReturnCustomerByEmail() {
        //then
        Optional<Customer> customerByEmail = customerRepository.findByEmail("leon@gmail.com");
        assertThat(customerByEmail).isPresent();
    }
    @Test
    void shouldReturnCustomerByEmailIsNotPresent() {
        //then
        Optional<Customer> customerByEmail = customerRepository.findByEmail("jason@gmail.com");
        assertThat(customerByEmail).isNotPresent();
    }
}