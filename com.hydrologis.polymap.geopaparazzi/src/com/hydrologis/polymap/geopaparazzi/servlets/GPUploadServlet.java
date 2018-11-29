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

import static com.hydrologis.polymap.geopaparazzi.servlets.GPDownloadServlet.nameParam;

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

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.hydrologis.polymap.geopaparazzi.catalog.GPServiceInfo;
import com.hydrologis.polymap.geopaparazzi.catalog.GPServiceResolver;
import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.catalog.IUpdateableMetadataCatalog.Updater;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.session.DefaultSessionContext;
import org.polymap.core.runtime.session.DefaultSessionContextProvider;
import org.polymap.core.runtime.session.SessionContext;
import org.polymap.core.style.model.FeatureStyle;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.project.ProjectRepository;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public class GPUploadServlet
        extends HttpServlet {

    private static final Log log = LogFactory.getLog( GPUploadServlet.class );

    private static final DefaultSessionContextProvider sessionProvider = new DefaultSessionContextProvider();
    
    private DefaultSessionContext           updateContext;

    private DefaultSessionContextProvider   contextProvider;

    
    public GPUploadServlet() {
        // sessionContext
        assert updateContext == null && contextProvider == null;
        updateContext = new DefaultSessionContext( getClass().getSimpleName() + hashCode() );
        contextProvider = new DefaultSessionContextProvider() {
            protected DefaultSessionContext newContext( String sessionKey ) {
                assert sessionKey.equals( updateContext.getSessionKey() );
                return updateContext;
            }
        };
        SessionContext.addProvider( contextProvider );
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
            PrintWriter outWriter = response.getWriter();
            File file = new File( projectsFolder, filename );
            log.info( "Importing new geopaparazzi database: " + file );

            if (file.exists()) {
                response.sendError( 409, "File already exists on the server: " + filename );
            }
            else {
                try (ServletInputStream in = request.getInputStream()) {
                    Files.copy( in, file.toPath() );
                }
                try {
                    sessionProvider.mapContext( updateContext.getSessionKey(), true );
                    addToCatalog( file );
                    outWriter.write( "Uploaded file: " + filename );
                }
                catch (Exception e) {
                    log.error( "Unable to add resource to the catalog.", e );
                    response.sendError( 500, "Error while importing: " + filename );
                    file.delete();
                }
                finally {
                    sessionProvider.unmapContext();
                }
            }
        });
    }


    protected void addToCatalog( File dbFile ) throws Exception {
        AtomicReference<GPServiceInfo> serviceInfo = new AtomicReference();
        NullProgressMonitor monitor = new NullProgressMonitor();

        // create catalog entry
        try (
            Updater update = P4Plugin.localCatalog().prepareUpdate();
            SqliteDb db = new SqliteDb(); 
            UnitOfWork uow = ProjectRepository.newUnitOfWork();
        ) {
            db.open( dbFile.getAbsolutePath() );
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
            });

            // create new layer(s) for resource(s)
            IMap map = uow.entity( IMap.class, ProjectRepository.ROOT_MAP_ID );
            for (IResourceInfo res : serviceInfo.get().getResources( monitor )) {
                String name = res.getName();

                FeatureStyle featureStyle4Layer = GPUtilities.getFeatureStyle4Layer( name, db.getConnection() );
                assert featureStyle4Layer != null : "Must never happen!?";

                // create ILayer entity
                uow.createEntity( ILayer.class, null, (ILayer proto) -> {
                    proto.label.set( name );
                    proto.description.set( "Geopaparazzi Project Layer" );
                    proto.resourceIdentifier.set( AllResolver.resourceIdentifier( res ) );
                    proto.styleIdentifier.set( featureStyle4Layer.id() );
                    proto.parentMap.set( map );
                    proto.orderKey.set( proto.maxOrderKey() + 1  );
                    return proto;
                });
                featureStyle4Layer.store();
            }
            
            // XXX adapt map extent
            //map.setMaxExtent( ...);
       
            update.commit();
            uow.commit();
        }
        catch (Exception e) {
            throw Throwables.propagate( e );
        }
    }
}
