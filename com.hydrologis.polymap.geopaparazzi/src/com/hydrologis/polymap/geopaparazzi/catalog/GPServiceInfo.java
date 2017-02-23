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
import java.util.HashMap;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.geotools.data.ResourceInfo;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.ows.ServiceException;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter;

import com.hydrologis.polymap.geopaparazzi.importer.GPProgressMonitor;

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
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GPServiceInfo
        extends DefaultServiceInfo {

    public static GPServiceInfo of( IMetadata metadata, Map<String,String> params ) 
            throws ServiceException, MalformedURLException, IOException {
        
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );
        File databaseFile = new File( new URI( url ) );
        try (SqliteDb db = new SqliteDb()) {
            db.open( databaseFile.getAbsolutePath() );
            IJGTConnection connection = db.getConnection();

            GPProgressMonitor pm = new GPProgressMonitor( monitor );
            switch (layerName) {
                case OmsGeopaparazzi4Converter.SIMPLE_NOTES:
                    features = OmsGeopaparazzi4Converter.simpleNotes2featurecollection( connection, pm );
                    break;
                case OmsGeopaparazzi4Converter.GPS_LOGS:
                    List<GpsLog> gpsLogsList = OmsGeopaparazzi4Converter.getGpsLogsList( connection );
                    features = OmsGeopaparazzi4Converter.getLogLinesFeatureCollection( pm, gpsLogsList );
                    break;
                case OmsGeopaparazzi4Converter.MEDIA_NOTES:
                    features = OmsGeopaparazzi4Converter.media2IdBasedFeatureCollection( connection, pm );
                    break;
                default:
                    HashMap<String,SimpleFeatureCollection> complexNotesMap = OmsGeopaparazzi4Converter.complexNotes2featurecollections( connection, pm );
                    features = complexNotesMap.get( layerName );
                    break;
            }

        }

        return new GPServiceInfo( metadata, ds );
    }


    // instance *******************************************
    
    protected GPServiceInfo( IMetadata metadata, ShapefileDataStore ds ) {
        super( metadata, ds.getInfo() );
        this.ds = ds;
    }

    
    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)ds;
    }


    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception {
        ResourceInfo info = ds.getFeatureSource().getInfo();
        return Collections.singletonList( new ShapefileResourceInfo( this, info ) );
    }

    
    /**
     * 
     */
    class ShapefileResourceInfo
            extends DefaultResourceInfo {

        public ShapefileResourceInfo( IServiceInfo serviceInfo, ResourceInfo delegate ) {
            super( serviceInfo, delegate );
        }
        
    }
    
}
