package org.genedb.install;

import groovy.sql.Sql
import java.util.regex.Matcher

/**
* Simple class for loading organism and related
* phylogeny tables, from the  XML file at the bottom
*/
class LoadGeneDbPhylogeny {

    def db

    int index

    def orgDataSet
    def orgId

    def nodeDataSet
    def nodeId

    def dbDataSet

    def nodeOrgDataSet
    def nodeOrgId

    def dbxref
    def dbRefId

    def orgPropDataSet
    def orgPropId

    def dbId;

    def genedbMiscCvId;

    def cvTerm;

    def organismDbXRefSet;

    def nodePropDataSet;

    boolean writeBack = true;

    LoadGeneDbPhylogeny() {
        db = Sql.newInstance(
            'jdbc:postgresql://pgsrv1.internal.sanger.ac.uk/pathogens',
            'chado',
            'chado',
            'org.postgresql.Driver')

        orgDataSet = db.dataSet("organism")

        orgPropDataSet = db.dataSet("organismprop")

        dbDataSet = db.dataSet("db")

        nodeDataSet = db.dataSet("phylonode")

        nodePropDataSet = db.dataSet("phylonodeprop")

        nodeOrgDataSet = db.dataSet("phylonode_organism")

        orgPropDataSet = db.dataSet("organismprop");

        List trees =   db.rows("SELECT * from db where name='null'");
        dbId = trees[0]."db_id";

        trees = db.rows("select * from cv where name='genedb_misc'");
        genedbMiscCvId = trees[0]."cv_id";

        dbxref = db.dataSet("dbxref");
        cvTerm = db.dataSet("cvterm");

        organismDbXRefSet = db.dataSet("organism_dbxref")

    }


    // Find the maximum current value of the primary key for this table
    def getMaxIdFromPrimaryKey(String tableName) {
        def col = tableName + "_id";
        int id = db.rows("SELECT max("+col+") as "+col+" from " + tableName)[0]["${col}"];
        if (id == null) {
            return 1
        }
        return id;
    }


    void process(def inp) {
        List trees = db.rows("SELECT * from phylotree where name='org_hierarchy'");
        def tree = trees[0]."phylotree_id";
        def hierarchy = new XmlParser().parseText(inp)

        index = getMaxIdFromPrimaryKey("phylonode")
        hierarchy.children().each(
                {setLeftAndRight(it, 1)}
        )
//def rootNode = createPhylonode(, null, );
//      setLeftAndRight(hierarchy.node, 1)
//        )
        //createNode(hierarchy.node, null, tree)
        hierarchy.children().each(
                {createNode(it, null, tree)}
        )
    }

    // Walk the tree for node and organism nodes, storing values for
    // depth and left on the way down, and right and taxonIds on the way back up
    List setLeftAndRight(def node, int depth) {
        if (node.name() != 'node' && node.name() != 'organism') {
            return Collections.EMPTY_LIST;
        }
        //System.out.println("Processing node on way down, depth='"+depth+"'")
        node.attributes().put('depth', depth);
        node.attributes().put('left', ++index);
        ++depth;
        List nestedTaxons = new ArrayList();
        for (child in node.children()) {
            nestedTaxons.addAll(setLeftAndRight(child, depth))
        }
        node.attributes().put('right', ++index)

        // Move values from organism props to attributes
        if (node.name() == 'organism') {
            node.children().each({
                def key
                def value
                for (attr in it.attributes()) {
                    if (attr.key == "name") {
                        key = attr.value
                    } else {
                        value = attr.value
                    }
                }
                node.attributes().put(key, value)
            }
            )
            // Extract taxon id and add to nestedTaxons
               checkAttributeExists(node, "taxonId")
            nestedTaxons.add(node.'@taxonId')
        }
        node.attributes().put('taxonList', nestedTaxons.join(" "));
        return nestedTaxons;
    }

    // Now walk the tree creating db entries
    void createNode(def node, def parentId, def tree) {
        assert node != null
        switch (node.name()) {
            case 'node':
                def newNodeId = createPhylonode(node, parentId, tree)
                node.children().each(
                    {createNode(it, newNodeId, tree)}
                )
            break

            case 'organism':
                def newNodeId = createPhylonode(node, parentId, tree)

                Map props = node.attributes()
                String temp = props.remove("fullName")

                List sections = temp.split(" ", 2);

                createOrganism(node,sections[0],sections[1])

                if (writeBack) {
                    try {
                        nodeOrgDataSet.add(
                            phylonode_id: newNodeId,
                            organism_id:  getMaxIdFromPrimaryKey("organism")
                        )
                        } catch (Exception exp) {
                         // May be a duplicate
                           System.err.println("Problem storing phylonode_organism - duplicate?");
                        }
                }

                checkAttributeExists(node, "translationTable")
                checkAttributeExists(node, "curatorEmail")

                if (!props.containsKey("nickname") && !props.containsKey("newOrg")) {
                    println "No nickname for '"+node.'@name'+"'"
                    throw new RuntimeException("Found an old organism without a nickname")
                }
                if (props.containsKey("nickname") && props.get("nickname").size()==0) {
                    println "No nickname for '"+node.'@name'+"'"
                    throw new RuntimeException("Found an old organism without a nickname")
                }
                props.remove("newOrg")
                //props.put("fullName",temp)

                int orgId = getMaxIdFromPrimaryKey("organism")
                props.each({
                    List rows = db.rows("select * from cvterm where name='"+it.key+"'")
                    if (rows[0] == null) {
                        throw new RuntimeException("Couldn't get cvterm for '"+it.key+"'");
                    }
                    int type_id = findCvTerm(it.key)
                    String value = it.value;
                    if (writeBack) {
                         try {
                            orgPropDataSet.add(
                                organism_id: orgId,
                                type_id: type_id,
                                value: value,
                                rank: 0)
                            } catch (Exception exp) {
                         // May be a duplicate
                           System.err.println("Problem storing organism property - duplicate?");
                            }
                    }
                })


/*                if (writeBack) {
                    try {
                        orgDbXRefDataSet.add(
                            organism_id: dbId,
                            dbxref_id: description
                        )
                       } catch (Exception exp) {
                         //May be a duplicate
                           System.err.println("Problem storing db - duplicate?");
                        }
                }*/
                break
            default:
                throw new RuntimeException("Saw a node of '"+node.name()
                        +"' when expecting node or organism");
        }
    }

    // Sanity check that an expected property is present
    void checkAttributeExists(def node, String key) {
        if (!node.attributes().containsKey(key)) {
            throw new RuntimeException("No "+key+" for '"+node.'@name'+"'")
        }
    }

    // Create a set of db entries when a phylogeny node is encountered
    int createPhylonode(def node, def parent, def tree) {
        //double dist = node.parent().size();
        int left = node.attributes().left;
        int right = node.attributes().right;
        int depth = node.attributes().depth;
        //println "CreatePhylonode: name='"+node.'@name'+"', left='"+left
        //    +"', right='"+right+"' depth='"+depth+"' taxonids='"
        //    +node.attributes().taxonIds+"'";
        int ptree = tree;
        //int parentid = parent;
        String nodeName = node.'@name'
        if (writeBack) {
            try {
                nodeDataSet.add(
                    phylotree_id:        ptree,
                    parent_phylonode_id: parent,
                        left_idx:            left,
                        right_idx:           right,
                        label:               nodeName,
                        distance:            depth,
                  )
               } catch (Exception exp) {
                 // May be a duplicate
                   System.err.println("Problem storing phylonode - duplicate?");
                }
        }
        node.attributes().remove("left");
        node.attributes().remove("right");
        node.attributes().remove("depth")
        node.attributes().remove("shortName") // TODO Is this right

        Set skipAttributeNames = new HashSet();
        skipAttributeNames.add("fullName");
        skipAttributeNames.add("newOrg");
        skipAttributeNames.add("name")

          for (attr in node.attributes()) {
              // println "CreatePhylonodeProp: '"+attr.key+"'='"+attr.value+"'"
              if (skipAttributeNames.contains(attr.key)) {
                  continue
              }
              int type_id = findCvTerm(attr.key)
            String value = attr.value;
              if (writeBack) {
                try {
                      nodePropDataSet.add(
                              phylonode_id: getMaxIdFromPrimaryKey("phylonode"),
                          type_id:      type_id,
                          value:          value,
                          rank:          0
                      )
                      } catch (Exception exp) {
                    // May be a duplicate
                      System.err.println("Problem storing phylonodeprop - duplicate?");
                   }
              }
          }
        node.attributes().remove("taxonList");
        return getMaxIdFromPrimaryKey("phylonode")
    }


    int findCvTerm(String termName) {
        def rows = db.rows("select cvterm_id from cvterm where name='"+termName
                +"' and cv_id='"+genedbMiscCvId+"'")
        if (rows[0] == null) {
            throw new RuntimeException("Couldn't get cvterm for '"+termName+"'");
        }
        return rows[0]["cvterm_id"]
    }


    // Create a set of db entries when an organism node is encountered
    void createOrganism(def node,def genus,def species) {
        //println "CreateOrganism called with '"+node.'@name'+"'"
        String abb = node.'@name'
        String gen = genus;
        String sp = species;
        if (writeBack) {
            try {
                orgDataSet.add(
                    abbreviation: abb,
                    genus:        gen,
                    species:      sp,
                    common_name:  abb
                   )
            } catch (Exception exp) {
                // May be a duplicate
                System.err.println("Problem storing org - duplicate?");
            }
        }

        String accession = node.'@name'
        //Create dbxref
//        if (writeBack) {
//             try {
//                dbxref.add(
//                    db_id:    dbId,
//                    accession:        accession,
//                    version:        1,
//                    description:    "dbxref for organism"
//                )
//               } catch (Exception exp) {
//                  May be a duplicate
//                   System.err.println("Problem storing cvterm - duplicate?");
//                }

//             Create organism dbxref
//             try {
//                organismDbXRefSet.add(
//                    organism_id: getMaxIdFromPrimaryKey("organism"),
//                     dbxref_id:   getMaxIdFromPrimaryKey("dbxref")
//                )
//               } catch (Exception exp) {
//                 May be a duplicate
//                   System.err.println("Problem storing cvterm - duplicate?");
//                }
//        }
        node.attributes().remove("name");
    }


    static void main(args) {
        LoadGeneDbPhylogeny lgp = new LoadGeneDbPhylogeny()
        lgp.process(input);
        System.err.println("Done");
    }


    // The XML config file for loading the organism and phylogeny modules
    static String input = '''<?xml version="1.0" encoding="UTF-8"?>
<org-hierarchy>
    <node name="Root">
        <node name="Helminths">
            <node name="Platyhelminths">
                <organism name="Smansoni">
                    <property name="taxonId" value="6183" />
                    <property name="fullName" value="Schistosoma mansoni" />
                    <property name="nickname" value="smansoni" />
                    <property name="translationTable" value="1"/>
                    <property name="mitochondrialTranslationTable" value="9"/>
                    <property name="curatorEmail" value="mb4"/>
                    <property name="htmlFullName" value="&lt;i&gt;Schistosoma mansoni&lt;/i&gt;"/>
                    <property name="htmlShortName" value="&lt;i&gt;S. mansoni&lt;/i&gt;"/>
                    <property name="shortName" value="S. mansoni" />
                    <property name="curatorName" value="" />
                </organism>
            </node>
        </node>
        <node name="Protozoa">
            <node name="Kinetoplastids">
                <node name="Leishmania">
                    <organism name="Lbraziliensis">
                        <property name="taxonId" value="420245" />
                        <property name="fullName" value="Leishmania braziliensis MHOM/BR/75/M2904" />
                        <property name="nickname" value="lbraziliensis" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="csp"/>
                        <property name="htmlFullName" value="&lt;i&gt;Leishmania braziliensis&lt;/i&gt; MHOM/BR/75/M2904"/>
                        <property name="htmlShortName" value="&lt;i&gt;L. braziliensis&lt;/i&gt; MHOM/BR/75/M2904"/>
                        <property name="shortName" value="L. braziliensis MHOM/BR/75/M2904" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Lmajor">
                        <property name="taxonId" value="347515" />
                        <property name="fullName" value="Leishmania major strain Friedlin" />
                        <property name="nickname" value="leish" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="csp"/>
                        <property name="htmlFullName" value="&lt;i&gt;Leishmania major&lt;/i&gt; strain Friedlin"/>
                        <property name="htmlShortName" value="&lt;i&gt;L. major&lt;/i&gt; strain Friedlin"/>
                        <property name="shortName" value="L. major strain Friedlin" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Linfantum">
                        <property name="taxonId" value="5671" />
                        <property name="fullName" value="Leishmania infantum JPCM5" />
                        <property name="nickname" value="linfantum" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="csp"/>
                        <property name="htmlFullName" value="&lt;i&gt;Leishmania infantum&lt;/i&gt; JPCM5"/>
                        <property name="htmlShortName" value="&lt;i&gt;L. infantum&lt;/i&gt; JPCM5"/>
                        <property name="shortName" value="L. infantum JPCM5" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Ldonovani">
                        <property name="taxonId" value="5661" />
                        <property name="fullName" value="Leishmania donovani" />
                        <property name="nickname" value="ldonovani" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="chf@sanger.ac.uk"/>
                        <property name="htmlFullName" value="&lt;i&gt;Leishmania donovani&lt;/i&gt;"/>
                        <property name="htmlShortName" value="&lt;i&gt;L. donovani&lt;/i&gt;"/>
                        <property name="shortName" value="L. donovani" />
                        <property name="curatorName" value="Christiane Hertz-Fowler" />
                    </organism>
                    <organism name="Lmexicana">
                        <property name="taxonId" value="5665" />
                        <property name="fullName" value="Leishmania mexicana" />
                        <property name="nickname" value="lmexicana" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="chf@sanger.ac.uk"/>
                        <property name="htmlFullName" value="&lt;i&gt;Leishmania mexicana&lt;/i&gt;"/>
                        <property name="htmlShortName" value="&lt;i&gt;L. mexicana&lt;/i&gt;"/>
                        <property name="shortName" value="L. mexicana" />
                        <property name="curatorName" value="Christiane Hertz-Fowler" />
                    </organism>
                </node>
                <node name="Trypanosoma" app_www_homePage="true">
                    <organism name="Tcongolense">
                        <property name="taxonId" value="5692" />
                        <property name="fullName" value="Trypanosoma congolense" />
                        <property name="nickname" value="tcongolense" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="chf"/>
                        <property name="htmlFullName" value="&lt;i&gt;&lt;/i&gt;"/>
                        <property name="htmlShortName" value="&lt;i&gt;&lt;/i&gt;"/>
                        <property name="shortName" value="" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Tbruceibrucei427">
                        <property name="taxonId" value="5761" />
                        <property name="fullName" value="Trypanosoma brucei brucei, strain 427" />
                        <property name="nickname" value="tbrucei427" />
                        <property name="translationTable" value=""/>
                        <property name="mitochondrialTranslationTable" value=""/>
                        <property name="curatorEmail" value="chf"/>
                        <property name="htmlFullName" value=""/>
                        <property name="htmlShortName" value="&lt;i&gt;L. infantum&lt;/i&gt; JPCM5"/>
                        <property name="shortName" value="" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Tbruceibrucei927">
                        <property name="taxonId" value="185431" />
                        <property name="fullName" value="Trypanosoma brucei brucei, strain 927" />
                        <property name="nickname" value="tryp" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="chf"/>
                        <property name="htmlFullName" value=""/>
                        <property name="htmlShortName" value=""/>
                        <property name="shortName" value="" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Tbruceigambiense">
                        <property name="taxonId" value="31285" />
                        <property name="fullName" value="Trypanosoma brucei gambiense" />
                        <property name="nickname" value="tgambiense" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="chf"/>
                        <property name="htmlFullName" value=""/>
                        <property name="htmlShortName" value=""/>
                        <property name="shortName" value="" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Tvivax">
                        <property name="taxonId" value="5699" />
                        <property name="fullName" value="Trypanosoma vivax" />
                        <property name="nickname" value="tvivax" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="chf"/>
                        <property name="htmlFullName" value=""/>
                        <property name="htmlShortName" value=""/>
                        <property name="shortName" value="" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Tcruzi">
                        <property name="taxonId" value="5693" />
                        <property name="fullName" value="Trypanosoma cruzi" />
                        <property name="nickname" value="tcruzi" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="chf"/>
                        <property name="htmlFullName" value=""/>
                        <property name="htmlShortName" value=""/>
                        <property name="shortName" value="" />
                        <property name="curatorName" value="" />
                    </organism>
                </node>
                <organism name="Ddiscoideum">
                    <property name="taxonId" value="44689" />
                    <property name="fullName" value="Dictyostelium discoideum" />
                    <property name="nickname" value="dicty" />
                    <property name="translationTable" value="1"/>
                    <property name="mitochondrialTranslationTable" value="1"/>
                    <property name="curatorEmail" value="mar"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Ehistolytica">
                    <property name="taxonId" value="5759" />
                    <property name="fullName" value="Entamoeba histolytica" />
                    <property name="nickname" value="ehistolytica" />
                    <property name="translationTable" value="1"/>
                    <property name="mitochondrialTranslationTable" value="1"/>
                    <property name="curatorEmail" value="mb4"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <node name="Apicomplexa">
                    <organism name="Etenella">
                        <property name="taxonId" value="5802" />
                        <property name="fullName" value="Eimeria tenella" />
                        <property name="nickname" value="etenella" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="mar"/>
                        <property name="htmlFullName" value=""/>
                        <property name="htmlShortName" value=""/>
                        <property name="shortName" value="" />
                        <property name="curatorName" value="" />
                      </organism>
                    <organism name="Tannulata">
                        <property name="taxonId" value="5874" />
                        <property name="fullName" value="Theileria annulata" />
                        <property name="nickname" value="annulata" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="ap2"/>
                        <property name="htmlFullName" value=""/>
                        <property name="htmlShortName" value=""/>
                        <property name="shortName" value="" />
                        <property name="curatorName" value="" />
                    </organism>
                    <organism name="Ncaninum">
                        <property name="taxonId" value="29176" />
                        <property name="fullName" value="Neospora caninum" />
                        <property name="nickname" value="Ncaninum" />
                        <property name="translationTable" value="1"/>
                        <property name="mitochondrialTranslationTable" value="4"/>
                        <property name="curatorEmail" value="ar11@sanger.ac.uk"/>
                        <property name="htmlFullName" value="<i>Neospora caninum</i>"/>
                        <property name="htmlShortName" value="<i>N. caninum</i>"/>
                        <property name="shortName" value="N. caninum" />
                        <property name="curatorName" value="Adam Reid" />
                    </organism>
                    <node name="Plasmodium" app_www_homePage="true">
                        <organism name="Pfalciparum">
                            <property name="taxonId" value="5833" />
                            <property name="fullName" value="Plasmodium falciparum" />
                            <property name="nickname" value="malaria" />
                            <property name="translationTable" value="1"/>
                            <property name="mitochondrialTranslationTable" value="4"/>
                            <property name="curatorEmail" value="aeb"/>
                            <property name="htmlFullName" value=""/>
                            <property name="htmlShortName" value=""/>
                            <property name="shortName" value="" />
                            <property name="curatorName" value="" />
                        </organism>
                        <organism name="Pknowlesi">
                            <property name="taxonId" value="5850" />
                            <property name="fullName" value="Plasmodium knowlesi" />
                            <property name="nickname" value="pknowlesi" />
                            <property name="translationTable" value="1"/>
                            <property name="mitochondrialTranslationTable" value="4"/>
                            <property name="curatorEmail" value="aeb"/>
                            <property name="htmlFullName" value=""/>
                            <property name="htmlShortName" value=""/>
                            <property name="shortName" value="" />
                            <property name="curatorName" value="" />
                        </organism>
                        <organism name="Pberghei">
                            <property name="taxonId" value="5821" />
                            <property name="fullName" value="Plasmodium berghei" />
                            <property name="nickname" value="pberghei" />
                            <property name="translationTable" value="1"/>
                            <property name="mitochondrialTranslationTable" value="4"/>
                            <property name="curatorEmail" value="aeb"/>
                            <property name="htmlFullName" value=""/>
                            <property name="htmlShortName" value=""/>
                            <property name="shortName" value="" />
                            <property name="curatorName" value="" />
                        </organism>
                        <organism name="Pchabaudi">
                            <property name="taxonId" value="5825" />
                            <property name="fullName" value="Plasmodium chabaudi" />
                            <property name="nickname" value="pchabaudi" />
                            <property name="translationTable" value="1"/>
                            <property name="mitochondrialTranslationTable" value="4"/>
                            <property name="curatorEmail" value="aeb"/>
                            <property name="htmlFullName" value=""/>
                            <property name="htmlShortName" value=""/>
                            <property name="shortName" value="" />
                            <property name="curatorName" value="" />
                        </organism>
                    </node>
                </node>
            </node>
        </node>
        <node name="Fungi">
            <organism name="Scerevisiae">
                <property name="taxonId" value="4932" />
                <property name="fullName" value="Saccharomyces cerevisiae" />
                <property name="nickname" value="cerevisiae" />
                <property name="translationTable" value="1"/>
                <property name="mitochondrialTranslationTable" value="3"/>
                <property name="curatorEmail" value="val"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Spombe">
                <property name="taxonId" value="4896" />
                <property name="fullName" value="Schizosaccharomyces pombe" />
                <property name="nickname" value="pombe" />
                <property name="translationTable" value="1"/>
                <property name="mitochondrialTranslationTable" value="4"/>
                <property name="curatorEmail" value="val"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Afumigatus">
                <property name="taxonId" value="5085" />
                <property name="fullName" value="Aspergillus fumigatus" />
                <property name="nickname" value="asp" />
                <property name="translationTable" value="1"/>
                <property name="mitochondrialTranslationTable" value="4"/>
                <property name="curatorEmail" value="mb4"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Cdubliniensis">
                <property name="taxonId" value="42374" />
                <property name="fullName" value="Candida dubliniensis" />
                <property name="nickname" value="cdubliniensis" />
                <property name="translationTable" value="12"/>
                <property name="mitochondrialTranslationTable" value="3"/>
                <property name="curatorEmail" value="mb4"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
        </node>
        <node name="bacteria">
            <organism name="Bmarinus">
                <property name="taxonId" value="97084" />
                <property name="fullName" value="Bacteriovorax marinus" />
                <property name="newOrg" value="true" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="sdb"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Bfragilis_NCTC9343">
                <property name="taxonId" value="272559" />
                <property name="fullName" value="Bacteroides fragilis NCTC 9343" />
                <property name="nickname" value="bfragilis" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="amct"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Cjejuni">
                <property name="taxonId" value="197" />
                <property name="fullName" value="Campylobacter jejuni" />
                <property name="nickname" value="cjejuni" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="sdb"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Ctrachomatis">
                <property name="taxonId" value="813" />
                <property name="fullName" value="Chlamydia trachomatis" />
                <property name="nickname" value="testing" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="nrt"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Cabortus">
                <property name="taxonId" value="83555" />
                <property name="fullName" value="Chlamydophila abortus" />
                <property name="nickname" value="cabortus" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="nrt"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Cmichiganensis">
                <property name="taxonId" value="28447" />
                <property name="fullName" value="Clavibacter michiganensis" />
                <property name="newOrg" value="true" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="sdb"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Cdiphtheriae">
                <property name="taxonId" value="1717" />
                <property name="fullName" value="Corynebacterium diphtheriae" />
                <property name="nickname" value="diphtheria" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="amct"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Ecarotovora">
                <property name="taxonId" value="218491" />
                <property name="fullName" value="Pectobacterium atrosepticum SCRI1043" />
                <property name="nickname" value="ecarot" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="ms5"/>
                <property name="htmlFullName" value="<i>Pectobacterium atrosepticum</i> SCRI1043"/>
                <property name="htmlShortName" value="<i>P. atrosepticum</i>"/>
                <property name="shortName" value="P. atrosepticum" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Ecoli_042">
                <property name="taxonId" value="216592" />
                <property name="fullName" value="Escherichia coli 042" />
                <property name="nickname" value="ecoli" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="net"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Pfluorescens">
                <property name="taxonId" value="294" />
                <property name="fullName" value="Pseudomonas fluorescens" />
                <property name="newOrg" value="true" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="amct"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Rleguminosarum">
                <property name="taxonId" value="384" />
                <property name="fullName" value="Rhizobium leguminosarum" />
                <property name="nickname" value="rleguminosarum" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="lcc"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <organism name="Scoelicolor">
                <property name="taxonId" value="1902" />
                <property name="fullName" value="Streptomyces coelicolor" />
                <property name="nickname" value="Scoelicolor" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="mh3@sanger.ac.uk"/>
                <property name="htmlFullName" value="<i>Streptomyces coelicolor</i>"/>
                <property name="htmlShortName" value="<i>S. coelicolor</i>'"/>
                <property name="shortName" value="S. coelicolor" />
                <property name="curatorName" value="Matt Holden" />
            </organism>
            <node name="Bordetella">
                <organism name="Bavium_197N">
                    <property name="taxonId" value="360910" />
                    <property name="fullName" value="Bordetella avium 197N" />
                    <property name="nickname" value="bavium" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="ms5"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Bbronchiseptica">
                    <property name="taxonId" value="518" />
                    <property name="fullName" value="Bordetella bronchiseptica" />
                    <property name="nickname" value="bronchi" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="ms5"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Bparapertussis">
                    <property name="taxonId" value="519" />
                    <property name="fullName" value="Bordetella parapertussis" />
                    <property name="nickname" value="parapert" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="ms5"/>
                    <property name="htmlFullName" value="&lt;i&gt;Bordetella parapertussis&lt;/i&gt;"/>
                    <property name="htmlShortName" value="&lt;i&gt;B. parapertussis&lt;/i&gt;"/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Bpertussis">
                    <property name="taxonId" value="520" />
                    <property name="fullName" value="Bordetella pertussis" />
                    <property name="nickname" value="pert" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="ms5"/>
                    <property name="htmlFullName" value="&lt;i&gt;Bordetella pertussis&lt;/i&gt;"/>
                    <property name="htmlShortName" value="&lt;i&gt;B. pertussis&lt;/i&gt;"/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
            </node>
            <node name="Burkholderia">
                <organism name="Bcenocepacia">
                    <property name="taxonId" value="95486" />
                    <property name="fullName" value="Burkholderia cenocepacia" />
                    <property name="nickname" value="bcenocepacia" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Bpseudomallei">
                    <property name="taxonId" value="28450" />
                    <property name="fullName" value="Burkholderia pseudomallei" />
                    <property name="nickname" value="bpseudomallei" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
            </node>
            <node name="Clostridium">
                <organism name="Cbotulinum">
                    <property name="taxonId" value="1491" />
                    <property name="fullName" value="Clostridium botulinum" />
                    <property name="nickname" value="cbotulinum" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="ms5"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Cdifficile">
                    <property name="taxonId" value="1496" />
                    <property name="fullName" value="Clostridium difficile" />
                    <property name="nickname" value="cdifficile" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="ms5"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
            </node>
            <node name="Mycobacterium">
                <organism name="Mbovis">
                    <property name="taxonId" value="1765" />
                    <property name="fullName" value="Mycobacterium bovis" />
                    <property name="nickname" value="mbovis" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="parkhill"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Mleprae">
                    <property name="taxonId" value="1769" />
                    <property name="fullName" value="Mycobacterium leprae" />
                    <property name="nickname" value="mleprae" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="parkhill"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Mmarinum">
                    <property name="taxonId" value="1781" />
                    <property name="fullName" value="Mycobacterium marinum" />
                    <property name="nickname" value="mmarinum" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="parkhill"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Mtuberculosis">
                    <property name="taxonId" value="1773" />
                    <property name="fullName" value="Mycobacterium tuberculosis" />
                    <property name="nickname" value="mtuberculosis" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="nrt"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
            </node>
            <node name="Neisseria">
                <organism name="Nmeningitidis">
                    <property name="taxonId" value="487" />
                    <property name="fullName" value="Neisseria meningitidis" />
                    <property name="nickname" value="nmeningitidis" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="sdb"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Nmeningitidis_C">
                    <property name="taxonId" value="272831" />
                    <property name="fullName" value="Neisseria meningitidis C" />
                    <property name="nickname" value="nmeningitidisC" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="sdb"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
            </node>
            <node name="Salmonella" app_www_homePage="true">
                <organism name="Sbongori">
                    <property name="taxonId" value="54736" />
                    <property name="fullName" value="Salmonella bongori" />
                    <property name="newOrg" value="true" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="nrt"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Senteritidis_PT4">
                    <property name="taxonId" value="592" />
                    <property name="fullName" value="Salmonella enteritidis PT4" />
                    <property name="newOrg" value="true" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="nrt"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Styphi">
                    <property name="taxonId" value="90370" />
                    <property name="fullName" value="Salmonella enterica subsp. enterica serovar Typhi" />
                    <property name="newOrg" value="true" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="nrt"/>
                    <property name="htmlFullName" value="<i>Salmonella enterica</i> subsp. enterica serovar Typhi'"/>
                    <property name="htmlShortName" value="<i>S. Typhi</i>'"/>
                    <property name="shortName" value="S. Typhi" />
                    <property name="curatorName" value="Nick Thomson" />
                </organism>
                <organism name="Styphimurium">
                    <property name="taxonId" value="568708" />
                    <property name="fullName" value="Salmonella enterica subsp. enterica serovar Typhimurium str. D23580" />
                    <property name="newOrg" value="true" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="nrt"/>
                    <property name="htmlFullName" value="<i>Salmonella enterica</i> subsp. enterica serovar Typhimurium str. D23580'"/>
                    <property name="htmlShortName" value="<i>S. Typhimurium</i>'"/>
                    <property name="shortName" value="S. Typhimurium" />
                    <property name="curatorName" value="Nick Thomson" />
                </organism>
            </node>
            <organism name="Smarcescens">
                    <property name="taxonId" value="615" />
                    <property name="fullName" value="Serratia marcescens" />
                    <property name="newOrg" value="true" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="nrt"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
            </organism>
            <node name="Staphylococcus">
                <organism name="Saureus_MRSA252">
                    <property name="taxonId" value="282458" />
                    <property name="fullName" value="Staphylococcus aureus (MRSA252)" />
                    <property name="nickname" value="Saureus_MRSA252" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3"/>
                    <property name="htmlFullName" value="Staphylococcus aureus MRSA252"/>
                    <property name="htmlShortName" value="S. aureus MRSA252"/>
                    <property name="shortName" value="S. aureus" />
                    <property name="curatorName" value="Matt Holden" />
                </organism>
                <organism name="Saureus_MSSA476">
                    <property name="taxonId" value="282459" />
                    <property name="fullName" value="Staphylococcus aureus (MSSA476)" />
                    <property name="nickname" value="Saureus_MSSA476" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3"/>
                    <property name="htmlFullName" value="<i>Staphylococcus aureus</i> MSSA476"/>
                    <property name="htmlShortName" value="<i>S. aureus</i> MSSA476"/>
                    <property name="shortName" value="S. aureus MSSA476" />
                    <property name="curatorName" value="mh3" />
                </organism>
                <organism name="Saureus_EMRSA15">
                    <property name="fullName" value="Staphylococcus aureus (EMRSA15)" />
                    <property name="nickname" value="Saureus_EMRSA15" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3"/>
                    <property name="htmlFullName" value="<i>Staphylococcus aureus</i> EMRSA15"/>
                    <property name="htmlShortName" value="<i>S. aureus</i> EMRSA15"/>
                    <property name="shortName" value="S. aureus MRSA15" />
                    <property name="curatorName" value="mh3" />
                </organism>
                <organism name="Saureus_TW20">
                    <property name="fullName" value="Staphylococcus aureus (TW20)" />
                    <property name="nickname" value="Saureus_TW20" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3@sanger.ac.uk"/>
                    <property name="htmlFullName" value="<i>Staphylococcus aureus</i> TW20"/>
                    <property name="htmlShortName" value="<i>S. aureus</i> TW20"/>
                    <property name="shortName" value="S. aureus TW20" />
                    <property name="curatorName" value="Matt Holden" />
                </organism>
                <organism name="Saureus_LGA251">
                    <property name="fullName" value="Staphylococcus aureus (LGA251)" />
                    <property name="nickname" value="Saureus_LGA251" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3@sanger.ac.uk"/>
                    <property name="htmlFullName" value="<i>Staphylococcus aureus</i> LGA251"/>
                    <property name="htmlShortName" value="<i>S. aureus</i> LGA251"/>
                    <property name="shortName" value="S. aureus LGA251" />
                    <property name="curatorName" value="Matt Holden" />
                </organism>
            </node>
            <organism name="Smaltophilia">
                <property name="taxonId" value="40324" />
                <property name="fullName" value="Stenotrophomonas maltophilia" />
                <property name="newOrg" value="true" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="lcc"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <node name="Streptococcus">
                <organism name="Spyogenes">
                    <property name="taxonId" value="1314" />
                    <property name="fullName" value="Streptococcus pyogenes" />
                    <property name="nickname" value="spyogenes" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Suberis">
                    <property name="taxonId" value="1349" />
                    <property name="fullName" value="Streptococcus uberis" />
                    <property name="nickname" value="suberis" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="mh3"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
            </node>
            <organism name="Twhipplei">
                <property name="taxonId" value="2039" />
                <property name="fullName" value="Tropheryma whipplei" />
                <property name="nickname" value="twhipplei" />
                <property name="translationTable" value="11"/>
                <property name="curatorEmail" value="sdb"/>
                <property name="htmlFullName" value=""/>
                <property name="htmlShortName" value=""/>
                <property name="shortName" value="" />
                <property name="curatorName" value="" />
            </organism>
            <node name="Yersinia">
                <organism name="Ypestis">
                    <property name="taxonId" value="632" />
                    <property name="fullName" value="Yersinia pestis" />
                    <property name="nickname" value="ypsetis" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="nrt"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
                <organism name="Yenterocolitica">
                    <property name="taxonId" value="630" />
                    <property name="fullName" value="Yersinia enterocolitica" />
                    <property name="newOrg" value="true" />
                    <property name="translationTable" value="11"/>
                    <property name="curatorEmail" value="nrt"/>
                    <property name="htmlFullName" value=""/>
                    <property name="htmlShortName" value=""/>
                    <property name="shortName" value="" />
                    <property name="curatorName" value="" />
                </organism>
            </node>
        </node>
        <node name="Parasite vectors">
            <organism name="Gmorsitans">
                <property name="taxonId" value="7394" />
                <property name="fullName" value="Glossina morsitans" />
                <property name="nickname" value="Gmorsitans" />
                <property name="translationTable" value="1"/>
                <property name="mitochondrialTranslationTable" value="9"/>
                <property name="htmlFullName" value="&lt;i&gt;Glossina morsitans&lt;/i&gt;"/>
                <property name="htmlShortName" value="&lt;i&gt;G. morsitans&lt;/i&gt;"/>
                <property name="shortName" value="G. morsitans" />
                <property name="curatorEmail" value="mb4"/>
                <property name="curatorName" value="Matt Berriman" />
            </organism>
            <organism name="Llongipalpis">
                <property name="taxonId" value="7200" />
                <property name="fullName" value="Lutzomyia longipalpis" />
                <property name="nickname" value="Llongipalpis" />
                <property name="translationTable" value="1"/>
                <property name="mitochondrialTranslationTable" value="9"/>
                <property name="htmlFullName" value="&lt;i&gt;Lutzomyia longipalpis&lt;/i&gt;"/>
                <property name="htmlShortName" value="&lt;i&gt;L. longipalpis&lt;/i&gt;"/>
                <property name="shortName" value="L. longipalpis" />
                <property name="curatorEmail" value="mb4"/>
                <property name="curatorName" value="Matt Berriman" />
            </organism>
        </node>
    </node>
</org-hierarchy>
''';

}
