/*
 * polymap.org 
 * Copyright (C) 2017, the @authors. All rights reserved.
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
package com.hydrologis.polymap.jgt;

import org.jgrasstools.gears.libs.modules.JGTModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Label;
import oms3.annotations.Name;
import oms3.annotations.Range;

/**
 * 
 *
 * @author Falko Bräutigam
 */
@Description("A background processing test.")
@Label("Background processing test")
@Name("Background processing test")
public class ATestModule 
        extends JGTModel {

    private static final Log log = LogFactory.getLog( ATestModule.class );

    @In
    @Description("An integer input (1 <= value <= 100).")
    @Range(min = 1, max = 100)
    public int input1 = 1;


    @Execute
    public void process() throws Exception {
        pm.beginTask( "Test", 100 );
        for (int i=0; i<10 && !pm.isCanceled(); i++) {
            Thread.sleep( 1000 );
            pm.worked( 1 );
        }
        pm.done();
    }

}
