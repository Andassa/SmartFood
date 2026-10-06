/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author Andassa
 */
package com.mycompany.smartfood.controlleur;

import com.mycompany.smartfood.service.RecipeService;
import com.mycompany.smartfood.entity.Recipe;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/recipes")
public class RecipeResource {

    @EJB
    private RecipeService recipeService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Recipe> getAllRecipes() {
        return recipeService.findAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRecipe(Recipe recipe) {
        Recipe created = recipeService.create(recipe);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRecipe(@PathParam("id") Long id, Recipe recipe) {
        recipe.setId(id);
        Recipe updated = recipeService.update(recipe);
        return Response.ok(updated).build();
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRecipe(@PathParam("id") Long id) {
        recipeService.remove(id);
        return Response.ok().entity("{\"message\":\"Recipe removed\"}").build();
    }

    @POST
    @Path("/recommend")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Recipe> recommendRecipes(List<String> availableIngredients) {
        return recipeService.recommendRecipes(availableIngredients);
    }
}