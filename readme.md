## import class structures from jar-files into Neo4j

usage:

    java -cp $CLASSPATH org.neo4j.datasource.java.JarFileImporter db-dir package.and.Class

`package.and.Class` is a class of the jar file which should be imported, must be in the classpath

e.g.

    java -cp $CLASSPATH org.neo4j.datasource.java.JarFileImporter test-db java.lang.Object

## Graph structure

    ROOT-[:ALL_TYPES]->all_types { type: "TYPE" }
        -[:TYPE]->class ({name : {slash-name}, access : {access} })

    ROOT-[:ALL_PACKAGES]->(all_packages { type : "ALL_PACKAGES" })
        -[:{package-name}]->(package { name : {package-name} })
        -[:IN_PACKAGE]->class

    ROOT-[:PACKAGE_TREE]->(p1 {name: {package-name}, part: {part}})
        -[:{part} *]->(p* ...)
        -[:{package-name}]->(package {...})
        -[:IN_PACKAGE]->class

    class-[:INTERFACE_TYPE]->interface
    class-[:SUPER_TYPE]->superclass

    class-[:FIELD]->field-[:FIELD_TYPE]->field_type

    class-[:METHOD_OF]->method-[:PARAM_TYPE]->param_type
                        method-[:RETURN_TYPE]->return_type
                        method-[:THROWS]->exception

![graph model](https://raw.github.com/jexp/class-graph/master/model.png)

## sample cypher queries

    (might need to increase memory in `neo4j-shell` e.g. `EXTRA_JVM_ARGUMENTS="-Xmx2G -server -d64"`)

    neo4j-community-1.7/bin/neo4j-shell -path test-db/

### Class Count

    start n=node(0) match n-[:ALL_TYPES]->t-[:TYPE]->() return count(*)
    +----------+
    | count(*) |
    +----------+
    | 19258    |
    +----------+


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

    start n=node(0) match n-[:PACKAGE_TREE]->m-->p-[*0..5]->()-[:IN_PACKAGE]->c return p.name,count(c) order by count(c) desc limit 10
    +----------------------+
    | p.name    | count(c) |
    +----------------------+
    | "sun"     | 4292     |
    | "javax"   | 3455     |
    | "java"    | 2854     |
    | "com"     | 1696     |
    | "org"     | 736      |
    | "apple"   | 60       |
    | "default" | 29       |
    | "sunw"    | 3        |
    +----------------------+


### Top Used

    start n=node(0) match n-[:ALL_TYPES]->t-[:TYPE]->c<-[:FIELD_TYPE|PARAM_TYPE]-() return c,count(*) order by count(*) desc limit 10
    +----------------------------------------------------------+
    | c                                             | count(*) |
    +----------------------------------------------------------+
    | Node[17]{name->"int"}                         | 59920    |
    | Node[23]{name->"java/lang/String"}            | 41002    |
    | Node[10]{name->"java/lang/Object",access->33} | 23268    |
    ....
    | Node[15]{name->"java/lang/Class"}             | 2833     |
    ....
    +----------------------------------------------------------+

### Top inherited

    start n=node(0) match n-[:ALL_TYPES]->t-[:TYPE]->c<-[:SUPER_TYPE]-() return c,count(*) order by count(*) desc limit 10
    +--------------------------------------------------------------------------------------------+
    | c                                                                               | count(*) |
    +--------------------------------------------------------------------------------------------+
    | Node[10]{name->"java/lang/Object",access->33}                                   | 10007    |
    | Node[12551]{access->1057,name->"java/util/ListResourceBundle"}                  | 443      |
    | Node[1004]{name->"java/lang/Enum",access->1057}                                 | 208      |
    | Node[240976]{access->1057,name->"sun/util/resources/LocaleNamesBundle"}         | 184      |
    | Node[12367]{name->"java/lang/Exception"}                                        | 124      |
    | Node[194573]{access->1056,name->"sun/awt/X11/XWrapperBase"}                     | 108      |
    | Node[3137]{name->"java/lang/RuntimeException"}                                  | 89       |
    | Node[4656]{access->1057,name->"javax/swing/AbstractAction"}                     | 70       |
    | Node[36898]{name->"javax/swing/plaf/nimbus/AbstractRegionPainter",access->1057} | 59       |
    | Node[1022]{name->"java/io/IOException"}                                         | 56       |
    +--------------------------------------------------------------------------------------------+



### Collaborative filtering

Which other classes were used by users of this class (1022 = IOException)

    start c=node(1022) match c<--()-->other return other,count(*) order by count(*) desc limit 20
    +--------------------------------------------------------------------------------+
    | other                                                               | count(*) |
    +--------------------------------------------------------------------------------+
    | Node[12]{name->"void"}                                              | 5010     |
    | Node[17]{name->"int"}                                               | 2791     |
    | Node[23]{name->"java/lang/String"}                                  | 1687     |
    | Node[385]{name->"byte"}                                             | 958      |
    | Node[19]{name->"boolean"}                                           | 828      |
    | Node[10]{name->"java/lang/Object",access->33}                       | 619      |
    | Node[27]{name->"long"}                                              | 505      |
    | Node[176]{name->"java/io/InputStream"}                              | 457      |
    | Node[1344]{name->"java/io/OutputStream"}                            | 369      |
    | Node[1023]{name->"java/lang/ClassNotFoundException"}                | 352      |
    | Node[1021]{name->"java/io/ObjectInputStream"}                       | 298      |
    | Node[12874]{name->"org/xml/sax/SAXException"}                       | 280      |
    | Node[1318]{name->"java/io/ObjectOutputStream"}                      | 260      |
    | Node[1331]{name->"char"}                                            | 197      |
    | Node[41890]{name->"javax/management/ObjectName"}                    | 196      |
    | Node[33623]{access->33,name->"javax/xml/stream/XMLStreamException"} | 194      |
    | Node[7283]{name->"java/net/URL"}                                    | 178      |
    | Node[230034]{name->"sun/security/krb5/Asn1Exception",access->33}    | 167      |
    | Node[142477]{name->"java/nio/file/Path"}                            | 165      |
    | Node[2274]{name->"java/io/File"}                                    | 154      |
    +--------------------------------------------------------------------------------+

