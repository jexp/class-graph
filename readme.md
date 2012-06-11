## import class structures from jar-files into Neo4j

usage:

    java -cp $CLASSPATH org.neo4j.datasource.java.JarFileImporter db-dir package.and.Class

`package.and.Class` is a class of the jar file which should be imported, must be in the classpath

## Graph structure

   ROOT-[:ALL_TYPES]->all_types { type: "TYPE" }-[:TYPE]->class ({name : {slash-name} })
   ROOT-[:ALL_PACKAGES]->(all_packages { type : "ALL_PACKAGES" })-[:packageName]->(package { name : {package-name} })-[:IN_PACKAGE]->class
   ROOT-[:PACKAGE_TREE]->(p1 {name: {package-name}, part: {part}})-[:{part} *]-(p* ...)[:packageName]->(package {...})-[:IN_PACKAGE]->class

   class-[:INTERFACE_TYPE]->interface
   class-[:SUPER_TYPE]->superclass

   class-[:FIELD]->field-[:FIELD_TYPE]->field_type

   class-[:METHOD_OF]->method-[:PARAM_TYPE]->param_type
   class-[:METHOD_OF]->method-[:RETURN_TYPE]->return_type
   class-[:METHOD_OF]->method-[:THROWS]->exception

## sample cypher queries

    (might need to increase memory in `neo4j-shell` e.g. `EXTRA_JVM_ARGUMENTS="-Xmx2G -server -d64"`)

    neo4j-community-1.7/bin/neo4j-shell -path test-db/

### Class Count

    start n=node(0) match n-[:ALL_TYPES]->t-[:TYPE]->() return count(*)

### Biggest Packages

    start n=node(0) match n-[:ALL_PACKAGES]->()-[]->p-[:IN_PACKAGE]-() return p,count(*) order by count(*) desc limit 10
    +--------------------------------------------------------------------------+
    | p                                                             | count(*) |
    +--------------------------------------------------------------------------+
    | Node[313]{name->"javax.swing"}                                | 494      |
    | Node[303]{name->"com.apple.laf"}                              | 411      |
    | Node[194570]{name->"sun.awt.X11"}                             | 403      |
    | Node[308]{name->"javax.swing.plaf.basic"}                     | 343      |
    +--------------------------------------------------------------------------+

### Biggest Top-Level-Packages

    start n=node(0) match n-[:PACKAGE_TREE]->m-->p-[*0..5]->()-[:IN_PACKAGE]->c<-[:TYPE]-types<-[:ALL_TYPES]-n return p.name,count(c) order by count(c) desc limit 10

### Top Used

    start n=node(0) match n-[:ALL_TYPES]->t-[:TYPE]->c<-[:FIELD_TYPE|PARAM_TYPE]-() return c,count(*) order by count(*) desc limit 10

### Top inherited

    start n=node(0) match n-[:ALL_TYPES]->t-[:TYPE]->c<-[:SUPER_TYPE]-() return c,count(*) order by count(*) desc limit 10

### Collaborative filtering

Which other classes were used by users of this class (1022 = IOException)

    start c=node(1022) match c<--()-->other return other,count(*) order by count(*) desc limit 20
