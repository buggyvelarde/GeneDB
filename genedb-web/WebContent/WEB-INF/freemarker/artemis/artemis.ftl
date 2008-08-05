<?xml version="1.0" encoding="UTF-8"?>
<jnlp
        spec="1.0+"
        codebase="http://www.sanger.ac.uk/Software/Artemis/v10/database">
         <information>
           <title>Artemis</title>
           <vendor>Sanger Institute</vendor>
           <homepage href="http://www.sanger.ac.uk/Software/Artemis/"/>
           <description>Artemis</description>
           <description kind="short">DNA sequence viewer and annotation tool.
           </description>
           <offline-allowed/>
         </information>
         <security>
           <all-permissions/>
         </security>
         <resources>
           <j2se version="1.4+ 1.4.2" initial-heap-size="32m" max-heap-size="200m"/>
             <jar href="sartemis.jar"/>
           <property name="com.apple.mrj.application.apple.menu.about.name" value="Artemis" />
           <property name="artemis.environment" value="UNIX" />
           <property name="j2ssh" value="" />
           <property name="ibatis" value="" />
           <property name="chado" value="193.62.203.5:5432/snapshot?genedb_ro" />
           <property name="jdbc.drivers" value="org.postgresql.Driver" />
           <property name="apple.laf.useScreenMenuBar" value="true" />
           <property name="offset" value="${offset}" />
           <property name="read_only" value="" />
         </resources>
         <application-desc main-class="uk.ac.sanger.artemis.components.ArtemisMain">
          <argument>${argument}</argument>
         </application-desc>
</jnlp>
