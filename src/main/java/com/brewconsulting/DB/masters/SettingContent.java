package com.brewconsulting.DB.masters;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lcom16 on 18/10/16.
 */
public class SettingContent {

    @JsonProperty("id")
    public int id;

    @JsonProperty("contentName")
    public String contentName;

    @JsonProperty("contentDesc")
    public String contentDesc;

    @JsonProperty("divId")
    public int divId;

    @JsonProperty("url")
    public String url;

    @JsonProperty("createBy")
    public int createBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    @JsonProperty("createdOn")
    public Date createdOn;

    @JsonProperty("username")
    public String username;


    /**
     * method to get all setting content
     *
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<SettingContent> getAllContent(LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<SettingContent> contents = new ArrayList<SettingContent>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("SELECT c.id, contentname, contentdesc, divid, url, createby, createdon ," +
                                    "  username FROM " + schemaName + ".content c left join master.users u on u.id = createby");
                    result = stmt.executeQuery();
                    while (result.next()) {
                        SettingContent content = new SettingContent();
                        content.id = result.getInt(1);
                        content.contentName = result.getString(2);

                        content.contentDesc = result.getString(3);
                        content.divId = result.getInt(4);
                        if (!result.getString(5).contains("null")) {
                            content.url = result.getString(5);
                        } else {
                            content.url = "https://s3.amazonaws.com/com.brewconsulting.client1/Product/1475134095978_no_image.png";
                        }
                        content.createBy = result.getInt(6);
                        content.createdOn = new java.sql.Date(result.getTimestamp(7).getTime());
                        content.username = result.getString(8);

                        contents.add(content);
                    }
                }
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

            return contents;
        } else {

            throw new NotAuthorizedException("");

        }
    }


    /**
     * Method allow user to add content from setting.
     *
     * @param contentName
     * @param contentDesc
     * @param divId
     * @param url
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addSettingContent(String contentName, String contentDesc, int divId, String url,
                                        LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".content(contentName,contentDesc,divId,url,createBy,createdOn) values (?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, contentName);

//                 It checks that description is empty or not
                if (contentDesc != null)
                    stmt.setString(2, contentDesc);
                else
                    stmt.setString(2, null);

                stmt.setInt(3, divId);
                stmt.setString(4, url);
                stmt.setInt(5, loggedInUser.id);
                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));

                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add content Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int contentId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    contentId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return contentId;
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
     *  Method allow user to update content from setting.
     *
     * @param contentName
     * @param contentDesc
     * @param divId
     * @param url
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateSettingContent(String contentName, String contentDesc, int divId, String url, int id,
                                           LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Update data
// It checks that description is empty or not
        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("UPDATE "
                                    + schemaName
                                    + ".content SET contentName = ?,contentDesc = ?,divId = ?,url=?"
                                    + " WHERE id = ?");

                    stmt.setString(1, contentName);
                    if (contentDesc != null)
                        stmt.setString(2, contentDesc);
                    else
                        stmt.setString(2, null);

                    stmt.setInt(3, divId);
                    stmt.setString(4, url);
                    stmt.setInt(5, id);

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

    /***
     * Method allows to delete content
     *
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int deleteSettingContent(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".content WHERE id = ?");

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

    /**
     * Method allows to store Files in AWS bucket.
     *
     * @param inputStream
     * @param fileName
     * @return
     * @throws IOException
     */

    // save uploaded file to new location
    public static String writeToFile(InputStream inputStream, String fileName)
            throws IOException {

        String existingBucketName = "com.brewconsulting.client1";
        String finalUrl = null;
        String amazonFileUploadLocationOriginal = existingBucketName
                + "/Product";

        try {
            AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAJZZRFGQGNZIDUFTQ", "12uUP7pQrvR3Kf0GpyeJr328RQ/a1m8TI+/8w2X8");
            AmazonS3 s3Client = new AmazonS3Client(awsCredentials);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    amazonFileUploadLocationOriginal, fileName, inputStream,
                    objectMetadata);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            System.out.println("Etag:" + result.getETag() + "-->" + result);

            finalUrl = "https://s3.amazonaws.com/"
                    + amazonFileUploadLocationOriginal + "/" + fileName;
            System.out.println(finalUrl);

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        return finalUrl;
    }
}