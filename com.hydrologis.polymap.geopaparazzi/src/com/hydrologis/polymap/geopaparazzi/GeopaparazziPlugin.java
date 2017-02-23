/*
 * Copyright (C) 2015, the @authors. All rights reserved.
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

import org.osgi.framework.BundleContext;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.rhei.batik.app.SvgImageRegistryHelper;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class GeopaparazziPlugin
        extends AbstractUIPlugin {

    public static final String        ID = "com.hydrologis.polymap.geopaparazzi";

    private static GeopaparazziPlugin instance;


    public static GeopaparazziPlugin instance() {
        return instance;
    }


    /**
     * Shortcut for <code>instance().images</code>.
     */
    public static SvgImageRegistryHelper images() {
        return instance().images;
    }

    // instance *******************************************

    public SvgImageRegistryHelper images = new SvgImageRegistryHelper( this );


    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
    }


    public void stop( BundleContext context ) throws Exception {
        super.stop( context );
        instance = null;
    }

}
