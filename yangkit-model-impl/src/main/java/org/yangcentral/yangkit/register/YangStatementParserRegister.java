package org.yangcentral.yangkit.register;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.Yang;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;
import org.yangcentral.yangkit.model.impl.schema.YangSchemaContextImpl;
import org.yangcentral.yangkit.model.impl.stmt.ActionImpl;
import org.yangcentral.yangkit.model.impl.stmt.AnyDataImpl;
import org.yangcentral.yangkit.model.impl.stmt.AnyxmlImpl;
import org.yangcentral.yangkit.model.impl.stmt.ArgumentImpl;
import org.yangcentral.yangkit.model.impl.stmt.AugmentImpl;
import org.yangcentral.yangkit.model.impl.stmt.BelongsToImpl;
import org.yangcentral.yangkit.model.impl.stmt.CaseImpl;
import org.yangcentral.yangkit.model.impl.stmt.ChoiceImpl;
import org.yangcentral.yangkit.model.impl.stmt.ConfigImpl;
import org.yangcentral.yangkit.model.impl.stmt.ContactImpl;
import org.yangcentral.yangkit.model.impl.stmt.ContainerImpl;
import org.yangcentral.yangkit.model.impl.stmt.DefaultImpl;
import org.yangcentral.yangkit.model.impl.stmt.DefaultYangUnknown;
import org.yangcentral.yangkit.model.impl.stmt.DesciptionImpl;
import org.yangcentral.yangkit.model.impl.stmt.DeviateImpl;
import org.yangcentral.yangkit.model.impl.stmt.DeviationImpl;
import org.yangcentral.yangkit.model.impl.stmt.ErrorAppTagImpl;
import org.yangcentral.yangkit.model.impl.stmt.ErrorMessageImpl;
import org.yangcentral.yangkit.model.impl.stmt.ExtensionImpl;
import org.yangcentral.yangkit.model.impl.stmt.FeatureImpl;
import org.yangcentral.yangkit.model.impl.stmt.GroupingImpl;
import org.yangcentral.yangkit.model.impl.stmt.IdentityImpl;
import org.yangcentral.yangkit.model.impl.stmt.IfFeatureImpl;
import org.yangcentral.yangkit.model.impl.stmt.ImportImpl;
import org.yangcentral.yangkit.model.impl.stmt.IncludeImpl;
import org.yangcentral.yangkit.model.impl.stmt.InputImpl;
import org.yangcentral.yangkit.model.impl.stmt.KeyImpl;
import org.yangcentral.yangkit.model.impl.stmt.LeafImpl;
import org.yangcentral.yangkit.model.impl.stmt.LeafListImpl;
import org.yangcentral.yangkit.model.impl.stmt.ListImpl;
import org.yangcentral.yangkit.model.impl.stmt.MainModuleImpl;
import org.yangcentral.yangkit.model.impl.stmt.MandatoryImpl;
import org.yangcentral.yangkit.model.impl.stmt.MaxElementsImpl;
import org.yangcentral.yangkit.model.impl.stmt.MinElementsImpl;
import org.yangcentral.yangkit.model.impl.stmt.MustImpl;
import org.yangcentral.yangkit.model.impl.stmt.NamespaceImpl;
import org.yangcentral.yangkit.model.impl.stmt.NotificationImpl;
import org.yangcentral.yangkit.model.impl.stmt.OrderedByImpl;
import org.yangcentral.yangkit.model.impl.stmt.OrganizationImpl;
import org.yangcentral.yangkit.model.impl.stmt.OutputImpl;
import org.yangcentral.yangkit.model.impl.stmt.PrefixImpl;
import org.yangcentral.yangkit.model.impl.stmt.PresenceImpl;
import org.yangcentral.yangkit.model.impl.stmt.ReferenceImpl;
import org.yangcentral.yangkit.model.impl.stmt.RefineImpl;
import org.yangcentral.yangkit.model.impl.stmt.RevisionDateImpl;
import org.yangcentral.yangkit.model.impl.stmt.RevisionImpl;
import org.yangcentral.yangkit.model.impl.stmt.RpcImpl;
import org.yangcentral.yangkit.model.impl.stmt.StatusImpl;
import org.yangcentral.yangkit.model.impl.stmt.SubModuleImpl;
import org.yangcentral.yangkit.model.impl.stmt.TypeImpl;
import org.yangcentral.yangkit.model.impl.stmt.TypedefImpl;
import org.yangcentral.yangkit.model.impl.stmt.UniqueImpl;
import org.yangcentral.yangkit.model.impl.stmt.UnitsImpl;
import org.yangcentral.yangkit.model.impl.stmt.UsesImpl;
import org.yangcentral.yangkit.model.impl.stmt.WhenImpl;
import org.yangcentral.yangkit.model.impl.stmt.YangVersionImpl;
import org.yangcentral.yangkit.model.impl.stmt.YinElementImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.BaseImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.BitImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.EnumImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.FractionDigitsImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.LengthImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.ModifierImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.PathImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.PatternImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.PositionImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.RangeImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.RequireInstanceImpl;
import org.yangcentral.yangkit.model.impl.stmt.type.ValueImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YangStatementParserRegister {
   private static final YangStatementParserRegister ourInstance = new YangStatementParserRegister();
   private Map<QName, YangStatementParserPolicy> policyMap = new ConcurrentHashMap();

   public static YangStatementParserRegister getInstance() {
      return ourInstance;
   }

   private YangStatementParserRegister() {
      this.builtinKeywordRegister();
   }

   public YangStatementParserPolicy getStatementParserPolicy(QName keyword) {
      return null == keyword ? null : (YangStatementParserPolicy)this.policyMap.get(keyword);
   }

   public YangUnknown getUnknownInstance(String keyword, String argStr) {
      return new DefaultYangUnknown(keyword, argStr);
   }

   public YangSchemaContext getSchemeContextInstance() {
      return new YangSchemaContextImpl();
   }

   public Collection<YangStatementParserPolicy> getStatementParserPolicys() {
      return this.policyMap.values();
   }

   public void register(QName keyword, YangStatementParserPolicy statementPolicy) {
      if (this.policyMap.containsKey(keyword)) {
         this.policyMap.replace(keyword, statementPolicy);
      } else {
         this.policyMap.put(keyword, statementPolicy);
      }
   }

   public void unRegister(QName keyword) {
      this.policyMap.remove(keyword);
   }

   private void builtinKeywordRegister() {
      this.register(Yang.UNKNOWN, new YangStatementParserPolicy(Yang.UNKNOWN, DefaultYangUnknown.class, new ArrayList(Arrays.asList(BuildPhase.GRAMMAR))));
      this.register(YangBuiltinKeyword.MODULE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MODULE.getQName(), MainModuleImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_EXPAND, BuildPhase.SCHEMA_MODIFIER, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.YANGVERSION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.YANGVERSION.getQName(), YangVersionImpl.class));
      this.register(YangBuiltinKeyword.NAMESPACE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.NAMESPACE.getQName(), NamespaceImpl.class));
      this.register(YangBuiltinKeyword.PREFIX.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.PREFIX.getQName(), PrefixImpl.class));
      this.register(YangBuiltinKeyword.IMPORT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.IMPORT.getQName(), ImportImpl.class, Arrays.asList(BuildPhase.LINKAGE)));
      this.register(YangBuiltinKeyword.REVISIONDATE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REVISIONDATE.getQName(), RevisionDateImpl.class));
      this.register(YangBuiltinKeyword.INCLUDE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.INCLUDE.getQName(), IncludeImpl.class, Arrays.asList(BuildPhase.LINKAGE)));
      this.register(YangBuiltinKeyword.ORGANIZATION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ORGANIZATION.getQName(), OrganizationImpl.class));
      this.register(YangBuiltinKeyword.CONTACT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CONTACT.getQName(), ContactImpl.class));
      this.register(YangBuiltinKeyword.REVISION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REVISION.getQName(), RevisionImpl.class));
      this.register(YangBuiltinKeyword.SUBMODULE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.SUBMODULE.getQName(), SubModuleImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_EXPAND, BuildPhase.SCHEMA_MODIFIER, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.BELONGSTO.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.BELONGSTO.getQName(), BelongsToImpl.class, new ArrayList(Arrays.asList(BuildPhase.LINKAGE))));
      this.register(YangBuiltinKeyword.TYPEDEF.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.TYPEDEF.getQName(), TypedefImpl.class));
      this.register(YangBuiltinKeyword.TYPE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.TYPE.getQName(), TypeImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      this.register(YangBuiltinKeyword.CONTAINER.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CONTAINER.getQName(), ContainerImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.MUST.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MUST.getQName(), MustImpl.class));
      this.register(YangBuiltinKeyword.ERRORMESSAGE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ERRORMESSAGE.getQName(), ErrorMessageImpl.class));
      this.register(YangBuiltinKeyword.ERRORAPPTAG.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ERRORAPPTAG.getQName(), ErrorAppTagImpl.class));
      this.register(YangBuiltinKeyword.PRESENCE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.PRESENCE.getQName(), PresenceImpl.class));
      this.register(YangBuiltinKeyword.LEAF.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.LEAF.getQName(), LeafImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.DEFAULT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.DEFAULT.getQName(), DefaultImpl.class));
      this.register(YangBuiltinKeyword.UNITS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.UNITS.getQName(), UnitsImpl.class));
      this.register(YangBuiltinKeyword.MANDATORY.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MANDATORY.getQName(), MandatoryImpl.class));
      this.register(YangBuiltinKeyword.LEAFLIST.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.LEAFLIST.getQName(), LeafListImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.MINELEMENTS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MINELEMENTS.getQName(), MinElementsImpl.class));
      this.register(YangBuiltinKeyword.MAXELEMENTS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MAXELEMENTS.getQName(), MaxElementsImpl.class));
      this.register(YangBuiltinKeyword.ORDEREDBY.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ORDEREDBY.getQName(), OrderedByImpl.class));
      this.register(YangBuiltinKeyword.LIST.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.LIST.getQName(), ListImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.KEY.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.KEY.getQName(), KeyImpl.class));
      this.register(YangBuiltinKeyword.UNIQUE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.UNIQUE.getQName(), UniqueImpl.class));
      this.register(YangBuiltinKeyword.CHOICE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CHOICE.getQName(), ChoiceImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.CASE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CASE.getQName(), CaseImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.ANYDATA.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ANYDATA.getQName(), AnyDataImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.ANYXML.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ANYXML.getQName(), AnyxmlImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.GROUPING.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.GROUPING.getQName(), GroupingImpl.class));
      this.register(YangBuiltinKeyword.USES.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.USES.getQName(), UsesImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.REFINE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REFINE.getQName(), RefineImpl.class, Arrays.asList(BuildPhase.SCHEMA_BUILD)));
      this.register(YangBuiltinKeyword.RPC.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.RPC.getQName(), RpcImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.INPUT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.INPUT.getQName(), InputImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.OUTPUT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.OUTPUT.getQName(), OutputImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.ACTION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ACTION.getQName(), ActionImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.NOTIFICATION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.NOTIFICATION.getQName(), NotificationImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.AUGMENT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.AUGMENT.getQName(), AugmentImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_EXPAND, BuildPhase.SCHEMA_TREE)));
      this.register(YangBuiltinKeyword.IDENTITY.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.IDENTITY.getQName(), IdentityImpl.class));
      this.register(YangBuiltinKeyword.BASE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.BASE.getQName(), BaseImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      this.register(YangBuiltinKeyword.EXTENSION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.EXTENSION.getQName(), ExtensionImpl.class));
      this.register(YangBuiltinKeyword.ARGUMENT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ARGUMENT.getQName(), ArgumentImpl.class));
      this.register(YangBuiltinKeyword.YINELEMENT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.YINELEMENT.getQName(), YinElementImpl.class));
      this.register(YangBuiltinKeyword.FEATURE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.FEATURE.getQName(), FeatureImpl.class));
      this.register(YangBuiltinKeyword.IFFEATURE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.IFFEATURE.getQName(), IfFeatureImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      this.register(YangBuiltinKeyword.DEVIATION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.DEVIATION.getQName(), DeviationImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_MODIFIER)));
      this.register(YangBuiltinKeyword.DEVIATE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.DEVIATE.getQName(), DeviateImpl.class, Arrays.asList(BuildPhase.SCHEMA_MODIFIER)));
      this.register(YangBuiltinKeyword.CONFIG.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CONFIG.getQName(), ConfigImpl.class));
      this.register(YangBuiltinKeyword.STATUS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.STATUS.getQName(), StatusImpl.class));
      this.register(YangBuiltinKeyword.DESCRIPTION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.DESCRIPTION.getQName(), DesciptionImpl.class));
      this.register(YangBuiltinKeyword.REFERENCE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REFERENCE.getQName(), ReferenceImpl.class));
      this.register(YangBuiltinKeyword.WHEN.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.WHEN.getQName(), WhenImpl.class));
      this.register(YangBuiltinKeyword.RANGE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.RANGE.getQName(), RangeImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      this.register(YangBuiltinKeyword.LENGTH.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.LENGTH.getQName(), LengthImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      this.register(YangBuiltinKeyword.FRACTIONDIGITS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.FRACTIONDIGITS.getQName(), FractionDigitsImpl.class));
      this.register(YangBuiltinKeyword.PATTERN.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.PATTERN.getQName(), PatternImpl.class));
      this.register(YangBuiltinKeyword.MODIFIER.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MODIFIER.getQName(), ModifierImpl.class));
      this.register(YangBuiltinKeyword.ENUM.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ENUM.getQName(), EnumImpl.class));
      this.register(YangBuiltinKeyword.VALUE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.VALUE.getQName(), ValueImpl.class));
      this.register(YangBuiltinKeyword.BIT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.BIT.getQName(), BitImpl.class));
      this.register(YangBuiltinKeyword.POSITION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.POSITION.getQName(), PositionImpl.class));
      this.register(YangBuiltinKeyword.PATH.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.PATH.getQName(), PathImpl.class));
      this.register(YangBuiltinKeyword.REQUIREINSTANCE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REQUIREINSTANCE.getQName(), RequireInstanceImpl.class));
      this.register(YangBuiltinKeyword.BASE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.BASE.getQName(), BaseImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
   }
}
