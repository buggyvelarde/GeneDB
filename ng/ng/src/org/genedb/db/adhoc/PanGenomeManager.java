package org.genedb.db.adhoc;

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import java.util.Map;
import java.util.Set;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.*;



/**
 * Insert a leaf phylonode (corresponding to an organism that already exists in the organism table)
 * into phylotree 1. This code has very few external dependencies: it needs only the PostgreSQL
 * JDBC driver to run.
 * <p>
 * Usage is of the form
 * <pre>java PhylonodeManager &lt;jdbc URL&gt; &lt;database username&gt; [&lt;parent phylonode label&gt; &lt;organism common name&gt;]</pre>
 * if the optional arguments are omitted, it just prints a textual representation of the phylotree to standard output. Otherwise it
 * inserts the new node, then prints the resulting tree.
 *
 * You can run it as follows (on one line for ease of copypasta):
 *
 * java -classpath lib/postgresql-8.3-603.jdbc4.jar:ant-build/dist/genedb-access.jar org.genedb.db.adhoc.PhylonodeManager jdbc:postgresql://localhost:5432/pathogens
 *
 * @author rn2
 *
 */
public class PanGenomeManager {


    private enum PanGeneStatisticalType {CORE,MISC,UNIQUE};
    private enum PanGeneBioType {CODING,NONCODING,PSEUDO,MIXED};

    private Connection conn;
    private static final Logger logger = Logger.getLogger(PanGenomeManager.class);


    private PanGenomeManager(Connection conn) {
        this.conn = conn;
    }

    private List <Set <Long>> getConnectedComponents(int organism_ids[]) throws Exception{

            String organism_ids_list=new String("");
            for(int i=0;i<organism_ids.length;i++){
                organism_ids_list=(i<organism_ids.length-1)?organism_ids_list+""+organism_ids[i]+",":organism_ids_list+""+organism_ids[i];
            }
            PreparedStatement st = conn.prepareStatement("select * from organism where organism_id in ("+organism_ids_list+")");
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String organism_name  = rs.getString("common_name");
                logger.info("organism to be searched "+organism_name);
            }
            String orthologues_query=new String("select  mRNA_gene_1.object_id as gene1_id,mRNA_gene_2.object_id as gene2_id"
                    +" from"
                    +" ("
                    +" select fr1.subject_id,fr2.subject_id as object_id"
                    +" from"
                    +" feature pmf"
                    +" join"
                    +" feature_relationship fr1 on (pmf.type_id=436 and pmf.uniquename not like 'PAN%' and pmf.feature_id=fr1.object_id and fr1.type_id=78)"
                    +" join"
                    +" feature_relationship fr2 on (fr1.object_id=fr2.object_id and fr1.subject_id!=fr2.subject_id)"
                    +" join "
                    +" feature ppf1 on (fr1.subject_id=ppf1.feature_id and ppf1.organism_id in ("+organism_ids_list+") )"
                    +" join "
                    +" feature ppf2 on (fr2.subject_id=ppf2.feature_id and ppf2.organism_id in ("+organism_ids_list+") )"
                    +" union"
                    +" select fr.subject_id,fr.object_id"
                    +" from"
                    +" feature ppf1"
                    +" join"
                    +" feature_relationship fr on (ppf1.feature_id=fr.subject_id and fr.type_id=78)"
                    +" join"
                    +" feature ppf2 on (fr.object_id=ppf2.feature_id "
                    +" and ppf1.organism_id in ("+organism_ids_list+") and ppf2.organism_id in ("+organism_ids_list+") )"
                    +" union "
                    +" select fr.object_id as subject_id,fr.subject_id as object_id"
                    +" from"
                    +" feature ppf1"
                    +" join"
                    +" feature_relationship fr on (ppf1.feature_id=fr.subject_id and fr.type_id=78)"
                    +" join"
                    +" feature ppf2 on (fr.object_id=ppf2.feature_id "
                    +" and ppf1.organism_id in ("+organism_ids_list+") and ppf2.organism_id in ("+organism_ids_list+") )"
                    +" )"
                    +" as polypeptide_relationship"
                    +" join"
                    +" feature_relationship polypeptide_mRNA_1 on (polypeptide_relationship.subject_id=polypeptide_mRNA_1.subject_id and polypeptide_mRNA_1.type_id=69)"
                    +" join"
                    +" feature_relationship polypeptide_mRNA_2 on (polypeptide_relationship.object_id=polypeptide_mRNA_2.subject_id and polypeptide_mRNA_2.type_id=69)"
                    +" join"
                    +" feature_relationship mRNA_gene_1 on (polypeptide_mRNA_1.object_id=mRNA_gene_1.subject_id and mRNA_gene_1.type_id=42)"
                    +" join"
                    +" feature_relationship mRNA_gene_2 on (polypeptide_mRNA_2.object_id=mRNA_gene_2.subject_id and mRNA_gene_2.type_id=42)");

            String allgenesQuery= "select feature_id from feature where (type_id=792 or type_id=423) and organism_id in ("+organism_ids_list+")";
            st = conn.prepareStatement(allgenesQuery);
            logger.info("sql= "+allgenesQuery);
            rs = st.executeQuery();
            UndirectedGraph<Long, DefaultEdge> geneGraph = new SimpleGraph<Long, DefaultEdge>(DefaultEdge.class);
            long vertexcount=0;
            logger.info("Adding all vertices to the graph ");
            while (rs.next()) {
                Long geneID=rs.getLong("feature_id");
                geneGraph.addVertex(geneID);
                vertexcount++;
            }
            logger.info(vertexcount+" vertices added to the graph ");

            logger.info("sql= "+orthologues_query);
            st = conn.prepareStatement(orthologues_query);
            rs = st.executeQuery();

            logger.info("Adding all edges to the graph ");
            long edgecount=0;
            while (rs.next()) {
                Long gene1=rs.getLong("gene1_id");
                Long gene2=rs.getLong("gene2_id");
                logger.info("edge = "+rs.getLong("gene1_id")+"->"+rs.getLong("gene2_id"));
                geneGraph.addEdge(gene1, gene2);
                edgecount++;
                
            }
            logger.info(edgecount+" edges added to the graph ");

            ConnectivityInspector ci = new ConnectivityInspector(geneGraph);
            List <Set <Long>> list =ci.connectedSets();
            return list;

    }

    private int createPanGenome(int organism_ids[],int vorganism_id) throws Exception{

            int no_of_organisms=organism_ids.length;
            int max_component_length=0;

            List <Set <Long>> list=getConnectedComponents(organism_ids);
            for ( Set<Long> componentSet : list){
                if(componentSet.size()>max_component_length) max_component_length=componentSet.size();
            }

            Long componentID= new Long(0);

            String qStr="(";
            for (int i=0;i<max_component_length-1;i++) {qStr+="?,";}
            qStr+="?)";

            String gene_residues_query=new String("select"
            +"  featureloc.feature_id,"
            +"  case"
            +"  when featureloc.strand=1 then"
            +"  substring(srcfeature.residues from featureloc.fmin+1 for (featureloc.fmax-featureloc.fmin) )"
            +"  else"
            +"  translate(substring(srcfeature.residues from featureloc.fmin+1 for (featureloc.fmax-featureloc.fmin) ),'acgt','tgca')"
            +"  end as residues,"
            +"  featureloc.strand,"
            +"  srcfeature.organism_id"        
            +"  from featureloc"
            +"  join feature srcfeature on (featureloc.srcfeature_id=srcfeature.feature_id)"
            +"  where featureloc.feature_id in "+qStr);

            PreparedStatement st = conn.prepareStatement(gene_residues_query);

            // build virtual chromosome
            logger.info("gene_residues_query= "+gene_residues_query);

            StringBuilder vchromosome = new StringBuilder(); // creates empty builder, capacity 16

            // empty chromosome
            Long chr_feature_id=insertVirtualChromosome(vchromosome,vorganism_id);
            long fmin=0;
            // for each connected component
            // create a virtual gene
            // make orthopara links b/w the virtual gene and the genes in the connected component

            int count=0;
            for ( Set<Long> componentSet : list){
                    //if(count==100) {
                    //    break;
                    //}
                    count++;
                    int gene_param_index=1;
                    for (Long gene_id_param : componentSet){
                        st.setLong(gene_param_index,gene_id_param);
                        gene_param_index++;
                    }
                    int i=gene_param_index;
                    while(i<=max_component_length){
                        st.setNull(i,Types.INTEGER);
                        i++;
                    }
                    ResultSet rs=st.executeQuery();

                    HashMap<Integer,Long> organism_gene_map= new HashMap<Integer,Long>();
                    HashMap<Integer,String> organism_genesequence_map= new HashMap<Integer,String>();

                    int ortholog_count=0;
                    while (rs.next()) {
                            Long gene_id=rs.getLong("feature_id");
                            String residues=rs.getString("residues");
                            Integer gene_strand=rs.getInt("strand");
                            Integer organism_id=rs.getInt("organism_id");
                            if(!organism_gene_map.containsKey(organism_id)){
                                organism_gene_map.put(organism_id,gene_id);
                                organism_genesequence_map.put(organism_id,residues);
                                ortholog_count++;
                            }
                            else{
                                logger.info("filtered gene "+gene_id+" of organism"+organism_id);
                                organism_gene_map.remove(organism_id);
                                organism_genesequence_map.remove(organism_id);
                                ortholog_count--;
                            }
                           
                            //logger.info(gene_id+"->"+residues);
                    }
                    ArrayList<String> seqArr = new ArrayList<String>(organism_genesequence_map.values());
                    String consensus_seq=consensusDNASequence(seqArr);
                    // check after applying connected components filter atleast 1 gene remains
                    if(consensus_seq.length()==0) {continue;}

                    vchromosome.append(consensus_seq);
                    vchromosome.append("nnnnnnnnnn");
                    HashMap<String,Long> vgene_map= new HashMap<String,Long>();
                    PanGeneStatisticalType geneStatisticalType;
                    if(ortholog_count==1){
                        geneStatisticalType=PanGeneStatisticalType.UNIQUE;
                    }
                    else if(ortholog_count==no_of_organisms){
                        geneStatisticalType=PanGeneStatisticalType.CORE;
                    }
                    else{
                        geneStatisticalType=PanGeneStatisticalType.MISC;
                    }

                    vgene_map=createVirtualGene(componentID,vorganism_id);
                    //logger.info("Successfully created virtual gene "+vgene_map.get("gene"));
                    long fmax=vchromosome.length()-10;
                    featureLocVirtualGene(vgene_map,vorganism_id,fmin,fmax);
                    fmin=vchromosome.length();
                    int rank=0;
                    PanGeneBioType consensusBiotype=PanGeneBioType.CODING;
                    boolean rna_flag=false;
                    boolean pseudo_flag=false;
                    for ( Long gene_id : organism_gene_map.values()){
                        PanGeneBioType geneBioType=getGeneBioType(gene_id);
                        consensusBiotype=geneBioType;
                        switch(geneBioType){
                            case NONCODING:
                                rna_flag=true;
                                break;
                            case PSEUDO:
                                pseudo_flag=true;
                                break;
                        }
                        createOrthoParalink(vgene_map.get("gene"),gene_id,rank,geneBioType);
                        //logger.info("ortho para link created b/w gene "+vgene_map.get("gene")+"and "+gene_id+ "rank="+rank);
                        rank++;
                    }
                    if(rna_flag && pseudo_flag){consensusBiotype=PanGeneBioType.MIXED;}
                    updateVirtalGeneType(vgene_map.get("gene"),consensusBiotype,geneStatisticalType);
                    logger.info("inserted pan-gene no "+componentID);
                    componentID++;
            }

            // now update the chromosome
            updateVirtualChromosome(chr_feature_id,vchromosome);
            logger.info("chromosome updated ");
            insertorganismMaxGeneNumber(vorganism_id, componentID-1);
            logger.info("max gene inserted");
        
    return 0;
    }

    private PanGeneBioType getGeneBioType(Long gene_id) throws Exception{
    PanGeneBioType geneBioType=PanGeneBioType.CODING;
    
    PreparedStatement st;
    ResultSet rs;
    String gene_bioTypeSQL=""
    +" select cvterm.name from feature_relationship rna_gene"
    +" join feature rna_feature on (rna_gene.subject_id=rna_feature.feature_id)"
    +" join feature gene_feature on (rna_gene.object_id=gene_feature.feature_id)"
    +" join cvterm on (cvterm.cvterm_id=rna_feature.type_id)"
    +" where rna_gene.type_id=42  and gene_feature.feature_id=?";

    st = conn.prepareStatement(gene_bioTypeSQL);
    st.setLong(1,gene_id);
    rs = st.executeQuery();
    rs.next();
    String rnatype=rs.getString("name");
    if(rnatype.equals("mRNA")){
       geneBioType=PanGeneBioType.CODING;
    }
    else if(rnatype.equals("pseudogenic_transcript")){
        geneBioType=PanGeneBioType.PSEUDO;
    }
    else{
        geneBioType=PanGeneBioType.NONCODING;
    }

    return geneBioType;
    }

    private String consensusDNASequence(ArrayList<String> residuesArr){
     String consensus_seq=new String();
     boolean length_mismatch=false;
     long len=residuesArr.get(0).length();
     for ( String seq : residuesArr){
         if(seq.length()>len) {
             len=seq.length();
             consensus_seq=seq;
             length_mismatch=true;
         }
         else if(seq.length()<len){
             length_mismatch=true;
         }

     }

     // if there is a mismatch return the longest sequence ( typically a MSA has to be peformed but its not possible due to time complexity)
     if(length_mismatch){
     return consensus_seq;
     }
     // no mismatch proceed and return the sequence with degeneracy/ambiguity  codes
     for(int i=0;i<len;i++){
            char result;
            String chars=new String("");
            for(int j=0;j<residuesArr.size();j++){
                residuesArr.get(j).charAt(i);
                if(!chars.contains(residuesArr.get(j).charAt(i)+"")) chars=chars+residuesArr.get(j).charAt(i);
            }
            if(chars.equals("a")){
              result='a';
            }
            else if(chars.equals("c")){
              result='c';
            }
            else if(chars.equals("g")){
              result='g';
            }
            else if(chars.equals("t")){
              result='t';
            }
            else if(chars.contains("a")&&chars.contains("c")&& !chars.contains("g")&& !chars.contains("t")){
              result='m';
            }
            else if(chars.contains("a")&&!chars.contains("c")&&chars.contains("g")&&!chars.contains("t")){
              result='r';
            }
            else if(chars.contains("a")&&!chars.contains("c")&&!chars.contains("g")&&chars.contains("t")){
              result='w';
            }
            else if(!chars.contains("a")&&chars.contains("c")&&chars.contains("g")&&!chars.contains("t")){
              result='s';
            }
            else if(!chars.contains("a")&&chars.contains("c")&&!chars.contains("g")&&chars.contains("t")){
              result='y';
            }
            else if(!chars.contains("a")&&!chars.contains("c")&&chars.contains("g")&&chars.contains("t")){
              result='k';
            }
            else if(chars.contains("a")&&chars.contains("c")&&chars.contains("g")&&!chars.contains("t")){
              result='v';
            }
            else if(chars.contains("a")&&chars.contains("c")&&!chars.contains("g")&&chars.contains("t")){
              result='h';
            }
            else if(chars.contains("a")&&!chars.contains("c")&&chars.contains("g")&&chars.contains("t")){
              result='d';
            }
            else if(!chars.contains("a")&&chars.contains("c")&&chars.contains("g")&&chars.contains("t")){
              result='b';
            }
            else{
            result='n';
            }
     consensus_seq=consensus_seq+result;
     }
     return consensus_seq;
    }


    private void deletePanGenome(String genus,String species,int organism_ids[]) throws Exception{
      PreparedStatement st;
      ResultSet rs;
      String qStr="";
      for(int i=0;i<organism_ids.length-1;i++) qStr+=organism_ids[i]+",";
      qStr+=organism_ids[organism_ids.length-1];
      String query_vorganismsql = "SELECT organism_id from organismprop where type_id=1706 and value=?";
      st = conn.prepareStatement(query_vorganismsql);
      st.setString(1,qStr);
      rs = st.executeQuery();
      rs.next();
      Long organism_id=rs.getLong("organism_id");

      String delete_vorganism_sql = "DELETE from organism where organism_id=?";
      st = conn.prepareStatement(delete_vorganism_sql);
      st.setLong(1,organism_id);
      st.executeUpdate();

      logger.info("deleted organism "+organism_id);

    }

    private void updatePanGenome(String genus,String species) throws Exception{
      PreparedStatement st;
      ResultSet rs;


      String toplevelfeature_organism_sql =
      "select feature.feature_id,organism.organism_id from"
      +" organism"
      +" join"
      +" feature on (feature.organism_id=organism.organism_id and feature.type_id=427)"
      +" where organism.genus=? and organism.species=?";
      st = conn.prepareStatement(toplevelfeature_organism_sql);
      st.setString(1,genus);
      st.setString(2,species);
      rs = st.executeQuery();
      rs.next();

      Long vchromosome_id=rs.getLong("feature_id");
      Long vorganism_id=rs.getLong("organism_id");

      String query_pangene_links_sql =
      "SELECT vfeature.match_feature_id,vfeature.vgene_id,vfeature.rgene_id"
     +" FROM feature"
     +" JOIN"
     +" (select cast(ltrim(split_part(uniquename,'->',1),'PANGENELINK') as integer) as vgene_id,"
     +" cast(split_part(uniquename,'->',2) as integer) as rgene_id,"
     +" feature_id as match_feature_id from feature where type_id=436 and uniquename like 'PANGENELINK%') as vfeature"
     +" on (vfeature.vgene_id=feature.feature_id)"
     +" where feature.organism_id=(select organism_id from organism where abbreviation=?)"
     +" order by vgene_id";

      st = conn.prepareStatement(query_pangene_links_sql,ResultSet.TYPE_FORWARD_ONLY,
                                     ResultSet.CONCUR_READ_ONLY);
      st.setString(1,genus.substring(0,1)+species);
      rs = st.executeQuery();

      Map <Long,Set <Long>> pangenemap=new HashMap <Long,Set <Long>>();

      long prev_vgene_id=0;
      Set<Long> gene_set=null;
      while (rs.next()) {
          Long rgene_id=rs.getLong("rgene_id");
          Long vgene_id=rs.getLong("vgene_id");

          if(rs.isFirst()){
                gene_set=new HashSet<Long>();
                gene_set.add(rgene_id);
              // if this the only set close the new set
              if(rs.isLast()){
                pangenemap.put(vgene_id, gene_set);
              }
          }
          // for sets {2,3,....n}
          else{
              if(prev_vgene_id!=vgene_id){
                  // close the previous set and open a new set
                  pangenemap.put(prev_vgene_id, gene_set);
                  gene_set=new HashSet<Long>();
              }
              // add the gene to the new set
              gene_set.add(rgene_id);
              // close the new set
              if(rs.isLast()){
                pangenemap.put(vgene_id, gene_set);
              }
          }
          prev_vgene_id=vgene_id;

      }

      for(Long vgene_id : pangenemap.keySet()){
         logger.info(vgene_id+"-->"+pangenemap.get(vgene_id));
      }


      String query_vorganismsql = "SELECT value from organismprop where type_id=1706 and organism_id=?";
      st = conn.prepareStatement(query_vorganismsql);
      st.setLong(1,vorganism_id);
      rs = st.executeQuery();
      rs.next();
      String organismidslist=rs.getString("value");
      String organismidslistStr []=organismidslist.split(",");
      int organism_ids[]=new int[organismidslistStr.length];
      for(int l=0;l<organismidslistStr.length;l++) organism_ids[l]=Integer.parseInt(organismidslistStr[l]);

      List <Set <Long>> componentList=getConnectedComponents(organism_ids);
      Map <Long,Set <Long>> u_pangenemap=new HashMap <Long,Set <Long>>();
      // copy list to map
      long comp_id=0;
      for (Set compSet:componentList){
        u_pangenemap.put(comp_id,compSet);
        comp_id++;
      }

      /*Set<Long> hst=new HashSet<Long>();
      hst.add(new Long(5552342));
      hst.add(new Long(19007700));
      hst.add(new Long(18572533));
      u_pangenemap.put(new Long(0),hst);
      hst=new HashSet<Long>();
      hst.add(new Long(5552318));
      u_pangenemap.put(new Long(1),hst);
      hst=new HashSet<Long>();
      hst.add(new Long(18572543));
      hst.add(new Long(19007712));
      hst.add(new Long(5552353));
      u_pangenemap.put(new Long(2),hst);
      hst=new HashSet<Long>();
      hst.add(new Long(5552333));
      hst.add(new Long(18572521));
      hst.add(new Long(19007692));
      u_pangenemap.put(new Long(3),hst);
      hst=new HashSet<Long>();
      hst.add(new Long(18572513));
      hst.add(new Long( 19007684));
      hst.add(new Long(5552322));
      u_pangenemap.put(new Long(4),hst);
      hst=new HashSet<Long>();
      hst.add(new Long(19007704));
      hst.add(new Long(18572539));
      hst.add(new Long(5552347));
      u_pangenemap.put(new Long(5),hst);
      hst=new HashSet<Long>();
      hst.add(new Long(5552367));
      hst.add(new Long(19007724));
      hst.add(new Long(18572556));
      u_pangenemap.put(new Long(6),hst);
      hst=new HashSet<Long>();
      hst.add(new Long(18572525));
      hst.add(new Long(19007696));
      hst.add(new Long(5552338));
      u_pangenemap.put(new Long(7),hst);
      hst=new HashSet<Long>();
      hst.add(new Long(19007688));
      hst.add(new Long(5552328));
      hst.add(new Long(18572517));
      u_pangenemap.put(new Long(8),hst);
      */
      

      Set <Long>  tobedeleted_vgene_set=new HashSet <Long>();
      Set <Long>  donothing_vgene_set=new HashSet <Long>();
      Map <Long,Set <Long>>  tobeupdated_add_vgene_map=new HashMap <Long,Set <Long>>();
      Map <Long,Set <Long>>  tobeupdated_delete_vgene_map=new HashMap <Long,Set <Long>>();
      Map <Long,Set <Long>>  tobeupdated_vgene_map=new HashMap <Long,Set <Long>>();
      Map <Long,Set <Long>>  tobeinserted_uvgene_map=new HashMap <Long,Set <Long>>();

      int max_component_length=0;

      for ( Long u_vgene_id : u_pangenemap.keySet()){

          Set<Long> u_geneset=u_pangenemap.get(u_vgene_id);
          if(u_geneset.size()>max_component_length){
              max_component_length=u_geneset.size();
          }
          ArrayList<List<Long>> op_vgeneids_rgeneids_list=get_listof_matching_sets(u_geneset,pangenemap);
          if(op_vgeneids_rgeneids_list.size()==0){
              // no matches
              //logger.info(u_vgene_id+"->"+u_geneset+" NO MATCHES");
              tobeinserted_uvgene_map.put(u_vgene_id,u_geneset);
          }
          else if(op_vgeneids_rgeneids_list.size()>1){
              // multiple matches
              //logger.info(u_vgene_id+"->"+u_geneset+" MULTIMATCH "+op_vgeneids_rgeneids_list );
              for (List<Long> op_vgeneids_rgeneids : op_vgeneids_rgeneids_list){
                   tobedeleted_vgene_set.add(op_vgeneids_rgeneids.get(1));
              }
              tobeinserted_uvgene_map.put(u_vgene_id,u_geneset);
          }
          else{
              // exactly one match
              //logger.info(u_vgene_id+"->"+u_geneset+" SINGLEMATCH "+op_vgeneids_rgeneids_list );
              Long vgene_id=op_vgeneids_rgeneids_list.get(0).get(1);
              Set <Long> rgene_id_set=new HashSet(op_vgeneids_rgeneids_list.get(0).subList(2,op_vgeneids_rgeneids_list.get(0).size()));
              // add these elements
              if(op_vgeneids_rgeneids_list.get(0).get(0)==1){
                tobeupdated_add_vgene_map.put(vgene_id,rgene_id_set);
                tobeupdated_vgene_map.put(vgene_id,u_geneset);
              }
              // remove these elements
              else if(op_vgeneids_rgeneids_list.get(0).get(0)==2){
                 tobeupdated_delete_vgene_map.put(vgene_id,rgene_id_set);
                 tobeupdated_vgene_map.put(vgene_id,u_geneset);
              }
              else{
                  donothing_vgene_set.add(vgene_id);
              }
           }
      }

      Set<Long> temp_set=new HashSet<Long>();
      temp_set.addAll(pangenemap.keySet());
      temp_set.removeAll(donothing_vgene_set);
      temp_set.removeAll(tobeupdated_add_vgene_map.keySet());
      temp_set.removeAll(tobeupdated_delete_vgene_map.keySet());
      tobedeleted_vgene_set.addAll(temp_set);
      
      logger.info("DO NOTHING to these vgenes"+donothing_vgene_set);
      logger.info("UPDATE these vgenes(add)"+tobeupdated_add_vgene_map.keySet());
      logger.info("UPDATE these vgenes(del)"+tobeupdated_delete_vgene_map.keySet());
      logger.info("DELETE these vgenes"+tobedeleted_vgene_set);
      logger.info("INSERT these vgenes"+tobeinserted_uvgene_map.keySet());

      /*if(true){
          return;
      }*/

      // rebuild chromosome
      StringBuilder vchromosome = new StringBuilder(); // creates empty builder, capacity 16
      long fmin=0;
      long fmax=0;

      for( Long donothing_vgene_id : donothing_vgene_set){
       String query_get_featureloc_sql =
        "select substring(srcfeature.residues from featureloc.fmin+1 for (featureloc.fmax-featureloc.fmin)) as sequence from featureloc"
        +" join feature on (featureloc.feature_id=feature.feature_id)"
        +" join feature srcfeature on (featureloc.srcfeature_id=srcfeature.feature_id)"
        +" where feature.type_id=792 and featureloc.feature_id=?";
        st = conn.prepareStatement(query_get_featureloc_sql,ResultSet.TYPE_FORWARD_ONLY,
                       ResultSet.CONCUR_READ_ONLY);
        st.setLong(1,donothing_vgene_id);
        rs = st.executeQuery();
        rs.next();
        String sequence=rs.getString("sequence");
        fmin=vchromosome.length();
        vchromosome.append(sequence);
        vchromosome.append("nnnnnnnnnn");
        fmax=vchromosome.length()-10;
        logger.info(donothing_vgene_id+"("+fmin+","+fmax+")");
        featureReLocVirtualGene(donothing_vgene_id,fmin,fmax);

      }


      // updating genes
      // refeatureloc
      // add/delete links
       int no_of_organisms=organism_ids.length;

       String qStr="(";
       for (int i=0;i<max_component_length-1;i++) {qStr+="?,";}
       qStr+="?)";

       String gene_residues_query=new String("select"
       +"  featureloc.feature_id,"
       +"  case"
       +"  when featureloc.strand=1 then"
       +"  substring(srcfeature.residues from featureloc.fmin+1 for (featureloc.fmax-featureloc.fmin) )"
       +"  else"
       +"  translate(substring(srcfeature.residues from featureloc.fmin+1 for (featureloc.fmax-featureloc.fmin) ),'acgt','tgca')"
       +"  end as residues,"
       +"  featureloc.strand,"
       +"  srcfeature.organism_id"        
       +"  from featureloc"
       +"  join feature srcfeature on (featureloc.srcfeature_id=srcfeature.feature_id)"
       +"  where featureloc.feature_id in "+qStr);

       st = conn.prepareStatement(gene_residues_query);
       for( Long update_add_vgene_id : tobeupdated_add_vgene_map.keySet()){
                    int gene_param_index=1;
                    for (Long gene_id_param : tobeupdated_vgene_map.get(update_add_vgene_id)){
                        st.setLong(gene_param_index,gene_id_param);
                        gene_param_index++;
                    }
                    int i=gene_param_index;
                    while(i<=max_component_length){
                        st.setNull(i,Types.INTEGER);
                        i++;
                    }
                    rs=st.executeQuery();

                    HashMap<Integer,Long> organism_gene_map= new HashMap<Integer,Long>();
                    HashMap<Integer,String> organism_genesequence_map= new HashMap<Integer,String>();

                    int ortholog_count=0;
                    while (rs.next()) {
                            Long gene_id=rs.getLong("feature_id");
                            String residues=rs.getString("residues");
                            Integer gene_strand=rs.getInt("strand");
                            Integer organism_id=rs.getInt("organism_id");
                            if(!organism_gene_map.containsKey(organism_id)){
                                organism_gene_map.put(organism_id,gene_id);
                                organism_genesequence_map.put(organism_id,residues);
                                ortholog_count++;

                            }
                            else{
                                logger.info("filtered gene "+gene_id+" of organism"+organism_id);
                                organism_gene_map.remove(organism_id);
                                organism_genesequence_map.remove(organism_id);
                                ortholog_count--;
                            }

                            //logger.info(gene_id+"->"+residues);
                    }
                    PanGeneStatisticalType geneStatisticalType;
                    if(ortholog_count==1){
                        geneStatisticalType=PanGeneStatisticalType.UNIQUE;
                    }
                    else if(ortholog_count==no_of_organisms){
                        geneStatisticalType=PanGeneStatisticalType.CORE;
                    }
                    else{
                        geneStatisticalType=PanGeneStatisticalType.MISC;
                    }
                    ArrayList<String> seqArr = new ArrayList<String>(organism_genesequence_map.values());
                    String consensus_seq=consensusDNASequence(seqArr);
                    // check after applying connected components filter atleast 1 gene remains
                    if(consensus_seq.length()==0) {continue;}
                    fmin=vchromosome.length();
                    vchromosome.append(consensus_seq);
                    vchromosome.append("nnnnnnnnnn");
                    fmax=vchromosome.length()-10;
                    featureReLocVirtualGene(update_add_vgene_id,fmin,fmax);

                    PanGeneBioType consensusBiotype=PanGeneBioType.CODING;
                    boolean rna_flag=false;
                    boolean pseudo_flag=false;
                    // add links
                    for (Long tobeadded_rgene_id: tobeupdated_add_vgene_map.get(update_add_vgene_id)){
                    PanGeneBioType geneBioType=getGeneBioType(tobeadded_rgene_id);
                    consensusBiotype=geneBioType;
                        switch(geneBioType){
                            case NONCODING:
                                rna_flag=true;
                                break;
                            case PSEUDO:
                                pseudo_flag=true;
                                break;
                        }
                    createOrthoParalink(update_add_vgene_id,tobeadded_rgene_id,0,geneBioType);
                    }
                    if(rna_flag && pseudo_flag){consensusBiotype=PanGeneBioType.MIXED;}
                    updateVirtalGeneType(update_add_vgene_id,consensusBiotype,geneStatisticalType);
         }

        st = conn.prepareStatement(gene_residues_query);
        for( Long update_delete_vgene_id : tobeupdated_delete_vgene_map.keySet()){
                    int gene_param_index=1;
                    for (Long gene_id_param : tobeupdated_vgene_map.get(update_delete_vgene_id)){
                        st.setLong(gene_param_index,gene_id_param);
                        gene_param_index++;
                    }
                    int i=gene_param_index;
                    while(i<=max_component_length){
                        st.setNull(i,Types.INTEGER);
                        i++;
                    }
                    rs=st.executeQuery();

                    HashMap<Integer,Long> organism_gene_map= new HashMap<Integer,Long>();
                    HashMap<Integer,String> organism_genesequence_map= new HashMap<Integer,String>();

                    int ortholog_count=0;
                    while (rs.next()) {
                            Long gene_id=rs.getLong("feature_id");
                            String residues=rs.getString("residues");
                            Integer gene_strand=rs.getInt("strand");
                            Integer organism_id=rs.getInt("organism_id");
                            if(!organism_gene_map.containsKey(organism_id)){
                                organism_gene_map.put(organism_id,gene_id);
                                organism_genesequence_map.put(organism_id,residues);
                                ortholog_count++;
                            }
                            else{
                                logger.info("filtered gene "+gene_id+" of organism"+organism_id);
                                organism_gene_map.remove(organism_id);
                                organism_genesequence_map.remove(organism_id);
                                ortholog_count--;
                            }

                            //logger.info(gene_id+"->"+residues);
                    }
                    PanGeneStatisticalType geneStatisticalType;
                    if(ortholog_count==1){
                        geneStatisticalType=PanGeneStatisticalType.UNIQUE;
                    }
                    else if(ortholog_count==no_of_organisms){
                        geneStatisticalType=PanGeneStatisticalType.CORE;
                    }
                    else{
                        geneStatisticalType=PanGeneStatisticalType.MISC;
                    }
                    ArrayList<String> seqArr = new ArrayList<String>(organism_genesequence_map.values());
                    String consensus_seq=consensusDNASequence(seqArr);
                    // check after applying connected components filter atleast 1 gene remains
                    if(consensus_seq.length()==0) {continue;}
                    fmin=vchromosome.length();
                    vchromosome.append(consensus_seq);
                    vchromosome.append("nnnnnnnnnn");
                    fmax=vchromosome.length()-10;
                    featureReLocVirtualGene(update_delete_vgene_id,fmin,fmax);

                    PanGeneBioType consensusBiotype=PanGeneBioType.CODING;
                    boolean rna_flag=false;
                    boolean pseudo_flag=false;
                    // delete links
                    for (Long tobedeleted_rgene_id: tobeupdated_delete_vgene_map.get(update_delete_vgene_id)){
                    PanGeneBioType geneBioType=getGeneBioType(tobedeleted_rgene_id);
                    consensusBiotype=geneBioType;
                        switch(geneBioType){
                            case NONCODING:
                                rna_flag=true;
                                break;
                            case PSEUDO:
                                pseudo_flag=true;
                                break;
                        }
                    deleteOrthoParalink(update_delete_vgene_id,tobedeleted_rgene_id);
                    }
                    if(rna_flag && pseudo_flag){consensusBiotype=PanGeneBioType.MIXED;}
                    updateVirtalGeneType(update_delete_vgene_id,consensusBiotype,geneStatisticalType);
         }

      // insert genes
      // create vgene
      // featureloc vgene
      // create orthopara link
      st = conn.prepareStatement(gene_residues_query);
      Long componentID= getorganismMaxGeneNumber(vorganism_id.intValue());
      componentID++;

      for( Long new_vgene_id : tobeinserted_uvgene_map.keySet()){

      logger.info("insert "+tobeinserted_uvgene_map.get(new_vgene_id));
                    int gene_param_index=1;
                    for (Long gene_id_param : tobeinserted_uvgene_map.get(new_vgene_id)){
                        st.setLong(gene_param_index,gene_id_param);
                        gene_param_index++;
                    }
                    int i=gene_param_index;
                    while(i<=max_component_length){
                        st.setNull(i,Types.INTEGER);
                        i++;
                    }
                    rs=st.executeQuery();

                    HashMap<Integer,Long> organism_gene_map= new HashMap<Integer,Long>();
                    HashMap<Integer,String> organism_genesequence_map= new HashMap<Integer,String>();

                    int ortholog_count=0;
                    while (rs.next()) {
                            Long gene_id=rs.getLong("feature_id");
                            String residues=rs.getString("residues");
                            Integer gene_strand=rs.getInt("strand");
                            Integer organism_id=rs.getInt("organism_id");
                            if(!organism_gene_map.containsKey(organism_id)){
                                organism_gene_map.put(organism_id,gene_id);
                                organism_genesequence_map.put(organism_id,residues);
                                ortholog_count++;
                            }
                            else{
                                logger.info("filtered gene "+gene_id+" of organism"+organism_id);
                                organism_gene_map.remove(organism_id);
                                organism_genesequence_map.remove(organism_id);
                                ortholog_count--;
                            }

                            //logger.info(gene_id+"->"+residues);
                    }
                    PanGeneStatisticalType geneStatisticalType;
                    if(ortholog_count==1){
                        geneStatisticalType=PanGeneStatisticalType.UNIQUE;
                    }
                    else if(ortholog_count==no_of_organisms){
                        geneStatisticalType=PanGeneStatisticalType.CORE;
                    }
                    else{
                        geneStatisticalType=PanGeneStatisticalType.MISC;
                    }
                    ArrayList<String> seqArr = new ArrayList<String>(organism_genesequence_map.values());
                    String consensus_seq=consensusDNASequence(seqArr);
                    // check after applying connected components filter atleast 1 gene remains
                    if(consensus_seq.length()==0) {continue;}

                    fmin=vchromosome.length();
                    vchromosome.append(consensus_seq);
                    vchromosome.append("nnnnnnnnnn");
                    HashMap<String,Long> vgene_map= new HashMap<String,Long>();
                    vgene_map=createVirtualGene(componentID,vorganism_id.intValue());
                    //logger.info("Successfully created virtual gene "+vgene_map.get("gene"));
                    fmax=vchromosome.length()-10;
                    featureLocVirtualGene(vgene_map,vorganism_id.intValue(),fmin,fmax);
                    int rank=0;
                    boolean rna_flag=false;
                    boolean pseudo_flag=false;
                    PanGeneBioType consensusBiotype=PanGeneBioType.CODING;
                    for ( Long gene_id : organism_gene_map.values()){
                        PanGeneBioType geneBioType=getGeneBioType(gene_id);
                        consensusBiotype=geneBioType;
                        switch(geneBioType){
                            case NONCODING:
                                rna_flag=true;
                                break;
                            case PSEUDO:
                                pseudo_flag=true;
                                break;
                        }
                        createOrthoParalink(vgene_map.get("gene"),gene_id,rank,geneBioType);
                        //logger.info("ortho para link created b/w gene "+vgene_map.get("gene")+"and "+gene_id+ "rank="+rank);
                        rank++;
                    }
                    if(rna_flag && pseudo_flag){consensusBiotype=PanGeneBioType.MIXED;}
                    updateVirtalGeneType(vgene_map.get("gene"),consensusBiotype,geneStatisticalType);
                    logger.info("inserted pan-gene no "+componentID);
                    componentID++;

      }

      // delete those remaining genes,mRNA,exon and polypeptides
      for( Long tobedeleted_vgene_id : tobedeleted_vgene_set){
         removeVirtualGene(tobedeleted_vgene_id);
      }
      // now update the chromosome
      updateVirtualChromosome(vchromosome_id,vchromosome);
      updateorganismMaxGeneNumber(vorganism_id.intValue(),componentID-1);




    }

    private ArrayList<List<Long>> get_listof_matching_sets(Set<Long> geneset,Map <Long,Set <Long>> pangenemap){
    ArrayList<List<Long>> op_vgeneids_rgeneids_list= new ArrayList<List<Long>>();
    Long operation=new Long(0);
    Long rgene_ids=new Long(0);
    
    for ( Long vgene_id : pangenemap.keySet()){

         Set <Long> old_geneset= pangenemap.get(vgene_id);
         HashSet<Long> intersection_set=new HashSet<Long>(old_geneset);
         intersection_set.retainAll(geneset);
         HashSet<Long> oldminusnew_set=new HashSet<Long>(old_geneset);
         oldminusnew_set.removeAll(geneset);
         HashSet<Long> newminusold_set=new HashSet<Long>(geneset);
         newminusold_set.removeAll(old_geneset);
         
         if(!intersection_set.isEmpty()){
            ArrayList<Long> op_vgeneids_rgeneids= new ArrayList<Long>();
            if(oldminusnew_set.isEmpty()&&newminusold_set.isEmpty()){
              // exact match
              operation=new Long(0);
              op_vgeneids_rgeneids.add(operation);
              op_vgeneids_rgeneids.add(vgene_id);
            } 
            else if(oldminusnew_set.isEmpty()){
              // addition only
              operation=new Long(1);  
              op_vgeneids_rgeneids.add(operation);
              op_vgeneids_rgeneids.add(vgene_id);
              op_vgeneids_rgeneids.addAll(newminusold_set);
            }
            else if(newminusold_set.isEmpty())
            {
              // deletion only
              operation=new Long(2);
              op_vgeneids_rgeneids.add(operation);
              op_vgeneids_rgeneids.add(vgene_id);
              op_vgeneids_rgeneids.addAll(oldminusnew_set);
            }
            else{
              // addition followed by deletion
              operation=new Long(3);
              op_vgeneids_rgeneids.add(operation);
              op_vgeneids_rgeneids.add(vgene_id);
              op_vgeneids_rgeneids.addAll(newminusold_set);
              // delimiter
              op_vgeneids_rgeneids.add(new Long(0));
              op_vgeneids_rgeneids.addAll(oldminusnew_set);
            }
            op_vgeneids_rgeneids_list.add(op_vgeneids_rgeneids);
         }
    }
    return op_vgeneids_rgeneids_list;
    }

    private int insertVirtualOrganism(String genus,String species,int organism_ids[]) throws Exception{

      PreparedStatement st;
      ResultSet rs;
      String organismsql = "INSERT INTO organism (abbreviation,genus,species,common_name,comment) values(?,?,?,?,?)  RETURNING organism_id";
      st = conn.prepareStatement(organismsql);
      st.setString(1,genus.substring(0,1)+species);
      st.setString(2,genus);
      st.setString(3,species);
      st.setString(4,genus.substring(0,1)+species);
      st.setString(5,"Pan-genome");

      rs = st.executeQuery();
      rs.next();
      int organism_id=rs.getInt("organism_id");


      String organismpropsql = "INSERT INTO organismprop (organism_id,type_id,value,rank) values(?,?,?,0)";
      st = conn.prepareStatement(organismpropsql);
      st.setLong(1,organism_id);
      st.setLong(2,73772);
      st.setString(3,"true");
      st.addBatch();

      String org_idStr="";
      for(int i=0;i<organism_ids.length-1;i++) org_idStr+=organism_ids[i]+",";
      org_idStr+=organism_ids[organism_ids.length-1];
      st.setLong(1,organism_id);
      st.setLong(2,1706);
      st.setString(3,org_idStr);
      st.addBatch();
      

      st.executeBatch();
      return organism_id;
   }


    private void insertorganismMaxGeneNumber(int organism_id,Long maxgeneNumber) throws Exception{

      PreparedStatement st;
      ResultSet rs;
      String organismpropsql = "INSERT INTO organismprop (organism_id,type_id,value,rank) values(?,?,?,0)";
      st = conn.prepareStatement(organismpropsql);
      st.setInt(1,organism_id);
      st.setLong(2,26772);
      st.setString(3,""+maxgeneNumber);
      st.executeUpdate();
    }

    private void updateorganismMaxGeneNumber(int organism_id,Long maxgeneNumber) throws Exception{

      PreparedStatement st;
      ResultSet rs;
      String organismpropsql = "UPDATE organismprop SET value=? where type_id=? and organism_id=?";
      st = conn.prepareStatement(organismpropsql);
      st.setString(1,""+maxgeneNumber);
      st.setLong(2,26772);
      st.setInt(3,organism_id);
      st.executeUpdate();
    }

    private Long getorganismMaxGeneNumber(int organism_id) throws Exception{

      PreparedStatement st;
      ResultSet rs;
      String organismpropsql = "SELECT cast(value as bigint) as maxgenenumber from organismprop where organism_id=? and type_id=?";
      st = conn.prepareStatement(organismpropsql);
      st.setInt(1,organism_id);
      st.setLong(2,26772);
      rs = st.executeQuery();
      rs.next();
      Long maxgenenumber=rs.getLong("maxgenenumber");
      return maxgenenumber;
    }


    private Long insertVirtualChromosome(StringBuilder vChromosome,int vorganism_id) throws Exception{

      PreparedStatement st;
      ResultSet rs;
      String uniquename="SALChr";
      String chromosomesql = "INSERT INTO feature(organism_id,uniquename,seqlen,residues,type_id) values (?,?,?,?,427) RETURNING feature_id";
      st = conn.prepareStatement(chromosomesql);
      st.setInt(1,vorganism_id);
      st.setString(2,uniquename);
      st.setLong(3,vChromosome.length());
      st.setString(4,vChromosome.toString());
      rs = st.executeQuery();
      rs.next();
      Long chr_feature_id=rs.getLong("feature_id");

      String toplevelfeaturesql = "INSERT INTO featureprop(feature_id,type_id,value,rank) values(?,26753,'true',0)";
      st = conn.prepareStatement(toplevelfeaturesql);
      st.setLong(1,chr_feature_id);
      st.executeUpdate();

      return chr_feature_id;
    }

    private void updateVirtualChromosome(Long feature_id,StringBuilder vChromosome) throws Exception{

      PreparedStatement st;
      ResultSet rs;
      String genesql = "UPDATE feature SET seqlen=?,residues=? where feature_id=?";
      st = conn.prepareStatement(genesql);
      st.setLong(1,vChromosome.length());
      st.setString(2,vChromosome.toString());
      st.setLong(3,feature_id);
      st.executeUpdate();
    }



    private HashMap<String,Long> createVirtualGene(Long gene_id,int vorganism_id) throws Exception{
      HashMap<String,Long> vgene_map= new HashMap<String,Long>();

      PreparedStatement st;
      ResultSet rs;
      String genename="SAL"+gene_id;
      String genesql = "INSERT INTO feature(organism_id,uniquename,type_id) values (?,?,792) RETURNING feature_id";
      st = conn.prepareStatement(genesql);
      st.setInt(1,vorganism_id);
      st.setString(2,genename);
      rs = st.executeQuery();
      rs.next();
      Long gene_feature_id=rs.getLong("feature_id");
            
      String mRNAsql = "INSERT INTO feature(organism_id,uniquename,type_id) values (?,?,321) RETURNING feature_id";
      st = conn.prepareStatement(mRNAsql);
      st.setInt(1,vorganism_id);
      st.setString(2,genename+".1");
      rs = st.executeQuery();
      rs.next();
      Long mRNA_feature_id=rs.getLong("feature_id");
      
      String exonsql = "INSERT INTO feature(organism_id,uniquename,type_id) values (?,?,234) RETURNING feature_id";
      st = conn.prepareStatement(exonsql);
      st.setInt(1,vorganism_id);
      st.setString(2,genename+".1:exon:1");
      rs = st.executeQuery();
      rs.next();
      Long exon_feature_id=rs.getLong("feature_id");      
      
      String polypeptidesql = "INSERT INTO feature(organism_id,uniquename,type_id) values (?,?,191) RETURNING feature_id";
      st = conn.prepareStatement(polypeptidesql);
      st.setInt(1,vorganism_id);
      st.setString(2,genename+".1:pep");
      rs = st.executeQuery();
      rs.next();
      Long polypeptide_feature_id=rs.getLong("feature_id");
      
      vgene_map.put("gene",gene_feature_id);
      vgene_map.put("mRNA",mRNA_feature_id);
      vgene_map.put("exon",exon_feature_id);
      vgene_map.put("polypeptide",polypeptide_feature_id);
      
      // feature_relationships
      
      String vgene_relationshipquerysql="INSERT INTO feature_relationship (subject_id,object_id,type_id) values (?,?,?)";
      st = conn.prepareStatement(vgene_relationshipquerysql);
      // mRNA part_of gene
      st.setLong(1,mRNA_feature_id);
      st.setLong(2,gene_feature_id);
      st.setLong(3,42);
      st.addBatch();
      // exon part_of mRNA
      st.setLong(1,exon_feature_id);
      st.setLong(2,mRNA_feature_id);
      st.setLong(3,42);
      st.addBatch();
      // polypeptide derives_from mRNA
      st.setLong(1,polypeptide_feature_id);
      st.setLong(2,mRNA_feature_id);
      st.setLong(3,69);
      st.addBatch();      
      st.executeBatch();
      return(vgene_map);
    }

    private void updateVirtalGeneType(Long vgene_id,PanGeneBioType bioType,PanGeneStatisticalType statType) throws Exception{

      // 1=grey 2=red 3=green 5=cyan
      int color=0;
      switch(statType){
          case CORE:
          color=1;
          break;
          case MISC:
          color=3;
          break;
          case UNIQUE:
          color=2;
          break;
      }

      PreparedStatement st;
      ResultSet rs;

      String geneColorSql = ""
      +" UPDATE featureprop set value=?,rank=0"
      +" where feature_id=? and type_id=26768";
      st = conn.prepareStatement(geneColorSql);
      st.setString(1,""+color);
      st.setLong(2,vgene_id);

      if(st.executeUpdate()==0){
      geneColorSql = ""
      +" INSERT INTO featureprop(feature_id,type_id,value,rank) values(?,26768,?,0)";
      st = conn.prepareStatement(geneColorSql);
      st.setLong(1,vgene_id);
      st.setString(2,""+color);
      st.executeUpdate();
      }

      //logger.info("gene"+vgene_id+"color updated");
      switch(bioType){
          case CODING:
          color=1;
          break;
          case NONCODING:
          color=3;
          break;
          case PSEUDO:
          color=2;
          break;
          case MIXED:
          color=5;
          break;
      }

      String rnaColorSql =""
      +" UPDATE featureprop set value=?,rank=0"
      +" where feature_id=("
      +" select rna_feature.feature_id"
      +" from feature_relationship rna_gene"
      +" join feature rna_feature on (rna_gene.subject_id=rna_feature.feature_id)"
      +" join feature gene_feature on (rna_gene.object_id=gene_feature.feature_id)"
      +" where rna_gene.type_id=42  and gene_feature.feature_id=?"
      +" and rna_feature.type_id in (321,339,340,361,743,362,734))"
      +" and type_id=26768";

      st = conn.prepareStatement(rnaColorSql);
      st.setString(1,""+color);
      st.setLong(2,vgene_id);
      if(st.executeUpdate()==0){
      rnaColorSql =""
      +" INSERT INTO featureprop(feature_id,type_id,value,rank)"
      +" select rna_feature.feature_id,26768 as type_id,? as value,0 as rank"
      +" from feature_relationship rna_gene"
      +" join feature rna_feature on (rna_gene.subject_id=rna_feature.feature_id)"
      +" join feature gene_feature on (rna_gene.object_id=gene_feature.feature_id)"
      +" where rna_gene.type_id=42  and gene_feature.feature_id=?"
      +" and rna_feature.type_id in (321,339,340,361,743,362,734)";
      st = conn.prepareStatement(rnaColorSql);
      st.setString(1,""+color);
      st.setLong(2,vgene_id);
      st.executeUpdate();
      }
      //logger.info("gene"+vgene_id+"rna color updated");
    }

    private void featureLocVirtualGene(HashMap<String,Long> vgene_map,int organism_id,long fmin,long fmax) throws Exception{
      PreparedStatement st;
      ResultSet rs;
      String featureLocsql = ""
      +" INSERT INTO featureloc(feature_id,srcfeature_id,fmin,fmax)"
      +" SELECT ? as feature_id,feature_id as srcfeature_id,? as fmin,? as fmax"
      +" from"
      +" feature where organism_id=? and type_id=427";
      st = conn.prepareStatement(featureLocsql);
      //logger.info("featureloc query= "+featureLocsql);
      st.setLong(1,vgene_map.get("gene"));
      st.setLong(2,fmin);
      st.setLong(3,fmax);
      st.setInt(4,organism_id);
      st.addBatch();
      st.setLong(1,vgene_map.get("mRNA"));
      st.setLong(2,fmin);
      st.setLong(3,fmax);
      st.setInt(4,organism_id);
      st.addBatch();
      st.setLong(1,vgene_map.get("exon"));
      st.setLong(2,fmin);
      st.setLong(3,fmax);
      st.setInt(4,organism_id);
      st.addBatch();
      st.setLong(1,vgene_map.get("polypeptide"));
      st.setLong(2,fmin);
      st.setLong(3,fmax);
      st.setInt(4,organism_id);
      st.addBatch();

      st.executeBatch();

    }

     private void featureReLocVirtualGene(Long vgene_id,long fmin,long fmax) throws Exception{
      PreparedStatement st;
      ResultSet rs;
      String featureLocsql = ""
        +" UPDATE featureloc set fmin=?,fmax=?"
        +" where feature_id in"
        +" (select gene.feature_id as feature_id from"
        +" feature gene"
        +" join"
        +" feature_relationship mRNA_gene on (gene.feature_id=mRNA_gene.object_id and mRNA_gene.type_id=42)"
        +" join"
        +" feature_relationship exon_mRNA on (mRNA_gene.subject_id=exon_mRNA.object_id and exon_mRNA.type_id=42)"
        +" join"
        +" feature_relationship polypeptide_mRNA on (polypeptide_mRNA.object_id=mRNA_gene.subject_id and polypeptide_mRNA.type_id=69)"
        +" where gene.feature_id=?"
        +" union"
        +" select mRNA_gene.subject_id as feature_id from"
        +" feature gene"
        +" join"
        +" feature_relationship mRNA_gene on (gene.feature_id=mRNA_gene.object_id and mRNA_gene.type_id=42)"
        +" join"
        +" feature_relationship exon_mRNA on (mRNA_gene.subject_id=exon_mRNA.object_id and exon_mRNA.type_id=42)"
        +" join"
        +" feature_relationship polypeptide_mRNA on (polypeptide_mRNA.object_id=mRNA_gene.subject_id and polypeptide_mRNA.type_id=69)"
        +" where gene.feature_id=?"
        +" union"
        +" select exon_mRNA.subject_id as feature_id from"
        +" feature gene"
        +" join"
        +" feature_relationship mRNA_gene on (gene.feature_id=mRNA_gene.object_id and mRNA_gene.type_id=42)"
        +" join"
        +" feature_relationship exon_mRNA on (mRNA_gene.subject_id=exon_mRNA.object_id and exon_mRNA.type_id=42)"
        +" join"
        +" feature_relationship polypeptide_mRNA on (polypeptide_mRNA.object_id=mRNA_gene.subject_id and polypeptide_mRNA.type_id=69)"
        +" where gene.feature_id=?"
        +" union"
        +" select polypeptide_mRNA.subject_id as feature_id from"
        +" feature gene"
        +" join"
        +" feature_relationship mRNA_gene on (gene.feature_id=mRNA_gene.object_id and mRNA_gene.type_id=42)"
        +" join"
        +" feature_relationship exon_mRNA on (mRNA_gene.subject_id=exon_mRNA.object_id and exon_mRNA.type_id=42)"
        +" join"
        +" feature_relationship polypeptide_mRNA on (polypeptide_mRNA.object_id=mRNA_gene.subject_id and polypeptide_mRNA.type_id=69)"
        +" where gene.feature_id=?)";
      st = conn.prepareStatement(featureLocsql);
      //logger.info("featureloc query= "+featureLocsql);
      st.setLong(1,fmin);
      st.setLong(2,fmax);
      st.setLong(3,vgene_id);
      st.setLong(4,vgene_id);
      st.setLong(5,vgene_id);
      st.setLong(6,vgene_id);
      st.executeUpdate();
    }

private void removeVirtualGene(Long vgene_id) throws Exception{
      PreparedStatement st;
      ResultSet rs;
      String deleteVirtualgenesql = ""
        +" DELETE from feature"
        +" where feature_id in"
        +" (select gene.feature_id as feature_id from"
        +" feature gene"
        +" join"
        +" feature_relationship mRNA_gene on (gene.feature_id=mRNA_gene.object_id and mRNA_gene.type_id=42)"
        +" join"
        +" feature_relationship exon_mRNA on (mRNA_gene.subject_id=exon_mRNA.object_id and exon_mRNA.type_id=42)"
        +" join"
        +" feature_relationship polypeptide_mRNA on (polypeptide_mRNA.object_id=mRNA_gene.subject_id and polypeptide_mRNA.type_id=69)"
        +" where gene.feature_id=?"
        +" union"
        +" select mRNA_gene.subject_id as feature_id from"
        +" feature gene"
        +" join"
        +" feature_relationship mRNA_gene on (gene.feature_id=mRNA_gene.object_id and mRNA_gene.type_id=42)"
        +" join"
        +" feature_relationship exon_mRNA on (mRNA_gene.subject_id=exon_mRNA.object_id and exon_mRNA.type_id=42)"
        +" join"
        +" feature_relationship polypeptide_mRNA on (polypeptide_mRNA.object_id=mRNA_gene.subject_id and polypeptide_mRNA.type_id=69)"
        +" where gene.feature_id=?"
        +" union"
        +" select exon_mRNA.subject_id as feature_id from"
        +" feature gene"
        +" join"
        +" feature_relationship mRNA_gene on (gene.feature_id=mRNA_gene.object_id and mRNA_gene.type_id=42)"
        +" join"
        +" feature_relationship exon_mRNA on (mRNA_gene.subject_id=exon_mRNA.object_id and exon_mRNA.type_id=42)"
        +" join"
        +" feature_relationship polypeptide_mRNA on (polypeptide_mRNA.object_id=mRNA_gene.subject_id and polypeptide_mRNA.type_id=69)"
        +" where gene.feature_id=?"
        +" union"
        +" select polypeptide_mRNA.subject_id as feature_id from"
        +" feature gene"
        +" join"
        +" feature_relationship mRNA_gene on (gene.feature_id=mRNA_gene.object_id and mRNA_gene.type_id=42)"
        +" join"
        +" feature_relationship exon_mRNA on (mRNA_gene.subject_id=exon_mRNA.object_id and exon_mRNA.type_id=42)"
        +" join"
        +" feature_relationship polypeptide_mRNA on (polypeptide_mRNA.object_id=mRNA_gene.subject_id and polypeptide_mRNA.type_id=69)"
        +" where gene.feature_id=?)";
      st = conn.prepareStatement(deleteVirtualgenesql);
      //logger.info("featureloc query= "+featureLocsql);
      st.setLong(1,vgene_id);
      st.setLong(2,vgene_id);
      st.setLong(3,vgene_id);
      st.setLong(4,vgene_id);
      st.executeUpdate();
    }



    private int createOrthoParalink(Long vgene_id,Long orthogene_id,int rank,PanGeneBioType geneBioType) throws Exception{

    PreparedStatement st;
    ResultSet rs;
    String uniquename="PANGENELINK"+vgene_id+"->"+orthogene_id+"";
    String orthoparafeaturesql=""
    +" INSERT INTO feature (organism_id,uniquename,type_id)"
    +" SELECT organism_id,? as uniquename,436 as type_id"
    +" from feature where feature_id=?"
    +" RETURNING feature_id";
    st = conn.prepareStatement(orthoparafeaturesql);
    st.setString(1,uniquename);
    st.setLong(2,orthogene_id);
    rs = st.executeQuery();
    rs.next();
    Long orthoparafeature_id=rs.getLong("feature_id");

    String vgene_orthopararelationshipquerysql="INSERT INTO feature_relationship (subject_id,object_id,type_id,rank) values (?,?,78,?)";
    st = conn.prepareStatement(vgene_orthopararelationshipquerysql);
    st.setLong(1,vgene_id);
    st.setLong(2,orthoparafeature_id);
    st.setInt(3,rank);
    st.executeUpdate();

    String polypeptide_orthopararelationshipquerysql=""
    +" INSERT INTO feature_relationship(subject_id,object_id,type_id,rank)"
    +" SELECT gene_product_RNA.subject_id,? as object_id,78 as type_id,? as rank"
    +" from"
    +" feature_relationship RNA_gene " 
    +" join feature_relationship gene_product_RNA on (RNA_gene.type_id=42 and RNA_gene.object_id=? and RNA_gene.subject_id=gene_product_RNA.object_id and gene_product_RNA.type_id=?)";
    //logger.info("orthopararelationshipquerysql= "+polypeptide_orthopararelationshipquerysql);
    //logger.info("orthoparafeature_id="+orthoparafeature_id+ " orthogene_id="+orthogene_id+ " rank="+rank);
    st = conn.prepareStatement(polypeptide_orthopararelationshipquerysql);
    st.setLong(1,orthoparafeature_id);
    st.setInt(2,rank);
    st.setLong(3,orthogene_id);
    if(geneBioType==PanGeneBioType.NONCODING){
    st.setLong(4,42); // exon part_of RNA
    }
    else{
    st.setLong(4,69); // polypeptide derives from mRNA
    }
    st.executeUpdate();
    return 0;
    }

    private int deleteOrthoParalink(Long vgene_id,Long orthogene_id) throws Exception{
    PreparedStatement st;
    ResultSet rs;
    String uniquename="PANGENELINK"+vgene_id+"->"+orthogene_id+"";
    String orthoparafeaturesql=""
    +" DELETE from feature "
    +" where uniquename=?";
    st = conn.prepareStatement(orthoparafeaturesql);
    st.setString(1,uniquename);
    st.executeUpdate();
    return 0;
    }

   
    public static void main(String[] args) throws ClassNotFoundException, SQLException, Exception{

    logger.info("hi there....");
    if (args.length !=4) {
            System.err.println("Usage: java GeneIndexManager <jdbc URL> <database username> create <list of organism ids seperated by commas>");
            System.err.println("Usage: java GeneIndexManager <jdbc URL> <database username> read <virtual organism id>");
            System.err.println("Usage: java GeneIndexManager <jdbc URL> <database username> update <virutal organism id>");
            System.err.println("Usage: java GeneIndexManager <jdbc URL> <database username> delete <virtual organism id>");
            System.exit(1);
        }
        String jdbcURL = args[0];
        String databaseUsername = args[1];
        String operation = args[2];
        String operationparam = args[3];

        String databasePassword = System.getProperty("password");
        if (databasePassword == null) {
            databasePassword = new String(
                System.console().readPassword("Password for %s @ %s: ", databaseUsername, jdbcURL)
            );
        }
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(jdbcURL, databaseUsername, databasePassword);
        if (operation.equals("create")) {
            logger.info("operation create with param="+operationparam);
            String sorganism_ids[]=operationparam.split(",");
            int organism_ids[] = new int[sorganism_ids.length];
            for (int i=0;i<sorganism_ids.length;i++){
                organism_ids[i]=Integer.parseInt(sorganism_ids[i]);
            }
            PanGenomeManager pgm= new PanGenomeManager(conn);
            conn.setAutoCommit(false);
            Arrays.sort(organism_ids);
            //pgm.deletePanGenome("Salmonella","pangenome",organism_ids);
            int vorganism_id=pgm.insertVirtualOrganism("Salmonella","pangenome",organism_ids);
            pgm.createPanGenome(organism_ids,vorganism_id);
            //pgm.deletePanGenome("Salmonella","pangenome",organism_ids);
            //pgm.updatePanGenome("Salmonella","pangenome");
            conn.commit();
            logger.info(":)");

        }
        conn.close();


 }

}