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

import java.lang.annotation.Annotation;

import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.FluentIterable;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.process.FieldInfo;
import org.polymap.core.data.process.ModuleInfo;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;

import oms3.ComponentAccess;
import oms3.annotations.Execute;
import oms3.annotations.Finalize;
import oms3.annotations.Initialize;

/**
 * 
 *
 * @author Falko Bräutigam
 */
public class JgtModuleInfo
        extends JgtBaseInfo
        implements ModuleInfo {

    // instance *******************************************
    
    private Class<?>                    type;

    /** All fields of this module. */
    private Lazy<FluentIterable<FieldInfo>> fields = new PlainLazyInit( () ->
            FluentIterable.of( type.getFields() )
                    .filter( field -> !IJGTProgressMonitor.class.isAssignableFrom( field.getType() ) )
                    .transform( f -> new JgtFieldInfo( f ) ) );
    
    
    protected JgtModuleInfo( Class<?> type ) {
        this.type = type;
    }
    
    @Override
    public Class<?> type() {
        return type;
    }
    
    @Override
    public <A extends Annotation> Optional<A> annotation( Class<A> atype ) {
        return Optional.ofNullable( type.getAnnotation( atype ) );
    }
    
    @Override
    public String label() {
        return StringUtils.capitalize( type.getSimpleName().substring( 3 ) );
    }
    
    @Override
    public List<FieldInfo> inputFields() {
        return fields.get().filter( field -> field.isInput() ).toList();
    }

    @Override
    public List<FieldInfo> outputFields() {
        return fields.get().filter( field -> ((JgtFieldInfo)field).isOutput() ).toList();
    }

    @Override
    public JGTModel createModuleInstance() {
        try {
            return (JGTModel)type.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void execute( Object module, IProgressMonitor monitor ) {
        assert module instanceof JGTModel;
        ((JGTModel)module).pm = new JgtProgressMonitorAdapter( monitor );
        ComponentAccess.callAnnotated( module, Initialize.class, true );
        ComponentAccess.callAnnotated( module, Execute.class, false );
        ComponentAccess.callAnnotated( module, Finalize.class, true );
    }

}
