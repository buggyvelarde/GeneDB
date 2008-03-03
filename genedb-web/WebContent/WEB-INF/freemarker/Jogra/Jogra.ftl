<?xml version="1.0" encoding="UTF-8"?>
<jnlp spec="1.0+" codebase="http://developer.genedb.org/test/Jogra/">
  <information>
    <title>Jogra</title>
    <vendor>Wellcome Trust Sanger Institute</vendor> 
    <homepage href="http://developer.genedb.org/"/>
    <description>Jogra</description>
    <description kind="short">Demo GeneDB technology preview</description>
    <offline-allowed/>
  </information>
  <security>
    <all-permissions/>
  </security>
  <resources>
    <j2se version="1.6+" />
    <jar href="Jogra.jar"/>
    <property name="com.apple.mrj.application.apple.menu.about.name" value="Jogra" />
    <property name="apple.laf.useScreenMenuBar" value="true" />
  </resources>
  <application-desc main-class="org.genedb.jogra.drawing.Jogra">
    <argument>${command}</argument>
    <#list args as x>
    <argument>${x}</argument>
    </#list>  
  </application-desc>
</jnlp>
