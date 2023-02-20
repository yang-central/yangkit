package org.yangcentral.yangkit.model.impl.restriction;

import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.stmt.Base;
import org.yangcentral.yangkit.model.api.stmt.Identity;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.Typedef;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IdentityRefImpl extends RestrictionImpl<QName> implements IdentityRef {
   private List<Base> bases = new ArrayList<>();

   public IdentityRefImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public IdentityRefImpl(YangContext context) {
      super(context);
   }

   public List<Base> getBases() {
      return Collections.unmodifiableList(this.bases);
   }

   public boolean addBase(Base identity) {
      Iterator baseIterator = this.bases.iterator();

      Base base;
      do {
         if (!baseIterator.hasNext()) {
            return this.bases.add(identity);
         }

         base = (Base)baseIterator.next();
      } while(!base.getArgStr().equals(identity.getArgStr()));

      return false;
   }

   public boolean evaluated(QName value) {
      URI namespace = value.getNamespace();
      List<Module> modules = this.getContext().getSchemaContext().getModule(namespace);
      if (modules.isEmpty()) {
         return false;
      } else {
         Module module = (Module)modules.get(0);
         Identity identity = module.getIdentity(value.getLocalName());
         if (identity == null) {
            return false;
         } else if (this.bases.size() == 0) {
            return this.getDerived().getType().getRestriction().evaluated(value);
         } else {
            Iterator baseIterator = this.bases.iterator();

            Identity baseIdentity;
            do {
               if (!baseIterator.hasNext()) {
                  return true;
               }

               Base base = (Base)baseIterator.next();
               baseIdentity = base.getIdentity();
            } while(null == baseIdentity || identity.isDerivedOrSelf(baseIdentity));

            return false;
         }
      }
   }

   public List<Base> getEffectiveBases() {
      if (this.bases.size() > 0) {
         return this.bases;
      } else if (this.getDerived() != null) {
         IdentityRef anotherResriction = (IdentityRef)this.getDerived().getType().getRestriction();
         return anotherResriction.getEffectiveBases();
      } else {
         return new ArrayList<>();
      }
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof IdentityRef)) {
         return false;
      } else {
         IdentityRefImpl another = (IdentityRefImpl)obj;
         List<Base> thisBases = this.getEffectiveBases();
         List<Base> anotherBases = another.getEffectiveBases();
         if (thisBases.size() != anotherBases.size()) {
            return false;
         } else {
            Iterator baseIterator = thisBases.iterator();

            Base theSame;
            do {
               if (!baseIterator.hasNext()) {
                  return true;
               }

               Base thisBase = (Base)baseIterator.next();
               theSame = null;
               Iterator anotherBaseIterator = anotherBases.iterator();

               while(anotherBaseIterator.hasNext()) {
                  Base anotherBase = (Base)anotherBaseIterator.next();
                  if (thisBase.getIdentity().equals(anotherBase.getIdentity())) {
                     theSame = anotherBase;
                     break;
                  }
               }
            } while(theSame != null);

            return false;
         }
      }
   }
}
