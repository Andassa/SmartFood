package com.mycompany.smartfood.controlleur;

import java.util.List;

import com.mycompany.smartfood.dto.ProductLookup;
import com.mycompany.smartfood.dto.ShopRoute;
import com.mycompany.smartfood.dto.RecipeInsight;
import com.mycompany.smartfood.dto.WeekProposal;
import com.mycompany.smartfood.dto.WeekReport;
import com.mycompany.smartfood.entity.ShoppingItemEntity;
import com.mycompany.smartfood.entity.WasteEntry;
import com.mycompany.smartfood.service.KitchenService;

import jakarta.ejb.EJB;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/kitchen")
public class KitchenResource {

    @EJB
    private KitchenService kitchenService;

    @GET
    @Path("/coverage")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeInsight> coverage() {
        return kitchenService.coverage();
    }

    @GET
    @Path("/suggestions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RecipeInsight> suggestions(@QueryParam("days") @DefaultValue("7") int days) {
        return kitchenService.suggestions(days);
    }

    @POST
    @Path("/shopping")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ShoppingItemEntity> planShopping() {
        return kitchenService.planShopping();
    }

    @GET
    @Path("/waste")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WasteEntry> waste() {
        return kitchenService.waste();
    }

    @POST
    @Path("/waste/sweep")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sweep() {
        int removed = kitchenService.sweepExpired();
        return Response.ok("{\"removed\":" + removed + "}").build();
    }

    @POST
    @Path("/week")
    @Produces(MediaType.APPLICATION_JSON)
    public WeekProposal proposeWeek() {
        return kitchenService.proposeWeek();
    }

    @GET
    @Path("/report")
    @Produces(MediaType.APPLICATION_JSON)
    public WeekReport report() {
        return kitchenService.report();
    }

    @GET
    @Path("/guide")
    @Produces(MediaType.APPLICATION_JSON)
    public ShopRoute guide() {
        return kitchenService.guide();
    }

    @GET
    @Path("/markets")
    @Produces(MediaType.APPLICATION_JSON)
    public ShopRoute markets(@QueryParam("lat") double latitude, @QueryParam("lon") double longitude) {
        return kitchenService.markets(latitude, longitude);
    }

    @GET
    @Path("/barcode/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductLookup barcode(@PathParam("code") String code) {
        return kitchenService.lookupBarcode(code);
    }
}
