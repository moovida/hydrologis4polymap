/*
 * polymap.org Copyright (C) 2017, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package com.hydrologis.polymap.geopaparazzi.servlets;

import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hydrologis.polymap.geopaparazzi.GeopaparazziPlugin;

import org.polymap.core.CorePlugin;

public class GeopaparazziUploadServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( GeopaparazziUploadServlet.class );


    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        // String authHeader = request.getHeader("Authorization");
        //
        // String[] userPwd =
        // StageUtils.getUserPwdWithBasicAuthentication(authHeader);
        // if (userPwd == null || !LoginChecker.isLoginOk(userPwd[0], userPwd[1])) {
        // throw new ServletException("No permission!");
        // }

        // File geopaparazziFolder =
        // StageWorkspace.getInstance().getGeopaparazziFolder(userPwd[0]);

        // TODO change this to be more solid and separated for users
        File gpapProjectsFolder = CorePlugin.getDataLocation( GeopaparazziPlugin.instance() );

        String projectFileName = "";
        String msg = "";
        PrintWriter outWriter = response.getWriter();
        Map<String,String[]> parms = request.getParameterMap();
        String[] nameParams = parms.get( "name" );
        if (nameParams != null && nameParams.length == 1) {
            projectFileName = nameParams[0];
            File file = new File( gpapProjectsFolder, projectFileName );
            log.info( "Importing new gp database: " + file );

            if (file.exists()) {
                msg = "File already exists on the server: " + projectFileName;
                outWriter.write( msg );
                return;
            }
            else {
                ServletInputStream inputStream = request.getInputStream();
                Files.copy( inputStream, file.toPath() );
                msg = "Uploaded file: " + projectFileName;
            }
        }
        
        // TODO create a project for this file and import the geopap project into it
        

        outWriter.write( msg );

    }
}
