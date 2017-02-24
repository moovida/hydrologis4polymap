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
import java.util.concurrent.atomic.AtomicReference;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;
import com.hydrologis.polymap.geopaparazzi.catalog.GPServiceInfo;
import com.hydrologis.polymap.geopaparazzi.catalog.GPServiceResolver;
import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.catalog.IUpdateableMetadataCatalog.Updater;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;

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
        File gpapProjectsFolder = GPUtilities.getGeopaparazziProjectsFolder();

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

            try {
                addToCatalog( file );
            }
            catch (Exception e) {
                log.error( "Unable to add resource to the catalog.", e );
            }
        }

        outWriter.write( msg );

    }


    private void addToCatalog( File dbFile ) throws Exception {
        AtomicReference<GPServiceInfo> serviceInfo = new AtomicReference();
        NullProgressMonitor monitor = new NullProgressMonitor();

        try (SqliteDb db = new SqliteDb()) {
            db.open( dbFile.getAbsolutePath() );

            // create catalog entry
            try (Updater update = P4Plugin.localCatalog().prepareUpdate()) {
                update.newEntry( metadata -> {
                    try {
                        String databasePath = db.getDatabasePath();
                        String title = FileUtilities.getNameWithoutExtention( dbFile );
                        String projectInfo = "Geopaparazzi Project";
                        projectInfo = GeopaparazziUtilities.getProjectInfo( db.getConnection(), false );

                        metadata.setTitle( title );
                        metadata.setDescription( projectInfo );
                        metadata.setType( "Geopaparazzi Sqlite Project Database" );
                        metadata.setFormats( Sets.newHashSet( "..." ) );

                        // actual connection to the data source; just an example
                        metadata.setConnectionParams( GPServiceResolver.createParams( databasePath ) );

                        // resolve the new data source, testing the connection params
                        // and choose resource to create a new layer for

                        serviceInfo.set( (GPServiceInfo)AllResolver.instance().resolve( metadata, monitor ) );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( "Unable to resolve imported data source.", e );
                    }
                } );
                update.commit();
            }
            catch (Exception e) {
                throw new RuntimeException( e );
            }

            // create new layer(s) for resource(s)
            // for (IResourceInfo res : serviceInfo.get().getResources( monitor )) {
            // String name = res.getName();
            //
            // FeatureStyle featureStyle4Layer = GPUtilities.getFeatureStyle4Layer(
            // name, db.getConnection() );
            // if (featureStyle4Layer == null) {
            // featureStyle4Layer = P4Plugin.styleRepo().newFeatureStyle();
            // DefaultStyle.createAllStyles( featureStyle4Layer );
            // }
            //
            // BatikApplication.instance().getContext().propagate( this );
            // NewLayerOperation op = new NewLayerOperation().label.put( name
            // ).res.put( res ).featureStyle.put( featureStyle4Layer ).uow.put(
            // ProjectRepository.unitOfWork() ).map.put( map.get() );
            //
            // OperationSupport.instance().execute( op, false, false );
            // }
        }
    }
}
