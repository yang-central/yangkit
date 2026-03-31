package org.yangcentral.yangkit.data.impl.util;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.jaxen.JaxenException;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.AnyxmlData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.impl.model.YangDataDocumentImpl;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public final class NetconfSelectUtil {
    private NetconfSelectUtil() {
    }

    public static YangDataDocument getEffectiveValue(AnyDataData anyDataData) {
        YangDataDocument value = anyDataData.getValue();
        String select = NetconfEditUtil.getSelect(anyDataData);
        if (value == null || select == null) {
            return value;
        }
        try {
            YangXPath xpath = new YangXPathImpl(select);
            List<?> selectedNodes = xpath.selectNodes(value);
            return buildSelectedYangDataDocument(value, selectedNodes);
        } catch (JaxenException e) {
            return value;
        }
    }

    public static Document getEffectiveValue(AnyxmlData anyxmlData) {
        Document value = anyxmlData.getValue();
        String select = NetconfEditUtil.getSelect(anyxmlData);
        if (value == null || select == null) {
            return value;
        }
        try {
            List<?> selectedNodes = value.selectNodes(select);
            return buildSelectedXmlDocument(value, selectedNodes);
        } catch (RuntimeException e) {
            return value;
        }
    }

    private static YangDataDocument buildSelectedYangDataDocument(YangDataDocument original, List<?> selectedNodes) {
        YangDataDocument filtered = new YangDataDocumentImpl(original.getQName(), original.getSchemaContext());
        filtered.setOnlyConfig(original.onlyConfig());
        copyYangDataDocumentAttributes(original, filtered);

        List<YangData<?>> normalized = normalizeSelectedYangData(selectedNodes);
        for (YangData<?> selected : normalized) {
            try {
                filtered.addDataChild(selected.clone(), false);
            } catch (CloneNotSupportedException | YangDataException e) {
                return original;
            }
        }
        return filtered;
    }

    private static List<YangData<?>> normalizeSelectedYangData(List<?> selectedNodes) {
        List<YangData<?>> candidates = new ArrayList<>();
        if (selectedNodes == null) {
            return candidates;
        }
        for (Object selectedNode : selectedNodes) {
            if (selectedNode instanceof YangData) {
                candidates.add((YangData<?>) selectedNode);
            }
        }
        candidates.sort(Comparator.comparingInt(NetconfSelectUtil::pathDepth));

        List<YangData<?>> normalized = new ArrayList<>();
        for (YangData<?> candidate : candidates) {
            if (hasSelectedAncestor(normalized, candidate)) {
                continue;
            }
            normalized.add(candidate);
        }
        return normalized;
    }

    private static boolean hasSelectedAncestor(List<YangData<?>> selected, YangData<?> candidate) {
        for (YangData<?> existing : selected) {
            if (isAncestorOrSelf(existing, candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAncestorOrSelf(YangData<?> ancestor, YangData<?> descendant) {
        if (ancestor == null || descendant == null || ancestor.getPath() == null || descendant.getPath() == null) {
            return false;
        }
        List<?> ancestorSteps = ancestor.getPath().getSteps();
        List<?> descendantSteps = descendant.getPath().getSteps();
        if (ancestorSteps.size() > descendantSteps.size()) {
            return false;
        }
        for (int i = 0; i < ancestorSteps.size(); i++) {
            if (!ancestorSteps.get(i).equals(descendantSteps.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static int pathDepth(YangData<?> yangData) {
        if (yangData == null || yangData.getPath() == null || yangData.getPath().getSteps() == null) {
            return Integer.MAX_VALUE;
        }
        return yangData.getPath().getSteps().size();
    }

    private static void copyYangDataDocumentAttributes(YangDataDocument source, YangDataDocument target) {
        if (source.getAttributes() == null) {
            return;
        }
        for (org.yangcentral.yangkit.common.api.Attribute attribute : source.getAttributes()) {
            if (attribute == null) {
                continue;
            }
            try {
                target.addAttribute(attribute.clone());
            } catch (CloneNotSupportedException e) {
                target.addAttribute(attribute);
            }
        }
    }

    private static Document buildSelectedXmlDocument(Document original, List<?> selectedNodes) {
        if (original == null || original.getRootElement() == null) {
            return original;
        }
        Element originalRoot = original.getRootElement();
        if (selectedNodes != null) {
            for (Object selectedNode : selectedNodes) {
                if (selectedNode == original || selectedNode == originalRoot) {
                    return (Document) original.clone();
                }
            }
        }

        Element filteredRoot = createShallowCopy(originalRoot);
        Document filtered = DocumentHelper.createDocument(filteredRoot);
        if (selectedNodes == null) {
            return filtered;
        }

        for (Object selectedNode : selectedNodes) {
            if (selectedNode instanceof Element) {
                appendSelectedElement(filteredRoot, originalRoot, (Element) selectedNode);
            } else if (selectedNode instanceof Attribute) {
                Attribute attribute = (Attribute) selectedNode;
                filteredRoot.addAttribute(attribute.getQName(), attribute.getValue());
            } else if (selectedNode instanceof Node) {
                filteredRoot.add((Node) ((Node) selectedNode).clone());
            }
        }
        return filtered;
    }

    private static Element createShallowCopy(Element source) {
        Element copy = DocumentHelper.createElement(source.getQName());
        for (Iterator<?> iterator = source.declaredNamespaces().iterator(); iterator.hasNext(); ) {
            Namespace namespace = (Namespace) iterator.next();
            if (namespace == null) {
                continue;
            }
            copy.add(namespace);
        }
        for (Iterator<?> iterator = source.attributeIterator(); iterator.hasNext(); ) {
            Attribute attribute = (Attribute) iterator.next();
            copy.addAttribute(attribute.getQName(), attribute.getValue());
        }
        return copy;
    }

    private static void appendSelectedElement(Element filteredRoot, Element originalRoot, Element selectedElement) {
        List<Element> path = buildElementPath(originalRoot, selectedElement);
        if (path.isEmpty()) {
            return;
        }

        Element current = filteredRoot;
        for (int i = 1; i < path.size(); i++) {
            Element source = path.get(i);
            boolean leaf = i == path.size() - 1;
            if (leaf) {
                current.add((Element) source.clone());
            } else {
                current = findOrCreateChild(current, source);
            }
        }
    }

    private static List<Element> buildElementPath(Element root, Element selectedElement) {
        List<Element> reversed = new ArrayList<>();
        Element current = selectedElement;
        while (current != null) {
            reversed.add(current);
            if (current == root) {
                break;
            }
            if (!(current.getParent() instanceof Element)) {
                return new ArrayList<>();
            }
            current = (Element) current.getParent();
        }
        if (reversed.isEmpty() || reversed.get(reversed.size() - 1) != root) {
            return new ArrayList<>();
        }

        List<Element> path = new ArrayList<>();
        for (int i = reversed.size() - 1; i >= 0; i--) {
            path.add(reversed.get(i));
        }
        return path;
    }

    private static Element findOrCreateChild(Element parent, Element source) {
        Element existing = parent.element(source.getQName());
        if (existing != null) {
            return existing;
        }
        Element created = createShallowCopy(source);
        parent.add(created);
        return created;
    }
}

