/*
 * Copyright (C) 2015, the @authors. All rights reserved.
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
package com.hydrologis.polymap.geopaparazzi;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IUpdateableMetadataCatalog.Updater;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.data.wms.catalog.WmsServiceResolver;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.session.DefaultSessionContext;
import org.polymap.core.runtime.session.DefaultSessionContextProvider;
import org.polymap.core.runtime.session.SessionContext;

import org.polymap.rhei.batik.app.SvgImageRegistryHelper;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.catalog.LocalCatalog;
import org.polymap.p4.project.ProjectRepository;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GeopaparazziPlugin
        extends AbstractUIPlugin {

    private static final Log log = LogFactory.getLog( GeopaparazziPlugin.class );
    
    public static final String      ID = "com.hydrologis.polymap.geopaparazzi";

    private static final String     BACKGROUND_SERVICE_ID = "_geopaparazzi_background_";

    private static final DefaultSessionContextProvider sessionProvider = new DefaultSessionContextProvider();
    
    private static GeopaparazziPlugin instance;


    public static GeopaparazziPlugin instance() {
        return instance;
    }


    /**
     * Shortcut for <code>instance().images</code>.
     */
    public static SvgImageRegistryHelper images() {
        return instance().images;
    }

    
    // instance *******************************************

    public SvgImageRegistryHelper images = new SvgImageRegistryHelper( this );


    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
     
        new Job( "Default layers" ) {
            private DefaultSessionContext           updateContext;
            private DefaultSessionContextProvider   contextProvider;
            
            @Override
            protected IStatus run( IProgressMonitor monitor ) {
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
                
                try {
                    sessionProvider.mapContext( updateContext.getSessionKey(), true );
                    initBackgroundLayer();
                    return Status.OK_STATUS;
                }
                finally {
                    sessionProvider.unmapContext();
                }
            }
        }.schedule( 1000 ); // give plugins time to settle
    }


    public void stop( BundleContext context ) throws Exception {
        super.stop( context );
        instance = null;
    }

    
    protected void initBackgroundLayer() {
        // check existing entry
        log.info( "Checking default background layers..." );
        LocalCatalog catalog = P4Plugin.localCatalog();
        NullProgressMonitor monitor = new NullProgressMonitor();
        if (catalog.entry( BACKGROUND_SERVICE_ID, monitor ).isPresent()) {
            return;
        }
        
        log.info( "Creating default background layers..." );
        try {
            try (Updater update = catalog.prepareUpdate()) {
                // create WMS service
                update.newEntry( metadata -> {
                    metadata.setIdentifier( BACKGROUND_SERVICE_ID );
                    metadata.setTitle( "Background" );
                    metadata.setDescription( "Default background for Geopaparazzis" );
                    metadata.setType( "Service" );
                    metadata.setFormats( Sets.newHashSet( "WMS", "WFS" ) );
                    metadata.setConnectionParams( WmsServiceResolver.createParams( "http://ows.terrestris.de/osm/service/" ) );
                });
                // remove default background service
                //update.removeEntry( LocalCatalog.WORLD_BACKGROUND_ID );
                update.commit();
            }
            
            try (UnitOfWork uow = ProjectRepository.newUnitOfWork()) {
                IMap rootMap = uow.entity( IMap.class, ProjectRepository.ROOT_MAP_ID );
                
                // remove default layers
                rootMap.layers.stream().forEach( layer -> uow.removeEntity( layer ) );
                
                IMetadata md = catalog.entry( BACKGROUND_SERVICE_ID, monitor ).get();
                IServiceInfo service = (IServiceInfo)AllResolver.instance().resolve( md ).get();
                for (IResourceInfo res : service.getResources( monitor )) {
                    if ("OSM-WMS".equalsIgnoreCase( res.getName() ) ) {
                        uow.createEntity( ILayer.class, null, (ILayer proto) -> {
                            proto.parentMap.set( rootMap );
                            proto.label.set( "Background" );
                            proto.description.set( res.getDescription().orElse( null ) );
                            proto.resourceIdentifier.set( AllResolver.resourceIdentifier( res ) );
                            proto.orderKey.set( 1 );
                            return proto;
                        });
                    }
                }
                uow.commit();
            }
        }
        catch (Exception e) {
            log.warn( "Error while creating default background layer.", e );
        }

    }
}
