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

import org.apache.commons.lang3.StringUtils;

import org.polymap.core.data.process.BaseInfo;
import oms3.annotations.Description;
import oms3.annotations.Name;

/**
 * Base info of modules and fields.
 *
 * @author Falko Bräutigam
 */
public abstract class JgtBaseInfo 
        implements BaseInfo {

    @Override
    public String name() {
        return annotation( Name.class ).map( a -> a.value() ).get();
    }

    @Override
    public String label() {
        return StringUtils.capitalize( name() );
       // return annotation( Label.class ).map( a -> a.value() ).get();
    }

    @Override
    public Optional<String> description() {
        return annotation( Description.class ).map( a -> a.value() );
    }
    
    protected abstract <A extends Annotation> Optional<A> annotation( Class<A> atype );
    
}
