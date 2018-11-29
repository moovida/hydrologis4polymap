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
import java.util.stream.Collectors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.geotools.feature.NameImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.jgrasstools.gears.utils.StringUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hydrologis.polymap.geopaparazzi.GeopaparazziPlugin;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.CorePlugin;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.feature.AttributeValue;
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
import org.polymap.core.style.model.feature.Stroke;
import org.polymap.core.style.model.feature.TextStyle;

import org.polymap.p4.P4Plugin;

public class GPUtilities {

    private static final Log                      log                = LogFactory.getLog( GPUtilities.class );

    public static final CoordinateReferenceSystem WGS84              = DefaultGeographicCRS.WGS84;

    public static final ReferencedEnvelope        WORLD              = new ReferencedEnvelope( -180, 180, -85, 85, WGS84 );

    public static final String                    EXTENDED_LAYER_SEP = "@";


    /**
     * Get the {@link FeatureStyle} for a given geopap layer.
     *
     *
     * @param layerName the name of the geopap layer.
     * @param connection the db connection.
     * @return the feature style for the layer.
     * @throws Exception
     */
    public static FeatureStyle getFeatureStyle4Layer( String extendedLayerName, IJGTConnection connection ) throws Exception {
        String layerName = GPUtilities.extendedName2layerName( extendedLayerName );
        
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
        SimpleFeatureType complexNotefeatureType = GeopaparazziUtilities
                .getComplexNotefeatureType( layerName, connection );
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

        text.property.createValue( AttributeValue.defaults( GeopaparazziUtilities.NOTES_textFN, null, null ) );

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
        text.property.createValue( AttributeValue.defaults( GeopaparazziUtilities.IMAGES_imageidFN, null, null ) );

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

        text.property.createValue( AttributeValue.defaults( GeopaparazziUtilities.GPSLOG_descrFN, null, null ) );

        return featureStyle;
    }


    private static void setDefaultText( TextStyle text, SimpleFeatureType schema ) {
        if (schema == null) {
            log.warn( "No schema! -> no text!" );
            return;
        }
        Collection<PropertyDescriptor> schemaDescriptors = schema.getDescriptors();
        GeometryDescriptor geometryDescriptor = schema.getGeometryDescriptor();
        for (PropertyDescriptor descriptor : schemaDescriptors) {
            if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
                if (String.class.isAssignableFrom( descriptor.getType().getBinding() )) {
                    text.property.createValue( AttributeValue.defaults( descriptor.getName().getLocalPart(), null, null ) );
                    //text.property.createValue( PropertyString.defaults( descriptor.getName().getLocalPart() ) );
                    break;
                }
            }
        }
    }


    /**
     * Get the geopaparazzi projects (data) folder where database files are stored.
     *
     * @return the default folder for geopaparazzi projects.
     */
    public static File projectsFolder() {
        return CorePlugin.getDataLocation( GeopaparazziPlugin.instance() );
    }


    /**
     * Gets a safe project file to use to make sure nothing gets overwritten.
     *
     * @param proposedFile the proposed file.
     * @return the safe file.
     */
    public static File getSafeProjectFile( File proposedFile ) {
        File gpapProjectsFolder = GPUtilities.projectsFolder();
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


    public static BufferedImage scaleImageFromStream( Control control, InputStream inputStream, boolean doOriginalSize,
            int maxSize )
            throws Exception {
        BufferedImage image = ImageIO.read( inputStream );

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        int width = imageWidth;
        int height = imageHeight;
        if (imageWidth > imageHeight) {
            if (width > maxSize && !doOriginalSize)
                width = maxSize;
            height = imageHeight * width / imageWidth;
        }
        else {
            if (height > maxSize && !doOriginalSize)
                height = maxSize;
            width = height * imageWidth / imageHeight;
        }

        BufferedImage resizedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage( image, 0, 0, width, height, null );
        g.dispose();

        return resizedImage;
    }


    public static Image buffered2SwtImage( BufferedImage bufferedImage, Device device ) {
        DirectColorModel colorModel = (DirectColorModel)bufferedImage.getColorModel();
        PaletteData palette = new PaletteData( colorModel.getRedMask(), colorModel.getGreenMask(), colorModel
                .getBlueMask() );
        ImageData data = new ImageData( bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel
                .getPixelSize(), palette );
        for (int y = 0; y < data.height; y++) {
            for (int x = 0; x < data.width; x++) {
                int rgb = bufferedImage.getRGB( x, y );
                int pixel = palette.getPixel( new RGB( (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF ) );
                data.setPixel( x, y, pixel );
                if (colorModel.hasAlpha()) {
                    data.setAlpha( x, y, (rgb >> 24) & 0xFF );
                }
            }
        }
        return new Image( device, data );
    }


    public static String dbNameForLayerExtension( SqliteDb db ) {
        File dbFile = new File( db.getDatabasePath() );
        String dbName = FileUtilities.getNameWithoutExtention( dbFile );
        if (dbName.startsWith( "geopaparazzi_" )) {
            dbName = dbName.replaceFirst( "geopaparazzi_", "" );
        }
        dbName = EXTENDED_LAYER_SEP + dbName;
        return dbName;
    }


    public static String extendedName2layerName( String extendedName ) {
        String name = extendedName.split( EXTENDED_LAYER_SEP )[0];
        return name;
    }


    public static List<Name> toExtendedNamesList( List<String> layerNamesList, String dbExtentionName ) {
        return layerNamesList.stream()
                .map( s -> s + dbExtentionName )
                .map( s -> new NameImpl( s ) )
                .collect( Collectors.toList() );
    }
}
