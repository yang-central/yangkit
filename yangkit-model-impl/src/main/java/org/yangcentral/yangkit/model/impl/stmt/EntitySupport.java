package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Description;
import org.yangcentral.yangkit.model.api.stmt.Reference;
import org.yangcentral.yangkit.model.api.stmt.Status;
import org.yangcentral.yangkit.model.api.stmt.StatusStmt;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable support for Entity fields (description, reference, status).
 * Used by EntityImpl and can be composed into non-EntityImpl classes that
 * implement the Entity interface.
 */
public class EntitySupport {
    private StatusStmt status;
    private Description description;
    private Reference reference;

    public StatusStmt getStatus() {
        return this.status;
    }

    public Status getEffectiveStatus() {
        return null == this.status ? Status.CURRENT : Status.getStatus(this.status.getArgStr());
    }

    public Description getDescription() {
        return this.description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public Reference getReference() {
        return this.reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public void setStatus(StatusStmt status) {
        this.status = status;
    }

    public void clear() {
        this.description = null;
        this.reference = null;
        this.status = null;
    }

    /**
     * Initialize Entity fields from the owner statement's sub-statements.
     * @param owner the YangStatementImpl that owns this support
     * @return validator result
     */
    public ValidatorResult init(YangStatementImpl owner) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();

        List<YangStatement> matched = owner.getSubStatement(YangBuiltinKeyword.DESCRIPTION.getQName());
        if (matched.size() != 0) {
            this.description = (Description) matched.get(0);
        }

        matched = owner.getSubStatement(YangBuiltinKeyword.REFERENCE.getQName());
        if (matched.size() != 0) {
            this.reference = (Reference) matched.get(0);
        }

        matched = owner.getSubStatement(YangBuiltinKeyword.STATUS.getQName());
        if (matched.size() != 0) {
            this.status = (StatusStmt) matched.get(0);
        }

        return validatorResultBuilder.build();
    }

    /**
     * Get effective sub-statements contributed by Entity fields.
     * @param owner the YangStatementImpl that owns this support
     * @return list of effective sub-statements
     */
    public List<YangStatement> getEffectiveSubStatements(YangStatementImpl owner) {
        List<YangStatement> statements = new ArrayList<>();
        if (this.description != null) {
            statements.add(this.description);
        }

        if (this.reference != null) {
            statements.add(this.reference);
        }

        if (this.status != null) {
            statements.add(this.status);
        } else {
            StatusStmt newStatus = new StatusImpl("current");
            newStatus.setContext(new YangContext(owner.getContext()));
            newStatus.setElementPosition(owner.getElementPosition());
            newStatus.setParentStatement(owner);
            newStatus.init();
            statements.add(newStatus);
        }

        return statements;
    }
}
