package org.yangcentral.yangkit.model.api.restriction;

import org.apache.commons.lang3.ObjectUtils;
/**
 * the interface for empty type
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7950#section-9.11">empty</a>
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface Empty extends Restriction<ObjectUtils.Null> {
}
