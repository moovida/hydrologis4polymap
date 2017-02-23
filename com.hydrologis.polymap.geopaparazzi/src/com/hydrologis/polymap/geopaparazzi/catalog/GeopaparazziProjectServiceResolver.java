/*
 * polymap.org Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package com.hydrologis.polymap.geopaparazzi.catalog;

import java.util.HashMap;
import java.util.Map;

import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResolvableInfo;

/**
 * 
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GeopaparazziProjectServiceResolver
        implements IMetadataResourceResolver {

    public static final String CONNECTION_TYPE = "Geopaparazzi Project";

    public static final String LAYER_NAME      = "_layer_name_";


    @Override
    public boolean canResolve( IMetadata metadata ) {
        Map<String,String> params = metadata.getConnectionParams();
        return CONNECTION_TYPE.equals( params.get( CONNECTION_PARAM_TYPE ) )
                && params.containsKey( CONNECTION_PARAM_URL ) && params.containsKey( LAYER_NAME );
    }


    @Override
    public IResolvableInfo resolve( IMetadata metadata, IProgressMonitor monitor ) throws Exception {
        return GeopaparazziServiceInfo.of( metadata, metadata.getConnectionParams() );
    }


    @Override
    public Map<String,String> createParams( Object service ) {
        assert service instanceof SqliteDb
                && ((SqliteDb)service).getDatabasePath().endsWith( ".gpap" ) : "Service has to be a geopaparazzi project.";

        Map<String,String> result = new HashMap();
        result.put( CONNECTION_PARAM_TYPE, CONNECTION_TYPE );
        // FIXME get URL from ShapefileDateStore?
        throw new RuntimeException( "FIXME: get URL from SqliteDb?" );
        // ResourceInfo info = ds.getFeatureSource().getInfo();
        // result.put( CONNECTION_PARAM_URL, ... );
        // return result;
    }


    public static Map<String,String> createParams( String serviceUrl, String tableName ) {
        Map<String,String> result = new HashMap();
        result.put( CONNECTION_PARAM_TYPE, CONNECTION_TYPE );
        result.put( CONNECTION_PARAM_URL, serviceUrl );
        result.put( LAYER_NAME, tableName );
        return result;
    }

}
