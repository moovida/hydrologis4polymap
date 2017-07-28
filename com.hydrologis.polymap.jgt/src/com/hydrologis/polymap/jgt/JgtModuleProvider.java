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

import org.jgrasstools.gears.modules.r.cutout.OmsCutOut;
import org.jgrasstools.hortonmachine.modules.basin.rescaleddistance.OmsRescaledDistance;
import org.jgrasstools.hortonmachine.modules.basin.topindex.OmsTopIndex;
import org.jgrasstools.hortonmachine.modules.demmanipulation.pitfiller.OmsDePitter;
import org.jgrasstools.hortonmachine.modules.demmanipulation.pitfiller.OmsPitfiller;
import org.jgrasstools.hortonmachine.modules.demmanipulation.wateroutlet.OmsExtractBasin;
import org.jgrasstools.hortonmachine.modules.geomorphology.ab.OmsAb;
import org.jgrasstools.hortonmachine.modules.geomorphology.aspect.OmsAspect;
import org.jgrasstools.hortonmachine.modules.geomorphology.curvatures.OmsCurvatures;
import org.jgrasstools.hortonmachine.modules.geomorphology.draindir.OmsDrainDir;
import org.jgrasstools.hortonmachine.modules.geomorphology.flow.OmsFlowDirections;
import org.jgrasstools.hortonmachine.modules.geomorphology.gradient.OmsGradient;
import org.jgrasstools.hortonmachine.modules.hydrogeomorphology.peakflow.OmsPeakflow;
import org.jgrasstools.hortonmachine.modules.network.extractnetwork.OmsExtractNetwork;

import com.google.common.collect.Lists;
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
                new JgtModuleInfo( OmsPitfiller.class ),
                new JgtModuleInfo( OmsDePitter.class ),
                new JgtModuleInfo( OmsFlowDirections.class ),
                new JgtModuleInfo( OmsDrainDir.class ),
                new JgtModuleInfo( OmsExtractNetwork.class ),
                new JgtModuleInfo( OmsExtractBasin.class ),
                new JgtModuleInfo( OmsCutOut.class ),
                new JgtModuleInfo( OmsGradient.class ),
                new JgtModuleInfo( OmsTopIndex.class ),
                new JgtModuleInfo( OmsCurvatures.class ),
                new JgtModuleInfo( OmsAb.class ),
                new JgtModuleInfo( OmsRescaledDistance.class ),
                new JgtModuleInfo( OmsPeakflow.class )
                //...
        );
    }

    @Override
    public Optional<ModuleInfo> findModuleInfo( Object module ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }
}
