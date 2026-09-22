package org.yangcentral.yangkit.data.api.model;

/**
 * Identifies an entry of a list without keys by its one-based position
 * among sibling entries of the same list.
 */
public interface PositionalListIdentifier extends ListIdentifier {
   int getPosition();
}
