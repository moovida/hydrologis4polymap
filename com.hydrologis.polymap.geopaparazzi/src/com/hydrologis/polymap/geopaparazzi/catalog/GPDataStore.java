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
package com.hydrologis.polymap.geopaparazzi.catalog;

import java.util.List;

import java.io.IOException;

import org.geotools.data.CollectionFeatureReader;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoGpsLog.GpsLog;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hydrologis.polymap.geopaparazzi.importer.GPProgressMonitor;
import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class GPDataStore
        extends ContentDataStore
        implements DataStore {

    private static final Log log = LogFactory.getLog( GPDataStore.class );

    /** Pass {@link SqliteDb} in user data of the features. */
    public static final String              USER_DATA_KEY_DB = "db";

    private SqliteDb         db;

    private String           dbName4Extension;


    public GPDataStore( SqliteDb db ) {
        this.db = db;
        dbName4Extension = GPUtilities.dbNameForLayerExtension( db );
    }


    public SqliteDb db() {
        return db;
    }


    @Override
    protected List<Name> createTypeNames() throws IOException {
        try {
            List<String> layerNames = GeopaparazziUtilities.getLayerNamesList( db.getConnection() );
            return GPUtilities.toExtendedNamesList( layerNames, dbName4Extension );
        }
        catch (Exception e) {
            throw new IOException( e );
        }
    }


    @Override
    protected ContentFeatureSource createFeatureSource( ContentEntry entry ) throws IOException {
        IJGTConnection conn = db.getConnection();

        String typeName = GPUtilities.extendedName2layerName( entry.getTypeName() );
        GPProgressMonitor pm = new GPProgressMonitor( new NullProgressMonitor() );
        try {
            switch (typeName) {
                case GeopaparazziUtilities.SIMPLE_NOTES: {
                    SimpleFeatureCollection fc = OmsGeopaparazzi4Converter.simpleNotes2featurecollection( conn, pm );
                    return new GPFeatureSource( entry, fc, conn );
                }
                case GeopaparazziUtilities.GPS_LOGS: {
                    List<GpsLog> gpsLogsList = OmsGeopaparazzi4Converter.getGpsLogsList( conn );
                    DefaultFeatureCollection fc = OmsGeopaparazzi4Converter.getLogLinesFeatureCollection( pm, gpsLogsList );
                    return new GPFeatureSource( entry, fc, conn );
                }
                case GeopaparazziUtilities.MEDIA_NOTES: {
                    SimpleFeatureCollection fc = OmsGeopaparazzi4Converter.media2IdBasedFeatureCollection( conn, pm );
                    return new GPFeatureSource( entry, fc, conn );
                }
                default: {
                    SimpleFeatureCollection fc = OmsGeopaparazzi4Converter.complexNote2featurecollection( typeName, conn, pm );
                    return new GPFeatureSource( entry, fc, conn );
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    public static class GPFeatureSource
            extends ContentFeatureSource {

        private SimpleFeatureCollection     fc;
        
        private IJGTConnection              conn;

        public GPFeatureSource( ContentEntry entry, SimpleFeatureCollection fc, IJGTConnection conn ) {
            super( entry, null );
            this.fc = fc;
            this.conn = conn;
        }

        public IJGTConnection conn() {
            return conn;
        }

        @Override
        protected ReferencedEnvelope getBoundsInternal( Query query ) throws IOException {
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        protected int getCountInternal( Query query ) throws IOException {
            //assert query == null || query.equals( Query.ALL );
            return fc.size();
        }

        @Override
        protected FeatureReader<SimpleFeatureType,SimpleFeature> getReaderInternal( Query query ) throws IOException {
            return new CollectionFeatureReader( fc, fc.getSchema() );
        }

        @Override
        protected SimpleFeatureType buildFeatureType() throws IOException {
            return fc.getSchema();
        }
    }


}
