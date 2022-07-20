package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.BuildPhase;
import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangElement;
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
      return (Type)(this.isDerivedType() ? this.derived.getType().getBuiltinType() : this);
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
      Iterator var4 = this.getSubElements().iterator();

      while(true) {
         while(true) {
            YangElement subElement;
            do {
               if (!var4.hasNext()) {
                  if (!find && !this.isDerivedType()) {
                     ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(this.getElementPosition());
                     validatorRecordBuilder.setBadElement(this);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MANDATORY_MISSING.toString(new String[]{"name=bit"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     return validatorResultBuilder.build();
                  }

                  return validatorResultBuilder.build();
               }

               subElement = (YangElement)var4.next();
            } while(!(subElement instanceof YangBuiltinStatement));

            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.BIT.getQName())) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               Bit bit = (Bit)builtinStatement;
               find = true;
               ValidatorRecordBuilder validatorRecordBuilder;
               if (this.isDerivedType()) {
                  String yangVersion = this.getContext().getCurModule().getEffectiveYangVersion();
                  if (!yangVersion.equals("1.1")) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                     validatorRecordBuilder.setBadElement(builtinStatement);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNEXPECTED_IDENTIFIER.getFieldName() + " Derived bits can not be restricted."));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     continue;
                  }

                  Bits base = (Bits)this.getBaseType();
                  Map<String, Bit> baseBitMap = (Map)base.getBits().stream().collect(Collectors.toMap(YangStatement::getArgStr, YangStatement::getSelf));
                  if (!baseBitMap.containsKey(bit.getArgStr())) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(bit.getElementPosition());
                     validatorRecordBuilder.setBadElement(bit);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNEXPECTED_IDENTIFIER.getFieldName() + " It should be in base-type's bit set."));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     continue;
                  }

                  if (bit.getPosition() != null) {
                     Long baseActualPosition = base.getBitActualPosition(bit.getArgStr());
                     if (bit.getPosition().getValue() != baseActualPosition) {
                        validatorRecordBuilder = new ValidatorRecordBuilder();
                        validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                        validatorRecordBuilder.setSeverity(Severity.ERROR);
                        validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                        validatorRecordBuilder.setBadElement(builtinStatement);
                        validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName() + " The bit's position MUST not be changed."));
                        validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                        continue;
                     }
                  }
               } else if (bits.getBits().size() > 0 && bits.getMaxPosition() == 4294967295L && bit.getPosition() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.BIT_NO_POSITION.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  continue;
               }

               boolean bool = bits.addBit((Bit)builtinStatement);
               if (!bool) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }
         }
      }
   }

   private ValidatorResult buildBinary(BinaryImpl binary) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean find = false;
      Iterator var4 = this.getSubElements().iterator();

      while(var4.hasNext()) {
         YangElement subElement = (YangElement)var4.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            ValidatorRecordBuilder validatorRecordBuilder;
            if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.LENGTH.getQName())) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else if (find) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
      Iterator var3 = this.getSubElements().iterator();

      while(var3.hasNext()) {
         YangElement subElement = (YangElement)var3.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
            validatorRecordBuilder.setBadElement(builtinStatement);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildDecimal64(Decimal64Impl decimal64) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean findfractionDigits = false;
      boolean findRange = false;
      Iterator var5 = this.getSubElements().iterator();

      while(var5.hasNext()) {
         YangElement subElement = (YangElement)var5.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            ValidatorRecordBuilder validatorRecordBuilder;
            if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.FRACTIONDIGITS.getQName())) {
               if (findfractionDigits) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  findfractionDigits = true;
                  decimal64.setFractionDigits((FractionDigits)builtinStatement);
               }
            } else if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.RANGE.getQName())) {
               if (findRange) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  findRange = true;
                  decimal64.setRange((Range)builtinStatement);
               }
            } else {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         }
      }

      if (!this.isDerivedType() && decimal64.getFractionDigits() == null) {
         ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
         validatorRecordBuilder.setBadElement(this);
         validatorRecordBuilder.setErrorPath(this.getElementPosition());
         validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MANDATORY_MISSING.toString(new String[]{"name=fraction-digits"})));
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         return validatorResultBuilder.build();
      } else {
         validatorResultBuilder.merge(decimal64.validate());
         return validatorResultBuilder.build();
      }
   }

   private ValidatorResult buildEmpty(EmptyImpl empty) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var3 = this.getSubElements().iterator();

      while(var3.hasNext()) {
         YangElement subElement = (YangElement)var3.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
            validatorRecordBuilder.setBadElement(builtinStatement);
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildEnumeration(EnumerationImpl enumeration) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean find = false;
      Iterator var4 = this.getSubElements().iterator();

      while(true) {
         while(true) {
            YangElement subElement;
            do {
               if (!var4.hasNext()) {
                  if (!find && !this.isDerivedType()) {
                     ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(this.getElementPosition());
                     validatorRecordBuilder.setBadElement(this);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MANDATORY_MISSING.toString(new String[]{"name=enum"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     return validatorResultBuilder.build();
                  }

                  return validatorResultBuilder.build();
               }

               subElement = (YangElement)var4.next();
            } while(!(subElement instanceof YangBuiltinStatement));

            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.ENUM.getQName())) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               YangEnum yangEnum = (YangEnum)builtinStatement;
               find = true;
               ValidatorRecordBuilder validatorRecordBuilder;
               if (this.isDerivedType()) {
                  String yangVersion = this.getContext().getCurModule().getEffectiveYangVersion();
                  if (!yangVersion.equals("1.1")) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                     validatorRecordBuilder.setBadElement(builtinStatement);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNEXPECTED_IDENTIFIER.getFieldName() + " Derived enumeration can not be restricted."));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     continue;
                  }

                  Enumeration base = (Enumeration)this.getBaseType();
                  Map<String, YangEnum> baseYangEnumMap = (Map)base.getEnums().stream().collect(Collectors.toMap(YangStatement::getArgStr, YangStatement::getSelf));
                  if (!baseYangEnumMap.containsKey(builtinStatement.getArgStr())) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                     validatorRecordBuilder.setBadElement(builtinStatement);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNEXPECTED_IDENTIFIER.getFieldName() + " It should be in base-type's enum set."));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     continue;
                  }

                  if (yangEnum.getValue() != null) {
                     Integer baseActualValue = base.getEnumActualValue(yangEnum.getArgStr());
                     if (yangEnum.getValue().getValue() != baseActualValue) {
                        validatorRecordBuilder = new ValidatorRecordBuilder();
                        validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                        validatorRecordBuilder.setSeverity(Severity.ERROR);
                        validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                        validatorRecordBuilder.setBadElement(builtinStatement);
                        validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.INVALID_SUBSTATEMENT.getFieldName() + " The enum's value MUST not be changed."));
                        validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                        continue;
                     }
                  }
               } else if (enumeration.getEnums().size() > 0 && enumeration.getHighestValue() == Integer.MAX_VALUE && yangEnum.getValue() == null) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.ENUM_NO_VALUE.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  continue;
               }

               boolean bool = enumeration.addEnum((YangEnum)builtinStatement);
               if (!bool) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }
         }
      }
   }

   private ValidatorResult buildIdentityRef(IdentityRefImpl identityRef) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      boolean find = false;
      Iterator var4 = this.getSubElements().iterator();

      while(true) {
         while(true) {
            YangElement subElement;
            do {
               if (!var4.hasNext()) {
                  if (!find && !this.isDerivedType()) {
                     ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(this.getElementPosition());
                     validatorRecordBuilder.setBadElement(this);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.MANDATORY_MISSING.toString(new String[]{"name=base"})));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     return validatorResultBuilder.build();
                  }

                  return validatorResultBuilder.build();
               }

               subElement = (YangElement)var4.next();
            } while(!(subElement instanceof YangBuiltinStatement));

            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            ValidatorRecordBuilder validatorRecordBuilder;
            if (this.isDerivedType()) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.IDENTITYREF_CANNOT_RESTRICTED.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.BASE.getQName())) {
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               find = true;
               Base newBase = (Base)builtinStatement;
               if (identityRef.getBases().size() > 0 && this.getContext().getCurModule().getEffectiveYangVersion().equals("1")) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.CARDINALITY_BROKEN.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  boolean bool = identityRef.addBase(newBase);
                  if (!bool) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                     validatorRecordBuilder.setBadElement(builtinStatement);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  }
               }
            }
         }
      }
   }

   private ValidatorResult buildInstanceIdentifier(InstanceIdentifierImpl instanceIdentifier) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var3 = this.getSubElements().iterator();

      while(true) {
         while(true) {
            YangElement subElement;
            do {
               if (!var3.hasNext()) {
                  return validatorResultBuilder.build();
               }

               subElement = (YangElement)var3.next();
            } while(!(subElement instanceof YangBuiltinStatement));

            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.REQUIREINSTANCE.getQName())) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               RequireInstance newRequireInstance = (RequireInstance)builtinStatement;
               if (this.isDerivedType()) {
                  InstanceIdentifier baseInstanceIdentifier = (InstanceIdentifier)this.getBaseType().getRestriction();
                  if (baseInstanceIdentifier.isRequireInstance() && !newRequireInstance.value()) {
                     ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity());
                     validatorRecordBuilder.setBadElement(builtinStatement);
                     validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getFieldName()));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                     if (ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity() == Severity.ERROR) {
                        continue;
                     }
                  }
               }

               instanceIdentifier.setRequireInstance(newRequireInstance);
            }
         }
      }
   }

   private ValidatorResult buildYangInteger(YangIntegerImpl integer) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var3 = this.getSubElements().iterator();

      while(var3.hasNext()) {
         YangElement subElement = (YangElement)var3.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.RANGE.getQName())) {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
      Iterator var3 = this.getSubElements().iterator();

      while(true) {
         while(true) {
            while(true) {
               YangElement subElement;
               do {
                  if (!var3.hasNext()) {
                     return validatorResultBuilder.build();
                  }

                  subElement = (YangElement)var3.next();
               } while(!(subElement instanceof YangBuiltinStatement));

               YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
               ValidatorRecordBuilder validatorRecordBuilder;
               if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.REQUIREINSTANCE.getQName())) {
                  if (this.getContext().getCurModule().getYangVersion().equals("1")) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                     validatorRecordBuilder.setBadElement(builtinStatement);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     RequireInstance newRequireInstance = (RequireInstance)builtinStatement;
                     if (this.isDerivedType()) {
                        LeafRef baseLeafref = (LeafRef)this.getBaseType().getRestriction();
                        if (baseLeafref.isRequireInstance() && !newRequireInstance.value()) {
                           validatorRecordBuilder = new ValidatorRecordBuilder();
                           validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                           validatorRecordBuilder.setSeverity(ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity());
                           validatorRecordBuilder.setBadElement(builtinStatement);
                           validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                           validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getFieldName()));
                           validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                           if (ErrorCode.DERIVEDTYPE_EXPAND_VALUESPACE.getSeverity() == Severity.ERROR) {
                              continue;
                           }
                        }
                     }

                     leafRef.setRequireInstance(newRequireInstance);
                  }
               } else if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.PATH.getQName())) {
                  Path newPath = (Path)builtinStatement;
                  if (this.isDerivedType()) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                     validatorRecordBuilder.setBadElement(builtinStatement);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.LEAFREF_CANNOT_RESTRICTED_BY_PATH.getFieldName()));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                  } else {
                     leafRef.setPath(newPath);
                  }
               } else {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            }
         }
      }
   }

   private ValidatorResult buildYangString(YangStringImpl string) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var3 = this.getSubElements().iterator();

      while(var3.hasNext()) {
         YangElement subElement = (YangElement)var3.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)subElement;
            if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.LENGTH.getQName())) {
               Length newLength = (Length)builtinStatement;
               if (string.getLength() != null) {
                  ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.CARDINALITY_BROKEN.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  validatorResultBuilder.merge(string.setLength(newLength));
               }
            } else if (builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.PATTERN.getQName())) {
               Pattern pattern = (Pattern)builtinStatement;
               boolean bool = string.addPattern(pattern);
               if (!bool) {
                  ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               }
            } else {
               ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            }
         }
      }

      return validatorResultBuilder.build();
   }

   private ValidatorResult buildUnion(UnionImpl union) {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      Iterator var3 = this.getSubElements().iterator();

      while(var3.hasNext()) {
         YangElement subElement = (YangElement)var3.next();
         if (subElement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement;
            ValidatorRecordBuilder validatorRecordBuilder;
            if (this.isDerivedType()) {
               builtinStatement = (YangBuiltinStatement)subElement;
               validatorRecordBuilder = new ValidatorRecordBuilder();
               validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
               validatorRecordBuilder.setSeverity(Severity.ERROR);
               validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
               validatorRecordBuilder.setBadElement(builtinStatement);
               validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNION_CANNOT_RESTRICTED.getFieldName()));
               validatorResultBuilder.addRecord(validatorRecordBuilder.build());
            } else {
               builtinStatement = (YangBuiltinStatement)subElement;
               if (!builtinStatement.getYangKeyword().equals(YangBuiltinKeyword.TYPE.getQName())) {
                  validatorRecordBuilder = new ValidatorRecordBuilder();
                  validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                  validatorRecordBuilder.setSeverity(Severity.ERROR);
                  validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                  validatorRecordBuilder.setBadElement(builtinStatement);
                  validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName()));
                  validatorResultBuilder.addRecord(validatorRecordBuilder.build());
               } else {
                  Type newType = (Type)builtinStatement;
                  boolean bool = union.addType(newType);
                  if (!bool) {
                     validatorRecordBuilder = new ValidatorRecordBuilder();
                     validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
                     validatorRecordBuilder.setSeverity(Severity.ERROR);
                     validatorRecordBuilder.setErrorPath(builtinStatement.getElementPosition());
                     validatorRecordBuilder.setBadElement(builtinStatement);
                     validatorRecordBuilder.setErrorMessage(new ErrorMessage(ErrorCode.DUPLICATE_DEFINITION.getFieldName()));
                     validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
         } catch (ModelException var9) {
            ValidatorRecordBuilder<Position, YangStatement> validatorRecordBuilder = new ValidatorRecordBuilder();
            validatorRecordBuilder.setErrorTag(ErrorTag.BAD_ELEMENT);
            validatorRecordBuilder.setSeverity(Severity.ERROR);
            validatorRecordBuilder.setErrorPath(var9.getElement().getElementPosition());
            validatorRecordBuilder.setBadElement(var9.getElement());
            validatorRecordBuilder.setErrorMessage(new ErrorMessage(var9.getDescription()));
            validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
         validatorResultBuilder.addRecord(validatorRecordBuilder.build());
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
         Iterator var3 = union.getTypes().iterator();

         while(var3.hasNext()) {
            Type subtype = (Type)var3.next();
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
                  Iterator var7 = typedefs.iterator();

                  while(var7.hasNext()) {
                     Typedef typedef = (Typedef)var7.next();
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
         statements.addAll(union.getTypes());
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
