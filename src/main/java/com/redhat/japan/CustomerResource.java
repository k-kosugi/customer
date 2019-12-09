package com.redhat.japan;

import com.redhat.japan.model.Customer;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    private EntityManager entityManager;

    @Path("/customer")
    @GET
    public List<Customer> customers() {
        return this.entityManager
                .createNamedQuery("Customer.findAll", Customer.class)
                .getResultList();
    }
}