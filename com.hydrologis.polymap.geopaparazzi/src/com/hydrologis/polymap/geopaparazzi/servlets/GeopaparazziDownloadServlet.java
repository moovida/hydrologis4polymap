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

import com.hydrologis.polymap.geopaparazzi.GeopaparazziPlugin;

import org.polymap.core.CorePlugin;

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
        FileInputStream inputStream = null;
        ServletOutputStream outputStream = null;
        try {
            Map<String, String[]> parms = request.getParameterMap();
            String[] idParams = parms.get("id");
            if (idParams != null && idParams.length == 1) {
                projectFileName = idParams[0];
                inputStream = new FileInputStream(new File(gpapProjectsFolder, projectFileName));
                String mimeType = "application/octet-stream";
                response.setContentType(mimeType);
                response.setHeader("Content-disposition", "attachment; filename=" + projectFileName);
                outputStream = response.getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }
}
