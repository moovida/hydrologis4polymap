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
package com.hydrologis.polymap.geopaparazzi;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import org.jgrasstools.dbs.compat.IJGTConnection;
import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.jgrasstools.gears.io.geopaparazzi.geopap4.DaoImages;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hydrologis.polymap.geopaparazzi.catalog.GPDataStore;
import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.data.pipeline.DataSourceDescriptor;

import org.polymap.rhei.batik.contribution.IContributionSite;
import org.polymap.rhei.batik.contribution.IPanelContribution;

import org.polymap.p4.layer.FeaturePanel;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class FeaturePanelContribution
        implements IPanelContribution {

    private static final Log log = LogFactory.getLog( FeaturePanelContribution.class );


    @Override
    public void fillPanel( IContributionSite site, Composite parent ) {
        if (site.panel() instanceof FeaturePanel
                && site.tagsContain( FeaturePanel.ID.id() )) {

            try {
                FeaturePanel panel = (FeaturePanel)site.panel();
                Feature feature = panel.feature();
                Property prop = feature.getProperty( GeopaparazziUtilities.IMAGES_imageidFN );
                // true for media layer
                if (prop != null && prop.getValue() != null) {
                    Long imageId = (Long)prop.getValue();
                    
                    // get db connection from underlying datasource
                    PipelineFeatureSource pfs = ((PipelineFeatureSource)panel.fs());
                    DataSourceDescriptor dsd = pfs.pipeline().dataSourceDescription();
                    GPDataStore ds = (GPDataStore)dsd.service.get();

                    IJGTConnection conn = ds.db().getConnection();
                    byte[] imageData = DaoImages.getImageData( conn, imageId );
                    log.info( "Image data bytes: " + imageData.length );

                    Label l = site.toolkit().createLabel( parent, null );

                    // TODO where should this go? Is it possible
                    // to adapt it to the screensize?
                    int maxSize = 400;

                    BufferedImage scaledImage = GPUtilities
                            .scaleImageFromStream( l, new ByteArrayInputStream( imageData ), false, maxSize );
                    Image swtImage = GPUtilities.buffered2SwtImage( scaledImage, l.getDisplay() );
                    l.setImage( swtImage );
                }
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
    }

}
