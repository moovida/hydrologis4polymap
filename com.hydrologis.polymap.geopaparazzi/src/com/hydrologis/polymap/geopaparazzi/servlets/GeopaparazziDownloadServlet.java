/*
 * polymap.org 
 * Copyright (C) 2017, the @authors. All rights reserved.
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
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

public class GeopaparazziDownloadServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( GeopaparazziDownloadServlet.class );

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        doPost( req, resp );
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        // TODO change this to be more solid and separated for users
        File gpapProjectsFolder = GPUtilities.getGeopaparazziProjectsFolder();

        // check 'id' param
        Map<String,String[]> parms = request.getParameterMap();
        String[] idParams = parms.get( "name" );
        if (idParams == null || idParams.length == 0) {
            response.sendError( 401, "No 'name' param given. This param specifies the name of the database." );
        }
        else if (idParams.length > 1) {
            response.sendError( 401, "Too many values for params 'name'. This param specifies the name of the database." );
        }
        // copy contents
        else {
            String projectFileName = idParams[0];
            try (
                FileInputStream in = new FileInputStream( new File( gpapProjectsFolder, projectFileName ) );
                ServletOutputStream out = response.getOutputStream();
            ){
                String mimeType = "application/octet-stream";
                response.setContentType( mimeType );
                response.setHeader( "Content-disposition", "attachment; filename=" + projectFileName );
                IOUtils.copy( in, out );
            }
        }
    }
}
