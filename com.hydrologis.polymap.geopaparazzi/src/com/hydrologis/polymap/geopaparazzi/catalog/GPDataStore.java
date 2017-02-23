/* 
 * polymap.org
 * Copyright (C) 2017, the @authors. All rights reserved.
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

import java.util.List;

import java.io.IOException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoGpsLog.GpsLog;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.google.common.base.Throwables;
import com.hydrologis.polymap.geopaparazzi.importer.GPProgressMonitor;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 *
 * @author Falko Bräutigam
 */
@SuppressWarnings( "deprecation" )
public class GPDataStore
        extends AbstractDataStore
        implements DataStore {

    private SqliteDb            db;

    public GPDataStore( SqliteDb db ) {
        this.db = db;
    }

    @Override
    public String[] getTypeNames() throws IOException {
        return new String[] { 
                OmsGeopaparazzi4Converter.SIMPLE_NOTES,
                OmsGeopaparazzi4Converter.MEDIA_NOTES,
                OmsGeopaparazzi4Converter.GPS_LOGS };
    
    }

    @Override
    public SimpleFeatureSource getFeatureSource( String typeName ) throws IOException {
        IJGTConnection conn = db.getConnection();

        GPProgressMonitor pm = new GPProgressMonitor( new NullProgressMonitor() );
        switch (typeName) {
            case OmsGeopaparazzi4Converter.SIMPLE_NOTES: {
                return new GPFeatureSource() {
                    @Override
                    protected SimpleFeatureCollection doGetFeatures() throws Exception {
                        return OmsGeopaparazzi4Converter.simpleNotes2featurecollection( conn, pm );
                    }
                };
            }
            case OmsGeopaparazzi4Converter.GPS_LOGS: {
                return new GPFeatureSource() {
                    @Override
                    protected SimpleFeatureCollection doGetFeatures() throws Exception {
                        List<GpsLog> gpsLogsList = OmsGeopaparazzi4Converter.getGpsLogsList( conn );
                        return OmsGeopaparazzi4Converter.getLogLinesFeatureCollection( pm, gpsLogsList );
                    }
                };
            }
            case OmsGeopaparazzi4Converter.MEDIA_NOTES: {
                return new GPFeatureSource() {
                    @Override
                    protected SimpleFeatureCollection doGetFeatures() throws Exception {
                        return OmsGeopaparazzi4Converter.media2IdBasedFeatureCollection( conn, pm );
                    }
                };
            }
            default: {
                return new GPFeatureSource() {
                    @Override
                    protected SimpleFeatureCollection doGetFeatures() throws Exception {
                        return OmsGeopaparazzi4Converter.complexNote2featurecollection( typeName, conn, pm );
                    }
                };
            }
        }
    }

    @Override
    public SimpleFeatureType getSchema( String typeName ) throws IOException {
        return getFeatureSource( typeName ).getSchema();
    }

    @Override
    protected ReferencedEnvelope getBounds( Query query ) throws IOException {
        throw new RuntimeException( "not implemented." );
    }

    @Override
    protected int getCount( Query query ) throws IOException {
        throw new RuntimeException( "not implemented." );
    }

    @Override
    protected FeatureReader<SimpleFeatureType,SimpleFeature> getFeatureReader( String typeName ) throws IOException {
        throw new RuntimeException( "not implemented." );
    }

    @Override
    public FeatureReader<SimpleFeatureType,SimpleFeature> getFeatureReader( Query query, Transaction transaction ) throws IOException {
        throw new RuntimeException( "not implemented." );
    }

    @Override
    protected FeatureWriter<SimpleFeatureType,SimpleFeature> createFeatureWriter( String typeName, Transaction transaction)
            throws IOException {
        throw new RuntimeException( "not implemented." );
    }

    
    /**
     * 
     */
    public abstract class GPFeatureSource
            extends AbstractFeatureSource {

        protected abstract SimpleFeatureCollection doGetFeatures() throws Exception;
        
        @Override
        public SimpleFeatureType getSchema() {
            // XXX GeopaparazziUtilities.getSimpleNotesfeatureType( );
            try {
                return getFeatures().getSchema();
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }
        }

        @Override
        public SimpleFeatureCollection getFeatures( Query query ) throws IOException {
            try {
                return doGetFeatures();
            }
            catch (Exception e) {
                Throwables.propagateIfInstanceOf( e, IOException.class );
                throw Throwables.propagate( e );
            }
        }

        @Override
        public DataStore getDataStore() {
            return GPDataStore.this;
        }

        @Override
        public void addFeatureListener( FeatureListener listener ) {
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public void removeFeatureListener( FeatureListener listener ) {
            throw new RuntimeException( "not yet implemented." );
        }
    }
    
}
