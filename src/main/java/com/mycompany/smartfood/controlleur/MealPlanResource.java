/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartfood.controlleur;

/**
 *
 * @author Andassa
 */
import com.mycompany.smartfood.service.MealPlanService;
import com.mycompany.smartfood.entity.MealPlan;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/mealplans")
public class MealPlanResource {

    @EJB
    private MealPlanService mealPlanService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<MealPlan> getMealPlans() {
        return mealPlanService.findAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMealPlan(MealPlan mealPlan) {
        MealPlan created = mealPlanService.create(mealPlan);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMealPlan(@PathParam("id") Long id, MealPlan mealPlan) {
        mealPlan.setId(id);
        MealPlan updated = mealPlanService.update(mealPlan);
        return Response.ok(updated).build();
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMealPlan(@PathParam("id") Long id) {
        mealPlanService.remove(id);
        return Response.ok().entity("{\"message\":\"Meal plan removed\"}").build();
    }
}