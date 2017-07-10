/* ****************************************************************************
 * Copyright (c) 2017 Simula Research Laboratory AS.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Shaukat Ali  shaukat@simula.no
 **************************************************************************** */

package no.simula.esocl.standalone.analysis;

import org.apache.log4j.Logger;
import org.dresdenocl.essentialocl.expressions.OclExpression;
import org.dresdenocl.essentialocl.expressions.impl.IntegerLiteralExpImpl;
import org.dresdenocl.essentialocl.expressions.impl.IteratorExpImpl;
import org.dresdenocl.essentialocl.expressions.impl.OperationCallExpImpl;
import org.dresdenocl.essentialocl.expressions.impl.PropertyCallExpImpl;
import org.dresdenocl.essentialocl.standardlibrary.OclAny;
import org.dresdenocl.essentialocl.standardlibrary.OclCollection;
import org.dresdenocl.metamodels.uml2.internal.model.UML2Class;
import org.dresdenocl.metamodels.uml2.internal.model.UML2Enumeration;
import org.dresdenocl.modelinstancetype.types.IModelInstanceElement;
import org.dresdenocl.modelinstancetype.types.base.JavaModelInstanceBoolean;
import org.dresdenocl.modelinstancetype.types.base.JavaModelInstanceInteger;
import org.dresdenocl.modelinstancetype.types.base.JavaModelInstanceReal;
import org.dresdenocl.modelinstancetype.types.base.ModelInstanceEnumerationLiteral;
import org.dresdenocl.pivotmodel.Constraint;
import org.dresdenocl.pivotmodel.EnumerationLiteral;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

import java.util.*;

/**
 * @author Shaukat Ali
 * @version 1.0
 * @since 2017-07-03
 */
public class OCLExpUtility {
    public static String OP_COMPLEX_SELECT_SIZE = "op_complex_select_size";
    public static String OP_COMPLEX_SELECT_ITERATE = "op_complex_select_iterate";
    public static String[] OP_BOOLEAN = {"implies", "not", "and", "or", "xor"};
    public static String[] OP_COMPARE = {"=", "<>", "<", "<=", ">", ">="};
    private static String[] OP_BOUND = {"<", "<=", ">", ">="};
    public static String[] OP_ITERATE = {"forAll", "exists", "isUnique", "one"};
    public static String[] OP_CHECK = {"includes", "excludes", "includesAll", "excludesAll", "isEmpty"};
    private static String[] OP_SELECT = {"select", "reject", "collect"};
    public static String[] OP_MISCELLANEOUS = {"oclIsTypeOf", "oclIsKindOf",
            "oclIsNew", "oclIsUndefined", "oclIsInvalid"};
    private static Logger logger = Logger.getLogger(OCLExpUtility.class);
    /**
     * singleton instance
     */
    private static OCLExpUtility instance;
    /**
     * Returns the single instance of the {@link OCLExpUtility}.
     */
    public static OCLExpUtility INSTANCE = instance();
    VESGenerator vesGenerator;
    String[][] typeArray;
    int[][] comb;
    Map<OperationCallExpImpl, Integer> oceMap;

    private static OCLExpUtility instance() {

        if (instance == null) {
            instance = new OCLExpUtility();
        }
        return instance;
    }

    public void setVesGenerator(VESGenerator vesGenerator) {
        this.vesGenerator = vesGenerator;
    }


    public String[][] getTypeArray() {
        return typeArray;
    }

    public Collection<IModelInstanceElement> getResultCollection(OclAny oclAny) {
        return ((OclCollection) oclAny).asOrderedSet()
                .getModelInstanceCollection().getCollection();
    }

    public double getResultNumericValue(IModelInstanceElement imiElement) {
        if (imiElement instanceof JavaModelInstanceInteger)
            return ((JavaModelInstanceInteger) imiElement).getDouble();
        else if (imiElement instanceof JavaModelInstanceReal) {
            return ((JavaModelInstanceReal) imiElement).getDouble();
        } else if (imiElement instanceof JavaModelInstanceBoolean) {
            if (((JavaModelInstanceBoolean) imiElement).getBoolean())
                return 1;
            else
                return 0;
        } else if (imiElement instanceof ModelInstanceEnumerationLiteral) {
            UML2Enumeration enumType = this.vesGenerator
                    .getEnumeration(((ModelInstanceEnumerationLiteral) imiElement)
                            .getType().getQualifiedName());
            for (EnumerationLiteral el : enumType.getOwnedLiteral()) {
                if (el.getName().equals(
                        ((ModelInstanceEnumerationLiteral) imiElement)
                                .getLiteral().getName()))
                    return enumType.getOwnedLiteral().indexOf(el);
            }
        }
        return -1;
    }

    private void printChild(EObject pObject, int depth) {
        EList<EObject> contents = pObject.eContents();
        if (contents.size() == 0)
            return;
        int now_depth = depth;
        for (EObject eObject : contents) {
            StringBuilder depthstring = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                depthstring.append("***");
            }
            EAttribute eatt = eObject.eClass().getEAllAttributes().get(0);
            String name = null;
            if (eObject instanceof OperationCallExpImpl) {
                OperationCallExpImpl oper = (OperationCallExpImpl) eObject;
                name = oper.getReferredOperation().getName();
            } else if (eObject instanceof PropertyCallExpImpl) {
                PropertyCallExpImpl pro = (PropertyCallExpImpl) eObject;
                name = pro.getReferredProperty().getName();
            } else if (eObject instanceof IteratorExpImpl) {
                IteratorExpImpl iter = (IteratorExpImpl) eObject;
                name = iter.getName();
            }

//			String name = eObject.eGet(eatt).toString();
//			ExpressionInOclImpl ex = (ExpressionInOclImpl) eObject;
            logger.debug(depthstring.toString() + " " + eObject.toString());
            printChild(eObject, ++now_depth);
        }

    }

	/*public void printOclClause4Depth(EObject e) {
		int depth = 0;
		for (EObject eObject : e.eContents()) {
			logger.debug(eObject.toString());
			printChild(eObject, ++depth);
			depth = 0;
		}
	}*/

    public void printOclClause4Depth(EObject e) {
        int depth = 0;
        Constraint con = (Constraint) e;
        EList<EObject> exp = con.eContents();
        for (EObject eObject : e.eContents()) {
            logger.debug(eObject.toString());

            printChild(eObject, ++depth);
            depth = 0;
        }
    }

    public String getOppositeOp(String opName) {
        switch (opName) {
            case "=":
                return "<>";
            case "<>":
                return "=";
            case "<":
                return ">=";
            case "<=":
                return ">";
            case ">":
                return "<=";
            case ">=":
                return "<";
        }
        return null;
    }

    public String isComplexType(OclExpression exp) {
        if (exp instanceof OperationCallExpImpl) {
            if (((OperationCallExpImpl) exp).getReferredOperation().getName()
                    .equals("size")) {
                EObject select_size_Exp = exp.eContents().get(0);
                if (select_size_Exp instanceof IteratorExpImpl) {
                    String select_size_Exp_opName = ((IteratorExpImpl) select_size_Exp)
                            .getName();
                    if (isBelongToOp(select_size_Exp_opName, OP_SELECT)) {
                        return OP_COMPLEX_SELECT_SIZE;
                    }
                }
            }
        }
        if (exp instanceof IteratorExpImpl) {
            EObject select_iterate_Exp = exp.eContents()
                    .get(0);
            if (select_iterate_Exp instanceof IteratorExpImpl) {
                String select_iterate_Exp_opName = ((IteratorExpImpl) select_iterate_Exp)
                        .getName();
                if (isBelongToOp(select_iterate_Exp_opName, OP_SELECT)) {
                    return OP_COMPLEX_SELECT_ITERATE;
                }// end if
            }// end if

        }
        return null;
    }

    public boolean isBelongToOp(String opName, String[] ops) {
        for (String op : ops) {
            if (op.equals(opName))
                return true;
        }
        return false;
    }

    public int[] getASC4String(String str) {
        int[] asc = new int[str.length()];
        for (int i = 0; i < str.length(); i++) {
            asc[i] = (int) str.charAt(i);
        }
        return asc;

    }

    public List<PropertyCallExpImpl> getPropertyInCons(Constraint constraint) {
        //OCLExpUtility.INSTANCE.printOclClause4Depth(constraint.getSpecification());
        List<PropertyCallExpImpl> propCallList = new ArrayList<PropertyCallExpImpl>();
        TreeIterator<EObject> it = constraint.getSpecification().eAllContents();
        while (it.hasNext()) {
            EObject e = (EObject) it.next();
            if (e instanceof PropertyCallExpImpl) {
                if (((PropertyCallExpImpl) e).getSourceType() instanceof UML2Class) {
                    boolean isExisted = false;
                    for (PropertyCallExpImpl propertyCallExpImpl : propCallList) {
                        PropertyCallExpImpl pce = (PropertyCallExpImpl) e;
                        String sourceTypeName = propertyCallExpImpl
                                .getSourceType().getQualifiedName();
                        String propertyName = propertyCallExpImpl
                                .getReferredProperty().getQualifiedName();
                        if (pce.getSourceType().getQualifiedName()
                                .equals(sourceTypeName)
                                && pce.getReferredProperty().getQualifiedName()
                                .equals(propertyName)) {
                            isExisted = true;
                        }
                    }
                    if (!isExisted)
                        propCallList.add((PropertyCallExpImpl) e);
                }

            }
        }
        return propCallList;
    }

    public int buildIndexArray4Bound(Constraint constraint) {
        this.oceMap = new HashMap<OperationCallExpImpl, Integer>();
        TreeIterator<EObject> it = constraint.getSpecification().eAllContents();
        while (it.hasNext()) {
            EObject e = (EObject) it.next();
            if (e instanceof OperationCallExpImpl) {
                OperationCallExpImpl oce = (OperationCallExpImpl) e;
                if (isBelongToOp(oce.getReferredOperation().getName(),
                        OP_BOUND)) {
                    OclExpression rightExp = (OclExpression) oce.eContents()
                            .get(1);
                    if (rightExp instanceof IntegerLiteralExpImpl) {
                        this.oceMap.put(oce, ((IntegerLiteralExpImpl) rightExp)
                                .getIntegerSymbol());
                    }
                }
            }
        }
        this.typeArray = buildBoundTypeArray(this.oceMap.keySet());
        int[] input = Utility.INSTANCE.genIndexArray(3);
        this.comb = Utility.INSTANCE.combInArrayDup(input, this.oceMap.size());
        return this.comb.length;
    }

    public void generateBoundValue(int iterate_index) {
        OperationCallExpImpl[] oceArray = new OperationCallExpImpl[this.oceMap
                .keySet().size()];
        oceArray = this.oceMap.keySet().toArray(oceArray);
        for (int i = 0; i < oceArray.length; i++) {
            OperationCallExpImpl oce = oceArray[i];
            IntegerLiteralExpImpl rightExp = (IntegerLiteralExpImpl) oce
                    .eContents().get(1);
            int index = this.comb[iterate_index][i];
            switch (index) {
                case 0:
                    rightExp.setIntegerSymbol(rightExp.getIntegerSymbol() - 1);
                    break;
                case 2:
                    rightExp.setIntegerSymbol(rightExp.getIntegerSymbol() + 1);
                    break;
            }
        }
    }

    public void restoreOriginalValue() {
        Set<OperationCallExpImpl> oceSet = this.oceMap.keySet();
        for (OperationCallExpImpl oce : oceSet) {
            int original = this.oceMap.get(oce);
            IntegerLiteralExpImpl rightExp = (IntegerLiteralExpImpl) oce
                    .eContents().get(1);
            rightExp.setIntegerSymbol(original);
        }
    }


    public String[][] buildBoundTypeArray(Set<OperationCallExpImpl> oceSet) {
        String[][] typeArray = new String[oceSet.size()][3];
        OperationCallExpImpl[] oceArray = new OperationCallExpImpl[oceSet
                .size()];
        oceArray = oceSet.toArray(oceArray);
        for (int i = 0; i < oceArray.length; i++) {
            OperationCallExpImpl oce = oceArray[i];
            String opName = oce.getReferredOperation().getName();
            switch (opName) {
                case "<":
                    typeArray[i][0] = "valid";
                    typeArray[i][1] = "invalid";
                    typeArray[i][2] = "invalid";
                    break;
                case "<=":
                    typeArray[i][0] = "valid";
                    typeArray[i][1] = "valid";
                    typeArray[i][2] = "invalid";
                    break;
                case ">":
                    typeArray[i][0] = "invalid";
                    typeArray[i][1] = "invalid";
                    typeArray[i][2] = "valid";
                    break;
                case ">=":
                    typeArray[i][0] = "invalid";
                    typeArray[i][1] = "valid";
                    typeArray[i][2] = "valid";
                    break;
            }
        }
        return typeArray;
    }
}
