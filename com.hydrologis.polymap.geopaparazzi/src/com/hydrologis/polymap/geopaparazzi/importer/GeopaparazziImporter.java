/*
 * polymap.org Copyright (C) 2015, Falko Bräutigam. All rights reserved.
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
package com.hydrologis.polymap.geopaparazzi.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import java.io.File;
import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureSource;
import org.jgrasstools.dbs.spatialite.jgt.SqliteDb;
import org.jgrasstools.gears.io.geopaparazzi.GeopaparazziUtilities;
import org.jgrasstools.gears.utils.StringUtilities;
import org.jgrasstools.gears.utils.files.FileUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.hydrologis.polymap.geopaparazzi.GeopaparazziPlugin;
import com.hydrologis.polymap.geopaparazzi.Messages;
import com.hydrologis.polymap.geopaparazzi.catalog.GPDataStore;
import com.hydrologis.polymap.geopaparazzi.catalog.GPServiceInfo;
import com.hydrologis.polymap.geopaparazzi.catalog.GPServiceResolver;
import com.hydrologis.polymap.geopaparazzi.utilities.GPUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.catalog.IUpdateableMetadataCatalog.Updater;
import org.polymap.core.catalog.resolve.IMetadataResourceResolver;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.DefaultStyle;
import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.UIUtils;

import org.polymap.rhei.batik.BatikApplication;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.Mandatory;
import org.polymap.rhei.batik.Scope;
import org.polymap.rhei.batik.app.SvgImageRegistryHelper;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.table.FeatureCollectionContentProvider;

import org.polymap.p4.P4Plugin;
import org.polymap.p4.catalog.AllResolver;
import org.polymap.p4.data.importer.ContextIn;
import org.polymap.p4.data.importer.Importer;
import org.polymap.p4.data.importer.ImporterPrompt;
import org.polymap.p4.data.importer.ImporterPrompt.PromptUIBuilder;
import org.polymap.p4.data.importer.ImporterSite;
import org.polymap.p4.layer.NewLayerOperation;
import org.polymap.p4.project.ProjectRepository;

/**
 * 
 *
 * @author hydrologis
 */
@SuppressWarnings( "deprecation" )
public class GeopaparazziImporter
        implements Importer {

    private static final Log       log        = LogFactory.getLog( GeopaparazziImporter.class );

    private static final IMessages i18nPrompt = Messages.forPrefix( "ImporterPrompt" );

    private static final IMessages i18n       = Messages.forPrefix( "ImporterGpap" );

    private ImporterSite           site;

    @ContextIn
    protected File                 geopapDatabaseFile;

    private SqliteDb               db;

    private GPDataStore            ds;

    private Exception              exception;

    private Composite              tableParent;

    private String                 promptSelection;


    @Override
    public ImporterSite site() {
        return site;
    }


    @Override
    @SuppressWarnings( "hiding" )
    public void init( ImporterSite site, IProgressMonitor monitor ) {
        this.site = site;

        try {
            site.summary.set( i18n.get( "summary", FilenameUtils.getBaseName( geopapDatabaseFile.getName() ) ) );
            site.description.set( i18n.get( "description" ) );
            site.terminal.set( true );
            site.icon.set( GeopaparazziPlugin.images().svgImage( "gpap.svg", SvgImageRegistryHelper.NORMAL24 ) );

            promptSelection = geopapDatabaseFile.getName();
        }
        catch (Exception e) {
            // FIXME ???
            log.info( e );
        }
    }


    @Override
    public void createPrompts( IProgressMonitor monitor ) throws Exception {

        // site.newPrompt( "schemaName" ).summary.put( i18n.get( "summary" )
        // ).description.put( i18n.get( "description" ) ).value.put( promptSelection
        // ).severity.put( Severity.REQUIRED ).ok.put( canCopyFile( false )
        // ).extendedUI.put( new TextUIBuilder() );
    }


    @Override
    public void verify( IProgressMonitor monitor ) {
        try {
            if (db != null) {
                db.close();
            }
            db = new SqliteDb();
            db.open( geopapDatabaseFile.getAbsolutePath() );
            log.info( "Tables: " + db.getTables( false ) );
            ds = new GPDataStore( db );
            log.info( "Resources: " + ds.getNames() );

            site.ok.set( true );
            exception = null;
        }
        catch (Exception e) {
            log.info( "", e );
            site.ok.set( false );
            exception = e;
        }
    }


    @Override
    public void createResultViewer( Composite parent, IPanelToolkit tk ) {
        if (exception != null) {
            tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: " + exception.getMessage() );
        }
        else {
            try {
                parent.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
                Combo combo = new Combo( parent, SWT.READ_ONLY | SWT.DROP_DOWN );
                FormDataFactory.on( combo ).fill().noBottom();
                combo.setFont( UIUtils.bold( combo.getFont() ) );
                combo.setItems( ds.getTypeNames() );
                combo.addSelectionListener( UIUtils.selectionListener( ev -> {
                    try {
                        String typeName = ds.getTypeNames()[combo.getSelectionIndex()];
                        createFeatureTable( tableParent, typeName );
                    }
                    catch (Exception e) {
                        tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: "
                                + exception.getMessage() );
                        site.ok.set( false );
                        exception = e;
                    }
                } ) );
                combo.select( 0 );

                tableParent = tk.createComposite( parent );
                FormDataFactory.on( tableParent ).fill().top( combo );

                String typeName = ds.getTypeNames()[0];
                createFeatureTable( tableParent, typeName );
            }
            catch (Exception e) {
                tk.createFlowText( parent, "\nUnable to read the data.\n\n**Reason**: " + exception.getMessage() );
                site.ok.set( false );
                exception = e;
            }
        }
    }


    protected void createFeatureTable( Composite parent, String typeName ) throws IOException {
        UIUtils.disposeChildren( parent );
        SimpleFeatureSource fs = ds.getFeatureSource( typeName );
        GPFeatureTableViewer table = new GPFeatureTableViewer( parent, fs.getSchema() );
        table.setContentProvider( new FeatureCollectionContentProvider() );
        table.setInput( fs.getFeatures() );
        parent.layout();
    }


    @Override
    public void execute( IProgressMonitor monitor ) throws Exception {
        safeCopyFile();

        importCatalogEntry( monitor );
    }


    private boolean canCopyFile( boolean alsoCopy ) throws IOException {
        File gpapProjectsFolder = GPUtilities.getGeopaparazziProjectsFolder();
        String name = geopapDatabaseFile.getName();
        File newFile = new File( gpapProjectsFolder, name );
        if (newFile.exists()) {
            return false;
        }
        if (alsoCopy)
            Files.copy( geopapDatabaseFile, newFile );
        return true;
    }


    private void safeCopyFile() throws IOException {
        File gpapProjectsFolder = GPUtilities.getGeopaparazziProjectsFolder();

        File[] geopaparazziFiles = GeopaparazziUtilities.getGeopaparazziFiles( gpapProjectsFolder );
        List<String> namesNoExt = new ArrayList<>();
        for (File file : geopaparazziFiles) {
            String name = FileUtilities.getNameWithoutExtention( file );
            namesNoExt.add( name );
        }

        String name = FileUtilities.getNameWithoutExtention( geopapDatabaseFile );
        String safeName = StringUtilities.checkSameName( namesNoExt, name );

        File newFile = new File( gpapProjectsFolder, safeName + ".gpap" );
        if (newFile.exists()) {
            throw new IOException( newFile + " already exists!" );
        }
        Files.copy( geopapDatabaseFile, newFile );
        geopapDatabaseFile = newFile;

        // reconnect database
        try {
            db = new SqliteDb();
            db.open( geopapDatabaseFile.getAbsolutePath() );
            log.info( "Tables: " + db.getTables( false ) );
            ds = new GPDataStore( db );
        }
        catch (Exception e) {
            log.error( "Error closing and reopening on new db: " + geopapDatabaseFile, e );
        }

    }


    /**
     * Directly link a data source represented by an {@link IServiceInfo} and
     * connected by an {@link IMetadataResourceResolver} to the local catalog. No
     * data is actually transfered.
     * 
     * @throws Exception
     */
    protected void importCatalogEntry( IProgressMonitor monitor ) throws Exception {
        // just an example, wherever this comes from
        AtomicReference<GPServiceInfo> serviceInfo = new AtomicReference();

        // create catalog entry
        try (Updater update = P4Plugin.localCatalog().prepareUpdate()) {
            update.newEntry( metadata -> {
                try {
                    String databasePath = db.getDatabasePath();
                    String title = FileUtilities.getNameWithoutExtention( new File( databasePath ) );
                    String projectInfo = "Geopaparazzi Project";
                    projectInfo = GeopaparazziUtilities.getProjectInfo( db.getConnection(), false );

                    metadata.setTitle( title );
                    metadata.setDescription( projectInfo );
                    metadata.setType( "Geopaparazzi Sqlite Project Database" );
                    metadata.setFormats( Sets.newHashSet( "..." ) );

                    // actual connection to the data source; just an example
                    metadata.setConnectionParams( GPServiceResolver.createParams( geopapDatabaseFile.getAbsolutePath() ) );

                    // resolve the new data source, testing the connection params
                    // and choose resource to create a new layer for

                    serviceInfo.set( (GPServiceInfo)AllResolver.instance().resolve( metadata, monitor ) );
                }
                catch (Exception e) {
                    throw new RuntimeException( "Unable to resolve imported data source.", e );
                }
            } );
            update.commit();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

        // create new layer(s) for resource(s)
        for (IResourceInfo res : serviceInfo.get().getResources( monitor )) {
            String name = res.getName();

            FeatureStyle featureStyle4Layer = GPUtilities.getFeatureStyle4Layer( name, db.getConnection() );
            if (featureStyle4Layer == null) {
                featureStyle4Layer = P4Plugin.styleRepo().newFeatureStyle();
                DefaultStyle.createAllStyles( featureStyle4Layer );
            }

            BatikApplication.instance().getContext().propagate( this );
            NewLayerOperation op = new NewLayerOperation().label.put( name ).res.put( res ).featureStyle.put( featureStyle4Layer ).uow.put( ProjectRepository.unitOfWork() ).map.put( map.get() );

            OperationSupport.instance().execute( op, false, false );
        }
    }

    /** Only required for {@link #importCatalogEntry()}. */
    @Mandatory
    @Scope( P4Plugin.Scope )
    protected Context<IMap> map;


    class TextUIBuilder
            implements PromptUIBuilder {

        @Override
        public void submit( ImporterPrompt prompt ) {
            try {
                prompt.ok.set( canCopyFile( false ) );
                prompt.value.put( promptSelection );
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void createContents( ImporterPrompt prompt, Composite parent, IPanelToolkit tk ) {
            parent.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
            Label desc = tk.createLabel( parent, prompt.description.get(), SWT.WRAP );
            Label l = tk.createLabel( parent, "", SWT.WRAP );

            Text text = tk.createText( parent, promptSelection, SWT.BORDER );
            text.addModifyListener( ev -> {
                promptSelection = ((Text)ev.getSource()).getText();
                checkName( prompt, l );
            } );
            text.forceFocus();

            FormDataFactory.on( desc ).fill().noBottom().width( DEFAULT_WIDTH ).control();
            FormDataFactory.on( text ).fill().top( desc, 10 ).noBottom().width( DEFAULT_WIDTH );
            FormDataFactory.on( l ).fill().top( text ).width( DEFAULT_WIDTH );

            checkName( prompt, l );
        }


        protected void checkName( ImporterPrompt prompt, Label l ) {
            try {
                if (StringUtils.isBlank( promptSelection )) {
                    l.setText( "No name set" );
                    // prompt.ok.set( false );
                }
                else if (!canCopyFile( false )) {
                    l.setText( "Can't use existing file name." );
                    // prompt.ok.set( false );
                }
                else {
                    l.setText( "File name is ok." );
                    // prompt.ok.set( true );
                }
            }
            catch (IOException e) {
                // XXX Auto-generated catch block
                l.setText( "An error occurred: " + e.getLocalizedMessage() );
            }
        }
    }
}
