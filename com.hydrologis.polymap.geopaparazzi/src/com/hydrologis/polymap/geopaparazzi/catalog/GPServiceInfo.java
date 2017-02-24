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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import java.io.File;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoGpsLog;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoImages;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoNotes;
import org.opengis.feature.type.Name;

import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@SuppressWarnings( "deprecation" )
public class GPServiceInfo
        implements IServiceInfo {

    /** FIXME dirty hack to get the db instance to FeaturePanelContribution. */
    public static SqliteDb globalDb;

    private IMetadata   metadata;

    private SqliteDb    db;

    private GPDataStore ds;


    protected GPServiceInfo( IMetadata metadata ) throws Exception {
        this.metadata = metadata;
        Map<String,String> params = metadata.getConnectionParams();
        String url = params.get( IMetadataResourceResolver.CONNECTION_PARAM_URL );
        File databaseFile = new File( url );
        this.db = new SqliteDb();
        this.db.open( databaseFile.getAbsolutePath() );
        this.ds = new GPDataStore( db );
        
        assert globalDb == null;
        globalDb = db;
    }


    @Override
    public <T> T createService( IProgressMonitor monitor ) throws Exception {
        return (T)new GPDataStore( db );
    }


    @Override
    public Iterable<IResourceInfo> getResources( IProgressMonitor monitor ) throws Exception {
        return FluentIterable.from( ds.getNames() ).transform( name -> new GPResourceInfo( name ) );
    }


    @Override
    public String getTitle() {
        return FilenameUtils.getBaseName( db.getDatabasePath() );
    }


    @Override
    public Set<String> getKeywords() {
        return Sets.newHashSet( "Geopaparazzi", "Digital Field Mapping" );
    }


    @Override
    public Optional<String> getDescription() {
        // XXX
        return Optional.empty();

        // StringBuilder sb = new StringBuilder();
        // for (Entry<String,String> entry : metadataMap.entrySet()) {
        // sb.append( entry.getKey() ).append( "=" ).append( entry.getValue()
        // ).append( "\n" );
        // }
        // return sb.toString();
    }


    @Override
    public IMetadata getMetadata() {
        return metadata;
    }


    @Override
    public IServiceInfo getServiceInfo() {
        return this;
    }


    /**
     * 
     */
    class GPResourceInfo
            implements IResourceInfo {

        private Name name;


        public GPResourceInfo( Name name ) {
            this.name = name;
        }


        @Override
        public String getName() {
            return name.getLocalPart();
        }


        @Override
        public String getTitle() {
            return name.getLocalPart();
        }


        @Override
        public Set<String> getKeywords() {
            return Collections.EMPTY_SET;
        }


        @Override
        public Optional<String> getDescription() {
            return Optional.empty();
        }


        @Override
        public ReferencedEnvelope getBounds() {
            try {
                IJGTConnection connection = db.getConnection();

                ReferencedEnvelope envelope = DaoNotes.getEnvelope( connection, null );

                ReferencedEnvelope tmp = DaoImages.getEnvelope( connection );
                expandEnvelope( envelope, tmp );

                tmp = DaoGpsLog.getEnvelope( connection );
                expandEnvelope( envelope, tmp );

                return envelope;
            }
            catch (Exception e) {
                // XXX Auto-generated catch block

            }

            return GPUtilities.WORLD;
        }


        private void expandEnvelope( ReferencedEnvelope envelope, ReferencedEnvelope tmp ) {
            if (tmp != null) {
                if (envelope != null) {
                    envelope.expandToInclude( tmp );
                }
                else {
                    envelope = tmp;
                }
            }
        }


        @Override
        public IServiceInfo getServiceInfo() {
            return GPServiceInfo.this;
        }

    }

}
