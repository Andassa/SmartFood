/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartfood.controlleur;

/**
 *
 * @author Andassa
 */
import com.mycompany.smartfood.service.IngredientService;
import com.mycompany.smartfood.entity.Ingredient;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;


@Path("/ingredients")
public class IngredientResource {

    @EJB
    private IngredientService ingredientService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ingredient> getAllIngredients() {
        return ingredientService.findAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createIngredient(Ingredient ingredient) {
        Ingredient created = ingredientService.create(ingredient);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateIngredient(@PathParam("id") Long id, Ingredient ingredient) {
        ingredient.setId(id);
        Ingredient updated = ingredientService.update(ingredient);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteIngredient(@PathParam("id") Long id) {
        ingredientService.remove(id);
        return Response.ok().entity("{\"message\":\"Ingredient removed\"}").build();
    }
}