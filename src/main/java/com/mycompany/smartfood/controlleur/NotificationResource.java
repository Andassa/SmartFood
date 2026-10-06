/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Andassa
 */
package com.mycompany.smartfood.controlleur;

import com.mycompany.smartfood.entity.Ingredient;
import com.mycompany.smartfood.service.NotificationService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/notifications")
public class NotificationResource {

    @EJB
    private NotificationService notificationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Ingredient> getExpiringIngredients(@QueryParam("days") @DefaultValue("2") int days) {
        return notificationService.getExpiringIngredients(days);
    }
}
