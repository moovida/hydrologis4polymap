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

import java.util.List;
import java.util.Optional;

import org.jgrasstools.hortonmachine.modules.geomorphology.aspect.OmsAspect;

import com.google.common.collect.Lists;
import com.hydrologis.polymap.jgt.aoi.OmsAreaOfInterestGridCoverage;

import org.polymap.core.data.process.ModuleInfo;
import org.polymap.core.data.process.ModuleProvider;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class JgtModuleProvider
        implements ModuleProvider {

    @Override
    public List<ModuleInfo> createModuleInfos() {
        return Lists.newArrayList(
                // Milestone "Go Live" modules
                new JgtModuleInfo( OmsAspect.class ),
                new JgtModuleInfo( OmsAreaOfInterestGridCoverage.class )
                //...
        );
    }

    @Override
    public Optional<ModuleInfo> findModuleInfo( Object module ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
