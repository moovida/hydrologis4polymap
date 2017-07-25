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

import org.jgrasstools.gears.libs.monitor.IJGTProgressMonitor;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Provides a {@link IJGTProgressMonitor} facade for an {@link IProgressMonitor} 
 *
 * @author Falko Bräutigam
 */
public class JgtProgressMonitorAdapter
        implements IJGTProgressMonitor {

    private IProgressMonitor    delegate;

    
    public JgtProgressMonitorAdapter( IProgressMonitor delegate ) {
        this.delegate = delegate;
    }

    @Override
    public void beginTask( String name, int totalWork ) {
        delegate.beginTask( name, totalWork );
    }

    @Override
    public void message( String message ) {
        // XXX is this ok?
        delegate.subTask( message );
    }

    @Override
    public void errorMessage( String message ) {
        // XXX how to implement?
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void exceptionThrown( String message ) {
        // XXX how to implement?
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void done() {
        delegate.done();
    }

    @Override
    public void worked( int work ) {
        delegate.worked( work );
    }

    @Override
    public void internalWorked( double work ) {
        delegate.internalWorked( work );
    }

    @Override
    public boolean isCanceled() {
        return delegate.isCanceled();
    }

    @Override
    public void setCanceled( boolean value ) {
        delegate.setCanceled( value );
    }

    @Override
    public void setTaskName( String name ) {
        delegate.setTaskName( name );
    }

    @Override
    public void subTask( String name ) {
        delegate.subTask( name );
    }

    @Override
    public <T> T adapt( Class<T> adaptee ) {
        // XXX how to implement?
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public void onModuleExit() {
        // XXX how to implement?
        throw new RuntimeException( "not yet implemented." );
    }
}
