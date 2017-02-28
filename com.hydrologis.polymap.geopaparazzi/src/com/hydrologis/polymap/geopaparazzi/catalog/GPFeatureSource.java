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

import java.util.Iterator;

import java.io.IOException;

import org.geotools.data.AbstractFeatureSource;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureListener;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.BaseSimpleFeatureCollection;
import org.geotools.feature.collection.DelegateSimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;

/**
 * 
 * @author Falko Bräutigam
 */
@SuppressWarnings( "deprecation" )
public abstract class GPFeatureSource
        extends AbstractFeatureSource {

    private static final Log log = LogFactory.getLog( GPFeatureSource.class );
    
    /** Pass {@link SqliteDb} in user data of the features. */
    public static final String              USER_DATA_KEY_DB = "db";
    
    private GPDataStore                     ds;
    
    private Lazy<SimpleFeatureType>         schema = new CachedLazyInit();
    
    private Lazy<SimpleFeatureCollection>   features = new CachedLazyInit();
    
    private Cache<Query,ReferencedEnvelope> bounds = CacheConfig.defaults().createCache();
    
    private Cache<Query,Integer>            count = CacheConfig.defaults().createCache();
    

    GPFeatureSource( GPDataStore ds ) {
        this.ds = ds;
    }


    protected abstract SimpleFeatureCollection doGetFeatures() throws Exception;

    protected abstract ReferencedEnvelope doGetBounds() throws Exception;


    @Override
    public SimpleFeatureType getSchema() {
        return schema.get( () -> {
            try {
                // XXX GeopaparazziUtilities.getSimpleNotesfeatureType( );
                return getFeatures( Query.ALL ).getSchema();
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }
        });
    }


    @Override
    public ReferencedEnvelope getBounds( Query query ) throws IOException {
        return bounds.get( query, key -> {
            try {
                // XXX does no handle query/filer
                //ReferencedEnvelope envelope = doGetBounds();
                
                ReferencedEnvelope envelope = DataUtilities.bounds( getFeatures( query ) );
                return envelope != null ? envelope : GPUtilities.WORLD;
            }
            catch (Exception e) {
                log.warn( "", e );
                return GPUtilities.WORLD;
            }
        });
    }


    @Override
    public int getCount( Query query ) throws IOException {
        return count.get( query, key -> {
            return getFeatures( key ).size();
        });
    }


    @Override
    public SimpleFeatureCollection getFeatures( Query query ) throws IOException {
        SimpleFeatureCollection allFeatures = features.get( () -> {
            try {
                return doGetFeatures();
            }
            catch (Exception e) {
                throw Throwables.propagate( e );
            }
        });
        
        return new BaseSimpleFeatureCollection( (SimpleFeatureType)allFeatures.getSchema() ) {
            @Override
            public SimpleFeatureIterator features() {
                Iterator<SimpleFeature> result = DataUtilities.iterator( allFeatures.features() );
                // geotools API is a mess sometimes :(
                if (query.getStartIndex() != null && query.getStartIndex() > 0) {
                    Iterators.advance( result, query.getStartIndex() );
                }
                if (query.getMaxFeatures() > 0) {
                    result = Iterators.limit( result, query.getMaxFeatures() );
                }
                result = Iterators.filter( result, f -> { 
                    return query.getFilter().evaluate( f ); 
                });
                result = Iterators.transform( result, f -> {
                    f.getUserData().put( USER_DATA_KEY_DB, ds.db() );
                    return f;
                });
                return new DelegateSimpleFeatureIterator( result );
            }
        };
        
    }


    @Override
    public DataStore getDataStore() {
        return ds;
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