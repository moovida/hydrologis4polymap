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

import java.util.Optional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.google.common.base.Joiner;

import org.polymap.core.data.process.FieldInfo;
import oms3.annotations.In;
import oms3.annotations.Out;

/**
 * 
 * 
 * @author Falko Bräutigam
 */
public class JgtFieldInfo<T>
        extends JgtBaseInfo
        implements FieldInfo<T> {

    private Field           field;

    protected JgtFieldInfo( Field field ) {
        this.field = field;
        field.setAccessible( true );
    }

    @Override
    public boolean isInput() {
        return annotation( In.class ).isPresent();
    }

    public boolean isOutput() {
        return annotation( Out.class ).isPresent();
    }

    @Override
    public Class<T> type() {
        return (Class<T>)field.getType();
    }
    
    @Override
    public T getValue( Object module ) {
        try {
        return (T)field.get( module );
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public JgtFieldInfo setValue( Object module, T value ) {
        try {
            field.set( module, value );
            return this;
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public boolean isAssignableFrom( Class<?> targetType ) {
        return field.getType().isAssignableFrom( targetType );
    }
    
    
    @Override
    public <A extends Annotation> Optional<A> annotation( Class<A> atype ) {
        return Optional.ofNullable( field.getAnnotation( atype ) );
    }

    
    @Override
    public String toString() {
        return Joiner.on( "" ).join( "FieldInfo[", 
                "name=", name(), ", " ,
                "description=", description().orElse( "<empty>" ),
                "]" );
    }
}
