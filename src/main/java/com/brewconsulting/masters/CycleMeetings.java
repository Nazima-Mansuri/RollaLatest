package com.brewconsulting.masters;

import com.brewconsulting.DB.masters.CycleMeeting;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.postgresql.util.PSQLException;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by lcom53 on 7/10/16.
 */

@Path("cyclemeeting")
@Secured
public class CycleMeetings {

    ObjectMapper mapper = new ObjectMapper();

    static final Logger logger = Logger.getLogger(CycleMeetings.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     * Produces a list of all Meetings.
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{groupid}")
    public Response meetings(@PathParam("groupid") int id , @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeeting
                            .getAllSubMeetings(id,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cyclemeetings\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        catch (Exception e) {
            logger.error("Exception ",e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     *  Produces specific Cycle meeting.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("meetingbyid/{id}")
    public Response meetingsById(@PathParam("id") int id , @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeeting
                            .getSubMeetingById(id,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get particular Cyclemeeting\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        catch (Exception e) {
            logger.error("Exception ",e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Produces a list of all Meetings of specific month.
     *
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("monthofmeetings/{month}")
    public Response monthmeetings(@PathParam("month") int month , @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeeting
                            .getAllSubMeetingsOfMonth(month,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cyclemeetings\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        catch (Exception e) {
            logger.error("Exception ",e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Add cycle Meeting
     *
     * @param input
     * @param crc
     * @return
     */
    @POST
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response createSubMeeting(InputStream input,
                                @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            int MeetingId = CycleMeeting.addSubCycleMeeting(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (MeetingId != 0)
                resp = Response.ok("{\"id\":" + MeetingId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Cycle Meeting")
                                .getJsonString()).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add Cyclemeeting \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }


    /**
     * Update cycle Meeting
     *
     * @param input
     * @param crc
     * @return
     */
    @PUT
    @Produces("application/json")
    @Secured
    @Consumes("application/json")
    public Response updateSubMeeting(InputStream input,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            JsonNode node = mapper.readTree(input);
            CycleMeeting.updateSubCycleMeeting(node,
                    (LoggedInUser) crc.getProperty("userObject"));
            resp = Response.ok().build();
        }catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update Cyclemeeting \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (IOException e) {
            logger.error("IOException ",e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Delete cycle Meeting
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deleteSubMeeting(@PathParam("id") Integer id,
                                  @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            // affectedRow given how many rows deleted from database.
            int affectedRow = CycleMeeting.deleteSubCycleMeeting(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        }catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to delete Cyclemeeting \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (PSQLException ex) {
            logger.error("PSQLException ",ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error("Exception ",e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();

        }
        return resp;
    }

    /***
     *  Produces Logged in users all meetings.
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    public Response usersallmeetings(@Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(CycleMeeting
                            .getAllSubMeetingsOfLogInUser((LoggedInUser) crc
                                    .getProperty("userObject")))).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ",na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get Cyclemeetings\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        catch (Exception e) {
            logger.error("Exception ",e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        }
        return resp;
    }
}
