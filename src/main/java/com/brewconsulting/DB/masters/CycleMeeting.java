package com.brewconsulting.DB.masters;

import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.NotAuthorizedException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.brewconsulting.DB.utils.stringToDate;

/**
 * Created by lcom53 on 7/10/16.
 */
public class CycleMeeting {

    @JsonProperty("id")
    public int id;

    @JsonProperty("title")
    public String title;

    @JsonView(UserViews.authView.class)
    @JsonProperty("groupId")
    public int groupId;

    @JsonView(UserViews.authView.class)
    @JsonProperty("venue")
    public String venue;

    @JsonView(UserViews.authView.class)
    @JsonProperty("startDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date startDate;

    @JsonView(UserViews.authView.class)
    @JsonProperty("endDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date endDate;

    @JsonView(UserViews.authView.class)
    @JsonProperty("organiser")
    public int organiser;

    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date createDate;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updateDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date updateDate;

    @JsonProperty("updateBy")
    public int updateBy;

    @JsonProperty("username")
    public String username;

    // make the default constructor visible to package only.
    public CycleMeeting() {
    }


    /***
     * Method is used to get all meetings.
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<CycleMeeting> getAllSubMeetings(int id,LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;

            Connection con = DBConnectionProvider.getConn();
            ArrayList<CycleMeeting> cycleMeetings = new ArrayList<CycleMeeting>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT c.id, title, groupid, venue, startdate, enddate, "
                                    +" organiser, createdon, createdby, updatedon, updatedby, u.username"
                                    +"  FROM "+ schemaName+".cyclemeeting c left join master.users u ON " +
                                    " u.id = organiser where groupId = ?");
                    stmt.setInt(1,id);
                    result = stmt.executeQuery();
                    System.out.print(result);
                    while (result.next()) {
                        CycleMeeting subMeeting = new CycleMeeting();
                        subMeeting.id = result.getInt(1);
                        subMeeting.title = result.getString(2);
                        subMeeting.groupId = result.getInt(3);
                        subMeeting.venue = result.getString(4);
                        subMeeting.startDate = result.getTimestamp(5);
                        subMeeting.endDate = result.getTimestamp(6);
                        subMeeting.organiser = result.getInt(7);
                        subMeeting.createDate = result.getTimestamp(8);
                        subMeeting.createBy = result.getInt(9);
                        subMeeting.updateDate = result.getTimestamp(10);
                        subMeeting.updateBy = result.getInt(11);
                        subMeeting.username = result.getString(12);
                        cycleMeetings.add(subMeeting);
                    }
                } else
                    throw new Exception("DB connection is null");

            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return cycleMeetings;
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /***
     * Method allows user to get meetings by id.
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static Division getSubMeetingById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

            Division division = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT id, title, groupid, venue, startdate, enddate, " +
                                    " organiser, createdon, createdby, updateon, updatedate " +
                                    "  FROM "+ schemaName+".cyclemeeting where id = ?");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        division = new Division();
                        division.id = result.getInt(1);
                        division.name = result.getString(2);
                        division.description = result.getString(3);
                        division.createDate = result.getTimestamp(4);
                        division.createBy = result.getInt(5);
                        division.updateDate = result.getTimestamp(6);
                        division.updateBy = result.getInt(7);
                        division.addLine1 = result.getString(8);
                        division.addLine2 = result.getString(9);
                        division.addLine3 = result.getString(10);
                        division.city = result.getString(11);
                        division.state = result.getString(12);
                        if (result.getArray(13) != null)
                            division.phones = (String[]) result.getArray(13)
                                    .getArray();
                        division.firstname = loggedInUser.firstName;
                        division.lastname = loggedInUser.lastName;
                    }
                } else
                    throw new Exception("DB connection is null");

            } finally {
                if (result != null)
                    if (!result.isClosed())
                        result.close();
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return division;
        } else {
            throw new NotAuthorizedException("");
        }
    }
    /**
     * Method Used to insert Meeting
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addSubCycleMeeting(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result;


            try {
                con.setAutoCommit(false);

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".cycleMeeting(title,groupId,venue,startDate,endDate,organiser,createdon,"
                                        + "createdby,updatedon,updatedby) values (?,?,?,?,?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, node.get("title").asText());

                stmt.setInt(2, node.get("groupId").asInt());

                stmt.setString(3, node.get("venue").asText());

                stmt.setTimestamp(4, new Timestamp((stringToDate(node.get("startDate").asText())).getTime()));

                stmt.setTimestamp(5, new Timestamp((stringToDate(node.get("endDate").asText())).getTime()));

                stmt.setInt(6, node.get("organiser").asInt());

                stmt.setTimestamp(7, new Timestamp((new Date()).getTime()));

                stmt.setInt(8, loggedInUser.id);

                stmt.setTimestamp(9, new Timestamp((new Date()).getTime()));

                stmt.setInt(10, loggedInUser.id);

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Meeting Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int meetingId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    meetingId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return meetingId;

            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /**
     * Method used to update Meeting
     *
     * @param node
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateSubCycleMeeting(JsonNode node, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt;
            int result;

            try {
                con.setAutoCommit(false);

                stmt = con.prepareStatement("UPDATE " + schemaName + ".cycleMeeting SET title = ? , venue = ?, " +
                        "startDate = ? , endDate = ?, organiser = ?, updatedOn = ?, updatedBy = ? " +
                        "WHERE id = ?");

                stmt.setString(1, node.get("title").asText());

                stmt.setString(2, node.get("venue").asText());

                stmt.setTimestamp(3, new Timestamp((stringToDate(node.get("startDate").asText())).getTime()));

                stmt.setTimestamp(4, new Timestamp((stringToDate(node.get("endDate").asText())).getTime()));

                stmt.setInt(5, node.get("organiser").asInt());

                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));

                stmt.setInt(7, loggedInUser.id);

                stmt.setInt(8, node.get("id").asInt());

                result = stmt.executeUpdate();

                con.commit();
                return result;

            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /**
     * Method is used to delete Meeting
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteSubCycleMeeting(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.DIVISION,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                // If connection is not null then perform delete operation.
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".cycleMeeting WHERE id = ?");

                    stmt.setInt(1, id);
                    result = stmt.executeUpdate();
                } else
                    throw new Exception("DB connection is null");
            } finally {

                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return result;
        } else {
            throw new NotAuthorizedException("");
        }
    }
}