/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JInsKind;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JType;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.JTypeTags;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRGlobalVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRPackage;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLProcIns;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.UnaryOP;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.model.VarKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SchedulerPolicy;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DCMPL;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DNEG;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DSUB;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2B;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.I2S;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INEG;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.IUSHR;
import static org.objectweb.asm.Opcodes.IXOR;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LAND;
import static org.objectweb.asm.Opcodes.LCMP;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LNEG;
import static org.objectweb.asm.Opcodes.LOR;
import static org.objectweb.asm.Opcodes.LSHL;
import static org.objectweb.asm.Opcodes.LSHR;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.LUSHR;
import static org.objectweb.asm.Opcodes.LXOR;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCheckCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generateCheckCastToByte;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.generatePlatformCheckCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCastGen.getTargetClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_MAP_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ANNOTATION_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ARRAY_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BTYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BXML_QNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.BYTE_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.DECIMAL_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.ERROR_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.FUNCTION_POINTER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.INT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.JSON_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.LONG_STREAM;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAP_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MATH_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_INIT_CLASS_NAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.OBJECT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.SHORT_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRAND;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_UTILS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.STRING_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_TYPE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TUPLE_VALUE_IMPL;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPEDESC_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TYPE_CHECKER;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_FACTORY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_QNAME;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.XML_VALUE;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmMethodGen.BalToJVMIndexMap;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.currentClass;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.getPackageName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lambdaIndex;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lambdas;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupGlobalVarClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.lookupTypeDef;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.symbolTable;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTerminatorGen.TerminatorGenerator.toNameString;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.duplicateServiceTypeWithAnnots;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.getTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadExternalType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmTypeGen.loadType;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmValueGen.getTypeValueClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JCast;
import static org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropMethodGen.JInstruction;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.BinaryOp;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.ConstantLoad;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.FPLoad;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.FieldAccess;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.IsLike;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.Move;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewArray;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewError;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewInstance;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStringXMLQName;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewStructure;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewTypeDesc;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLComment;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLElement;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLQName;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.NewXMLText;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.TypeCast;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.TypeTest;
import static org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator.XMLAccess;

/**
 * BIR instructions to JVM byte code generation related methods.
 *
 * @since 1.2.0
 */
public class JvmInstructionGen {

    public static final String I_STRING_VALUE = "org/ballerinalang/jvm/values/StringValue";
    public static final String B_STRING_VALUE = "org/ballerinalang/jvm/values/api/BString";
    public static final String NON_BMP_STRING_VALUE = "org/ballerinalang/jvm/values/NonBmpStringValue";
    public static boolean isBString = false;

    static void addBoxInsn(MethodVisitor mv, @Nilable BType bType) {

        if (bType != null) {
            generateCast(mv, bType, symbolTable.anyType);
        }
    }

    public static void addUnboxInsn(MethodVisitor mv, @Nilable BType bType) {

        if (bType != null) {
            generateCast(mv, symbolTable.anyType, bType);
        }
    }

    static void addJUnboxInsn(MethodVisitor mv, @Nilable JType jType) {

        if (jType == null) {
            return;
        }
        if (jType.jTag == JTypeTags.JBYTE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJByte", String.format("(L%s;)B", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JCHAR) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJChar", String.format("(L%s;)C", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JSHORT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJShort", String.format("(L%s;)S", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JINT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJInt", String.format("(L%s;)I", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JLONG) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJLong", String.format("(L%s;)J", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JFLOAT) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJFloat", String.format("(L%s;)F", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JDOUBLE) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJDouble", String.format("(L%s;)D", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JBOOLEAN) {
            mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "anyToJBoolean", String.format("(L%s;)Z", OBJECT), false);
        } else if (jType.jTag == JTypeTags.JREF) {
            mv.visitTypeInsn(CHECKCAST, ((JType.JRefType) jType).typeValue);
            //} else {
            //    error err = error(io:sprintf("Unboxing is not supported for '%s'", bType));
            //    panic err;
        }
    }

    public static void generateVarLoad(MethodVisitor mv, BIRVariableDcl varDcl, String currentPackageName,
                                       int valueIndex) {

        BType bType = varDcl.type;

        if (varDcl.kind == VarKind.GLOBAL) {
            BIRGlobalVariableDcl globalVar = (BIRGlobalVariableDcl) varDcl;
            PackageID modId = globalVar.pkgId;
            String moduleName = getPackageName(modId.orgName, modId.name);

            String varName = varDcl.name.value;
            String className = lookupGlobalVarClassName(moduleName, varName);

            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
            return;
        } else if (varDcl.kind == VarKind.SELF) {
            mv.visitVarInsn(ALOAD, 0);
            return;
        } else if (varDcl.kind == VarKind.CONSTANT) {
            String varName = varDcl.name.value;
            PackageID moduleId = ((BIRGlobalVariableDcl) varDcl).pkgId;
            String pkgName = getPackageName(moduleId.orgName, moduleId.name);
            String className = lookupGlobalVarClassName(pkgName, varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(GETSTATIC, className, varName, typeSig);
            return;
        }

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitVarInsn(LLOAD, valueIndex);
        } else if (bType.tag == TypeTags.BYTE) {
            mv.visitVarInsn(ILOAD, valueIndex);
            mv.visitInsn(I2B);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
        } else if (bType.tag == TypeTags.FLOAT) {
            mv.visitVarInsn(DLOAD, valueIndex);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (bType.tag == TypeTags.ARRAY ||
                TypeTags.isStringTypeTag(bType.tag) ||
                bType.tag == TypeTags.MAP ||
                bType.tag == TypeTags.STREAM ||
                bType.tag == TypeTags.ANY ||
                bType.tag == TypeTags.ANYDATA ||
                bType.tag == TypeTags.NIL ||
                bType.tag == TypeTags.UNION ||
                bType.tag == TypeTags.TUPLE ||
                bType.tag == TypeTags.RECORD ||
                bType.tag == TypeTags.ERROR ||
                bType.tag == TypeTags.JSON ||
                bType.tag == TypeTags.FUTURE ||
                bType.tag == TypeTags.OBJECT ||
                bType.tag == TypeTags.DECIMAL ||
                TypeTags.isXMLTypeTag(bType.tag) ||
                bType.tag == TypeTags.INVOKABLE ||
                bType.tag == TypeTags.FINITE ||
                bType.tag == TypeTags.HANDLE ||
                bType.tag == TypeTags.TYPEDESC) {
            mv.visitVarInsn(ALOAD, valueIndex);
        } else if (bType.tag == JTypeTags.JTYPE) {
            generateJVarLoad(mv, (JType) bType, currentPackageName, valueIndex);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", bType));
        }
    }

    private static void generateJVarLoad(MethodVisitor mv, JType jType, String currentPackageName, int valueIndex) {

        if (jType.jTag == JTypeTags.JBYTE) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JCHAR) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JSHORT) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JINT) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JLONG) {
            mv.visitVarInsn(LLOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JFLOAT) {
            mv.visitVarInsn(FLOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JDOUBLE) {
            mv.visitVarInsn(DLOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JBOOLEAN) {
            mv.visitVarInsn(ILOAD, valueIndex);
        } else if (jType.jTag == JTypeTags.JARRAY ||
                jType.jTag == JTypeTags.JREF) {
            mv.visitVarInsn(ALOAD, valueIndex);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", jType));
        }
    }

    public static void generateVarStore(MethodVisitor mv, BIRVariableDcl varDcl, String currentPackageName,
                                        int valueIndex) {

        BType bType = varDcl.type;

        if (varDcl.kind == VarKind.GLOBAL) {
            String varName = varDcl.name.value;
            String className = lookupGlobalVarClassName(currentPackageName, varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        } else if (varDcl.kind == VarKind.CONSTANT) {
            String varName = varDcl.name.value;
            PackageID moduleId = ((BIRGlobalVariableDcl) varDcl).pkgId;
            String pkgName = getPackageName(moduleId.orgName, moduleId.name);
            String className = lookupGlobalVarClassName(pkgName, varName);
            String typeSig = getTypeDesc(bType);
            mv.visitFieldInsn(PUTSTATIC, className, varName, typeSig);
            return;
        }

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            mv.visitVarInsn(LSTORE, valueIndex);
        } else if (bType.tag == TypeTags.BYTE) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType.tag == TypeTags.FLOAT) {
            mv.visitVarInsn(DSTORE, valueIndex);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (bType.tag == TypeTags.ARRAY ||
                TypeTags.isStringTypeTag(bType.tag) ||
                bType.tag == TypeTags.MAP ||
                bType.tag == TypeTags.STREAM ||
                bType.tag == TypeTags.ANY ||
                bType.tag == TypeTags.ANYDATA ||
                bType.tag == TypeTags.NIL ||
                bType.tag == TypeTags.UNION ||
                bType.tag == TypeTags.TUPLE ||
                bType.tag == TypeTags.DECIMAL ||
                bType.tag == TypeTags.RECORD ||
                bType.tag == TypeTags.ERROR ||
                bType.tag == TypeTags.JSON ||
                bType.tag == TypeTags.FUTURE ||
                bType.tag == TypeTags.OBJECT ||
                bType.tag == TypeTags.SERVICE ||
                TypeTags.isXMLTypeTag(bType.tag) ||
                bType.tag == TypeTags.INVOKABLE ||
                bType.tag == TypeTags.FINITE ||
                bType.tag == TypeTags.HANDLE ||
                bType.tag == TypeTags.TYPEDESC) {
            mv.visitVarInsn(ASTORE, valueIndex);
        } else if (bType.tag == JTypeTags.JTYPE) {
            generateJVarStore(mv, (JType) bType, currentPackageName, valueIndex);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", bType));
        }
    }

    private static void generateJVarStore(MethodVisitor mv, JType jType, String currentPackageName, int valueIndex) {

        if (jType.jTag == JTypeTags.JBYTE) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JCHAR) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JSHORT) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JINT) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JLONG) {
            mv.visitVarInsn(LSTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JFLOAT) {
            mv.visitVarInsn(FSTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JDOUBLE) {
            mv.visitVarInsn(DSTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JBOOLEAN) {
            mv.visitVarInsn(ISTORE, valueIndex);
        } else if (jType.jTag == JTypeTags.JARRAY ||
                jType.jTag == JTypeTags.JREF) {
            mv.visitVarInsn(ASTORE, valueIndex);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type " + String.format("%s", jType));
        }
    }

    /**
     * Instruction generator helper class to hold its enclosing pkg and index map.
     *
     * @since 1.2.0
     */
    public static class InstructionGenerator {

        MethodVisitor mv;
        BalToJVMIndexMap indexMap;
        String currentPackageName;
        BIRPackage currentPackage;

        public InstructionGenerator(MethodVisitor mv, BalToJVMIndexMap indexMap, BIRPackage currentPackage) {

            this.mv = mv;
            this.indexMap = indexMap;
            this.currentPackage = currentPackage;
            this.currentPackageName = getPackageName(currentPackage.org.value, currentPackage.name.value);
        }

        void generatePlatformIns(JInstruction ins) {

            if (ins.jKind == JInsKind.JCAST) {
                JCast castIns = (JCast) ins;
                BType targetType = castIns.targetType;
                this.loadVar(castIns.rhsOp.variableDcl);
                generatePlatformCheckCast(this.mv, this.indexMap, castIns.rhsOp.variableDcl.type, targetType);
                this.storeToVar(castIns.lhsOp.variableDcl);
            }
        }

        void generateMoveIns(Move moveIns) {

            this.loadVar(moveIns.rhsOp.variableDcl);
            this.storeToVar(moveIns.lhsOp.variableDcl);
        }

        void generateBinaryOpIns(BinaryOp binaryIns) {

            InstructionKind insKind = binaryIns.kind;
            switch (insKind) {
                case ADD:
                    this.generateAddIns(binaryIns);
                    break;
                case SUB:
                    this.generateSubIns(binaryIns);
                    break;
                case MUL:
                    this.generateMulIns(binaryIns);
                    break;
                case DIV:
                    this.generateDivIns(binaryIns);
                    break;
                case MOD:
                    this.generateRemIns(binaryIns);
                    break;
                case EQUAL:
                    this.generateEqualIns(binaryIns);
                    break;
                case NOT_EQUAL:
                    this.generateNotEqualIns(binaryIns);
                    break;
                case GREATER_THAN:
                    this.generateGreaterThanIns(binaryIns);
                    break;
                case GREATER_EQUAL:
                    this.generateGreaterEqualIns(binaryIns);
                    break;
                case LESS_THAN:
                    this.generateLessThanIns(binaryIns);
                    break;
                case LESS_EQUAL:
                    this.generateLessEqualIns(binaryIns);
                    break;
                case REF_EQUAL:
                    this.generateRefEqualIns(binaryIns);
                    break;
                case REF_NOT_EQUAL:
                    this.generateRefNotEqualIns(binaryIns);
                    break;
                case CLOSED_RANGE:
                    this.generateClosedRangeIns(binaryIns);
                    break;
                case HALF_OPEN_RANGE:
                    this.generateClosedRangeIns(binaryIns);
                    break;
                case ANNOT_ACCESS:
                    this.generateAnnotAccessIns(binaryIns);
                    break;
                case BITWISE_AND:
                    this.generateBitwiseAndIns(binaryIns);
                    break;
                case BITWISE_OR:
                    this.generateBitwiseOrIns(binaryIns);
                    break;
                case BITWISE_XOR:
                    this.generateBitwiseXorIns(binaryIns);
                    break;
                case BITWISE_LEFT_SHIFT:
                    this.generateBitwiseLeftShiftIns(binaryIns);
                    break;
                case BITWISE_RIGHT_SHIFT:
                    this.generateBitwiseRightShiftIns(binaryIns);
                    break;
                case BITWISE_UNSIGNED_RIGHT_SHIFT:
                    this.generateBitwiseUnsignedRightShiftIns(binaryIns);
                    break;
                default:
                    throw new BLangCompilerException("JVM generation is not supported for instruction kind : " +
                            String.format("%s", insKind));
            }
        }

        void generateBinaryRhsAndLhsLoad(BinaryOp binaryIns) {

            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);
        }

        private void generateLessThanIns(BinaryOp binaryIns) {

            this.generateBinaryCompareIns(binaryIns, IFLT);
        }

        private void generateGreaterThanIns(BinaryOp binaryIns) {

            this.generateBinaryCompareIns(binaryIns, IFGT);
        }

        private void generateLessEqualIns(BinaryOp binaryIns) {

            this.generateBinaryCompareIns(binaryIns, IFLE);

        }

        private void generateGreaterEqualIns(BinaryOp binaryIns) {

            this.generateBinaryCompareIns(binaryIns, IFGE);
        }

        private void generateBinaryCompareIns(BinaryOp binaryIns, int opcode) {

            if (opcode != IFLT && opcode != IFGT && opcode != IFLE && opcode != IFGE) {
                throw new BLangCompilerException(String.format("Unsupported opcode '%s' for binary operator.", opcode));
            }

            this.generateBinaryRhsAndLhsLoad(binaryIns);
            Label label1 = new Label();
            Label label2 = new Label();

            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;

            if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
                this.mv.visitInsn(LCMP);
                this.mv.visitJumpInsn(opcode, label1);
            } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
                if (opcode == IFLT) {
                    this.mv.visitJumpInsn(IF_ICMPLT, label1);
                } else if (opcode != IFGT) {
                    this.mv.visitJumpInsn(IF_ICMPGT, label1);
                } else if (opcode != IFLE) {
                    this.mv.visitJumpInsn(IF_ICMPLE, label1);
                } else if (opcode == IFGE) {
                    this.mv.visitJumpInsn(IF_ICMPGE, label1);
                }
            } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DCMPL);
                this.mv.visitJumpInsn(opcode, label1);
            } else if (lhsOpType.tag == TypeTags.DECIMAL && rhsOpType.tag == TypeTags.DECIMAL) {
                String compareFuncName = this.getDecimalCompareFuncName(opcode);
                this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, compareFuncName,
                        String.format("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
                this.storeToVar(binaryIns.lhsOp.variableDcl);
                return;
            }

            this.mv.visitInsn(ICONST_0);
            this.mv.visitJumpInsn(GOTO, label2);

            this.mv.visitLabel(label1);
            this.mv.visitInsn(ICONST_1);

            this.mv.visitLabel(label2);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        private String getDecimalCompareFuncName(int opcode) {

            if (opcode == IFGT) {
                return "checkDecimalGreaterThan";
            } else if (opcode == IFGE) {
                return "checkDecimalGreaterThanOrEqual";
            } else if (opcode == IFLT) {
                return "checkDecimalLessThan";
            } else if (opcode == IFLE) {
                return "checkDecimalLessThanOrEqual";
            } else {
                throw new BLangCompilerException(String.format("Opcode: '%s' is not a comparison opcode.", opcode));
            }
        }

        void generateEqualIns(BinaryOp binaryIns) {

            this.generateBinaryRhsAndLhsLoad(binaryIns);

            Label label1 = new Label();
            Label label2 = new Label();

            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;

            if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
                this.mv.visitInsn(LCMP);
                this.mv.visitJumpInsn(IFNE, label1);
            } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
                this.mv.visitJumpInsn(IF_ICMPNE, label1);
            } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DCMPL);
                this.mv.visitJumpInsn(IFNE, label1);
            } else if (lhsOpType.tag == TypeTags.BOOLEAN && rhsOpType.tag == TypeTags.BOOLEAN) {
                this.mv.visitJumpInsn(IF_ICMPNE, label1);
            } else if (lhsOpType.tag == TypeTags.DECIMAL && rhsOpType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkDecimalEqual",
                        String.format("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
                this.storeToVar(binaryIns.lhsOp.variableDcl);
                return;
            } else {
                this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isEqual",
                        String.format("(L%s;L%s;)Z", OBJECT, OBJECT), false);
                this.storeToVar(binaryIns.lhsOp.variableDcl);
                return;
            }

            this.mv.visitInsn(ICONST_1);
            this.mv.visitJumpInsn(GOTO, label2);

            this.mv.visitLabel(label1);
            this.mv.visitInsn(ICONST_0);

            this.mv.visitLabel(label2);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateNotEqualIns(BinaryOp binaryIns) {

            this.generateBinaryRhsAndLhsLoad(binaryIns);

            Label label1 = new Label();
            Label label2 = new Label();

            // It is assumed that both operands are of same type
            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
            if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
                this.mv.visitInsn(LCMP);
                this.mv.visitJumpInsn(IFEQ, label1);
            } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
                this.mv.visitJumpInsn(IF_ICMPEQ, label1);
            } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DCMPL);
                this.mv.visitJumpInsn(IFEQ, label1);
            } else if (lhsOpType.tag == TypeTags.BOOLEAN && rhsOpType.tag == TypeTags.BOOLEAN) {
                this.mv.visitJumpInsn(IF_ICMPEQ, label1);
            } else if (lhsOpType.tag == TypeTags.DECIMAL && rhsOpType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkDecimalEqual",
                        String.format("(L%s;L%s;)Z", DECIMAL_VALUE, DECIMAL_VALUE), false);
                this.mv.visitJumpInsn(IFNE, label1);
            } else {
                this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isEqual",
                        String.format("(L%s;L%s;)Z", OBJECT, OBJECT), false);
                this.mv.visitJumpInsn(IFNE, label1);
            }

            this.mv.visitInsn(ICONST_1);
            this.mv.visitJumpInsn(GOTO, label2);

            this.mv.visitLabel(label1);
            this.mv.visitInsn(ICONST_0);

            this.mv.visitLabel(label2);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateRefEqualIns(BinaryOp binaryIns) {

            this.generateBinaryRhsAndLhsLoad(binaryIns);

            Label label1 = new Label();
            Label label2 = new Label();

            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
            if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
                this.mv.visitInsn(LCMP);
                this.mv.visitJumpInsn(IFNE, label1);
            } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
                this.mv.visitJumpInsn(IF_ICMPNE, label1);
            } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DCMPL);
                this.mv.visitJumpInsn(IFNE, label1);
            } else if (lhsOpType.tag == TypeTags.BOOLEAN && rhsOpType.tag == TypeTags.BOOLEAN) {
                this.mv.visitJumpInsn(IF_ICMPNE, label1);
            } else {
                this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isReferenceEqual",
                        String.format("(L%s;L%s;)Z", OBJECT, OBJECT), false);
                this.storeToVar(binaryIns.lhsOp.variableDcl);
                return;
            }

            this.mv.visitInsn(ICONST_1);
            this.mv.visitJumpInsn(GOTO, label2);

            this.mv.visitLabel(label1);
            this.mv.visitInsn(ICONST_0);

            this.mv.visitLabel(label2);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateRefNotEqualIns(BinaryOp binaryIns) {

            this.generateBinaryRhsAndLhsLoad(binaryIns);

            Label label1 = new Label();
            Label label2 = new Label();

            // It is assumed that both operands are of same type
            BType lhsOpType = binaryIns.rhsOp1.variableDcl.type;
            BType rhsOpType = binaryIns.rhsOp2.variableDcl.type;
            if (TypeTags.isIntegerTypeTag(lhsOpType.tag) && TypeTags.isIntegerTypeTag(rhsOpType.tag)) {
                this.mv.visitInsn(LCMP);
                this.mv.visitJumpInsn(IFEQ, label1);
            } else if (lhsOpType.tag == TypeTags.BYTE && rhsOpType.tag == TypeTags.BYTE) {
                this.mv.visitJumpInsn(IF_ICMPEQ, label1);
            } else if (lhsOpType.tag == TypeTags.FLOAT && rhsOpType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DCMPL);
                this.mv.visitJumpInsn(IFEQ, label1);
            } else if (lhsOpType.tag == TypeTags.BOOLEAN && rhsOpType.tag == TypeTags.BOOLEAN) {
                this.mv.visitJumpInsn(IF_ICMPEQ, label1);
            } else {
                this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "isReferenceEqual",
                        String.format("(L%s;L%s;)Z", OBJECT, OBJECT), false);
                this.mv.visitJumpInsn(IFNE, label1);
            }

            this.mv.visitInsn(ICONST_1);
            this.mv.visitJumpInsn(GOTO, label2);

            this.mv.visitLabel(label1);
            this.mv.visitInsn(ICONST_0);

            this.mv.visitLabel(label2);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateClosedRangeIns(BinaryOp binaryIns) {

            this.mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
            this.mv.visitInsn(DUP);
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            this.mv.visitMethodInsn(INVOKESTATIC, LONG_STREAM, "rangeClosed", String.format("(JJ)L%s;", LONG_STREAM),
                    true);
            this.mv.visitMethodInsn(INVOKEINTERFACE, LONG_STREAM, "toArray", "()[J", true);
            this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, "<init>", "([J)V", false);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateAnnotAccessIns(BinaryOp binaryIns) {

            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "getAnnotValue",
                    String.format("(L%s;L%s;)L%s;", TYPEDESC_VALUE, STRING_VALUE, OBJECT), false);

            BType targetType = binaryIns.lhsOp.variableDcl.type;
            addUnboxInsn(this.mv, targetType);
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateAddIns(BinaryOp binaryIns) {

            BType bType = binaryIns.lhsOp.variableDcl.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                this.mv.visitInsn(LADD);
            } else if (bType.tag == TypeTags.BYTE) {
                this.mv.visitInsn(IADD);
            } else if (TypeTags.isStringTypeTag(bType.tag)) {
                if (isBString) {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, B_STRING_VALUE, "concat",
                                            String.format("(L%s;)L%s;", B_STRING_VALUE, B_STRING_VALUE), true);
                } else {
                    this.mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
                            String.format("(L%s;)L%s;", STRING_VALUE, STRING_VALUE), false);
                }
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "add",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DADD);
            } else if (TypeTags.isXMLTypeTag(bType.tag)) {
                this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "concatenate",
                        String.format("(L%s;L%s;)L%s;", XML_VALUE, XML_VALUE, XML_VALUE), false);
            } else {
                throw new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.variableDcl.type));
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateSubIns(BinaryOp binaryIns) {

            BType bType = binaryIns.lhsOp.variableDcl.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                this.mv.visitInsn(LSUB);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DSUB);
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "subtract",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else {
                throw new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.variableDcl.type));
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateDivIns(BinaryOp binaryIns) {

            BType bType = binaryIns.lhsOp.variableDcl.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                this.mv.visitMethodInsn(INVOKESTATIC, MATH_UTILS, "divide", "(JJ)J", false);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DDIV);
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "divide",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else {
                throw new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.variableDcl.type));
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateMulIns(BinaryOp binaryIns) {

            BType bType = binaryIns.lhsOp.variableDcl.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                this.mv.visitInsn(LMUL);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DMUL);
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "multiply",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else {
                throw new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.variableDcl.type));
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateRemIns(BinaryOp binaryIns) {

            BType bType = binaryIns.lhsOp.variableDcl.type;
            this.generateBinaryRhsAndLhsLoad(binaryIns);
            if (TypeTags.isIntegerTypeTag(bType.tag)) {
                this.mv.visitMethodInsn(INVOKESTATIC, MATH_UTILS, "remainder", "(JJ)J", false);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DREM);
            } else if (bType.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "remainder",
                        String.format("(L%s;)L%s;", DECIMAL_VALUE, DECIMAL_VALUE), false);
            } else {
                throw new BLangCompilerException("JVM generation is not supported for type " +
                        String.format("%s", binaryIns.lhsOp.variableDcl.type));
            }
            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateBitwiseAndIns(BinaryOp binaryIns) {

            BType opType1 = binaryIns.rhsOp1.variableDcl.type;
            BType opType2 = binaryIns.rhsOp2.variableDcl.type;

            int opType1Tag = opType1.tag;
            int opType2Tag = opType2.tag;

            if (opType1Tag == TypeTags.BYTE && opType2Tag == TypeTags.BYTE) {
                this.loadVar(binaryIns.rhsOp1.variableDcl);
                generateCheckCastToByte(this.mv, opType1);

                this.loadVar(binaryIns.rhsOp2.variableDcl);
                generateCheckCastToByte(this.mv, opType2);

                this.mv.visitInsn(IAND);
            } else {
                boolean byteResult = false;

                this.loadVar(binaryIns.rhsOp1.variableDcl);
                if (opType1Tag == TypeTags.BYTE) {
                    this.mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "toUnsignedLong", "(I)J", false);
                    byteResult = true;
                }

                this.loadVar(binaryIns.rhsOp2.variableDcl);
                if (opType2Tag == TypeTags.BYTE) {
                    this.mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "toUnsignedLong", "(I)J", false);
                    byteResult = true;
                }

                this.mv.visitInsn(LAND);
                if (byteResult) {
                    generateCheckCastToByte(this.mv, symbolTable.intType);
                }
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateBitwiseOrIns(BinaryOp binaryIns) {

            BType opType1 = binaryIns.rhsOp1.variableDcl.type;
            BType opType2 = binaryIns.rhsOp2.variableDcl.type;

            if (opType1.tag == TypeTags.BYTE && opType2.tag == TypeTags.BYTE) {
                this.loadVar(binaryIns.rhsOp1.variableDcl);
                this.loadVar(binaryIns.rhsOp2.variableDcl);
                this.mv.visitInsn(IOR);
                this.storeToVar(binaryIns.lhsOp.variableDcl);
                return;
            }

            this.loadVar(binaryIns.rhsOp1.variableDcl);
            generateCheckCast(this.mv, opType1, symbolTable.intType, this.indexMap);

            this.loadVar(binaryIns.rhsOp2.variableDcl);
            generateCheckCast(this.mv, opType2, symbolTable.intType, this.indexMap);

            this.mv.visitInsn(LOR);

            if (!TypeTags.isSignedIntegerTypeTag(opType1.tag) && !TypeTags.isSignedIntegerTypeTag(opType2.tag)) {
                generateIntToUnsignedIntConversion(this.mv, getSmallerUnsignedIntSubType(opType1, opType2));
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateBitwiseXorIns(BinaryOp binaryIns) {

            BType opType1 = binaryIns.rhsOp1.variableDcl.type;
            BType opType2 = binaryIns.rhsOp2.variableDcl.type;

            if (opType1.tag == TypeTags.BYTE && opType2.tag == TypeTags.BYTE) {
                this.loadVar(binaryIns.rhsOp1.variableDcl);
                this.loadVar(binaryIns.rhsOp2.variableDcl);
                this.mv.visitInsn(IXOR);
                this.storeToVar(binaryIns.lhsOp.variableDcl);
                return;
            }

            this.loadVar(binaryIns.rhsOp1.variableDcl);
            generateCheckCast(this.mv, opType1, symbolTable.intType, this.indexMap);

            this.loadVar(binaryIns.rhsOp2.variableDcl);
            generateCheckCast(this.mv, opType2, symbolTable.intType, this.indexMap);

            this.mv.visitInsn(LXOR);

            if (!TypeTags.isSignedIntegerTypeTag(opType1.tag) && !TypeTags.isSignedIntegerTypeTag(opType2.tag)) {
                generateIntToUnsignedIntConversion(this.mv, getSmallerUnsignedIntSubType(opType1, opType2));
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateBitwiseLeftShiftIns(BinaryOp binaryIns) {

            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);

            BType secondOpType = binaryIns.rhsOp2.variableDcl.type;
            if (TypeTags.isIntegerTypeTag(secondOpType.tag)) {
                this.mv.visitInsn(L2I);
            }

            BType firstOpType = binaryIns.rhsOp1.variableDcl.type;
            if (TypeTags.isIntegerTypeTag(firstOpType.tag)) {
                this.mv.visitInsn(LSHL);
            } else {
                this.mv.visitInsn(ISHL);
                this.mv.visitInsn(I2L);
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateBitwiseRightShiftIns(BinaryOp binaryIns) {

            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);

            if (TypeTags.isIntegerTypeTag(binaryIns.rhsOp2.variableDcl.type.tag)) {
                this.mv.visitInsn(L2I);
            }

            if (TypeTags.isIntegerTypeTag(binaryIns.rhsOp1.variableDcl.type.tag)) {
                this.mv.visitInsn(LSHR);
            } else {
                this.mv.visitInsn(ISHR);
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        void generateBitwiseUnsignedRightShiftIns(BinaryOp binaryIns) {

            this.loadVar(binaryIns.rhsOp1.variableDcl);
            this.loadVar(binaryIns.rhsOp2.variableDcl);

            if (TypeTags.isIntegerTypeTag(binaryIns.rhsOp2.variableDcl.type.tag)) {
                this.mv.visitInsn(L2I);
            }

            if (TypeTags.isIntegerTypeTag(binaryIns.rhsOp1.variableDcl.type.tag)) {
                this.mv.visitInsn(LUSHR);
            } else {
                this.mv.visitInsn(IUSHR);
            }

            this.storeToVar(binaryIns.lhsOp.variableDcl);
        }

        int getJVMIndexOfVarRef(BIRVariableDcl varDcl) {

            return this.indexMap.getIndex(varDcl);
        }

        void generateMapNewIns(NewStructure mapNewIns, int localVarOffset) {

            BType typeOfMapNewIns = mapNewIns.type;
            String className = MAP_VALUE_IMPL;

            if (typeOfMapNewIns.tag == TypeTags.RECORD) {
                if (mapNewIns.isExternalDef) {
                    className = getTypeValueClassName(mapNewIns.externalPackageId, toNameString(typeOfMapNewIns));
                } else {
                    className = getTypeValueClassName(this.currentPackage, toNameString(typeOfMapNewIns));
                }

                this.mv.visitTypeInsn(NEW, className);
                this.mv.visitInsn(DUP);
                this.mv.visitInsn(DUP);
                if (mapNewIns.isExternalDef) {
                    loadExternalType(this.mv, mapNewIns.externalPackageId, mapNewIns.recordName);
                } else {
                    loadType(this.mv, mapNewIns.type);
                }
                this.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", String.format("(L%s;)V", BTYPE), false);

                // Invoke the init-function of this type.
                this.mv.visitVarInsn(ALOAD, localVarOffset);
                this.mv.visitInsn(SWAP);
                this.mv.visitMethodInsn(INVOKESTATIC, className, "$init",
                        String.format("(L%s;L%s;)V", STRAND, MAP_VALUE), false);
            } else {
                this.mv.visitTypeInsn(NEW, className);
                this.mv.visitInsn(DUP);
                loadType(this.mv, mapNewIns.type);
                this.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", String.format("(L%s;)V", BTYPE), false);
            }
            this.storeToVar(mapNewIns.lhsOp.variableDcl);
        }

        void generateMapStoreIns(FieldAccess mapStoreIns) {
            // visit map_ref
            this.loadVar(mapStoreIns.lhsOp.variableDcl);
            BType varRefType = mapStoreIns.lhsOp.variableDcl.type;

            // visit key_expr
            this.loadVar(mapStoreIns.keyOp.variableDcl);

            // visit value_expr
            BType valueType = mapStoreIns.rhsOp.variableDcl.type;
            this.loadVar(mapStoreIns.rhsOp.variableDcl);
            addBoxInsn(this.mv, valueType);

            if (varRefType.tag == TypeTags.JSON) {
                this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setElement",
                        String.format("(L%s;L%s;L%s;)V", OBJECT, isBString ? B_STRING_VALUE : STRING_VALUE, OBJECT),
                        false);
            } else {
                String signature = String.format("(L%s;L%s;L%s;)V",
                        MAP_VALUE, isBString ? B_STRING_VALUE : STRING_VALUE, OBJECT);
                this.mv.visitMethodInsn(INVOKESTATIC, MAP_UTILS, "handleMapStore", signature, false);
            }
        }

        void generateMapLoadIns(FieldAccess mapLoadIns) {
            // visit map_ref
            this.loadVar(mapLoadIns.rhsOp.variableDcl);
            BType varRefType = mapLoadIns.rhsOp.variableDcl.type;
            addUnboxInsn(this.mv, varRefType);

            // visit key_expr
            this.loadVar(mapLoadIns.keyOp.variableDcl);

            if (varRefType.tag == TypeTags.JSON) {

                if (mapLoadIns.optionalFieldAccess) {
                    this.mv.visitTypeInsn(CHECKCAST, isBString ? B_STRING_VALUE : STRING_VALUE);
                    this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElementOrNil",
                            String.format("(L%s;L%s;)L%s;", OBJECT, isBString ? B_STRING_VALUE : STRING_VALUE, OBJECT),
                            false);
                } else {
                    this.mv.visitTypeInsn(CHECKCAST, isBString ? B_STRING_VALUE : STRING_VALUE);
                    this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "getElement",
                            String.format("(L%s;L%s;)L%s;", OBJECT, isBString ? B_STRING_VALUE : STRING_VALUE, OBJECT),
                            false);
                }
            } else {
                if (mapLoadIns.fillingRead) {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, MAP_VALUE, "fillAndGet",
                            String.format("(L%s;)L%s;", OBJECT, OBJECT), true);
                } else {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, MAP_VALUE, "get",
                            String.format("(L%s;)L%s;", OBJECT, OBJECT), true);
                }
            }

            // store in the target reg
            BType targetType = mapLoadIns.lhsOp.variableDcl.type;
            addUnboxInsn(this.mv, targetType);
            this.storeToVar(mapLoadIns.lhsOp.variableDcl);
        }

        void generateObjectLoadIns(FieldAccess objectLoadIns) {
            // visit object_ref
            this.loadVar(objectLoadIns.rhsOp.variableDcl);

            // visit key_expr
            this.loadVar(objectLoadIns.keyOp.variableDcl);

            // invoke get() method, and unbox if needed
            this.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "get",
                    String.format("(L%s;)L%s;", isBString ? B_STRING_VALUE : STRING_VALUE, OBJECT), true);
            BType targetType = objectLoadIns.lhsOp.variableDcl.type;
            addUnboxInsn(this.mv, targetType);

            // store in the target reg
            this.storeToVar(objectLoadIns.lhsOp.variableDcl);
        }

        void generateObjectStoreIns(FieldAccess objectStoreIns) {
            // visit object_ref
            this.loadVar(objectStoreIns.lhsOp.variableDcl);

            // visit key_expr
            this.loadVar(objectStoreIns.keyOp.variableDcl);

            // visit value_expr
            BType valueType = objectStoreIns.rhsOp.variableDcl.type;
            this.loadVar(objectStoreIns.rhsOp.variableDcl);
            addBoxInsn(this.mv, valueType);

            // invoke set() method
            this.mv.visitMethodInsn(INVOKEINTERFACE, OBJECT_VALUE, "set",
                                    String.format("(L%s;L%s;)V", isBString ? B_STRING_VALUE : STRING_VALUE, OBJECT),
                                    true);
        }

        void generateStringLoadIns(FieldAccess stringLoadIns) {
            // visit the string
            this.loadVar(stringLoadIns.rhsOp.variableDcl);

            // visit the key expr
            this.loadVar(stringLoadIns.keyOp.variableDcl);

            String consVal = isBString ? B_STRING_VALUE : STRING_VALUE;
            // invoke the `getStringAt()` method
            this.mv.visitMethodInsn(INVOKESTATIC, STRING_UTILS, "getStringAt",
                                    String.format("(L%s;J)L%s;", consVal, consVal), false);

            // store in the target reg
            this.storeToVar(stringLoadIns.lhsOp.variableDcl);
        }

        void generateArrayNewIns(NewArray inst) {

            if (inst.type.tag == TypeTags.ARRAY) {
                this.mv.visitTypeInsn(NEW, ARRAY_VALUE_IMPL);
                this.mv.visitInsn(DUP);
                loadType(this.mv, inst.type);
                this.loadVar(inst.sizeOp.variableDcl);
                if (isBString) {
                    this.mv.visitInsn(ICONST_1);
                    this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, "<init>",
                                            String.format("(L%s;JZ)V", ARRAY_TYPE), false);
                } else {
                    this.mv.visitMethodInsn(INVOKESPECIAL, ARRAY_VALUE_IMPL, "<init>",
                                            String.format("(L%s;J)V", ARRAY_TYPE), false);
                }
                this.storeToVar(inst.lhsOp.variableDcl);
            } else {
                this.mv.visitTypeInsn(NEW, TUPLE_VALUE_IMPL);
                this.mv.visitInsn(DUP);
                loadType(this.mv, inst.type);
                this.loadVar(inst.sizeOp.variableDcl);
                this.mv.visitMethodInsn(INVOKESPECIAL, TUPLE_VALUE_IMPL, "<init>",
                        String.format("(L%s;J)V", TUPLE_TYPE), false);
                this.storeToVar(inst.lhsOp.variableDcl);
            }
        }

        void generateArrayStoreIns(FieldAccess inst) {

            this.loadVar(inst.lhsOp.variableDcl);
            this.loadVar(inst.keyOp.variableDcl);
            this.loadVar(inst.rhsOp.variableDcl);

            BType valueType = inst.rhsOp.variableDcl.type;
            BType varRefType = inst.lhsOp.variableDcl.type;
            if (varRefType.tag == TypeTags.JSON ||
                    (varRefType.tag == TypeTags.ARRAY && ((BArrayType) varRefType).eType instanceof BJSONType)) {
                addBoxInsn(this.mv, valueType);
                this.mv.visitMethodInsn(INVOKESTATIC, JSON_UTILS, "setArrayElement",
                        String.format("(L%s;JL%s;)V", OBJECT, OBJECT), false);
                return;
            }

            if (TypeTags.isIntegerTypeTag(valueType.tag)) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JJ)V", true);
            } else if (valueType.tag == TypeTags.FLOAT) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JD)V", true);
            } else if (TypeTags.isStringTypeTag(valueType.tag)) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add",
                                        String.format("(JL%s;)V", isBString ? B_STRING_VALUE : STRING_VALUE), true);
            } else if (valueType.tag == TypeTags.BOOLEAN) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JZ)V", true);
            } else if (valueType.tag == TypeTags.BYTE) {
                this.mv.visitInsn(I2B);
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", "(JB)V", true);
            } else {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "add", String.format("(JL%s;)V", OBJECT), true);
            }
        }

        void generateArrayValueLoad(FieldAccess inst) {

            this.loadVar(inst.rhsOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, ARRAY_VALUE);
            this.loadVar(inst.keyOp.variableDcl);
            BType bType = inst.lhsOp.variableDcl.type;

            BType varRefType = inst.rhsOp.variableDcl.type;
            if (varRefType.tag == TypeTags.TUPLE) {
                if (inst.fillingRead) {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "fillAndGetRefValue",
                                            String.format("(J)L%s;", OBJECT), true);
                } else {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue",
                                            String.format("(J)L%s;", OBJECT), true);
                }
                addUnboxInsn(this.mv, bType);
            } else if (TypeTags.isIntegerTypeTag(bType.tag)) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getInt", "(J)J", true);
            } else if (TypeTags.isStringTypeTag(bType.tag)) {
                if (isBString) {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBString",
                                            String.format("(J)L%s;", B_STRING_VALUE), true);
                } else {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getString",
                                            String.format("(J)L%s;", STRING_VALUE), true);
                }
            } else if (bType.tag == TypeTags.BOOLEAN) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getBoolean", "(J)Z", true);
            } else if (bType.tag == TypeTags.BYTE) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getByte", "(J)B", true);
                this.mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "toUnsignedInt", "(B)I", false);
            } else if (bType.tag == TypeTags.FLOAT) {
                this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getFloat", "(J)D", true);
            } else {
                if (inst.fillingRead) {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "fillAndGetRefValue",
                                            String.format("(J)L%s;", OBJECT), true);
                } else {
                    this.mv.visitMethodInsn(INVOKEINTERFACE, ARRAY_VALUE, "getRefValue",
                                            String.format("(J)L%s;", OBJECT), true);
                }
                @Nilable String targetTypeClass = getTargetClass(bType);
                if (targetTypeClass != null) {
                    this.mv.visitTypeInsn(CHECKCAST, targetTypeClass);
                } else {
                    addUnboxInsn(this.mv, bType);
                }
            }
            this.storeToVar(inst.lhsOp.variableDcl);
        }

        void generateNewErrorIns(NewError newErrorIns) {

            this.mv.visitTypeInsn(NEW, ERROR_VALUE);
            this.mv.visitInsn(DUP);
            // load errorType
            loadType(this.mv, newErrorIns.type);
            this.loadVar(newErrorIns.reasonOp.variableDcl);
            this.loadVar(newErrorIns.detailOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESPECIAL, ERROR_VALUE, "<init>",
                                    String.format("(L%s;L%s;L%s;)V", BTYPE,
                                                  isBString ? B_STRING_VALUE : STRING_VALUE, OBJECT), false);
            this.storeToVar(newErrorIns.lhsOp.variableDcl);
        }

        void generateCastIns(TypeCast typeCastIns) {
            // load source value
            this.loadVar(typeCastIns.rhsOp.variableDcl);
            if (typeCastIns.checkTypes) {
                generateCheckCast(this.mv, typeCastIns.rhsOp.variableDcl.type, typeCastIns.type, this.indexMap);
            } else {
                generateCast(this.mv, typeCastIns.rhsOp.variableDcl.type, typeCastIns.type);
            }
            this.storeToVar(typeCastIns.lhsOp.variableDcl);
        }

        void generateTypeTestIns(TypeTest typeTestIns) {
            // load source value
            this.loadVar(typeTestIns.rhsOp.variableDcl);

            // load targetType
            loadType(this.mv, typeTestIns.type);

            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsType",
                    String.format("(L%s;L%s;)Z", OBJECT, BTYPE), false);
            this.storeToVar(typeTestIns.lhsOp.variableDcl);
        }

        void generateIsLikeIns(IsLike isLike) {
            // load source value
            this.loadVar(isLike.rhsOp.variableDcl);

            // load targetType
            loadType(this.mv, isLike.type);

            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "checkIsLikeType",
                    String.format("(L%s;L%s;)Z", OBJECT, BTYPE), false);
            this.storeToVar(isLike.lhsOp.variableDcl);
        }

        void generateObjectNewIns(NewInstance objectNewIns, int strandIndex) {

            BType type = lookupTypeDef(objectNewIns);
            String className;
            if (objectNewIns.isExternalDef) {
                className = getTypeValueClassName(objectNewIns.externalPackageId, objectNewIns.objectName);
            } else {
                className = getTypeValueClassName(this.currentPackage, objectNewIns.def.name.value);
            }

            this.mv.visitTypeInsn(NEW, className);
            this.mv.visitInsn(DUP);

            if (type instanceof BServiceType) {
                // For services, create a new type for each new service value. TODO: do only for local vars
                duplicateServiceTypeWithAnnots(this.mv, (BObjectType) type, this.currentPackageName, strandIndex);
            } else {
                loadType(mv, type);
            }
            this.mv.visitTypeInsn(CHECKCAST, OBJECT_TYPE);
            this.mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", String.format("(L%s;)V", OBJECT_TYPE), false);
            this.storeToVar(objectNewIns.lhsOp.variableDcl);
        }

        void generateFPLoadIns(FPLoad inst) {

            this.mv.visitTypeInsn(NEW, FUNCTION_POINTER);
            this.mv.visitInsn(DUP);

            String lambdaName = inst.funcName.value + "$lambda" + lambdaIndex + "$";
            lambdaIndex += 1;
            String pkgName = getPackageName(inst.pkgId.orgName, inst.pkgId.name);
            String lookupKey = pkgName + inst.funcName.value;

            BType returnType = inst.lhsOp.variableDcl.type;
            if (returnType.tag != TypeTags.INVOKABLE) {
                throw new BLangCompilerException("Expected BInvokableType, found " + String.format("%s", returnType));
            }

            for (BIROperand operand : inst.closureMaps) {
                if (operand != null) {
                    this.loadVar(operand.variableDcl);
                }
            }

            visitInvokeDyn(mv, currentClass, lambdaName, inst.closureMaps.size());
            loadType(this.mv, returnType);
            if (inst.schedulerPolicy == SchedulerPolicy.ANY) {
                mv.visitInsn(ICONST_1);
            } else {
                mv.visitInsn(ICONST_0);
            }
            this.mv.visitMethodInsn(INVOKESPECIAL, FUNCTION_POINTER, "<init>",
                                    String.format("(L%s;L%s;Z)V", FUNCTION, BTYPE), false);

            // Set annotations if available.
            this.mv.visitInsn(DUP);
            String pkgClassName = pkgName.equals(".") || pkgName.equals("") ? MODULE_INIT_CLASS_NAME :
                    lookupGlobalVarClassName(pkgName, ANNOTATION_MAP_NAME);
            this.mv.visitFieldInsn(GETSTATIC, pkgClassName, ANNOTATION_MAP_NAME, String.format("L%s;", MAP_VALUE));
            this.mv.visitLdcInsn(inst.funcName.value);
            this.mv.visitMethodInsn(INVOKESTATIC, String.format("%s", ANNOTATION_UTILS), "processFPValueAnnotations",
                    String.format("(L%s;L%s;L%s;)V", FUNCTION_POINTER, MAP_VALUE, STRING_VALUE), false);

            this.storeToVar(inst.lhsOp.variableDcl);
            lambdas.put(lambdaName, inst);
        }

        static void visitInvokeDyn(MethodVisitor mv, String currentClass, String lambdaName, int size) {

            String mapDesc = getMapsDesc(size);
            Handle handle = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory",
                    "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;" +
                    "Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;" +
                    "Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);

            mv.visitInvokeDynamicInsn("apply", "(" + mapDesc + ")Ljava/util/function/Function;", handle,
                    Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
                    new Handle(Opcodes.H_INVOKESTATIC, currentClass, lambdaName, "(" + mapDesc + "[" +
                            "Ljava/lang/Object;)Ljava/lang/Object;", false),
                    Type.getType("([Ljava/lang/Object;" + ")Ljava/lang/Object;"));
        }

        private static String getMapsDesc(long count) {

            StringBuilder builder = new StringBuilder();
            for (long i = count; i > 0; i--) {
                builder.append("Lorg/ballerinalang/jvm/values/MapValue;");
            }
            return builder.toString();
        }

        void generateNewXMLElementIns(NewXMLElement newXMLElement) {

            this.loadVar(newXMLElement.startTagOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);
            this.loadVar(newXMLElement.defaultNsURIOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLElement",
                                    String.format("(L%s;L%s;)L%s;", XML_QNAME,
                                                  isBString ? B_STRING_VALUE : STRING_VALUE, XML_VALUE), false);
            this.storeToVar(newXMLElement.lhsOp.variableDcl);
        }

        void generateNewXMLQNameIns(NewXMLQName newXMLQName) {

            this.mv.visitTypeInsn(NEW, XML_QNAME);
            this.mv.visitInsn(DUP);
            this.loadVar(newXMLQName.localnameOp.variableDcl);
            this.loadVar(newXMLQName.nsURIOp.variableDcl);
            this.loadVar(newXMLQName.prefixOp.variableDcl);
            String consVal = isBString ? B_STRING_VALUE : STRING_VALUE;
            this.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, "<init>",
                                    String.format("(L%s;L%s;L%s;)V", consVal, consVal, consVal), false);
            this.storeToVar(newXMLQName.lhsOp.variableDcl);
        }

        void generateNewStringXMLQNameIns(NewStringXMLQName newStringXMLQName) {

            this.mv.visitTypeInsn(NEW, XML_QNAME);
            this.mv.visitInsn(DUP);
            this.loadVar(newStringXMLQName.stringQNameOP.variableDcl);
            this.mv.visitMethodInsn(INVOKESPECIAL, XML_QNAME, "<init>",
                    String.format("(L%s;)V", STRING_VALUE), false);
            this.storeToVar(newStringXMLQName.lhsOp.variableDcl);
        }

        void generateNewXMLTextIns(NewXMLText newXMLText) {

            this.loadVar(newXMLText.textOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLText",
                                    String.format("(L%s;)L%s;", isBString ? B_STRING_VALUE : STRING_VALUE, XML_VALUE),
                                    false);
            this.storeToVar(newXMLText.lhsOp.variableDcl);
        }

        void generateNewXMLCommentIns(NewXMLComment newXMLComment) {

            this.loadVar(newXMLComment.textOp.variableDcl);
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLComment",
                                    String.format("(L%s;)L%s;", isBString ? B_STRING_VALUE : STRING_VALUE, XML_VALUE),
                                    false);
            this.storeToVar(newXMLComment.lhsOp.variableDcl);
        }

        void generateNewXMLProcIns(NewXMLProcIns newXMLPI) {

            this.loadVar(newXMLPI.targetOp.variableDcl);
            this.loadVar(newXMLPI.dataOp.variableDcl);
            String consVal = isBString ? B_STRING_VALUE : STRING_VALUE;
            this.mv.visitMethodInsn(INVOKESTATIC, XML_FACTORY, "createXMLProcessingInstruction",
                                    String.format("(L%s;L%s;)L%s;", consVal, consVal, XML_VALUE), false);
            this.storeToVar(newXMLPI.lhsOp.variableDcl);
        }

        void generateXMLStoreIns(XMLAccess xmlStoreIns) {

            this.loadVar(xmlStoreIns.lhsOp.variableDcl);
            this.loadVar(xmlStoreIns.rhsOp.variableDcl);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "addChildren", String.format("(L%s;)V", XML_VALUE),
                    false);
        }

        void generateXMLLoadAllIns(XMLAccess xmlLoadAllIns) {

            this.loadVar(xmlLoadAllIns.rhsOp.variableDcl);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "children", String.format("()L%s;", XML_VALUE),
                    false);
            this.storeToVar(xmlLoadAllIns.lhsOp.variableDcl);
        }

        void generateXMLAttrLoadIns(FieldAccess xmlAttrStoreIns) {
            // visit xml_ref
            this.loadVar(xmlAttrStoreIns.rhsOp.variableDcl);

            // visit attribute name expr
            this.loadVar(xmlAttrStoreIns.keyOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);

            // invoke getAttribute() method
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getAttribute",
                    String.format("(L%s;)L%s;", BXML_QNAME, STRING_VALUE), false);

            // store in the target reg
            BType targetType = xmlAttrStoreIns.lhsOp.variableDcl.type;
            this.storeToVar(xmlAttrStoreIns.lhsOp.variableDcl);
        }

        void generateXMLAttrStoreIns(FieldAccess xmlAttrStoreIns) {
            // visit xml_ref
            this.loadVar(xmlAttrStoreIns.lhsOp.variableDcl);

            // visit attribute name expr
            this.loadVar(xmlAttrStoreIns.keyOp.variableDcl);
            this.mv.visitTypeInsn(CHECKCAST, XML_QNAME);

            // visit attribute value expr
            this.loadVar(xmlAttrStoreIns.rhsOp.variableDcl);

            // invoke setAttribute() method
            String signature = String.format("(L%s;L%s;)V", BXML_QNAME, isBString ? B_STRING_VALUE : STRING_VALUE);
            this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "setAttribute", signature, false);
        }

        void generateXMLLoadIns(FieldAccess xmlLoadIns) {
            // visit xml_ref
            this.loadVar(xmlLoadIns.rhsOp.variableDcl);

            // visit element name/index expr
            this.loadVar(xmlLoadIns.keyOp.variableDcl);

            if (TypeTags.isStringTypeTag(xmlLoadIns.keyOp.variableDcl.type.tag)) {
                // invoke `children(name)` method
                this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "children",
                        String.format("(L%s;)L%s;", STRING_VALUE, XML_VALUE), false);
            } else {
                // invoke `getItem(index)` method
                this.mv.visitInsn(L2I);
                this.mv.visitMethodInsn(INVOKEVIRTUAL, XML_VALUE, "getItem",
                        String.format("(I)L%s;", XML_VALUE), false);
            }

            // store in the target reg
            BType targetType = xmlLoadIns.lhsOp.variableDcl.type;
            this.storeToVar(xmlLoadIns.lhsOp.variableDcl);
        }

        void generateTypeofIns(UnaryOP unaryOp) {

            this.loadVar(unaryOp.rhsOp.variableDcl);
            addBoxInsn(this.mv, unaryOp.rhsOp.variableDcl.type);
            this.mv.visitMethodInsn(INVOKESTATIC, TYPE_CHECKER, "getTypedesc",
                    String.format("(L%s;)L%s;", OBJECT, TYPEDESC_VALUE), false);
            this.storeToVar(unaryOp.lhsOp.variableDcl);
        }

        void generateNotIns(UnaryOP unaryOp) {

            this.loadVar(unaryOp.rhsOp.variableDcl);

            Label label1 = new Label();
            Label label2 = new Label();

            this.mv.visitJumpInsn(IFNE, label1);
            this.mv.visitInsn(ICONST_1);
            this.mv.visitJumpInsn(GOTO, label2);
            this.mv.visitLabel(label1);
            this.mv.visitInsn(ICONST_0);
            this.mv.visitLabel(label2);

            this.storeToVar(unaryOp.lhsOp.variableDcl);
        }

        void generateNegateIns(UnaryOP unaryOp) {

            this.loadVar(unaryOp.rhsOp.variableDcl);

            BType btype = unaryOp.rhsOp.variableDcl.type;
            if (TypeTags.isIntegerTypeTag(btype.tag)) {
                this.mv.visitInsn(LNEG);
            } else if (btype.tag == TypeTags.BYTE) {
                this.mv.visitInsn(INEG);
            } else if (btype.tag == TypeTags.FLOAT) {
                this.mv.visitInsn(DNEG);
            } else if (btype.tag == TypeTags.DECIMAL) {
                this.mv.visitMethodInsn(INVOKEVIRTUAL, DECIMAL_VALUE, "negate",
                        String.format("()L%s;", DECIMAL_VALUE), false);
            } else {
                throw new BLangCompilerException(String.format("Negation is not supported for type: %s", btype));
            }

            this.storeToVar(unaryOp.lhsOp.variableDcl);
        }

        void generateNewTypedescIns(NewTypeDesc newTypeDesc) {

            this.mv.visitTypeInsn(NEW, TYPEDESC_VALUE);
            this.mv.visitInsn(DUP);
            loadType(this.mv, newTypeDesc.type);
            this.mv.visitMethodInsn(INVOKESPECIAL, TYPEDESC_VALUE, "<init>",
                    String.format("(L%s;)V", BTYPE), false);
            this.storeToVar(newTypeDesc.lhsOp.variableDcl);
        }

        private void loadVar(BIRVariableDcl varDcl) {

            generateVarLoad(this.mv, varDcl, this.currentPackageName, this.getJVMIndexOfVarRef(varDcl));
        }

        private void storeToVar(BIRVariableDcl varDcl) {

            generateVarStore(this.mv, varDcl, this.currentPackageName, this.getJVMIndexOfVarRef(varDcl));
        }

        void generateConstantLoadIns(ConstantLoad loadIns) {

            loadConstantValue(loadIns.type, loadIns.value, this.mv);
            this.storeToVar(loadIns.lhsOp.variableDcl);
        }
    }

    private static int[] listHighSurrogates(String str) {

        List<Integer> highSurrogates = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isHighSurrogate(c)) {
                highSurrogates.add(i - highSurrogates.size());
            }
        }
        int[] highSurrogatesArr = new int[highSurrogates.size()];
        for (int i = 0; i < highSurrogates.size(); i++) {
            Integer highSurrogate = highSurrogates.get(i);
            highSurrogatesArr[i] = highSurrogate;
        }
        return highSurrogatesArr;
    }

    static void loadConstantValue(BType bType, Object constVal, MethodVisitor mv) {

        if (TypeTags.isIntegerTypeTag(bType.tag)) {
            long intValue = constVal instanceof Long ? (long) constVal : Long.parseLong(String.valueOf(constVal));
            mv.visitLdcInsn(intValue);
        } else if (bType.tag == TypeTags.BYTE) {
            int byteValue = ((Number) constVal).intValue();
            mv.visitLdcInsn(byteValue);
        } else if (bType.tag == TypeTags.FLOAT) {
            double doubleValue = constVal instanceof Double ? (double) constVal :
                    Double.parseDouble(String.valueOf(constVal));
            mv.visitLdcInsn(doubleValue);
        } else if (bType.tag == TypeTags.BOOLEAN) {
            boolean booleanVal = constVal instanceof Boolean ? (boolean) constVal :
                    Boolean.parseBoolean(String.valueOf(constVal));
            mv.visitLdcInsn(booleanVal);
        } else if (TypeTags.isStringTypeTag(bType.tag)) {
            String val = String.valueOf(constVal);
            if (isBString) {
                int[] highSurrogates = listHighSurrogates(val);

                mv.visitTypeInsn(NEW, NON_BMP_STRING_VALUE);
                mv.visitInsn(DUP);
                mv.visitLdcInsn(val);
                mv.visitIntInsn(BIPUSH, highSurrogates.length);
                mv.visitIntInsn(NEWARRAY, T_INT);

                int i = 0;
                for (int ch : highSurrogates) {
                    mv.visitInsn(DUP);
                    mv.visitIntInsn(BIPUSH, i);
                    mv.visitIntInsn(BIPUSH, ch);
                    i = i + 1;
                    mv.visitInsn(IASTORE);
                }
                mv.visitMethodInsn(INVOKESPECIAL, NON_BMP_STRING_VALUE, "<init>",
                        String.format("(L%s;[I)V", STRING_VALUE), false);
            } else {
                mv.visitLdcInsn(val);
            }
        } else if (bType.tag == TypeTags.DECIMAL) {
            mv.visitTypeInsn(NEW, DECIMAL_VALUE);
            mv.visitInsn(DUP);
            mv.visitLdcInsn(String.valueOf(constVal));
            mv.visitMethodInsn(INVOKESPECIAL, DECIMAL_VALUE, "<init>", String.format("(L%s;)V", STRING_VALUE), false);
        } else if (bType.tag == TypeTags.NIL) {
            mv.visitInsn(ACONST_NULL);
        } else {
            throw new BLangCompilerException("JVM generation is not supported for type : " +
                    String.format("%s", bType));
        }
    }

    private static void generateIntToUnsignedIntConversion(MethodVisitor mv, BType targetType) {
        switch (targetType.tag) {
            case TypeTags.BYTE: // Wouldn't reach here for int atm.
            case TypeTags.UNSIGNED8_INT:
                mv.visitInsn(L2I);
                mv.visitInsn(I2B);
                mv.visitMethodInsn(INVOKESTATIC, BYTE_VALUE, "toUnsignedLong", "(B)J", false);
                return;
            case TypeTags.UNSIGNED16_INT:
                mv.visitInsn(L2I);
                mv.visitInsn(I2S);
                mv.visitMethodInsn(INVOKESTATIC, SHORT_VALUE, "toUnsignedLong", "(S)J", false);
                return;
            case TypeTags.UNSIGNED32_INT:
                mv.visitInsn(L2I);
                mv.visitMethodInsn(INVOKESTATIC, INT_VALUE, "toUnsignedLong", "(I)J", false);
        }
    }

    private static BType getSmallerUnsignedIntSubType(BType lhsType, BType rhsType) {
        if (TypeTags.isSignedIntegerTypeTag(lhsType.tag) || TypeTags.isSignedIntegerTypeTag(rhsType.tag)) {
            throw new BLangCompilerException("expected two unsigned int subtypes, found '" + lhsType + "' and '" +
                                                     rhsType + "'");
        }

        if (lhsType.tag == TypeTags.BYTE || rhsType.tag == TypeTags.BYTE) {
            return symbolTable.unsigned8IntType;
        }

        if (lhsType.tag == TypeTags.UNSIGNED8_INT || rhsType.tag == TypeTags.UNSIGNED8_INT) {
            return symbolTable.unsigned8IntType;
        }

        if (lhsType.tag == TypeTags.UNSIGNED16_INT || rhsType.tag == TypeTags.UNSIGNED16_INT) {
            return symbolTable.unsigned16IntType;
        }

        return symbolTable.unsigned32IntType;
    }
}
