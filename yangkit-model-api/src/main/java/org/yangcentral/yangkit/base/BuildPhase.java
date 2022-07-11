package org.yangcentral.yangkit.base;

/**
 * define the enum item for build phase
 * @author frank feng
 * @since 2022-07-07
 */
public enum BuildPhase {
   LINKAGE,//process linkage,such as import,include,belongs-to
   GRAMMAR,//build grammar
   SCHEMA_BUILD,//build schema node, build the relationship of schema nodes
   SCHEMA_EXPAND,//expand schema, for example expand uses
   SCHEMA_MODIFIER,//process deviation/augment
   SCHEMA_TREE;//build some meta data after schema tree is built
}
