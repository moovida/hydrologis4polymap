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
package com.hydrologis.polymap.geopaparazzi.catalog;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import java.io.File;
import java.net.URI;

import org.geotools.data.ResourceInfo;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoGpsLog;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoImages;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoNotes;
import org.jgrasstools.gears.utils.CrsUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GeopaparazziResourceInfo
        implements ResourceInfo {

    private LinkedHashMap<String,String> metadataMap;

    private String                       dbName;

    private File                         dbFile;

    private IJGTConnection               connection;


    public GeopaparazziResourceInfo( SqliteDb db ) {
        connection = db.getConnection();

        String databasePath = db.getDatabasePath();
        dbFile = new File( databasePath );
        dbName = FileUtilities.getNameWithoutExtention( dbFile );

        try {
            metadataMap = OmsGeopaparazzi4Converter.getMetadataMap( connection );
        }
        catch (Exception e) {
            // XXX Auto-generated catch block

        }

    }


    @Override
    public String getTitle() {
        return dbName;
    }


    @Override
    public Set<String> getKeywords() {
        HashSet<String> set = new HashSet<>();
        set.add( "Geopaparazzi" );
        set.add( "Digital Field Mapping" );

        return set;
    }


    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String,String> entry : metadataMap.entrySet()) {
            sb.append( entry.getKey() ).append( "=" ).append( entry.getValue() ).append( "\n" );
        }
        return sb.toString();
    }


    @Override
    public URI getSchema() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public String getName() {
        return dbName;
    }


    @Override
    public ReferencedEnvelope getBounds() {
        try {
            ReferencedEnvelope envelope = DaoNotes.getEnvelope( connection );

            ReferencedEnvelope tmp = DaoImages.getEnvelope( connection );
            expandEnvelope( envelope, tmp );

            tmp = DaoGpsLog.getEnvelope( connection );
            expandEnvelope( envelope, tmp );

            return envelope;
        }
        catch (Exception e) {
            // XXX Auto-generated catch block

        }

        return CrsUtilities.WORLD;
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
    public CoordinateReferenceSystem getCRS() {
        return DefaultGeographicCRS.WGS84;
    }
}
