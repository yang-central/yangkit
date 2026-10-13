package org.yangcentral.yangkit.model.api.validate;

import org.yangcentral.yangkit.common.api.Builder;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecord;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.impl.validate.ValidatorResultImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A validator result builder that shares a single immutable successful result.
 */
public class ValidatorResultBuilder implements Builder<ValidatorResult> {
   private static final ValidatorResult EMPTY_RESULT = new EmptyValidatorResult();

   private List<ValidatorRecord<?, ?>> validatorRecords;

   public ValidatorResultBuilder() {
   }

   public ValidatorResultBuilder(ValidatorResult validatorResult) {
      this.validatorRecords = validatorResult.getRecords();
   }

   public void addRecord(ValidatorRecord<?, ?> record) {
      if (record != null) {
         if (this.validatorRecords == null) {
            this.validatorRecords = new ArrayList<>();
         }
         this.validatorRecords.add(record);
      }
   }

   public void merge(ValidatorResult validatorResult) {
      if (validatorResult == null) {
         return;
      }
      List<ValidatorRecord<?, ?>> records = validatorResult.getRecords();
      if (records == null || records.isEmpty()) {
         return;
      }
      Iterator<ValidatorRecord<?, ?>> iterator = records.iterator();
      while (iterator.hasNext()) {
         ValidatorRecord<?, ?> record = iterator.next();
         if (record != null) {
            this.addRecord(record);
         }
      }
   }

   @Override
   public ValidatorResult build() {
      if (this.validatorRecords == null || this.validatorRecords.isEmpty()) {
         return EMPTY_RESULT;
      }
      ValidatorResultImpl validatorResult = new ValidatorResultImpl();
      validatorResult.setValidatorRecords(this.validatorRecords);
      return validatorResult;
   }

   public void clear() {
      if (this.validatorRecords != null) {
         this.validatorRecords.clear();
      }
   }

   private static final class EmptyValidatorResult implements ValidatorResult {
      @Override
      public boolean isOk() {
         return true;
      }

      @Override
      public List<ValidatorRecord<?, ?>> getRecords() {
         return null;
      }

      @Override
      public boolean contains(ValidatorRecord<?, ?> record) {
         return false;
      }

      @Override
      public void sort() {
      }

      @Override
      public String print(Severity severity) {
         return "result is true";
      }

      @Override
      public void clear() {
      }

      @Override
      public String toString() {
         return print(Severity.DEBUG);
      }
   }
}
