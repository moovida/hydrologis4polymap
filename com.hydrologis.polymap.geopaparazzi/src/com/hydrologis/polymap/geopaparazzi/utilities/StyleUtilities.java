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

import java.awt.Color;

import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.jgrasstools.gears.io.geopaparazzi.OmsGeopaparazzi4Converter;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.TableDescriptions.NotesTableFields;
import org.opengis.feature.simple.SimpleFeatureType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.DefaultStyle;
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
import org.polymap.core.style.model.feature.PointStyle;
import org.polymap.core.style.model.feature.PropertyString;
import org.polymap.core.style.model.feature.Stroke;
import org.polymap.core.style.model.feature.TextStyle;

import org.polymap.p4.P4Plugin;

public class StyleUtilities {

    private static final Log log = LogFactory.getLog( StyleUtilities.class );


    public static FeatureStyle getFeatureStyle4Layer( String layerName, IJGTConnection connection ) throws Exception {
        switch (layerName) {
            case OmsGeopaparazzi4Converter.SIMPLE_NOTES:
                return getSimpleNotesStyle();
            case OmsGeopaparazzi4Converter.GPS_LOGS:
                return getGpsLogStyle();
            case OmsGeopaparazzi4Converter.MEDIA_NOTES:
                return getImageNotesStyle();
            default:
                return getComplexNotesStyle( layerName, connection );
        }
    }


    private static FeatureStyle getComplexNotesStyle( String layerName, IJGTConnection connection ) throws Exception {
        SimpleFeatureType simpleNotesfeatureType = GeopaparazziUtilities.getComplexNotefeatureType( layerName, connection );
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        DefaultStyle.create( featureStyle, simpleNotesfeatureType );

        PointStyle point = featureStyle.members().createElement( PointStyle.defaults );
        point.diameter.createValue( ConstantNumber.defaults( 10.0 ) );
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
        font.weight.createValue( ConstantFontWeight.defaults() );
        font.size.createValue( ConstantNumber.defaults( 12.0 ) );
        text.color.createValue( ConstantColor.defaults( Color.BLUE ) );

        String textFN = NotesTableFields.COLUMN_TEXT.getFieldName();
        text.property.createValue( PropertyString.defaults( textFN ) );

        return featureStyle;
    }


    private static FeatureStyle getSimpleNotesStyle() {
        SimpleFeatureType simpleNotesfeatureType = GeopaparazziUtilities.getSimpleNotesfeatureType();
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        DefaultStyle.create( featureStyle, simpleNotesfeatureType );

        PointStyle point = featureStyle.members().createElement( PointStyle.defaults );
        point.diameter.createValue( ConstantNumber.defaults( 10.0 ) );
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
        font.weight.createValue( ConstantFontWeight.defaults() );
        font.size.createValue( ConstantNumber.defaults( 12.0 ) );
        text.color.createValue( ConstantColor.defaults( Color.BLUE ) );

        String textFN = NotesTableFields.COLUMN_TEXT.getFieldName();
        text.property.createValue( PropertyString.defaults( textFN ) );

        return featureStyle;
    }


    private static FeatureStyle getImageNotesStyle() {
        SimpleFeatureType simpleNotesfeatureType = GeopaparazziUtilities.getSimpleNotesfeatureType();
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        DefaultStyle.create( featureStyle, simpleNotesfeatureType );

        PointStyle point = featureStyle.members().createElement( PointStyle.defaults );
        point.diameter.createValue( ConstantNumber.defaults( 8.0 ) );
        Fill fill = point.fill.get();
        fill.color.createValue( ConstantColor.defaults( Color.RED ) );
        fill.opacity.createValue( ConstantNumber.defaults( 0.7 ) );
        Stroke stroke = point.stroke.get();
        stroke.color.createValue( ConstantColor.defaults( Color.RED ) );
        stroke.width.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        stroke.strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        stroke.strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        // TODO
        // TextStyle text = featureStyle.members().createElement( TextStyle.defaults
        // );
        // Font font = text.font.get();
        // font.family.createValue( ConstantFontFamily.defaults() );
        // font.style.createValue( ConstantFontStyle.defaults() );
        // font.weight.createValue( ConstantFontWeight.defaults() );
        // font.size.createValue( ConstantNumber.defaults( 12.0 ) );
        // text.color.createValue( ConstantColor.defaults( Color.BLUE ) );

        return featureStyle;
    }


    private static FeatureStyle getGpsLogStyle() {
        SimpleFeatureType simpleNotesfeatureType = GeopaparazziUtilities.getSimpleNotesfeatureType();
        FeatureStyle featureStyle = P4Plugin.styleRepo().newFeatureStyle();
        DefaultStyle.create( featureStyle, simpleNotesfeatureType );

        PointStyle point = featureStyle.members().createElement( PointStyle.defaults );
        point.diameter.createValue( ConstantNumber.defaults( 8.0 ) );
        Fill fill = point.fill.get();
        fill.color.createValue( ConstantColor.defaults( Color.RED ) );
        fill.opacity.createValue( ConstantNumber.defaults( 0.7 ) );
        Stroke stroke = point.stroke.get();
        stroke.color.createValue( ConstantColor.defaults( Color.RED ) );
        stroke.width.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        stroke.strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        stroke.strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        stroke.strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        // TODO
        // TextStyle text = featureStyle.members().createElement( TextStyle.defaults
        // );
        // Font font = text.font.get();
        // font.family.createValue( ConstantFontFamily.defaults() );
        // font.style.createValue( ConstantFontStyle.defaults() );
        // font.weight.createValue( ConstantFontWeight.defaults() );
        // font.size.createValue( ConstantNumber.defaults( 12.0 ) );
        // text.color.createValue( ConstantColor.defaults( Color.BLUE ) );

        return featureStyle;
    }
}
