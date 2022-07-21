package org.yangcentral.yangkit.register;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.Yang;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.model.impl.schema.YangSchemaContextImpl;
import org.yangcentral.yangkit.model.impl.stmt.*;
import org.yangcentral.yangkit.model.impl.stmt.type.*;

import java.util.ArrayList;
import java.util.Arrays;

public class YangStatementImplRegister {

   public static void registerImpl(){
      YangStatementRegister.getInstance().registerYangSchemaContext(YangSchemaContextImpl.class);
      YangStatementRegister.getInstance().registerDefaultUnknown(DefaultYangUnknown.class);
      builtinKeywordRegister();
   }

   private static  void builtinKeywordRegister() {
      YangStatementRegister.getInstance().register(Yang.UNKNOWN, new YangStatementParserPolicy(Yang.UNKNOWN, DefaultYangUnknown.class, new ArrayList(Arrays.asList(BuildPhase.GRAMMAR))));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.MODULE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MODULE.getQName(), MainModuleImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_EXPAND, BuildPhase.SCHEMA_MODIFIER, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.YANGVERSION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.YANGVERSION.getQName(), YangVersionImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.NAMESPACE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.NAMESPACE.getQName(), NamespaceImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.PREFIX.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.PREFIX.getQName(), PrefixImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.IMPORT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.IMPORT.getQName(), ImportImpl.class, Arrays.asList(BuildPhase.LINKAGE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.REVISIONDATE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REVISIONDATE.getQName(), RevisionDateImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.INCLUDE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.INCLUDE.getQName(), IncludeImpl.class, Arrays.asList(BuildPhase.LINKAGE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ORGANIZATION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ORGANIZATION.getQName(), OrganizationImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.CONTACT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CONTACT.getQName(), ContactImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.REVISION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REVISION.getQName(), RevisionImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.SUBMODULE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.SUBMODULE.getQName(), SubModuleImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_EXPAND, BuildPhase.SCHEMA_MODIFIER, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.BELONGSTO.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.BELONGSTO.getQName(), BelongsToImpl.class, new ArrayList(Arrays.asList(BuildPhase.LINKAGE))));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.TYPEDEF.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.TYPEDEF.getQName(), TypedefImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.TYPE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.TYPE.getQName(), TypeImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.CONTAINER.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CONTAINER.getQName(), ContainerImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.MUST.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MUST.getQName(), MustImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ERRORMESSAGE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ERRORMESSAGE.getQName(), ErrorMessageImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ERRORAPPTAG.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ERRORAPPTAG.getQName(), ErrorAppTagImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.PRESENCE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.PRESENCE.getQName(), PresenceImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.LEAF.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.LEAF.getQName(), LeafImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.DEFAULT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.DEFAULT.getQName(), DefaultImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.UNITS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.UNITS.getQName(), UnitsImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.MANDATORY.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MANDATORY.getQName(), MandatoryImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.LEAFLIST.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.LEAFLIST.getQName(), LeafListImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.MINELEMENTS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MINELEMENTS.getQName(), MinElementsImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.MAXELEMENTS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MAXELEMENTS.getQName(), MaxElementsImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ORDEREDBY.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ORDEREDBY.getQName(), OrderedByImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.LIST.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.LIST.getQName(), ListImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.KEY.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.KEY.getQName(), KeyImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.UNIQUE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.UNIQUE.getQName(), UniqueImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.CHOICE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CHOICE.getQName(), ChoiceImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.CASE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CASE.getQName(), CaseImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ANYDATA.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ANYDATA.getQName(), AnyDataImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ANYXML.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ANYXML.getQName(), AnyxmlImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.GROUPING.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.GROUPING.getQName(), GroupingImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.USES.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.USES.getQName(), UsesImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.REFINE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REFINE.getQName(), RefineImpl.class, Arrays.asList(BuildPhase.SCHEMA_BUILD)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.RPC.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.RPC.getQName(), RpcImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.INPUT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.INPUT.getQName(), InputImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.OUTPUT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.OUTPUT.getQName(), OutputImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ACTION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ACTION.getQName(), ActionImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.NOTIFICATION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.NOTIFICATION.getQName(), NotificationImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.AUGMENT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.AUGMENT.getQName(), AugmentImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_BUILD, BuildPhase.SCHEMA_EXPAND, BuildPhase.SCHEMA_TREE)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.IDENTITY.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.IDENTITY.getQName(), IdentityImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.BASE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.BASE.getQName(), BaseImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.EXTENSION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.EXTENSION.getQName(), ExtensionImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ARGUMENT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ARGUMENT.getQName(), ArgumentImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.YINELEMENT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.YINELEMENT.getQName(), YinElementImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.FEATURE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.FEATURE.getQName(), FeatureImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.IFFEATURE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.IFFEATURE.getQName(), IfFeatureImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.DEVIATION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.DEVIATION.getQName(), DeviationImpl.class, Arrays.asList(BuildPhase.GRAMMAR, BuildPhase.SCHEMA_MODIFIER)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.DEVIATE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.DEVIATE.getQName(), DeviateImpl.class, Arrays.asList(BuildPhase.SCHEMA_MODIFIER)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.CONFIG.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.CONFIG.getQName(), ConfigImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.STATUS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.STATUS.getQName(), StatusImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.DESCRIPTION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.DESCRIPTION.getQName(), DescriptionImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.REFERENCE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REFERENCE.getQName(), ReferenceImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.WHEN.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.WHEN.getQName(), WhenImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.RANGE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.RANGE.getQName(), RangeImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.LENGTH.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.LENGTH.getQName(), LengthImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.FRACTIONDIGITS.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.FRACTIONDIGITS.getQName(), FractionDigitsImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.PATTERN.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.PATTERN.getQName(), PatternImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.MODIFIER.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.MODIFIER.getQName(), ModifierImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.ENUM.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.ENUM.getQName(), EnumImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.VALUE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.VALUE.getQName(), ValueImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.BIT.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.BIT.getQName(), BitImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.POSITION.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.POSITION.getQName(), PositionImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.PATH.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.PATH.getQName(), PathImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.REQUIREINSTANCE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.REQUIREINSTANCE.getQName(), RequireInstanceImpl.class));
      YangStatementRegister.getInstance().register(YangBuiltinKeyword.BASE.getQName(), new YangStatementParserPolicy(YangBuiltinKeyword.BASE.getQName(), BaseImpl.class, Arrays.asList(BuildPhase.GRAMMAR)));
   }
}
