/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.runtime.interceptors;

import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;

/**
 * Represents a Ballerina Resource Interceptor.
 */
public class ResourceInterceptor {

    private ProgramFile programFile;
    private PackageInfo packageInfo;
    private FunctionInfo interceptorFunction;

    public ResourceInterceptor(ProgramFile bLangProgram, PackageInfo bLangPackage, FunctionInfo functionInfo) {
        this.programFile = bLangProgram;
        this.packageInfo = bLangPackage;
        this.interceptorFunction = functionInfo;
    }

    public ProgramFile getProgramFile() {
        return programFile;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public FunctionInfo getInterceptorFunction() {
        return interceptorFunction;
    }

    @Override
    public String toString() {
        return packageInfo.getPkgPath() + ":" + interceptorFunction.getName();
    }

    /**
     * Represents Interceptor type
     */
    public enum Type {
        REQUEST,
        RESPONSE
    }

    /**
     * Represent Result of an interception.
     */
    public static class Result {

        boolean invokeNext;
        BMessage messageIntercepted;

        public Result(boolean invokeNext, BMessage messageIntercepted) {
            this.invokeNext = invokeNext;
            this.messageIntercepted = messageIntercepted;
        }

        public boolean isInvokeNext() {
            return invokeNext;
        }

        public BMessage getMessageIntercepted() {
            return messageIntercepted;
        }
    }
}
