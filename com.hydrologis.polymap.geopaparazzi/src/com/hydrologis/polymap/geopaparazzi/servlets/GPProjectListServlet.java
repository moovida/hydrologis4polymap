/*
 * polymap.org 
 * Copyright (C) 2017-2018, the @authors. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

/**
 * 
 * @author Falko Bräutigam
 */
public class GPProjectListServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( GPProjectListServlet.class );

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
        File projectsFolder = GPUtilities.projectsFolder();
        response.setContentType( "text/json" );
        PrintWriter out = response.getWriter();
        String projectsList = GeopaparazziUtilities.loadProjectsList( projectsFolder );
        out.write( projectsList );
    }

}
