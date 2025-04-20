/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartfood.controlleur;

import java.util.List;

import com.mycompany.smartfood.entity.ShoppingItemEntity;
import com.mycompany.smartfood.service.ShoppingListService;

import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/shoppinglist")
public class ShoppingListResource {

    @EJB
    private ShoppingListService shoppingListService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ShoppingItemEntity> getShoppingList() {
        return shoppingListService.findAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createShoppingItem(ShoppingItemEntity item) {
        ShoppingItemEntity created = shoppingListService.create(item);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateShoppingItem(@PathParam("id") Long id, ShoppingItemEntity item) {
        item.setId(id);
        ShoppingItemEntity updated = shoppingListService.update(item);
        return Response.ok(updated).build();
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteShoppingItem(@PathParam("id") Long id) {
        shoppingListService.remove(id);
        return Response.ok().entity("{\"message\":\"Item removed\"}").build();
    }
}
