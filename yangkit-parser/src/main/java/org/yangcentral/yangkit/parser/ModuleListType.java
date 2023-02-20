package org.yangcentral.yangkit.parser;

import com.google.gson.annotations.SerializedName;

public enum ModuleListType {
   @SerializedName("yang-library")
   YANG_LIB,
   @SerializedName("capabilities")
   CAPABILITIES,
}
