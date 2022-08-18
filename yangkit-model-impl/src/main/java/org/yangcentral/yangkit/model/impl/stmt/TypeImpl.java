package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.restriction.Binary;
import org.yangcentral.yangkit.model.api.restriction.Bits;
import org.yangcentral.yangkit.model.api.restriction.BuiltinType;
import org.yangcentral.yangkit.model.api.restriction.Decimal64;
import org.yangcentral.yangkit.model.api.restriction.Enumeration;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.restriction.InstanceIdentifier;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.restriction.Union;
import org.yangcentral.yangkit.model.api.restriction.YangInteger;
import org.yangcentral.yangkit.model.api.restriction.YangString;
import org.yangcentral.yangkit.model.api.stmt.Base;
import org.yangcentral.yangkit.model.api.stmt.Import;
import org.yangcentral.yangkit.model.api.stmt.ModelException;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.type.Bit;
import org.yangcentral.yangkit.model.api.stmt.type.FractionDigits;
import org.yangcentral.yangkit.model.api.stmt.type.Length;
import org.yangcentral.yangkit.model.api.stmt.type.Path;
import org.yangcentral.yangkit.model.api.stmt.type.Pattern;
import org.yangcentral.yangkit.model.api.stmt.type.Range;
import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
import org.yangcentral.yangkit.model.api.stmt.type.YangEnum;
import org.yangcentral.yangkit.model.impl.restriction.BinaryImpl;
import org.yangcentral.yangkit.model.impl.restriction.BitsImpl;
import org.yangcentral.yangkit.model.impl.restriction.Decimal64Impl;
import org.yangcentral.yangkit.model.impl.restriction.EmptyImpl;
import org.yangcentral.yangkit.model.impl.restriction.EnumerationImpl;
import org.yangcentral.yangkit.model.impl.restriction.IdentityRefImpl;
import org.yangcentral.yangkit.model.impl.restriction.InstanceIdentifierImpl;
import org.yangcentral.yangkit.model.impl.restriction.LeafRefImpl;
import org.yangcentral.yangkit.model.impl.restriction.UnionImpl;
import org.yangcentral.yangkit.model.impl.restriction.YangBooleanImpl;
import org.yangcentral.yangkit.model.impl.restriction.YangIntegerImpl;
import org.yangcentral.yangkit.model.impl.restriction.YangStringImpl;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeImpl extends YangBuiltInStatementImpl implements Type {
   private Restriction restriction;
   private Typedef derived;

   public TypeImpl(String argStr) {
      super(argStr);
   }

   public boolean isDerivedType() {
      return !BuiltinType.isBuiltinType(this.getArgStr());
   }

   public Typedef getDerived() {
      return this.derived;
   }

   public Type getBuiltinType() {
      return (this.isDerivedType() ? this.derived.getType().getBuiltinType() : this);
   }

   public Type getBaseType() {
      return this.isDerivedType() ? this.derived.getType() : null;
   }

   public Restriction getRestriction() {
      return this.restriction;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.TYPE.getQName();
   }

   private ValidatorResult buildBits(BitsImpl bits) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean find = false;
      Iterator<YangElement> elementIterator = this.getSubElements().iterator();
      while (elementIterator.hasNext()){
         YangElement subElement = elementIterator.next();
         if(!(subElement instanceof YangBuiltinStatement)){
            continue;
         }
         YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
         if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.BIT.getQName())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            continue;
         }
         Bit bit = (Bit) builtinStatement;
         find = true;
         if(isDerivedType()){
            String yangVersion = this.getContext().getCurModule().getEffectiveYangVersion();
            if (!yangVersion.equals(Yang.VERSION_11)) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNEXPECTED_IDENTIFIER.getFieldName() + " Derived bits can not be restricted."));
               continue;
            }
            Bits base = (Bits)this.getBaseType();
            Map<String, Bit> baseBitMap = (Map)base.getBits().stream().collect(Collectors.toMap(YangStatement::getArgStr, YangStatement::getSelf));
            if (!baseBitMap.containsKey(bit.getArgStr())) {
              validatorResultBuilder.addRecord(ModelUtil.reportError(bit,
                       ErrorCode.UNEXPECTED_IDENTIFIER.getFieldName() + " It should be in base-type's bit set." ));
               continue;
            }

            if (bit.getPosition() != null) {
               Long baseActualPosition = base.getBitActualPosition(bit.getArgStr());
               if (bit.getPosition().getValue() != baseActualPosition) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName() + " The bit's position MUST not be changed." ));
                  continue;
               }
            }
         } else {
            if (bits.getBits().size() > 0 && bits.getMaxPosition() == Bits.MAX_POSITION && bit.getPosition() == null) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.BIT_NO_POSITION.getFieldName() ));
               continue;
            }
         }
         boolean bool = bits.addBit((Bit)builtinStatement);
         if (!bool) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
            continue;
         }

      }
      if (!find && !this.isDerivedType()) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.MANDATORY_MISSING.toString(new String[]{"name=bit"})));
         return validatorResultBuilder.build();
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildBinary(BinaryImpl binary) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean find = false;
      Iterator elementIterator = this.getSubElements().iterator();
      binary.setLength(null);
      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.LENGTH.getQName())) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            } else if (find) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
            } else {
               find = true;
               binary.setLength((Length)builtinStatement);
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildBoolean(YangBooleanImpl aBoolean) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator elementIterator = this.getSubElements().iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildDecimal64(Decimal64Impl decimal64) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean findfractionDigits = false;
      boolean findRange = false;
      Iterator elementIterator = this.getSubElements().iterator();
      decimal64.setFractionDigits(null);
      decimal64.setRange(null);

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.FRACTIONDIGITS.getQName())) {
               if (findfractionDigits) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                          ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
               } else {
                  findfractionDigits = true;
                  decimal64.setFractionDigits((FractionDigits)builtinStatement);
               }
            } else if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.RANGE.getQName())) {
               if (findRange) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                          ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
               } else {
                  findRange = true;
                  decimal64.setRange((Range)builtinStatement);
               }
            } else {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            }
         }
      }

      if (!this.isDerivedType() && decimal64.getFractionDigits() == null) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.MANDATORY_MISSING.toString(new String[]{"name=fraction-digits"})));
         return validatorResultBuilder.build();
      } else {
         validatorResultBuilder.merge(decimal64.validate());
         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult buildEmpty(EmptyImpl empty) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator elementIterator = this.getSubElements().iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildEnumeration(EnumerationImpl enumeration) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean find = false;
      Iterator<YangElement> elementIterator = this.getSubElements().iterator();

      while (elementIterator.hasNext()){
         YangElement subElement = elementIterator.next();
         if(!(subElement instanceof YangBuiltinStatement)){
            continue;
         }
         YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
         if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.ENUM.getQName())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            continue;
         }

         YangEnum yangEnum = (YangEnum)builtinStatement;
         find = true;
         ValidatorRecordBuilder validatorRecordBuilder;
         if (this.isDerivedType()) {
            String yangVersion = this.getContext().getCurModule().getEffectiveYangVersion();
            if (!yangVersion.equals(Yang.VERSION_11)) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNEXPECTED_IDENTIFIER.getFieldName() + " Derived enumeration can not be restricted."));
               continue;
            }

            Enumeration base = (Enumeration)this.getBaseType();
            Map<String, YangEnum> baseYangEnumMap = (Map)base.getEnums().stream().collect(Collectors.toMap(YangStatement::getArgStr, YangStatement::getSelf));
            if (!baseYangEnumMap.containsKey(builtinStatement.getArgStr())) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNEXPECTED_IDENTIFIER.getFieldName() + " It should be in base-type's enum set."));
               continue;
            }

            if (yangEnum.getValue() != null) {
               Integer baseActualValue = base.getEnumActualValue(yangEnum.getArgStr());
               if (yangEnum.getValue().getValue() != baseActualValue) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                          ErrorCode.INVALID_SUBSTATEMENT.getFieldName() + " The enum's value MUST not be changed." ));
                  continue;
               }
            }
         } else if (enumeration.getEnums().size() > 0 && enumeration.getHighestValue() == Enumeration.MAX_VALUE && yangEnum.getValue() == null) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.ENUM_NO_VALUE.getFieldName()));
            continue;
         }

         boolean bool = enumeration.addEnum((YangEnum)builtinStatement);
         if (!bool) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
            continue;
         }

      }
      if (!find && !this.isDerivedType()) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.MANDATORY_MISSING.toString(new String[]{"name=enum"})));
         return validatorResultBuilder.build();
      }
      return validatorResultBuilder.build();

   }

   private ValidatorResult buildIdentityRef(IdentityRefImpl identityRef) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean find = false;
      Iterator<YangElement> elementIterator = this.getSubElements().iterator();
      while (elementIterator.hasNext()){
         YangElement subElement = elementIterator.next();
         if(!(subElement instanceof YangBuiltinStatement)){
            continue;
         }
         YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
         ValidatorRecordBuilder validatorRecordBuilder;
         if (this.isDerivedType()) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.IDENTITYREF_CANNOT_RESTRICTED.getFieldName()));
            continue;
         }

         if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.BASE.getQName())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            continue;
         }
         find = true;
         Base newBase = (Base)builtinStatement;
         if (identityRef.getBases().size() > 0 && this.getContext().getCurModule().getEffectiveYangVersion().equals(Yang.VERSION_1)) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.CARDINALITY_BROKEN.getFieldName()));
            continue;
         }

         boolean bool = identityRef.addBase(newBase);
         if (!bool) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
            continue;
         }
      }
      if (!find && !this.isDerivedType()) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.MANDATORY_MISSING.toString(new String[]{"name=base"})));
         return validatorResultBuilder.build();
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildInstanceIdentifier(InstanceIdentifierImpl instanceIdentifier) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      instanceIdentifier.setRequireInstance(null);
      Iterator<YangElement> elementIterator = this.getSubElements().iterator();
      while (elementIterator.hasNext()){
         YangElement subElement = elementIterator.next();
         if(!(subElement instanceof YangBuiltinStatement)){
            continue;
         }
         YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
         if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.REQUIREINSTANCE.getQName())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            continue;
         }
         RequireInstance newRequireInstance = (RequireInstance)builtinStatement;
         if (this.isDerivedType()) {
            InstanceIdentifier baseInstanceIdentifier = (InstanceIdentifier)this.getBaseType().getRestriction();
            if (baseInstanceIdentifier.isRequireInstance() && !newRequireInstance.value()) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity(),ErrorTag.BAD_ELEMENT,
                       ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getFieldName()));
               if (ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity() == Severity.ERROR) {
                  continue;
               }
            }
         }

         instanceIdentifier.setRequireInstance(newRequireInstance);
      }
      return validatorResultBuilder.build();

   }

   private ValidatorResult buildYangInteger(YangIntegerImpl integer) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator elementIterator = this.getSubElements().iterator();
      integer.setRange(null);
      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.RANGE.getQName())) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            } else {
               Range newRange = (Range)builtinStatement;
               validatorResultBuilder.merge(integer.setRange(newRange));
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildLeafRef(LeafRefImpl leafRef) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator<YangElement> elementIterator = this.getSubElements().iterator();
      while (elementIterator.hasNext()){
         YangElement subElement = elementIterator.next();
         if(!(subElement instanceof YangBuiltinStatement)){
            continue;
         }
         YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
         ValidatorRecordBuilder validatorRecordBuilder;
         if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.REQUIREINSTANCE.getQName())) {
            if (this.getContext().getCurModule().getYangVersion().equals(Yang.VERSION_1)) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               continue;
            }

            RequireInstance newRequireInstance = (RequireInstance) builtinStatement;
            if (this.isDerivedType()) {
               LeafRef baseLeafref = (LeafRef) this.getBaseType().getRestriction();
               if (baseLeafref.isRequireInstance() && !newRequireInstance.value()) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                          ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity(),ErrorTag.BAD_ELEMENT,
                          ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getFieldName()));
                  if (ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity() == Severity.ERROR) {
                     continue;
                  }
               }
            }
            leafRef.setRequireInstance(newRequireInstance);
         } else if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.PATH.getQName())) {
            Path newPath = (Path)builtinStatement;
            if (this.isDerivedType()) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.LEAFREF_CANNOT_RESTRICTED_BY_PATH.getFieldName()));
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.LEAFREF_CANNOT_RESTRICTED_BY_PATH.getFieldName()));
               continue;
            }
            leafRef.setPath(newPath);
         } else {
            validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
            validatorRecordBuilder.setBadElement(builtinStatement);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                    ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            continue;
         }
      }
      return validatorResultBuilder.build();
   }

   private ValidatorResult buildYangString(YangStringImpl string) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator elementIterator = this.getSubElements().iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.LENGTH.getQName())) {
               Length newLength = (Length)builtinStatement;
               if (string.getLength() != null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                          ErrorCode.CARDINALITY_BROKEN.getFieldName()));
               } else {
                  validatorResultBuilder.merge(string.setLength(newLength));
               }
            } else if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.PATTERN.getQName())) {
               Pattern pattern = (Pattern)builtinStatement;
               boolean bool = string.addPattern(pattern);
               if (!bool) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                          ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
               }
            } else {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName() ));
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildUnion(UnionImpl union) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator elementIterator = this.getSubElements().iterator();

      while(elementIterator.hasNext()) {
         YangElement subElement = (YangElement)elementIterator.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement) subElement;
            if (this.isDerivedType()) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                       ErrorCode.UNION_CANNOT_RESTRICTED.getFieldName()));
            } else {
               if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.TYPE.getQName())) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                          ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               } else {
                  Type newType = (Type)builtinStatement;
                  boolean bool = union.addType(newType);
                  if (!bool) {
                     validatorResultBuilder.addRecord(ModelUtil.reportError(builtinStatement,
                             ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  }
               }
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult validateDerived(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      FName fName = new FName(this.getArgStr());
      String prefix = fName.getPrefix();
      boolean isSelf = false;
      if (null == prefix) {
         isSelf = true;
      } else if (this.getContext().getCurModule().isSelfPrefix(prefix)) {
         isSelf = true;
      }

      Module dependModule = null;
      if (isSelf) {
         dependModule = this.getContext().getCurModule();
      } else {
         try {
            dependModule = ModelUtil.findModuleByPrefix(this.getContext(), prefix);
         } catch (ModelException e) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(e.getElement(),
                    e.getDescription()));
            return validatorResultBuilder.build();
         }
      }

      if (isSelf) {
         this.derived = this.getContext().getTypedef(fName.getLocalName());
      }

      if (null == this.derived) {
         this.derived = dependModule.getContext().getTypedef(fName.getLocalName());
      }

      if (null == this.derived) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
         validatorRecordBuilder.setSeverity(Severity.ERROR);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_TYPE.getFieldName()));
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,ErrorCode.UNRECOGNIZED_TYPE.getFieldName()));
         return validatorResultBuilder.build();
      } else {
         ValidatorResult derivedResult = this.derived.build(phase);
         validatorResultBuilder.merge(derivedResult);
         return !derivedResult.isOk() ? validatorResultBuilder.build() : validatorResultBuilder.build();
      }
   }

   private ValidatorResult buildRestriction(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Type base = this.getBuiltinType();
      BuiltinType builtinType = BuiltinType.getBuiltinType(base.getArgStr());
      ValidatorResult result;
      switch (builtinType) {
         case BITS:
            BitsImpl bits = new BitsImpl(this.getContext(), this.derived);
            result = this.buildBits(bits);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = bits;
            break;
         case BINARY:
            BinaryImpl binary = new BinaryImpl(this.getContext(), this.derived);
            result = this.buildBinary(binary);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = binary;
            break;
         case BOOLEAN:
            YangBooleanImpl yangBoolean = new YangBooleanImpl(this.getContext(), this.derived);
            result = this.buildBoolean(yangBoolean);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = yangBoolean;
            break;
         case DECIMAL64:
            Decimal64Impl decimal64 = new Decimal64Impl(this.getContext(), this.derived);
            result = this.buildDecimal64(decimal64);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = decimal64;
            break;
         case EMPTY:
            EmptyImpl empty = new EmptyImpl(this.getContext(), this.derived);
            result = this.buildEmpty(empty);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = empty;
            break;
         case ENUMERATION:
            EnumerationImpl enumeration = new EnumerationImpl(this.getContext(), this.derived);
            result = this.buildEnumeration(enumeration);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = enumeration;
            break;
         case IDENTITYREF:
            IdentityRefImpl identityRef = new IdentityRefImpl(this.getContext(), this.derived);
            result = this.buildIdentityRef(identityRef);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = identityRef;
            break;
         case INSTANCEIDENTIFIER:
            InstanceIdentifierImpl instanceIdentifier = new InstanceIdentifierImpl(this.getContext(), this.derived);
            result = this.buildInstanceIdentifier(instanceIdentifier);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = instanceIdentifier;
            break;
         case INT8:
         case INT16:
         case INT32:
         case INT64:
         case UINT8:
         case UINT16:
         case UINT32:
         case UINT64:
            YangIntegerImpl integer = YangIntegerImpl.getInstance(builtinType, this.getContext(), this.derived);
            result = this.buildYangInteger(integer);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = integer;
            break;
         case LEAFREF:
            LeafRefImpl leafRef = new LeafRefImpl(this.getContext(), this.derived);
            result = this.buildLeafRef(leafRef);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = leafRef;
            break;
         case STRING:
            YangStringImpl string = new YangStringImpl(this.getContext(), this.derived);
            result = this.buildYangString(string);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = string;
            break;
         case UNION:
            UnionImpl union = new UnionImpl(this.getContext(), this.derived);
            result = this.buildUnion(union);
            validatorResultBuilder.merge(result);
            if (!result.isOk()) {
               return validatorResultBuilder.build();
            }

            this.restriction = union;
      }

      return validatorResultBuilder.build();
   }

   public static List<Typedef> getAllDerived(Type type) {
      List<Typedef> typedefs = new ArrayList();
      if (type.isDerivedType()) {
         typedefs.add(type.getDerived());
         Typedef derived = type.getDerived();
         typedefs.addAll(getAllDerived(derived.getType()));
      } else if (type.getRestriction() instanceof Union) {
         Union union = (Union)type.getRestriction();
         Iterator typeIterator = union.getTypes().iterator();

         while(typeIterator.hasNext()) {
            Type subtype = (Type)typeIterator.next();
            typedefs.addAll(getAllDerived(subtype));
         }
      }

      return typedefs;
   }

   protected ValidatorResult buildSelf(BuildPhase phase) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.buildSelf(phase));
      switch (phase) {
         case GRAMMAR:
            boolean lastResult = true;
            if (this.isDerivedType()) {
               FName fName = new FName(this.getArgStr());
               if (fName.getPrefix() != null) {
                  String prefix = fName.getPrefix();
                  Import im = this.getContext().getCurModule().getImportByPrefix(prefix);
                  if (im != null) {
                     im.addReference(this);
                  }
               }

               ValidatorResult derivedResult = this.validateDerived(phase);
               if (!derivedResult.isOk()) {
                  lastResult = false;
               } else {
                  List<Typedef> typedefs = getAllDerived(this);
                  Iterator typedefIterator = typedefs.iterator();

                  while(typedefIterator.hasNext()) {
                     Typedef typedef = (Typedef)typedefIterator.next();
                     typedef.addReference(this);
                  }
               }

               validatorResultBuilder.merge(derivedResult);
            }

            if (lastResult) {
               validatorResultBuilder.merge(this.buildRestriction(phase));
            }
         default:
            return validatorResultBuilder.build();
      }
   }

   public YangStatement getReferenceStatement() {
      return this.derived;
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.restriction instanceof YangInteger) {
         YangInteger yangInteger = (YangInteger)this.restriction;
         if (yangInteger.getEffectiveRange() != null) {
            statements.add(yangInteger.getEffectiveRange());
         }
      } else if (this.restriction instanceof Binary) {
         Binary binary = (Binary)this.restriction;
         if (binary.getEffectiveLength() != null) {
            statements.add(binary.getEffectiveLength());
         }
      } else if (this.restriction instanceof Bits) {
         Bits bits = (Bits)this.restriction;
         if (bits.getEffectiveBits() != null) {
            statements.addAll(bits.getEffectiveBits());
         }
      } else if (this.restriction instanceof Decimal64) {
         Decimal64 decimal64 = (Decimal64)this.restriction;
         if (decimal64.getEffectiveRange() != null) {
            statements.add(decimal64.getEffectiveRange());
         }

         if (decimal64.getEffectiveFractionDigits() != null) {
            statements.add(decimal64.getEffectiveFractionDigits());
         }
      } else if (this.restriction instanceof Enumeration) {
         Enumeration enumeration = (Enumeration)this.restriction;
         if (enumeration.getEffectiveEnums() != null) {
            statements.addAll(enumeration.getEffectiveEnums());
         }
      } else if (this.restriction instanceof IdentityRef) {
         IdentityRef identityRef = (IdentityRef)this.restriction;
         if (identityRef.getEffectiveBases() != null) {
            statements.addAll(identityRef.getEffectiveBases());
         }
      } else if (this.restriction instanceof InstanceIdentifier) {
         InstanceIdentifier instanceIdentifier = (InstanceIdentifier)this.restriction;
         if (instanceIdentifier.getEffectiveRequireInstance() != null) {
            statements.add(instanceIdentifier.getEffectiveRequireInstance());
         }
      } else if (this.restriction instanceof LeafRef) {
         LeafRef leafRef = (LeafRef)this.restriction;
         if (leafRef.getEffectivePath() != null) {
            statements.add(leafRef.getEffectivePath());
         }

         if (leafRef.getEffectiveRequireInstance() != null) {
            statements.add(leafRef.getEffectiveRequireInstance());
         }
      } else if (this.restriction instanceof YangString) {
         YangString yangString = (YangString)this.restriction;
         if (yangString.getEffectiveLength() != null) {
            statements.add(yangString.getEffectiveLength());
         }

         if (yangString.getEffectivePatterns().size() > 0) {
            statements.addAll(yangString.getEffectivePatterns());
         }
      } else if (this.restriction instanceof Union) {
         Union union = (Union)this.restriction;
         for(Type type: union.getTypes()){
            String arg = type.getArgStr();
            if(type.isDerivedType()){
               arg = type.getBuiltinType().getArgStr();
            }
            Type newType = new TypeImpl(arg);
            List<YangStatement> effectiveStatements = type.getEffectiveSubStatements();
            for(YangStatement statement:effectiveStatements){
               newType.addChild(statement);
            }
            newType.setContext(type.getContext());
            newType.init();
            newType.build();
            statements.add(newType);
         }
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
