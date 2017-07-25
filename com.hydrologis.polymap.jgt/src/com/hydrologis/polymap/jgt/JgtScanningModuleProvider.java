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

import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.replace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.libs.modules.JGTConstants;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.osgi.framework.Bundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.process.ModuleInfo;
import org.polymap.core.data.process.ModuleProvider;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.Timer;
import oms3.annotations.Execute;
import oms3.annotations.UI;

/**
 * Provides all modules found by scanning the classspath.
 *
 * @deprecated Just saving the classpath scan code. See {@link JgtModuleProvider}.
 * @author Falko Bräutigam
 */
public class JgtScanningModuleProvider
        implements ModuleProvider {

    private static final Log log = LogFactory.getLog( JgtScanningModuleProvider.class );
    
    private Lazy<List<ModuleInfo>>  executables = new CachedLazyInit();
    
    
    @Override
    public List<ModuleInfo> createModuleInfos() {
        return executables.get( () -> {
            logMemory();            
            List<ModuleInfo> result = new ArrayList( 256 );
            Bundle bundle = JgtPlugin.instance().getBundle();
            Predicate<JgtModuleInfo> filter = hasAnnotatedMethod( Execute.class )
                    .and( isVisibleOmsModule() )
                    .and( hasInputField( GridCoverage2D.class ) );
            String version = "0.8.1";
            result.addAll( scanJar( bundle.getEntry( "/lib/jgt-hortonmachine-" + version + ".jar" ), filter ) );
            result.addAll( scanJar( bundle.getEntry( "/lib/jgt-jgrassgears-" + version + ".jar" ), filter ) );
            result.addAll( scanJar( bundle.getEntry( "/lib/jgt-lesto-" + version + ".jar" ), filter ) );
            logMemory();
            return result;
        });
    }

    
    @Override
    public Optional<ModuleInfo> findModuleInfo( Object module ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    protected void logMemory() {
        System.gc();
        long total = Runtime.getRuntime().totalMemory();
        long free = Runtime.getRuntime().freeMemory();            
        log.info( "Memory used: " + (total-free) / (1024*1024) + "MB");
    }

    /**
     * 
     *
     * @param jarUrl {@link URL} of a {@link Bundle} entry.
     * @return
     */
    protected List<ModuleInfo> scanJar( URL jarUrl, Predicate<JgtModuleInfo> filters ) {
        try (
            JarInputStream jar = new JarInputStream( jarUrl.openStream() );
        ){
            log.info( "Scanning: " + jarUrl.getFile() + " ..." );
            Timer timer = new Timer();
            List<ModuleInfo> result = new ArrayList( 256 );
            ClassLoader cl = getClass().getClassLoader();
            for (JarEntry entry=jar.getNextJarEntry(); entry!=null; entry=jar.getNextJarEntry()) {
                if (entry.getName().endsWith( ".class" )) {
                    String classname = replace( removeEnd( entry.getName(), ".class" ), "/", "." );
                    try {
                        ModuleInfo candidate = new JgtModuleInfo( (Class<? extends JGTModel>)cl.loadClass( classname ) );
                        if (filters.test( (JgtModuleInfo)candidate )) {
                            log.info( "    entry: " + classname );
                            result.add( candidate );                            
                        }
                    }
                    catch (Throwable e) {
                        log.warn( "    failed: " + classname + " (" + e.getMessage() + ")" );
                    }
                }
            }
            log.info( "Scan of " + jarUrl.getFile() + " took " + timer.elapsedTime() + "ms" );
            return result;
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }

    
    protected Predicate<JgtModuleInfo> isVisibleOmsModule() {
        return (JgtModuleInfo candidate) -> {
            boolean isOmsAndVisible = candidate.type().getSimpleName().startsWith( "Oms" );
            UI uiHints = candidate.type().getAnnotation(UI.class);
            if (uiHints != null) {
                String uiHintStr = uiHints.value();
                if (uiHintStr.contains(JGTConstants.HIDE_UI_HINT)) {
                    isOmsAndVisible = false;
                }
            }
            return isOmsAndVisible;
        };
    }

    protected Predicate<JgtModuleInfo> hasAnnotatedMethod( Class<? extends Annotation> type ) {
        return (JgtModuleInfo candidate) -> {
            for (Method m : candidate.type().getMethods()) {
                if (m.getAnnotation( type ) != null) {
                    return true;
                }
            }
            return false;
        };
    }

    
    protected Predicate<JgtModuleInfo> hasAnnotation( Class<? extends Annotation> type ) {
        return (JgtModuleInfo candidate) -> {
            return candidate.annotation( type ).isPresent();
        };
    }
    
    
    protected Predicate<JgtModuleInfo> hasInputField( Class<?> type ) {
        return (JgtModuleInfo candidate) -> {
            return candidate.inputFields().stream()
                    .filter( f -> f.isInput() && f.type().isAssignableFrom( type ) )
                    .findAny().isPresent();
        };
    }
    
    
//    public static void main( String[] args ) throws Exception {
//        Modules.instance.get().all();
//    }
    

}
