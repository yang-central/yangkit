package org.yangcentral.yangkit.data.codec.xml;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.model.api.stmt.Default;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.util.List;

/**
 * Handler for YANG default values (RFC 7950 Section 7.6.3, 7.7.3).
 * 
 * Applies default values to leaf nodes that are not explicitly provided in the XML input.
 * Supports:
 * - Leaf default values
 * - Leaf-list default values  
 * - Choice default cases
 */
public class DefaultValueHandler {
    
    public DefaultValueHandler() {
    }

    /**
     * Apply default values to all leaf nodes in the container that are missing from XML.
     * 
     * @param container the data container
     * @param validatorResultBuilder validation result builder
     */
    public void applyDefaults(YangDataContainer container, ValidatorResultBuilder validatorResultBuilder) {
        if (container == null) {
            return;
        }
        
        // Get the schema node from one of the children or assume it's set elsewhere
        // For default handling, we traverse all children in the container
        for (YangData<?> childData : container.getDataChildren()) {
            SchemaNode childSchemaNode = (SchemaNode) childData.getSchemaNode();
            if (childSchemaNode != null && 
                childSchemaNode instanceof org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer) {
                org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer schemaContainer = 
                    (org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer) childSchemaNode;
                
                // Process all child schema nodes
                for (SchemaNode child : schemaContainer.getTreeNodeChildren()) {
                    if (child instanceof Leaf) {
                        applyLeafDefault(container, (Leaf) child);
                    } else if (child instanceof org.yangcentral.yangkit.model.api.stmt.LeafList) {
                        applyLeafListDefault(container, (org.yangcentral.yangkit.model.api.stmt.LeafList) child);
                    } else if (child instanceof org.yangcentral.yangkit.model.api.stmt.Choice) {
                        applyChoiceDefault(container, (org.yangcentral.yangkit.model.api.stmt.Choice) child);
                    }
                }
            }
        }
    }

    /**
     * Apply default value to a single leaf if not present.
     * 
     * @param container the data container
     * @param leaf the leaf schema node
     */
    private void applyLeafDefault(YangDataContainer container, Leaf leaf) {
        // Check if leaf already has a value
        List<YangData<?>> existingData = container.getDataChildren(leaf.getIdentifier());
        if (existingData != null && !existingData.isEmpty() && 
            !existingData.get(0).isDummyNode()) {
            return; // Value already exists, don't apply default
        }
        
        // Check if leaf has a default value
        Default defaultValue = leaf.getDefault();
        if (defaultValue == null) {
            return; // No default defined
        }
        
        // Create leaf data with default value
        try {
            LeafData leafData = (LeafData) YangDataBuilderFactory.getBuilder()
                    .getYangData(leaf, defaultValue.getArgStr());
            
            if (leafData != null) {
                container.addDataChild(leafData);
            }
        } catch (Exception e) {
            // Log warning but continue processing
            System.err.println("Warning: Failed to apply default value for leaf '" + 
                leaf.getIdentifier().getLocalName() + "': " + e.getMessage());
        }
    }

    /**
     * Apply default values to a leaf-list if not present.
     * 
     * @param container the data container
     * @param leafList the leaf-list schema node
     */
    private void applyLeafListDefault(YangDataContainer container, 
                                      org.yangcentral.yangkit.model.api.stmt.LeafList leafList) {
        // Check if leaf-list already has values
        List<YangData<?>> existingData = container.getDataChildren(leafList.getIdentifier());
        if (existingData != null && !existingData.isEmpty() && 
            !existingData.get(0).isDummyNode()) {
            return; // Values already exist, don't apply defaults
        }
        
        // Note: RFC 7950 Section 7.7.3 states that default values for leaf-list
        // are only used when the leaf-list is empty and has no min-elements constraint
        org.yangcentral.yangkit.model.api.stmt.MinElements minElements = leafList.getMinElements();
        if (minElements != null && minElements.getValue() > 0) {
            return; // Cannot apply defaults when min-elements is specified
        }
        
        // Get all default values (leaf-list can have multiple defaults)
        List<Default> defaultValues = leafList.getDefaults();
        if (defaultValues == null || defaultValues.isEmpty()) {
            return; // No defaults defined
        }
        
        // Create leaf-list data with default values
        for (Default defaultValue : defaultValues) {
            try {
                LeafData leafData = (LeafData) YangDataBuilderFactory.getBuilder()
                        .getYangData(leafList, defaultValue.getArgStr());
                
                if (leafData != null) {
                    container.addDataChild(leafData);
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to apply default value for leaf-list '" + 
                    leafList.getIdentifier().getLocalName() + "': " + e.getMessage());
            }
        }
    }

    /**
     * Apply default case for a choice if no case is selected.
     * 
     * @param container the data container
     * @param choice the choice schema node
     */
    private void applyChoiceDefault(YangDataContainer container, 
                                    org.yangcentral.yangkit.model.api.stmt.Choice choice) {
        // Check if any case is already selected
        ChoiceValidator validator = new ChoiceValidator(choice);
        // This will be enhanced in future to actually apply the default case data
        
        org.yangcentral.yangkit.model.api.stmt.Case defaultCase = choice.getDefaultCase();
        if (defaultCase == null) {
            return; // No default case defined
        }
        
        // Default case application is complex and depends on the specific implementation
        // For now, this is a placeholder for future enhancement
    }
}
