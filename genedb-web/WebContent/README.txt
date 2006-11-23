Flash GViewer README
--------------------

The CVS contains the following directories and files at the top level:

GViewer/	Directory containing all files required to use FlashGViewer
                        on the web, doesnt contain any source 
                        code (.fla files)
index.html	Documentation on installation, configuration, etc.
sample.html	Simple web page containing Object and Embed tags that can
                        be used as a starting point for implementing the
                        FlashGViewer on a new web page.
src/		Directory containing the current development version of
                        the files, including the GViewer2.fla source file.
build.xml	Ant build script to create web distribution versions
                        of FlashGViewer
README.txt	This file
LICENSE		File describing the license under which this software is
                        release.

FlashGViewer is written using Actionscript 2 object oriented features
and uses some of the AS2 data components that are part of Flash
Professional v7. Consequently it requires FlashProfessional v7+ to be
able to edit the source file and the resultant .swf requires the
Flash Plugin version 7 in order to run correctly.

The Actionscript code for FlashGViewer lives in two main places:

1) In the GViewer2.fla file itself on the DataLoadingAction layer, Frame 1
2) In external actionscript files (.as) such as Feature.as, Chromosome.as
and drawRect.as

An ANT build.xml file is provided to facilitate the creation of a web-friendly package. Use the create-web-release target to extract all appropriate files into a fresh directory called 'FlashGViewer_forweb'
