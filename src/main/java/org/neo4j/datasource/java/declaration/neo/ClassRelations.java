package org.neo4j.datasource.java.declaration.neo;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author mh14 @ jexp.de
 * @since 27.09.2008 16:31:45 (c) 2008 jexp.de
 */
public enum ClassRelations implements RelationshipType {
    METHOD_OF, OF_TYPE, RETURN_TYPE, PARAM_TYPE, FIELD_TYPE, FIELD, ALL_TYPES, TYPE, INTERFACE_TYPE, SUPER_TYPE, TYPE_ARRAY;
    
}
