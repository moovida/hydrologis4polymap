/* 
 * polymap.org
 * Copyright (C) 2015, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package com.hydrologis.polymap.geopaparazzi.catalog;

import java.util.Collections;
import java.util.Map;

import org.geotools.data.ResourceInfo;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.DefaultResourceInfo;
import org.polymap.core.catalog.resolve.DefaultServiceInfo;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;

/**
 * 
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeopaparazziServiceInfo
        extends DefaultServiceInfo {

    private static Log log = LogFactory.getLog( GeopaparazziServiceInfo.class );
    
    public static GeopaparazziServiceInfo of( IMetadata metadata, Map<String,String> params ) 
            throws Exception {
        
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );
        SqliteDb ds = new SqliteDb();
        ds.open( url );
        return new GeopaparazziServiceInfo( metadata, ds );
    }



    // instance *******************************************
    
    private SqliteDb ds;


    protected GeopaparazziServiceInfo( IMetadata metadata, SqliteDb ds ) {
        super( metadata, new GeopaparazziSI( ds ));
        this.ds = ds;
    }

    
    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)ds;
    }


    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception {
        
        
        
        ResourceInfo info = new GeopaparazziResourceInfo( ds );
        return Collections.singletonList( new GeopaparazziProjectResourceInfo( this, info ) );
    }

    
    /**
     * 
     */
    class GeopaparazziProjectResourceInfo
            extends DefaultResourceInfo {

        public GeopaparazziProjectResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate ) {
            super( serviceInfo, delegate );
        }
        
    }
    
}
