package org.yangcentral.yangkit.data.codec.xml;

import org.dom4j.Element;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.model.api.stmt.Case;
import org.yangcentral.yangkit.model.api.stmt.Choice;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validator for YANG Choice/Case data nodes (RFC 7950 Section 7.13).
 * 
 * Choice statement defines a set of alternatives, only one of which may be present at any given time.
 * This validator ensures that:
 * - At most one case from each choice is selected
 * - If mandatory choice is present, at least one case must be selected
 * - Default cases are applied when no case is explicitly selected
 */
public class ChoiceValidator {
    
    private final Choice choice;
    
    public ChoiceValidator(Choice choice) {
        this.choice = choice;
    }

    /**
     * Validate that only one case is selected in the choice.
     * 
     * @param container the parent data container
     * @param element the XML element being validated
     * @param validatorResultBuilder validation result builder
     * @return true if validation passed, false otherwise
     */
    public boolean validateSingleCase(YangDataContainer container, Element element, 
                                      ValidatorResultBuilder validatorResultBuilder) {
        List<Case> selectedCases = findSelectedCases(container);
        
        // Check if multiple cases are selected
        if (selectedCases.size() > 1) {
            ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.INVALID_VALUE);
            recordBuilder.setErrorPath(element.getUniquePath());
            recordBuilder.setBadElement(element);
            recordBuilder.setErrorMessage(
                new ErrorMessage("Multiple cases selected for choice '" + choice.getIdentifier().getLocalName() + 
                    "': " + getCaseNames(selectedCases)));
            validatorResultBuilder.addRecord(recordBuilder.build());
            return false;
        }
        
        // Check mandatory constraint
        if (selectedCases.isEmpty() && choice.isMandatory()) {
            ValidatorRecordBuilder<String, Element> recordBuilder = new ValidatorRecordBuilder<>();
            recordBuilder.setErrorTag(ErrorTag.MISSING_ELEMENT);
            recordBuilder.setErrorPath(element.getUniquePath());
            recordBuilder.setBadElement(element);
            recordBuilder.setErrorMessage(
                new ErrorMessage("Mandatory choice '" + choice.getIdentifier().getLocalName() + "' requires at least one case"));
            validatorResultBuilder.addRecord(recordBuilder.build());
            return false;
        }
        
        return true;
    }

    /**
     * Find all cases that have data present in the container.
     * 
     * @param container the data container to search
     * @return list of selected cases
     */
    private List<Case> findSelectedCases(YangDataContainer container) {
        List<Case> selectedCases = new ArrayList<>();
        
        for (Case caseNode : choice.getCases()) {
            if (isCaseSelected(container, caseNode)) {
                selectedCases.add(caseNode);
            }
        }
        
        return selectedCases;
    }

    /**
     * Check if a specific case has data present.
     * 
     * @param container the data container
     * @param caseNode the case to check
     * @return true if case has data, false otherwise
     */
    private boolean isCaseSelected(YangDataContainer container, Case caseNode) {
        // Check all data nodes defined in this case
        List<SchemaNode> caseChildren = caseNode.getTreeNodeChildren();
        for (SchemaNode child : caseChildren) {
            List<YangData<?>> childData = container.getDataChildren(child.getIdentifier());
            if (childData != null && !childData.isEmpty() && 
                !childData.get(0).isDummyNode()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get comma-separated names of cases.
     * 
     * @param cases list of cases
     * @return formatted string of case names
     */
    private String getCaseNames(List<Case> cases) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cases.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(cases.get(i).getIdentifier().getLocalName());
        }
        return sb.toString();
    }

    /**
     * Apply default case if no case is selected and default is defined.
     * 
     * @param container the data container
     * @param validatorResultBuilder validation result builder
     */
    public void applyDefaultCase(YangDataContainer container, ValidatorResultBuilder validatorResultBuilder) {
        List<Case> selectedCases = findSelectedCases(container);
        
        if (selectedCases.isEmpty()) {
            Case defaultCase = choice.getDefaultCase();
            if (defaultCase != null) {
                // Default case will be applied by the data builder
                // This is just a placeholder for future enhancement
            }
        }
    }
}
