
grammar IfFeatureExpression;
@header {
package org.yangcentral.yangkit.antlr;
}
NOT : 'not';
LP : '(';
RP : ')';
AND : 'and';
OR : 'or';
COLON : ':';
SEP: [ \n\r\t]+;
IDENTIFIER : [a-zA-Z][a-zA-Z0-9_-]*;
if_feature_expr: if_feature_term (SEP OR SEP if_feature_expr)?;
if_feature_term: if_feature_factor (SEP AND SEP if_feature_term)?;
if_feature_factor: NOT SEP if_feature_factor
                 | LP SEP? if_feature_expr SEP? RP
                 | identifier_ref_arg;

identifier_ref_arg : (IDENTIFIER COLON)? IDENTIFIER;
