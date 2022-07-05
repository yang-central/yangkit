package org.yangcentral.yangkit.parser;

public class ModuleListFile {
   private ModuleListType type;
   private String owner;
   private String repository;
   private String path;
   private String branch;

   public ModuleListFile() {
      this.type = ModuleListType.CAPABILITIES;
   }

   public ModuleListType getType() {
      return this.type;
   }

   public void setType(ModuleListType type) {
      this.type = type;
   }

   public String getOwner() {
      return this.owner;
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public String getRepository() {
      return this.repository;
   }

   public void setRepository(String repository) {
      this.repository = repository;
   }

   public String getPath() {
      return this.path;
   }

   public void setPath(String path) {
      this.path = path;
   }

   public String getBranch() {
      return this.branch;
   }

   public void setBranch(String branch) {
      this.branch = branch;
   }
}
