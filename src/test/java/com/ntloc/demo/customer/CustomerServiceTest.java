package com.ntloc.demo.customer;


import com.ntloc.demo.exception.CustomerEmailUnavailableException;
import com.ntloc.demo.exception.CustomerNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {


    CustomerService underTest;

    @Mock
    CustomerRepository customerRepository;

    @Captor
    ArgumentCaptor<Customer> customerArgumentCaptor;

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerRepository);
    }

    @Test
    void shouldGetAllCustomers() {
        //given
        //when
        underTest.getCustomers();
        //then //verifica que llave el metodo findall en este caso en getCustomer
        verify(customerRepository).findAll();
    }

    @Test
    void shouldReturnMoreThanOneCustomer() {
        // given
        List<Customer> customers = List.of(
                new Customer(1L, "juan", "a@a", "us"),
                new Customer(2L, "juan", "a@a", "us")
        );
        // when
        when(customerRepository.findAllByAddress("us")).thenReturn(customers);

        List<Customer> actual = underTest.getCustomersByAddress("us");

        assertThat(actual.size()).isGreaterThan(1);

        verify(customerRepository).findAllByAddress("us");

    }

    @Test
    void shouldThrowNotFoundWhenGivenInvalidIDWhileGetCustomerById() {
        // g
        long id = 1L;
        // w
        when(customerRepository.findById(id)).thenReturn(Optional.empty());
        // t
        assertThatThrownBy(() -> underTest.getCustomerById(id))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with id " + id + " doesn't found");


    }

    @Test
    void shouldGetCustomerById() {
        long id = 1L;
        Customer customer = new Customer(id, "juan", "juan@juan.com", "us");

        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Customer customerById = underTest.getCustomerById(id);

        assertThat(customerById).isEqualTo(customer);


    }

    @Test
    void shouldCreateCustomer() {
        //g
        CreateCustomerRequest createCustomerRequest
                = new CreateCustomerRequest("juan", "a@a", "us");
        //w
        underTest.createCustomer(createCustomerRequest);
        //t

        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer customerCaptured = customerArgumentCaptor.getValue();

        assertThat(customerCaptured.getEmail()).isEqualTo(createCustomerRequest.email());
        assertThat(customerCaptured.getAddress()).isEqualTo(createCustomerRequest.address());
        assertThat(customerCaptured.getName()).isEqualTo(createCustomerRequest.name());

    }

    @Test
    void shouldNotCreateCustomerAndThrowExceptionWhenEmailIsUnavailable() {
        //g

        CreateCustomerRequest createCustomerRequest
                = new CreateCustomerRequest("juan", "a@a.com", "us");

        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(new Customer()));
        //when
        assertThatThrownBy(() -> underTest.createCustomer(createCustomerRequest))
                .isInstanceOf(CustomerEmailUnavailableException.class)
                .hasMessageContaining("The email " + createCustomerRequest.email() + " unavailable.");


    }

    @Test
    void shouldThrowNotFoundWhenGivenInvalidIDWhileUpdateCustomer() {
        //given
        Long id = 1L;
        String name = "leon";
        String email = "leon@gmail.com";
        String address = "US";

        when(customerRepository.findById(id)).thenReturn(Optional.empty());
        //when
        //
        assertThatThrownBy(() -> underTest.updateCustomer(id, name, email, address))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with id " + id + " doesn't found");
        //then
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldOnlyUpdateCustomerName() {
        //
        Long id = 1L;
        String name = "leon";
        String email = "leon@gmail.com";
        String address = "US";
        Customer customer = new Customer(id, name, email, address);
        String newName = "kennedy";

        when(customerRepository.findById(anyLong()))
                .thenReturn(Optional.of(customer));
        //
        underTest.updateCustomer(id, newName, null, null);
        //
        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(newName);
        assertThat(capturedCustomer.getEmail()).isEqualTo("leon@gmail.com");
        assertThat(capturedCustomer.getAddress()).isEqualTo("US");

    }

    @Test
    void shouldThrowEmailUnavailableWhenGivenEmailUpdateCustomer() {
        //
        Long id = 1L;
        String name = "leon";
        String email = "leon@gmail.com";
        String address = "US";

        Customer customer = new Customer(id, name, email, address);

        String newEmail = "leonaldo@gmail.com";

        Customer customerByEmail = new Customer(2L, name, newEmail, address);

        when(customerRepository.findById(anyLong()))
                .thenReturn(Optional.of(customer));

        when(customerRepository.findByEmail(newEmail))
                .thenReturn(Optional.of(customerByEmail));
        //

        assertThatThrownBy(() -> underTest.updateCustomer(1L, name, newEmail, address))
                .isInstanceOf(CustomerEmailUnavailableException.class)
                .hasMessageContaining("The email \"" + newEmail + "\" unavailable to update");

        verify(customerRepository, never()).save(any());

    }

    @Test
    void shouldUpdateOnlyCustomerEmail() {

        Long id = 1L;
        Customer customer = new Customer(
                id,
                "leon",
                "leon@gmail.com",
                "US");

        String newEmail = "leonaldo@gmail.com";

        when(customerRepository.findById(anyLong()))
                .thenReturn(Optional.of(customer));

        when(customerRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        underTest.updateCustomer(id, null, newEmail, null);

        verify(customerRepository).save(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getEmail()).isEqualTo(newEmail);
        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getAddress()).isEqualTo(customer.getAddress());

    }

    @Test
    void shouldUpdateOnlyCustomerAddress() {
        Long id = 1L;
        Customer customer = new Customer(
                id,
                "leon",
                "leon@gmail.com",
                "US");

        String newAddress = "RU";

        when(customerRepository.findById(anyLong()))
                .thenReturn(Optional.of(customer));


        underTest.updateCustomer(id, null, null, newAddress);

        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getAddress()).isEqualTo(newAddress);


    }


    @Test
    void shouldUpdateAllAttributeWhenUpdateCustomer() {
        Long id = 1L;
        Customer customer = new Customer(
                id,
                "leon",
                "leon@gmail.com",
                "US");

        String newName = "leonaldo";
        String newEmail = "leonaldo@gmail.com";
        String newAddress = "RU";

        when(customerRepository.findById(anyLong()))
                .thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail(newEmail))
                .thenReturn(Optional.empty());

        underTest.updateCustomer(id, newName, newEmail, newAddress);

        verify(customerRepository).save(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getEmail()).isEqualTo(newEmail);
        assertThat(capturedCustomer.getName()).isEqualTo(newName);
        assertThat(capturedCustomer.getAddress()).isEqualTo(newAddress);

    }

    @Test
    void shouldThrowNotFoundWhenGivenIdDoesNotExistWhileDeleteCustomer() {
        //
        long id = 1L;
        when(customerRepository.existsById(id)).thenReturn(false);
        //
        assertThatThrownBy(() -> underTest.deleteCustomer(id))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with id " + id + " doesn't exist.");

        //
        verify(customerRepository, never()).deleteById(anyLong());

    }

    @Test
    void shouldDeleteCustomer() {
        long id = 1L;
        when(customerRepository.existsById(id)).thenReturn(true);
        //
        underTest.deleteCustomer(id);
        //
        verify(customerRepository).deleteById(id);

    }

}