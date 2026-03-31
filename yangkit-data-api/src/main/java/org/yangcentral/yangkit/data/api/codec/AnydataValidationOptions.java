package org.yangcentral.yangkit.data.api.codec;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class AnydataValidationOptions implements AnydataValidationContextResolver {
   private AnydataValidationContext defaultContext;
   private final Map<QName, AnydataValidationContext> schemaNodeContexts = new LinkedHashMap<>();
   private final List<Rule> rules = new ArrayList<>();

   public AnydataValidationOptions defaultSchemaContext(YangSchemaContext schemaContext) {
      this.defaultContext = schemaContext == null ? null : new DefaultAnydataValidationContext(schemaContext);
      return this;
   }

   public AnydataValidationOptions defaultContext(AnydataValidationContext context) {
      this.defaultContext = context;
      return this;
   }

   public AnydataValidationOptions registerSchemaContext(QName anydataSchemaNode, YangSchemaContext schemaContext) {
      return registerContext(anydataSchemaNode,
              schemaContext == null ? null : new DefaultAnydataValidationContext(schemaContext));
   }

   public AnydataValidationOptions registerContext(QName anydataSchemaNode, AnydataValidationContext context) {
      if (anydataSchemaNode != null && context != null) {
         schemaNodeContexts.put(anydataSchemaNode, context);
      }
      return this;
   }

   public AnydataValidationOptions addRule(Predicate<AnydataValidationRequest> matcher,
                                           YangSchemaContext schemaContext) {
      return addRule(matcher,
              schemaContext == null ? null : new DefaultAnydataValidationContext(schemaContext));
   }

   public AnydataValidationOptions addRule(Predicate<AnydataValidationRequest> matcher,
                                           AnydataValidationContext context) {
      if (matcher != null && context != null) {
         rules.add(new Rule(matcher, context));
      }
      return this;
   }

   @Override
   public AnydataValidationContext resolve(AnydataValidationRequest request) {
      for (Rule rule : rules) {
         if (rule.matcher.test(request)) {
            return rule.context;
         }
      }
      if (request != null && request.getSchemaNodeIdentifier() != null) {
         AnydataValidationContext context = schemaNodeContexts.get(request.getSchemaNodeIdentifier());
         if (context != null) {
            return context;
         }
      }
      return defaultContext;
   }

   private static class Rule {
      private final Predicate<AnydataValidationRequest> matcher;
      private final AnydataValidationContext context;

      private Rule(Predicate<AnydataValidationRequest> matcher, AnydataValidationContext context) {
         this.matcher = matcher;
         this.context = context;
      }
   }
}

