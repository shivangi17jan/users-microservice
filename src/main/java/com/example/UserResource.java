package com.example;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class UserResource {


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {

        int a =10000;
        int b = 200000;
        String hello = "hello";

        return "Hello from RESTEasy Reactive";
    }
}
