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
package com.hydrologis.polymap.jgt;

import java.util.EventObject;
import java.util.Optional;

import java.text.NumberFormat;

import org.geotools.geometry.jts.JTS;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.data.process.ui.FieldViewerSite;
import org.polymap.core.data.process.ui.InputFieldSupplier;
import org.polymap.core.data.util.Geometries;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.toolkit.SimpleDialog;

import org.polymap.p4.layer.RasterLayer;
import org.polymap.p4.process.ClickMapViewer;

import oms3.annotations.UI;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class CoordinateSupplier
        extends InputFieldSupplier {

    private static final Log log = LogFactory.getLog( CoordinateSupplier.class );
    
    private NumberFormat    nf;
    
    private Button          btn;
    
    
    public CoordinateSupplier() {
        nf = NumberFormat.getNumberInstance( Polymap.getSessionLocale() );
        nf.setMaximumIntegerDigits( 100 );
        nf.setMaximumFractionDigits( 2 );
        nf.setMinimumIntegerDigits( 1 );
        nf.setMinimumFractionDigits( 2 );        
    }


    @Override
    public String label() {
        return "Coordinate";
    }
    
    
    @Override
    public boolean init( @SuppressWarnings( "hiding" ) FieldViewerSite site ) {
        if (super.init( site )) {
            Optional<UI> uiHint = ((JgtFieldInfo)site.fieldInfo.get()).annotation( UI.class );
            if (uiHint.isPresent()) {
                String v = uiHint.get().value();
                if (JGTConstants.EASTING_UI_HINT.equals( v ) || JGTConstants.NORTHING_UI_HINT.equals( v )) {
                    Class<?> fieldType = site.fieldInfo.get().type();
                    if (Double.class.isAssignableFrom( fieldType ) || Double.TYPE.equals( fieldType )) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void createContents( Composite parent ) {
        // shadows
        parent.setLayout( FormLayoutFactory.defaults().margins( 1, 2, 3, 2 ).create() );
        
        btn = new Button( parent, SWT.PUSH );
        btn.setText( nf.format( site.getFieldValue() ) + " ..." );
        btn.setToolTipText( "Choose coordinate from a map" );
        btn.addSelectionListener( UIUtils.selectionListener( ev -> {
            openClickMap();
        }));
        FormDataFactory.on( btn ).fill();
        
        EventManager.instance().subscribe( this, ev -> ev instanceof OrdinateSupplierEvent );
    }

    
    protected void openClickMap() {
        SimpleDialog dialog = new SimpleDialog();
        dialog.title.put( "Choose a coordinate" );
        dialog.addCancelAction();
        dialog.setContents( parent -> {
            parent.setLayout( FormLayoutFactory.defaults().create() );
            ClickMapViewer mapViewer = new ClickMapViewer( parent ) {
                @Override
                protected void onClick( Coordinate coordinate ) {
                    try {
                        CoordinateReferenceSystem layerCrs = RasterLayer.of( (ILayer)site.layer.get() ).get().get()
                                .gridCoverage().getCoordinateReferenceSystem();
                        
                        MathTransform transform = Geometries.transform( 
                                maxExtent.get().getCoordinateReferenceSystem(),
                                layerCrs );
                        coordinate = JTS.transform( coordinate, null, transform );

                        CoordinateSupplier.this.onClick( coordinate );
                        dialog.close();
                        
                        EventManager.instance().publish( new OrdinateSupplierEvent( this, coordinate ) );
                    }
                    catch (Exception e) {
                    }
                }
            };
            FormDataFactory.on( mapViewer.getControl() ).fill().width( 450 ).height( 400 );
        });
        dialog.open();
    }


    protected void onClick( Coordinate coordinate ) {
        Optional<UI> uiHint = ((JgtFieldInfo)site.fieldInfo.get()).annotation( UI.class );
        String v = uiHint.get().value();
        if (JGTConstants.EASTING_UI_HINT.equals( v )) {
            site.setFieldValue( coordinate.x );
            btn.setText( nf.format( coordinate.x ) );
        }
        else if (JGTConstants.NORTHING_UI_HINT.equals( v )) {
            site.setFieldValue( coordinate.y );
            btn.setText( nf.format( coordinate.y ) );
        }
        else {
            throw new RuntimeException( "Unknown value of @UI: " + v );
        }
    }
    
    
    @EventHandler( display=true )
    protected void onOtherSupplier( OrdinateSupplierEvent ev ) {
        if (btn != null && !btn.isDisposed()) {
            onClick( ev.coordinate );
        }
        else {
            EventManager.instance().unsubscribe( this );
        }
    }
    
    
    /**
     * Inform the viewer of the other ordinate. 
     */
    public static class OrdinateSupplierEvent
            extends EventObject {

        public Coordinate       coordinate;
        
        public OrdinateSupplierEvent( ClickMapViewer source, Coordinate coordinate ) {
            super( source );
            this.coordinate = coordinate;
        }
    }

}
