package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Task;
import com.brewconsulting.DB.masters.UserViews;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PSQLException;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lcom53 on 15/10/16.
 */

@Path("cyclemeetingtasks")
@Secured
public class ChildTasks {

    ObjectMapper mapper = new ObjectMapper();

    /***
     * Produces list of group tasks.
     *
     * @param crc
     * @return /*
     */
    @GET
    @Secured
    @Produces("application/json")
    @Path("{id}")
    public Response getTasks(@PathParam("id") int id ,@Context ContainerRequestContext crc) {
        Response resp = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            resp = Response.ok(
                    mapper.writerWithView(UserViews.childTaskView.class).writeValueAsString(Task
                            .getCycleMeetingTasks(id,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get Sub Tasks").build();
        } catch (Exception e) {
            resp = Response.serverError().entity(e.getMessage()).build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * get a particular Group Task.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("subtaskbyid/{id}")
    public Response getTasks(@PathParam("id") Integer id,
                                @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            Task task = Task.getCycleMeetingTaskById(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (task == null) {
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("This Sub Task does not exist")
                                .getJsonString()).build();
            } else {
                resp = Response.ok(mapper.writerWithView(UserViews.childTaskView.class).
                        writeValueAsString(task)).build();
            }

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get Group Task").build();
        } catch (Exception e) {

            resp = Response.serverError().entity(e.getMessage()).build();
            e.printStackTrace();

        }
        return resp;
    }

    /***
     * Add Sub Tasks.
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Produces("application/json")

    public Response createsubTask(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            int subTaskId = Task.addCycleMeetingTask(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (subTaskId != 0)
                resp = Response.ok("{\"id\":" + subTaskId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Sub Task")
                                .getJsonString()).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to Insert Sub Task").build();
        } catch (IOException e) {
            if (resp == null) {
                resp = Response.serverError().entity(e.getMessage()).build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Update Sub Task
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Secured
    @Produces("application/json")

    public Response updateTask(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            JsonNode node = mapper.readTree(input);
            Task.updateCycleMeetingTask(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to update Sub Task")
                    .build();
        } catch (IOException e) {
            if (resp == null)
                resp = Response.serverError().entity(e.getMessage()).build();
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Delete Group Task
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Secured
    @Produces("application/json")
    @Path("{id}")
    public Response deleteTask(@PathParam("id") Integer id,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            // affectedRow given how many rows deleted from database.
            int affectedRow = Task.deleteCycleMeetingTask(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to delete Sub Task")
                    .build();
        } catch (PSQLException ex) {
            resp = Response
                    .status(409)
                    .entity("This id is already Use in another table as foreign key")
                    .type(MediaType.TEXT_PLAIN).build();
            ex.printStackTrace();
        } catch (Exception e) {
            if (resp == null)
                resp = Response.serverError().entity(e.getMessage()).build();
            e.printStackTrace();

        }
        return resp;
    }
}