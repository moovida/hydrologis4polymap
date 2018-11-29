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

import org.polymap.core.runtime.collect.Opt;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public class GPDownloadServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( GPDownloadServlet.class );

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        doPost( req, resp );
    }

    @Override
    protected void doPut( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        doPost( req, resp );
    }


    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        // TODO change this to be more solid and separated for users
        File projectsFolder = GPUtilities.projectsFolder();

        nameParam( request, response ).ifPresent( filename -> {
            try (
                FileInputStream in = new FileInputStream( new File( projectsFolder, filename ) );
                ServletOutputStream out = response.getOutputStream();
            ){
                String mimeType = "application/octet-stream";
                response.setContentType( mimeType );
                response.setHeader( "Content-disposition", "attachment; filename=" + filename );
                IOUtils.copy( in, out );
            }
        });
    }
    
    
    protected static Opt<String> nameParam( HttpServletRequest request, HttpServletResponse response ) throws IOException {
        String[] idParams = request.getParameterMap().get( "name" );
        if (idParams == null || idParams.length == 0) {
            response.sendError( 400, "No 'name' param given. This param specifies the name of the database." );
            return Opt.missing();
        }
        else if (idParams.length > 1) {
            response.sendError( 400, "Too many values for params 'name'. This param specifies the name of the database." );
            return Opt.missing();
        }
        else {
            return Opt.of( idParams[0] );
        }
    }
    
}
