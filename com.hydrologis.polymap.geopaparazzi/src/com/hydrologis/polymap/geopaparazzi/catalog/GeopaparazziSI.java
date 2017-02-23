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

import org.geotools.data.ServiceInfo;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;

public class GeopaparazziSI
        implements ServiceInfo {

    private LinkedHashMap<String,String> metadataMap;

    private String                       dbName;

    private File                         dbFile;


    public GeopaparazziSI( SqliteDb db ) {
        IJGTConnection connection = db.getConnection();

        String databasePath = db.getDatabasePath();
        dbFile = new File( databasePath );
        dbName = FileUtilities.getNameWithoutExtention( dbFile );

        try {
            metadataMap = GeopaparazziUtilities.getProjectMetadata( connection );
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
    public URI getPublisher() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public URI getSchema() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public URI getSource() {
        return dbFile.toURI();
    }
}
