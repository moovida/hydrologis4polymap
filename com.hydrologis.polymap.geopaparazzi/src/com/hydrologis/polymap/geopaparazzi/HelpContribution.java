/* 
 * polymap.org
 * Copyright (C) 2018, the @authors. All rights reserved.
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
package com.hydrologis.polymap.geopaparazzi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.contribution.IContributionSite;
import org.polymap.rhei.batik.contribution.IDashboardContribution;
import org.polymap.rhei.batik.dashboard.Dashboard;
import org.polymap.rhei.batik.help.HelpPanel;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.batik.toolkit.md.MdToolkit;


/**
 * 
 *
 * @author Falko Bräutigam
 */
public class HelpContribution
        implements IDashboardContribution {

    private static final Log log = LogFactory.getLog( HelpContribution.class );


    public void fillDashboard( IContributionSite site, Dashboard dashboard ) {
        if (site.tagsContain( HelpPanel.DASHBOARD_ID )) {
            dashboard.addDashlet( new HelpDashlet( (MdToolkit)site.toolkit() )
                    .addConstraint( new PriorityConstraint( 1000 ) ) );
        }
    }
    
    
    protected class HelpDashlet
            extends org.polymap.rhei.batik.help.HelpDashlet {

        private MdToolkit       tk;

        public HelpDashlet( MdToolkit toolkit ) {
            this.tk = toolkit;
        }

        @Override
        public void createContents( Composite parent ) {
            site().title.set( "Geopaparazzi" );
            
//            HttpServletRequest request = RWT.getRequest();
//            for (String header : Collections.list( request.getHeaderNames() )) {
//                log.info( "Header: " + header + " = " + request.getHeader( header ) );
//            }
            
            tk.createFlowText( parent, "In order to connect the Geopaparazzi App with this project "
                    + "use the following **Cloud Project Server:**\n\n"
                    + "    " +  "mapzone.io/projects/<<username>>/<<projectname>>" );
        }
    }
    
}
