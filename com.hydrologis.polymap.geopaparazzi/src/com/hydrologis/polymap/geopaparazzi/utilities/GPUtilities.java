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
package com.hydrologis.polymap.geopaparazzi.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.awt.Color;
import java.io.File;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.jgrasstools.gears.utils.StringUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hydrologis.polymap.geopaparazzi.GeopaparazziPlugin;

import org.polymap.core.CorePlugin;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.feature.ConstantColor;
import org.polymap.core.style.model.feature.ConstantFontFamily;
import org.polymap.core.style.model.feature.ConstantFontStyle;
import org.polymap.core.style.model.feature.ConstantFontWeight;
import org.polymap.core.style.model.feature.ConstantNumber;
import org.polymap.core.style.model.feature.ConstantStrokeCapStyle;
import org.polymap.core.style.model.feature.ConstantStrokeDashStyle;
import org.polymap.core.style.model.feature.ConstantStrokeJoinStyle;
import org.polymap.core.style.model.feature.Fill;
import org.polymap.core.style.model.feature.Font;
import org.polymap.core.style.model.feature.FontStyle;
import org.polymap.core.style.model.feature.FontWeight;
import org.polymap.core.style.model.feature.LineStyle;
import org.polymap.core.style.model.feature.PointStyle;
import org.polymap.core.style.model.feature.PropertyString;
import org.polymap.core.style.model.feature.Stroke;
import org.polymap.core.style.model.feature.TextStyle;

import org.polymap.p4.P4Plugin;

public class GPUtilities {

    private static final Log                      log   = LogFactory.getLog( GPUtilities.class );

    public static final CoordinateReferenceSystem WGS84 = DefaultGeographicCRS.WGS84;

    public static final ReferencedEnvelope        WORLD = new ReferencedEnvelope( -180, 180, -85, 85, WGS84 );


    /**
     * Get the {@link FeatureStyle} for a given geopap layer.
     *
     *
     * @param layerName the name of the geopap layer.
     * @param connection the db connection.
     * @return the feature style for the layer.
     * @throws Exception
     */
    public static FeatureStyle getFeatureStyle4Layer( String layerName, IJGTConnection connection ) throws Exception {
        switch (layerName) {
            case GeopaparazziUtilities.SIMPLE_NOTES:
                return getSimpleNotesStyle();
            case GeopaparazziUtilities.GPS_LOGS:
                return getGpsLogStyle();
            case GeopaparazziUtilities.MEDIA_NOTES:
                return getImageNotesStyle();
            default:
                return getComplexNotesStyle( layerName, connection );
        }
    }


    private static FeatureStyle getComplexNotesStyle( String layerName, IJGTConnection connection ) throws Exception {
        SimpleFeatureType complexNotefeatureType = GeopaparazziUtilities.getComplexNotefeatureType( layerName, connection );
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        // DefaultStyle.create( featureStyle, simpleNotesfeatureType );

        PointStyle point = featureStyle.members().createElement( PointStyle.defaults );
        point.diameter.createValue( ConstantNumber.defaults( 15.0 ) );
        Fill fill = point.fill.get();
        fill.color.createValue( ConstantColor.defaults( Color.GREEN ) );
        fill.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        Stroke stroke = point.stroke.get();
        Color darkGreen = Color.decode( "#076507" );
        stroke.color.createValue( ConstantColor.defaults( darkGreen ) );
        stroke.width.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        stroke.strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        stroke.strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        TextStyle text = featureStyle.members().createElement( TextStyle.defaults );
        Font font = text.font.get();
        font.family.createValue( ConstantFontFamily.defaults() );
        font.style.createValue( ConstantFontStyle.defaults() );
        font.weight.createValue( ConstantFontWeight.defaults( FontWeight.bold ) );
        font.size.createValue( ConstantNumber.defaults( 12.0 ) );
        text.color.createValue( ConstantColor.defaults( darkGreen ) );

        setDefaultText( text, complexNotefeatureType );

        return featureStyle;
    }


    private static FeatureStyle getSimpleNotesStyle() {
        // SimpleFeatureType simpleNotesfeatureType =
        // GeopaparazziUtilities.getSimpleNotesfeatureType();
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        // DefaultStyle.create( featureStyle, simpleNotesfeatureType );

        PointStyle point = featureStyle.members().createElement( PointStyle.defaults );
        point.diameter.createValue( ConstantNumber.defaults( 15.0 ) );
        Fill fill = point.fill.get();
        fill.color.createValue( ConstantColor.defaults( Color.CYAN ) );
        fill.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        Stroke stroke = point.stroke.get();
        stroke.color.createValue( ConstantColor.defaults( Color.BLUE ) );
        stroke.width.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        stroke.strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        stroke.strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        TextStyle text = featureStyle.members().createElement( TextStyle.defaults );
        Font font = text.font.get();
        font.family.createValue( ConstantFontFamily.defaults() );
        font.style.createValue( ConstantFontStyle.defaults() );
        font.weight.createValue( ConstantFontWeight.defaults( FontWeight.bold ) );
        font.size.createValue( ConstantNumber.defaults( 12.0 ) );
        text.color.createValue( ConstantColor.defaults( Color.BLUE ) );

        text.property.createValue( PropertyString.defaults( GeopaparazziUtilities.NOTES_textFN ) );

        return featureStyle;
    }


    private static FeatureStyle getImageNotesStyle() {
        // SimpleFeatureType simpleNotesfeatureType =
        // GeopaparazziUtilities.getSimpleNotesfeatureType();
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        // DefaultStyle.create( featureStyle, simpleNotesfeatureType );

        PointStyle point = featureStyle.members().createElement( PointStyle.defaults );
        point.diameter.createValue( ConstantNumber.defaults( 15.0 ) );
        Fill fill = point.fill.get();
        fill.color.createValue( ConstantColor.defaults( Color.RED ) );
        fill.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        Stroke stroke = point.stroke.get();
        Color darkRed = Color.decode( "#730D0D" );
        stroke.color.createValue( ConstantColor.defaults( darkRed ) );
        stroke.width.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        stroke.strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        stroke.strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        TextStyle text = featureStyle.members().createElement( TextStyle.defaults );
        Font font = text.font.get();
        font.family.createValue( ConstantFontFamily.defaults() );
        font.style.createValue( ConstantFontStyle.defaults() );
        font.weight.createValue( ConstantFontWeight.defaults() );
        font.size.createValue( ConstantNumber.defaults( 12.0 ) );
        text.color.createValue( ConstantColor.defaults( darkRed ) );
        text.property.createValue( PropertyString.defaults( GeopaparazziUtilities.IMAGES_imageidFN ) );

        return featureStyle;
    }


    private static FeatureStyle getGpsLogStyle() {
        // SimpleFeatureType simpleNotesfeatureType =
        // GeopaparazziUtilities.getSimpleNotesfeatureType();
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        // DefaultStyle.create( featureStyle, simpleNotesfeatureType );

        LineStyle line = featureStyle.members().createElement( LineStyle.defaults );
        line.fill.get().width.createValue( ConstantNumber.defaults( 3.0 ) );
        line.fill.get().color.createValue( ConstantColor.defaults( Color.ORANGE ) );
        line.fill.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );

        TextStyle text = featureStyle.members().createElement( TextStyle.defaults );
        Font font = text.font.get();
        font.family.createValue( ConstantFontFamily.defaults() );
        font.style.createValue( ConstantFontStyle.defaults( FontStyle.italic ) );
        font.weight.createValue( ConstantFontWeight.defaults( FontWeight.bold ) );
        font.size.createValue( ConstantNumber.defaults( 12.0 ) );
        text.color.createValue( ConstantColor.defaults( Color.ORANGE ) );

        return featureStyle;
    }


    private static void setDefaultText( TextStyle text, SimpleFeatureType schema ) {
        Collection<PropertyDescriptor> schemaDescriptors = schema.getDescriptors();
        GeometryDescriptor geometryDescriptor = schema.getGeometryDescriptor();
        for (PropertyDescriptor descriptor : schemaDescriptors) {
            if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
                if (String.class.isAssignableFrom( descriptor.getType().getBinding() )) {
                    text.property.createValue( PropertyString.defaults( descriptor.getName().getLocalPart() ) );
                    break;
                }
            }
        }
    }


    /**
     *  Get the geopaparazzi projects folder.
     *
     * @return the default folder for geopaparazzi projects.
     */
    public static File getGeopaparazziProjectsFolder() {
        File gpapProjectsFolder = CorePlugin.getDataLocation( GeopaparazziPlugin.instance() );
        return gpapProjectsFolder;
    }


    /**
     * Gets a safe project file to use to make sure nothing gets overwritten.
     *
     * @param proposedFile the proposed file.
     * @return the safe file.
     */
    public static File getSafeProjectFile( File proposedFile ) {
        File gpapProjectsFolder = GPUtilities.getGeopaparazziProjectsFolder();
        File[] geopaparazziFiles = GeopaparazziUtilities.getGeopaparazziFiles( gpapProjectsFolder );
        List<String> namesNoExt = new ArrayList<>();
        for (File file : geopaparazziFiles) {
            String name = FileUtilities.getNameWithoutExtention( file );
            namesNoExt.add( name );
        }

        String name = FileUtilities.getNameWithoutExtention( proposedFile );
        String safeName = StringUtilities.checkSameName( namesNoExt, name );

        File newFile = new File( gpapProjectsFolder, safeName + ".gpap" );

        return newFile;
    }

}
